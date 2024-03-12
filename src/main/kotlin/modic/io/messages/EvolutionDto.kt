package modic.io.messages

import com.fasterxml.jackson.annotation.JsonProperty


class EvolutionDto {

    @JsonProperty("request")
    var request: String = ""

    fun EvolutionDto() {}

}