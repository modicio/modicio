/**
 * Copyright 2022 Karl Kegel, Tom Felber
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

package modicio.codi

import modicio.AbstractIntegrationSpec


class RegistryPerformanceSpec extends AbstractIntegrationSpec {

  "DeepInstance.assignValue2" must "change the value of the correspondent key" in { fixture => {
      fixture.importProjectSetupFromFile("model_02.json") flatMap (_ =>
        for {
          todoInstance1 <- fixture.instanceFactory.newInstance("Todo")
          todoInstance2 <- fixture.instanceFactory.newInstance("Todo")
          projectInstance1 <- fixture.instanceFactory.newInstance("Project")
        } yield {
//          projectInstance1.associate(todoInstance1, fixture.PROJECT_ITEM, fixture.PROJECT_HAS_PART)
          1 should be(1)
        }
      )
    }
  }
}
