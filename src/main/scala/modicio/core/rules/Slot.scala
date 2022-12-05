package modicio.core.rules

/**
 * @param targetName name of the target [[modicio.core.ModelElement ModelElement]]
 * @param targetVariantTimeArg variantTime of the target [[modicio.core.ModelElement ModelElement]] starting with a comparison
 *                          prefix ">", "<" or "=", or no prefix which is interpreted as "="
 */
case class Slot(targetName: String, targetVariantTimeArg: String)

object Slot {

  def parseTimeArg(slot: Slot): Long = {
    val prefix = slot.targetVariantTimeArg.head.toString
    val tail = slot.targetVariantTimeArg.tail
    prefix match {
      case "<" => tail.toLong
      case ">" => tail.toLong
      case "=" => tail.toLong
      case _ => slot.targetVariantTimeArg.toLong
    }
  }

  def parsePrefix(slot: Slot): String = {
    val head = slot.targetVariantTimeArg.head.toString
    if(Seq("<", ">", "=").contains(head)){
      head
    }else{
      "="
    }
  }

}
