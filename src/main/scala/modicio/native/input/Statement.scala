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
package modicio.native.input

/**
 * @param name
 * @param template
 * @param childOf
 * @param associations
 * @param attributes
 * @param values
 */
case class Statement(name: String,
                     template: Boolean,
                     childOf: Seq[String],
                     associations: Seq[String],
                     attributes: Seq[String],
                     values: Seq[String])

/**
 * TODO documentation
 */
object Statement {
  def parseIdentity(statement: Statement): String = statement.name.split(":").head

  def parseName(statement: Statement): String = statement.name.split(":").reverse.head
}
