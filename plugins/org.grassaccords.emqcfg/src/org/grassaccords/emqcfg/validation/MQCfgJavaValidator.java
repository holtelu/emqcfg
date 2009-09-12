package org.grassaccords.emqcfg.validation;

import static ch.lambdaj.Lambda.*;
import static org.grassaccords.emqcfg.util.MQCfgUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.validation.Check;
import org.grassaccords.emqcfg.mQCfg.Cluster;
import org.grassaccords.emqcfg.mQCfg.ClusterQMgrBinding;
import org.grassaccords.emqcfg.mQCfg.Interface;
import org.grassaccords.emqcfg.mQCfg.MQCfgPackage;
import org.grassaccords.emqcfg.mQCfg.Model;
import org.grassaccords.emqcfg.mQCfg.Node;
import org.grassaccords.emqcfg.mQCfg.QMgr;
import org.grassaccords.emqcfg.mQCfg.TopLevelType;

import ch.lambdaj.Lambda;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.sun.xml.internal.bind.v2.TODO;

public class MQCfgJavaValidator extends AbstractMQCfgJavaValidator {

	private static Log LOG = LogFactory.getLog(MQCfgJavaValidator.class);

	private class MQCfgValidationMessageExceptor implements
			ValidationMessageAcceptor2 {
		public void acceptWarning(String message, EObject object,
				Integer feature) {
			State state = getStateAccess().getState();
			state.chain.add(new MQCfgDiagnostic(Diagnostic.WARNING, 0, message,
					object, feature, state.currentCheckType));
		}

		public void acceptError(String message, EObject object, Integer feature) {
			State state = getStateAccess().getState();
			state.hasErrors = true;
			state.chain.add(new MQCfgDiagnostic(Diagnostic.ERROR, 0, message,
					object, feature, state.currentCheckType));
		}

		public void acceptError(EObject object, Integer feature, int code,
				String message, Object... parameter) {
			State state = getStateAccess().getState();
			state.hasErrors = true;
			state.chain.add(new MQCfgDiagnostic(Diagnostic.ERROR, code,
					safeStringFormat(message, parameter), object, feature,
					state.currentCheckType));
		}

		public void acceptWarning(EObject object, Integer feature, int code,
				String message, Object... parameter) {
			State state = getStateAccess().getState();
			state.chain.add(new MQCfgDiagnostic(Diagnostic.WARNING, code,
					safeStringFormat(message, parameter), object, feature,
					state.currentCheckType));
		}

		private String safeStringFormat(String message, Object... parameter) {
			try {
				return String.format(message, parameter);
			} catch (IllegalFormatException e) {
				LOG.error("Could not format the message '" + message + "'.");
				return message;
			}
		}
	}

	private StateAccess stateAccess;

	public ValidationMessageAcceptor2 validationMessageAcceptor = new MQCfgValidationMessageExceptor();

	public MQCfgJavaValidator() {
		setStateAccess(this.setMessageAcceptor(validationMessageAcceptor));
	}

	private void setStateAccess(StateAccess stateAccess) {
		this.stateAccess = stateAccess;
	}

	private StateAccess getStateAccess() {
		return stateAccess;
	}

	protected void warning(EObject source, Integer feature, int code,
			String message, Object... para) {
		validationMessageAcceptor.acceptWarning(source, feature, code, message,
				para);
	}

	protected void error(EObject source, Integer feature, int code,
			String message, Object... para) {
		validationMessageAcceptor.acceptError(source, feature, code, message,
				para);
	}

	@Check
	public void checkQMgrsNoAndDuplicateAssignment(Model model) {
		List<QMgr> allQMgrs = allQMgrs(model);
		Multimap<QMgr, Node> qmgr2NodeMap = new HashMultimap<QMgr, Node>();
		for (Node node : allNodes(model)) {
			for (QMgr qMgr : node.getQmgrs()) {
				qmgr2NodeMap.put(qMgr, node);
				allQMgrs.remove(qMgr);
			}
		}
		for (QMgr qMgr : qmgr2NodeMap.keySet()) {
			Collection<Node> nodes = qmgr2NodeMap.get(qMgr);
			if (nodes.size() > 1) {
				Iterable<String> nodeNameList = computeNodeNameList(nodes);
				for (Node node : nodes) {
					error(
							node,
							MQCfgPackage.NODE__QMGRS,
							0,
							"%1$s: Duplicate assignment of queue manager %2$s to the nodes %3$s.",
							node.getName(), qMgr.getName(), nodeNameList);
				}
			}

		}
		for (QMgr qMgr : allQMgrs) {
			warning(qMgr, MQCfgPackage.QMGR, 0,
					"%1$s: The queue manager is not assigned to a node.", qMgr
							.getName());
		}
	}

