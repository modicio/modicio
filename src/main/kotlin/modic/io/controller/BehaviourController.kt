/**
 * Copyright 2023 Karl Kegel
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

package modic.io.controller

import modic.io.logic.BehaviourService
import modic.io.model.Fragment
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class BehaviourController(val behaviourService: BehaviourService) {

    @PostMapping("instance/action", produces = [MediaType.APPLICATION_XML_VALUE])
    fun triggerStoredActionScript(
        @RequestParam(required = true, name = "fragment_data_id") fragmentDataID: Long,
        @RequestParam(required = true, name = "script_uri") scriptURI: String
    ): Fragment?{
        behaviourService.triggerStoredActionScript(fragmentDataID, scriptURI)
        return null
    }
}