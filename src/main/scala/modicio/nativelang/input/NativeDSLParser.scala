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
package modicio.nativelang.input

import io.circe.{Decoder, Json, parser}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.generic.semiauto.deriveDecoder

object NativeDSLParser {

  def parse(nativeInput: String): NativeDSL = {
    val decodingResult = parser.decode[NativeDSL](nativeInput)
    decodingResult.toOption.getOrElse(throw new Exception("Decoding Error"))
  }

  def parseCompartment(nativeInput: String): NativeCompartment = {
    val decodingResult = parser.decode[NativeCompartment](nativeInput)
    decodingResult.toOption.getOrElse(throw new Exception("Decoding Error"))
  }

  def produceJson(extendedNativeDSL: NativeCompartment): Json = {
    implicit val decodeNativeDSL: Decoder[NativeDSL] = deriveDecoder[NativeDSL]
    implicit val decodeExtendedNativeDSL: Decoder[NativeCompartment] = deriveDecoder[NativeCompartment]
    val encodingResult = extendedNativeDSL.asJson
    encodingResult
  }

  def produceJson(nativeDSL: NativeDSL): Json = {
    implicit val decodeNativeDSL: Decoder[NativeDSL] = deriveDecoder[NativeDSL]
    val encodingResult = nativeDSL.asJson
    encodingResult
  }

  def produceString(extendedNativeDSL: NativeCompartment): String = {
    produceJson(extendedNativeDSL).toString()
  }

  def produceString(nativeDSL: NativeDSL): String = {
    produceJson(nativeDSL).toString()
  }

}
