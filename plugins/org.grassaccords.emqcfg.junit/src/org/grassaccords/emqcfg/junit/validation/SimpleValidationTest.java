package org.grassaccords.emqcfg.junit.validation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.MweReader;
import org.eclipse.xtext.parsetree.CompositeNode;
import org.eclipse.xtext.resource.XtextResource;
import org.grassaccords.emqcfg.MQCfgStandaloneSetup;
import org.grassaccords.emqcfg.junit.AbstractXtextTests;
import org.grassaccords.emqcfg.mQCfg.Cluster;
import org.grassaccords.emqcfg.util.MQCfgUtil;
import org.grassaccords.emqcfg.validation.MQCfgJavaValidator;

public class SimpleValidationTest extends AbstractXtextTests {

	private MQCfgJavaValidator validator;
	
	public SimpleValidationTest() {
	}

	protected void setUp() throws Exception {
		super.setUp();
		with(MQCfgStandaloneSetup.class);
		validator=get(MQCfgJavaValidator.class);
	}
	
	public void testSimple() throws Exception {
		
	}
	
	
	public void testAllClusterChecks() throws Exception {
		InputStream is=this.getClass().getResourceAsStream("ClusterChecks.mqcfg");
		assertNotNull(is);
		XtextResource resource = getResource(is);
		
		EObject object = resource.getParseResult().getRootASTElement();
		assertNotNull( object );
		
		Map<Object, Object> context=new HashMap<Object, Object>();
		DiagnosticChain diagnostics=new DiagnosticChain() {
			private final List<Integer> integers = new ArrayList<Integer>();

			public boolean isEmpty() {
				return integers.isEmpty();
			}

			public void add(org.eclipse.emf.common.util.Diagnostic diagnostic) {
				System.out.println(diagnostic);
				Assert.assertTrue(diagnostic.getData().get(0) instanceof EObject);
				integers.add((Integer) diagnostic.getData().get(1));
			}

			public void addAll(org.eclipse.emf.common.util.Diagnostic diagnostic) {
				throw new UnsupportedOperationException();
			}

			public void merge(org.eclipse.emf.common.util.Diagnostic diagnostic) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String toString() {
				return integers.toString();
			}
		};
		List<Cluster> allCluster = MQCfgUtil.allCluster(object);
		for (Cluster cluster : allCluster) {
			validator.validate(cluster, diagnostics, context);
		}
		System.out.println(diagnostics.toString());
	}
	
	


}
