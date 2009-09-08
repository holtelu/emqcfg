/**
 * 
 */
package org.grassaccords.emqcfg.validation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

public interface ValidationMessageAcceptor2 extends
		ValidationMessageAcceptor {
	void acceptWarning(EObject object, Integer feature, int code,
			String message, Object... parameter);

	void acceptError(EObject object, Integer feature, int code,
			String message, Object... parameter);

}