	private Iterable<String> computeNodeNameList(Collection<Node> nodes) {
		return collect(nodes, on(Node.class).getName());
	}

	@Check
	public void checkTopLevelElementsForDuplicateNames(Model m) {
		List<TopLevelType> types = m.getElements();
		Map<String, TopLevelType> firstOccurrenceOfNameMap = new HashMap<String, TopLevelType>();
		Set<String> duplicateNamesSet = new HashSet<String>();
		for (TopLevelType element : types) {
			String elementName = element.getName();
			if (firstOccurrenceOfNameMap.containsKey(elementName)) {
				duplicateNamesSet.add(elementName);
				error(
						element,
						MQCfgPackage.TOP_LEVEL_TYPE__NAME,
						0,
						"%1$s: The name is not unique in the scope of the model.",
						elementName);
			} else {
				firstOccurrenceOfNameMap.put(elementName, element);
			}
		}
		for (String duplicateName : duplicateNamesSet) {
			error(firstOccurrenceOfNameMap.get(duplicateName),
					MQCfgPackage.TOP_LEVEL_TYPE__NAME, 0,
					"%1$s: The name is not unique in the scope of the model.",
					duplicateName);
		}
	}

	@Check
	public void checkIfClusterMembersAreAssignedToANode(final Cluster cluster) {
		for (ClusterQMgrBinding binding : cluster.getMembers()) {
			QMgr qmgr = binding.getQmgr();
			if (isNotAssignedToANode(qmgr)) {
				error(
						binding,
						MQCfgPackage.CLUSTER_QMGR_BINDING,
						0,
						"%2$s: The queue manager %1$s is a member of the cluster, but not assigned to a node.",
						qmgr.getName(), cluster.getName());
			}
		}
	}

	@Check
	public void checkForDuplicateInterfaceNames(final QMgr qmgr) {
		Iterable<String> interfaceNames = Iterables.transform(qmgr
				.getInterfaces(), new Function<Interface, String>() {
			public String apply(Interface from) {
				return from.getName();
			}
		});
		for (Interface iface : qmgr.getInterfaces()) {
			if (Iterables.frequency(interfaceNames, iface.getName()) > 1) {
				error(
						iface,
						MQCfgPackage.INTERFACE__NAME,
						0,
						"%2$s: The interface name %1$s is not unique in the scope of the queue manager.",
						iface.getName(), qmgr.getName());
			}
		}
	}

	@Check
	public void checkClusterHasAtLeastOneMainRepository(final Cluster cluster) {
		Iterable<ClusterQMgrBinding> repositoryQMgrBinding = allRepositoryQMgrBindings(cluster);
		if (Iterables.size(repositoryQMgrBinding) == 0) {
			error(
					cluster,
					MQCfgPackage.CLUSTER__MEMBERS,
					0,
					"%1$s: The cluster must have at least one repository queue manager.",
					cluster.getName());

		}
	}

	@Check
	public void checkClusterShouldHaveAtLeastTwoMainRepositories(
			final Cluster cluster) {
		Iterable<ClusterQMgrBinding> repositoryQMgrBinding = allRepositoryQMgrBindings(cluster);
		if (cluster.getMembers().size() > 1
				&& Iterables.size(repositoryQMgrBinding) < 2) {
			warning(
					cluster,
					MQCfgPackage.CLUSTER__MEMBERS,
					0,
					"%1$s: The cluster should have at least two repository queue manager.",
					cluster.getName());

		}
	}

