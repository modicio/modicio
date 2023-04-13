package modicio.core.util

case class IODiff[T](toDelete: Set[T], toAdd: Set[T], toUpdate: Set[T])
