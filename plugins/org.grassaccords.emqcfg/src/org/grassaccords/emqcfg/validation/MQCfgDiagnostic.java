/**
 * 
 */
package org.grassaccords.emqcfg.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.validation.CheckType;

public class MQCfgDiagnostic implements Diagnostic {

	MQCfgDiagnostic(int severity, int code, String message,
			EObject source, Integer feature, CheckType checkType) {
		super();
		this.code = code;
		this.severity = severity;
		this.message = message;
		this.source = source;
		this.feature = feature;
		this.checkType = checkType;
	}

	private final int code;
	private final String message;
	private final EObject source;
	private final Integer feature;
	private final int severity;
	private final CheckType checkType;

	public List<Diagnostic> getChildren() {
		return Collections.emptyList();
	}

	public int getCode() {
		return code;
	}

	public List<?> getData() {
		List<Object> result = new ArrayList<Object>(2);
		result.add(source);
		if (feature != null)
			result.add(feature);
		return result;
	}

	public Throwable getException() {
		return null;
	}

	public String getMessage() {
		return message;
	}

	public int getSeverity() {
		return severity;
	}

	public String getSource() {
		return source.toString();
	}

	public CheckType getCheckType() {
		return checkType;
	}

	// partially copied from BasicDiagnostic#toString()
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("Diagnostic ");
		switch (severity) {
		case OK: {
			result.append("OK");
			break;
		}
		case INFO: {
			result.append("INFO");
			break;
		}
		case WARNING: {
			result.append("WARNING");
			break;
		}
		case ERROR: {
			result.append("ERROR");
			break;
		}
		case CANCEL: {
			result.append("CANCEL");
			break;
		}
		default: {
			result.append(Integer.toHexString(severity));
			break;
		}
		}
		result.append(" [");
		result.append(code);
		result.append("] ");
		result.append(message);

		result.append(" source=");
		result.append(source);

		result.append(" feature=");
		result.append(feature);



		return result.toString();
	}

}