/**
 * Copyright 2022 Karl Kegel
 * Johannes Gröschel
 * Tom Felber
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
package modicio.codi.integrations

import modicio.Spec

class NativeInputTransformerIntegrationSpec extends Spec with NativeInputTransformerBehaviors {

  "The NativeInputTransformer paired with a SimpleMapRegistry" should behave like nativeInputTransformer(simpleMapRegistry)

  "The NativeInputTransformer paired with a VolatilePersistenRegistry" should behave like nativeInputTransformer(volatilePersistentRegistry)

  "The NativeInputTransformer paired with a cached Registry" should behave like nativeInputTransformer(cachedRegistry)

}
