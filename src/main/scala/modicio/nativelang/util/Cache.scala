package modicio.nativelang.util

trait Cache[K, V] {
  def set(key: K, value: V): Boolean;
  def get(key: K): Option[V] ;
  def size(): Int;
  def isEmpty(): Boolean;
  def clear(): Unit;

}
