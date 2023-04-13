package modicio.nativelang.util

import scala.util.Try

trait AccessCounting {
  def writeAccessCounts(fileName: String, path: String): Try[Unit];
}
