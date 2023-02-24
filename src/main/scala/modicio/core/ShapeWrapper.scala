package modicio.core

import modicio.core.datamappings.{AssociationData, AttributeData, ParentRelationData}
import modicio.core.util.IODiff

import scala.collection.mutable

class ShapeWrapper(attributes: Set[AttributeData], associations: mutable.Set[AssociationData], parentRelations: Set[ParentRelationData])
  extends Shape(attributes, associations, parentRelations) {

  private val hotShape = new Shape(attributes, associations, parentRelations)
  private val coldShape = new Shape(attributes.map(data => data.copy()), associations.map(data => data.copy()), parentRelations.map(data => data.copy()))

  override def getAttribute(key: String): Option[AttributeData] = hotShape.getAttribute(key)

  override def getAttributes: Set[AttributeData] = hotShape.getAttributes

  override def setAttributeValue(key: String, value: String): Boolean = hotShape.setAttributeValue(key, value)

  override def getAssociations: Set[AssociationData] = hotShape.getAssociations

  override def removeAssociation(associationId: Long): Unit = hotShape.removeAssociation(associationId)

  override def addAssociation(association: AssociationData): Unit = hotShape.addAssociation(association)

  override def getParentRelations: Set[ParentRelationData] = hotShape.getParentRelations

  def getAttributeDiff(): IODiff[AttributeData] = {
    val add = hotShape.getAttributes.filter(datum => datum.id == 0)
    val changed = mutable.Set[AttributeData]()
    for (hotAttribute <- hotShape.getAttributes.filter(datum => datum.id != 0)) {
      val coldAttribute = coldShape.getAttribute(hotAttribute.key)
      if (coldAttribute.isEmpty) {
        throw new IllegalStateException("Attribute was removed from Instance!")
      }
      if (coldAttribute.get.value != hotAttribute.value) {
        changed += hotAttribute
      }
    }
    IODiff[AttributeData](Set(), add, changed.toSet)
  }

  def getAssociationDiff(): IODiff[AssociationData] = {
    val add = hotShape.getAssociations.filter(datum => datum.id == 0)
    val unchanged = hotShape.getAssociations.intersect(coldShape.getAssociations)
    add.concat(hotShape.getAssociations.diff(unchanged))
    val remove = coldShape.getAssociations.diff(unchanged)
    IODiff[AssociationData](remove, add, Set())
  }

  def getParentRelationDiff(): IODiff[ParentRelationData] = {
    val add = hotShape.getParentRelations.filter(datum => datum.id == 0)
    IODiff[ParentRelationData](Set(), add, Set())
  }
}