	@Check
	public void checkAllNodeInterfacesMustBeUnique(final Node node) {
		boolean hasError = false;
		EList<QMgr> qmgrs = node.getQmgrs();
		List<String> ifaceConnectionNameList = new ArrayList<String>();
		for (QMgr qmgr : qmgrs) {
			for (Interface iface : qmgr.getInterfaces()) {
				String ifaceConnectionName = ipPortConnectionName(iface);
				if (ifaceConnectionNameList.contains(ifaceConnectionName)) {
					error(
							iface,
							MQCfgPackage.INTERFACE,
							0,
							"%3$s: The interface %1$s is not unique in the scope of the node %2$s.",
							iface.getName(), node.getName(), qmgr.getName());
					hasError = true;
				} else {
					ifaceConnectionNameList.add(ifaceConnectionName);
				}
			}
		}
		if (hasError) {
			error(
					node,
					MQCfgPackage.NODE__QMGRS,
					0,
					"%1$s: The queue manager interfaces on the node are not unique.",
					node.getName());
		}
	}

	public static String mqInterfaceConnectionName(Interface iface) {
		return String.format("%1$s(%2$s)", iface.getHost(), iface.getPort());
	}

	public static String ipPortConnectionName(Interface iface) {
		return String.format("%1$s:%2$s", iface.getIp(), iface.getPort());
	}

	@Check
	public void checkAllQMgrInterfacesMustBeUnique(final QMgr qmgr) {
		List<String> ifaceConnectionNameList = new ArrayList<String>();
		for (Interface iface : qmgr.getInterfaces()) {
			String ifaceConnectionName = ipPortConnectionName(iface);
			if (ifaceConnectionNameList.contains(ifaceConnectionName)) {
				error(
						iface,
						MQCfgPackage.INTERFACE,
						0,
						"%2$s: The interface %1$s is not unique in the scope of the queue manager.",
						iface.getName(), qmgr.getName());
			} else {
				ifaceConnectionNameList.add(ifaceConnectionName);
			}
		}
	}

	@Check
	public void checkForMultipleIPsForAHostname(final Model model) {
		List<QMgr> allQMgrs = allQMgrs(model);
		Multimap<String, Interface> host2InterfaceMap = new HashMultimap<String, Interface>();
		for (QMgr qmgr : allQMgrs) {
			for (Interface currentInterface : qmgr.getInterfaces()) {
				host2InterfaceMap.put(currentInterface.getHost(),
						currentInterface);
			}
		}
		for (String host : host2InterfaceMap.keySet()) {
			Set<Interface> errorList = new HashSet<Interface>();
			Collection<Interface> interfaceCollection = host2InterfaceMap
					.get(host);
			if (interfaceCollection.size() > 1) {
				Interface baseInterface = null;
				for (Interface currentInterface : interfaceCollection) {
					if (baseInterface == null) {
						baseInterface = currentInterface;
					}
					if (!baseInterface.getIp().equals(currentInterface.getIp())) {
						errorList.add(baseInterface);
						errorList.add(currentInterface);
					}
				}
			}
			for (Interface currentInterface : errorList) {
				error(currentInterface, MQCfgPackage.INTERFACE, 0,
						"%1$s: A hostname cannot have multiple IPs.",
						currentInterface.getName());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Check
	public void checkThatAnIPIsAssignedToOnlyOneNode(Model model) {
		List<Node> allNodes = allNodes(model);
		Map<String, Node> interface2NodeList = new HashMap<String, Node>();
		for (Node node : allNodes) {
			Object tmpInterfaceList = collect(node.getQmgrs(), on(QMgr.class)
					.getInterfaces());
			List<Interface> nodeInterfaceList = (List<Interface>) tmpInterfaceList;
			for (Interface currentInterface : nodeInterfaceList) {
				Node tmpNode = interface2NodeList.get(currentInterface.getIp());
				if (tmpNode == null) {
					interface2NodeList.put(currentInterface.getIp(), node);
				} else if (tmpNode != node) {
					error(
							currentInterface,
							MQCfgPackage.INTERFACE,
							0,
							"%1$s: The IP of the interface is assigned to more than one node %2$s.",
							currentInterface.getName(), Arrays.asList(tmpNode.getName(),
									node.getName()));
				}
			}

		}
	}
}
