package org.grassaccords.emqcfg.util;

import static org.eclipse.xtext.EcoreUtil2.*;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.grassaccords.emqcfg.mQCfg.Channel;
import org.grassaccords.emqcfg.mQCfg.Cluster;
import org.grassaccords.emqcfg.mQCfg.ClusterQMgrBinding;
import org.grassaccords.emqcfg.mQCfg.Node;
import org.grassaccords.emqcfg.mQCfg.QMgr;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public final class MQCfgUtil {

	private MQCfgUtil() {
		//
	}

	public static List<QMgr> allQMgrs(EObject m) {
		return all(m, QMgr.class);
	}

	public static List<Node> allNodes(EObject m) {
		return all(m, Node.class);
	}

	public static List<Cluster> allCluster(EObject m) {
		return all(m, Cluster.class);
	}

	public static List<Channel> allChannels(EObject m) {
		return all(m, Channel.class);
	}

	public static <T> List<T> all(EObject m, Class<T> clazz) {
		EObject container = m.eContainer();
		while (container != null) {
			m = container;
			container = m.eContainer();
		}
		return typeSelect(m.eContents(), clazz);
	}

	
	public static boolean isNotAssignedToANode(final QMgr qmgr) {
		return !isAssignedToANode(qmgr);
	}

	public static boolean isAssignedToANode(final QMgr qmgr) {
		Iterable<Node> listHostingNodes = Iterables.filter(all(qmgr, Node.class),
				new Predicate<Node>() {
					public boolean apply(Node input) {
						return input.getQmgrs().contains(qmgr);
					}
				});
		return Iterables.size(listHostingNodes)>0;
	}

	public static boolean isMemberOfCluster(final QMgr qmgr) {
		Iterable<Cluster> listHostingNodes = Iterables.filter(all(qmgr, Cluster.class),
				new Predicate<Cluster>() {
					public boolean apply(Cluster input) {
						return Iterables.size(Iterables.filter(input.getMembers(),
								new Predicate<ClusterQMgrBinding>() {
							public boolean apply(ClusterQMgrBinding input) {
								return input.getQmgr().equals(qmgr);
							}
						}))>0;
					}
				});
		return Iterables.size(listHostingNodes)>0;
	}

	public static Iterable<ClusterQMgrBinding> allRepositoryQMgrBindings(
			final Cluster cluster) {
		Iterable<ClusterQMgrBinding> repositoryQMgrBinding = Iterables.filter(cluster.getMembers(),new Predicate<ClusterQMgrBinding>() {
			public boolean apply(ClusterQMgrBinding input) {
				return input.isIsRepository();
			}
		});
		return repositoryQMgrBinding;
	}

}
