package quo.vadis.megasys

import akka.actor.AbstractLoggingActor

class TicketSeller: AbstractLoggingActor() {
  companion object {
    data class Add(val tickets: List<Ticket>)
    data class Buy(val tickets: Int)
    data class Ticket(val id: Int)
    data class Tickets(val event: String,
                   val entries: MutableList<Ticket> = mutableListOf())
    object GetEvent
    object Cancel
  }


  override fun createReceive(): Receive =
    receiveBuilder()
      .match(Add::class.java) {

      }
      .match(Buy::class.java) {

      }
      .match(GetEvent::class.java) {

      }
      .match(Cancel::class.java) {

      }
      .build();
}