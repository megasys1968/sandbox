package quo.vadis.megasys

import akka.actor.AbstractLoggingActor
import akka.actor.PoisonPill
import akka.actor.Props


class TicketSeller(private val event: String) : AbstractLoggingActor() {
  companion object {
    fun props(event: String): Props {
      return Props.create(TicketSeller::class.java) { TicketSeller(event) }
    }
  }

  data class Add(val tickets: List<Ticket>)
  data class Buy(val tickets: Int)
  data class Ticket(val id: Int)
  data class Tickets(val event: String,
                     val entries: MutableList<Ticket> = mutableListOf())

  object GetEvent
  object Cancel

  private val msg = "    📩 {}";
  private val tickets = mutableListOf<Ticket>()

  override fun createReceive(): Receive =
    receiveBuilder()
      .match(Add::class.java) {
        log().debug(msg, it)
        tickets.addAll(it.tickets)
      }
      .match(Buy::class.java) {
        log().debug(msg, it)

        if (tickets.size >= it.tickets) {
          val entries = tickets.subList(0, it.tickets)
          context.sender().tell(Tickets(event, entries), self);
          entries.clear();
        } else {
          context.sender().tell(Tickets(event), self);
        }

      }
      .match(GetEvent::class.java) {
        log().debug(msg, it)

        sender().tell(BoxOffice.Event(event, tickets.size), self);

      }
      .match(Cancel::class.java) {
        log().debug(msg, it)

        sender().tell(BoxOffice.Event(event, tickets.size), self);
        self().tell(PoisonPill.getInstance(), self());

      }
      .build()
}