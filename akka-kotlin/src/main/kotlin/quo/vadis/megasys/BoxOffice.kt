package quo.vadis.megasys

import akka.actor.AbstractLoggingActor
import java.time.Duration

class BoxOffice(val timeout: Duration): AbstractLoggingActor() {

  companion object {
    data class CreateEvent(val name: String, val tickets: Int)
    data class GetEvent(val name: String)
    object GetEvents
    data class GetTickets(val event: String, val tickets: Int)
    data class CancelEvent(val name: String)

    interface EventResponse
    data class Event(val name: String, val tickets: Int): EventResponse
    data class Events(val events: List<Event>): EventResponse
  }

  override fun createReceive(): Receive =
    receiveBuilder()
      .match(CreateEvent::class.java) {

      }
      .match(GetTickets::class.java) {

      }
      .match(GetEvent::class.java) {

      }
      .match(GetEvents::class.java) {

      }
      .match(CancelEvent::class.java) {

      }
      .build()

}