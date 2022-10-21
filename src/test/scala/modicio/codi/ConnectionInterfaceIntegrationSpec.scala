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

package modicio.codi

import modicio.AbstractIntegrationSpec


class ConnectionInterfaceIntegrationSpec extends AbstractIntegrationSpec {

  "DeepInstance.associate()" must "create correct AssociationData if a matching Slot is found" in {
    initProjectSetup() flatMap (_ => {
      for {
        todoInstance <- instanceFactory.newInstance(TODO)
        projectInstance <- instanceFactory.newInstance(PROJECT)
      } yield {
        val res = projectInstance.associate(todoInstance, TODO, PROJECT_CONTAINS_TODO)
        res should be(true)
      }
    })
  }

  "DeepInstance.associate()" must "fail if no matching Slot is found by targetName" in {
    initProjectSetup() flatMap (_ => {
      for {
        todoInstance <- instanceFactory.newInstance(TODO)
        projectInstance <- instanceFactory.newInstance(PROJECT)
      } yield {
        val thrown = intercept[Exception] {
          val res = projectInstance.associate(todoInstance, PROJECT, PROJECT_CONTAINS_TODO)
        }
        assert(thrown.getMessage === "The proposed relation is not defined on top of a type which is in the instance type hierarchy")
      }
    })
  }

}
