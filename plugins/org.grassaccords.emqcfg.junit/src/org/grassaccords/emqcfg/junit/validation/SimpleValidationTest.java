package org.grassaccords.emqcfg.junit.validation;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.grassaccords.emqcfg.MQCfgStandaloneSetup;
import org.grassaccords.emqcfg.junit.AbstractMQCfgXtextTests;
import org.grassaccords.emqcfg.mQCfg.Cluster;
import org.grassaccords.emqcfg.mQCfg.Model;
import org.grassaccords.emqcfg.util.MQCfgUtil;
import org.grassaccords.emqcfg.validation.MQCfgJavaValidator;

public class SimpleValidationTest extends AbstractMQCfgXtextTests {

	public static class TestDiagnosticChain implements DiagnosticChain, Iterable<Diagnostic>{
		private List<Diagnostic> diagnosticList=new ArrayList<Diagnostic>();

		public boolean isEmpty() {
			return diagnosticList.isEmpty();
		}

		public void add(org.eclipse.emf.common.util.Diagnostic diagnostic) {
			diagnosticList.add(diagnostic);
		}

		public void addAll(org.eclipse.emf.common.util.Diagnostic diagnostic) {
			throw new UnsupportedOperationException();
		}

		public void merge(org.eclipse.emf.common.util.Diagnostic diagnostic) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return diagnosticList.toString();
		}

		@Override
		public Iterator<Diagnostic> iterator() {
			return diagnosticList.iterator();
		}
	}

	private MQCfgJavaValidator validator;
	private Map<Object, Object> context;
	private TestDiagnosticChain diagnostics;
	
	public SimpleValidationTest() {
	}

	protected void setUp() throws Exception {
		super.setUp();
		with(MQCfgStandaloneSetup.class);
		validator=get(MQCfgJavaValidator.class);
		context = new HashMap<Object, Object>();
		diagnostics = new TestDiagnosticChain();
	}
	
	public void testAllClusterChecks() throws Exception {
		Model model = readModelFromClasspath(Model.class, "ClusterChecks.mqcfg");
		assertNotNull( model );
		
		List<Cluster> allCluster = MQCfgUtil.allCluster(model);
		for (Cluster cluster : allCluster) {
			validator.validate(cluster, diagnostics, context);
		}
		
		List<Diagnostic> sortedList = sort(diagnostics, on(Diagnostic.class).getMessage());
		for (Diagnostic object : sortedList) {
			System.out.println(object.getMessage());
		}
		
	}

	public void testIPIsAssignedToOnlyOneNode() throws Exception {
		Model model = readModelFromClasspath(Model.class, "IPIsAssignedToOnlyOneNode.mqcfg");
		assertNotNull( model );

		validator.validate(model, diagnostics, context);
		
		assertEquals(2, diagnostics.diagnosticList.size());
		assertThat(diagnostics.diagnosticList.get(0).getMessage(),containsString("[Node1, Node2]"));
		assertThat(diagnostics.diagnosticList.get(1).getMessage(),containsString("[Node1, Node2]"));
	}

	


}
