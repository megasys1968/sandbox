package quo.vadis.megasys

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class EventDescription @JsonCreator constructor(
  @param:JsonProperty("tickets")
  val tickets: Int
)

data class TicketRequest @JsonCreator constructor(
  @param:JsonProperty("tickets")
  val tickets: Int
)

data class EventError @JsonCreator constructor(
  @param:JsonProperty("message")
  val message: String
)