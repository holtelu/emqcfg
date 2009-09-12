package org.grassaccords.emqcfg.junit;

import java.io.InputStream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.XtextResource;

public abstract class AbstractMQCfgXtextTests extends AbstractXtextTests {

	@SuppressWarnings("unchecked")
	protected <T extends EObject> T readModelFromClasspath(Class<T> clazz, String modelResourceName)
			throws Exception {
				InputStream is=this.getClass().getResourceAsStream(modelResourceName);
				assertNotNull(is);
				XtextResource resource = getResource(is);
				EObject object = resource.getParseResult().getRootASTElement();
				return (T) object;
			}

}
