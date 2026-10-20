package ar.uba.dc.rfm.dynalloy.parser;

/**
 * Unused. The only place that throws it is commented out in {@code dynalloy.g} (rule
 * {@code alloySignature}): the check for a field declared in more than one signature. So a later
 * field with the same name replaces the earlier one in the module's fields
 * ({@code DynalloyModule.getDynalloyFields()}).
 */
public class AmbiguosFieldDeclarationException extends RuntimeException {

	public AmbiguosFieldDeclarationException(String message) {
		super(message);
	}
}
