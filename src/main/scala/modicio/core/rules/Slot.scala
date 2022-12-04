package modicio.core.rules

/**
 * @param targetName name of the target [[modicio.core.ModelElement ModelElement]]
 * @param targetVariantTimeArg variantTime of the target [[modicio.core.ModelElement ModelElement]] starting with a comparison
 *                          prefix ">", "<" or "=", or no prefix which is interpreted as "="
 */
case class Slot(targetName: String, targetVariantTimeArg: String)
