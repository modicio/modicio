/**
 * Copyright 2022 Karl Kegel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package modicio.core

import scala.collection.mutable.ListBuffer

class TypeIterator(private[modicio] val initialModelElement: ModelElement) {

  private val _currentBuffer: ListBuffer[ModelElement] = ListBuffer(initialModelElement)

  def name: String = current.get.name

  private def current: Option[ModelElement] = {
    if(_currentBuffer.isEmpty){
      None
    }else {
      val head = _currentBuffer.head
      Some(head)
    }
  }

  @Deprecated
  def isDynamic: Boolean = current.get.isInstanceOf[ModelElement]

  def get: Option[Base] = {
    if(current.isEmpty){
      None
    }else{
      Some(current.get.definition)
    }
  }

  def getName: Option[String] = {
    if(current.isDefined){
      Some(current.get.name)
    }else{
      None
    }
  }

  def asDefinition: Option[Definition] = {
    if(current.isEmpty){
      None
    }else{
      Some(current.get.definition)
    }
  }

  def split: Option[TypeIterator] = {
    if(current.isEmpty){
      None
    }else{
      Some(new TypeIterator(current.get))
    }
  }

  def splitHandle: Option[TypeHandle] = {
    if(current.isEmpty){
      None
    }else{
      Some(current.get.createHandle)
    }
  }

  def getAssociated: Set[TypeHandle] = {
    if(current.isDefined){
      current.get.getParents.map(_.createHandle)
    }else{
      Set()
    }
  }

  def up: Boolean = {
    if(current.isDefined){
      val parents = current.get.getParents
      if(parents.nonEmpty){
        _currentBuffer.clear()
        _currentBuffer.addAll(parents)
        return true
      }
    }
    false
  }

  def next: Boolean = {
    if(current.isDefined){
      _currentBuffer.remove(0)
      true
    }else{
      false
    }
  }

}
