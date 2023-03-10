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

class ModelModificationIntegrationSpec extends Spec with ModelModificationIntegrationBehaviors {

  "A SimpleMapRegistry" should behave like registry(simpleMapRegistry)

  "A VolatilePersistentRegistry" should behave like registry(volatilePersistentRegistry)

  "A cached Registry" should behave like registry(cachedRegistry)

}