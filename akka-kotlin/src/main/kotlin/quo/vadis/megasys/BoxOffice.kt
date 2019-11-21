package quo.vadis.megasys

import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import akka.actor.Props
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture
import akka.pattern.PatternsCS.ask
import akka.pattern.PatternsCS.pipe
import java.util.concurrent.CompletionStage
import java.util.stream.Collectors
import java.util.stream.IntStream

class BoxOffice(val timeout: Duration): AbstractLoggingActor() {
  companion object {
    fun props(timeout: Duration): Props {
      return Props.create(BoxOffice::class.java) { BoxOffice(timeout) }
    }
  }

  data class CreateEvent(val name: String, val tickets: Int)
  data class GetEvent(val name: String)
  object GetEvents
  data class GetTickets(val event: String, val tickets: Int)
  data class CancelEvent(val name: String)

  data class Event(val name: String, val tickets: Int)
  data class Events(val events: List<Event>)

  interface EventResponse
  data class EventCreated(val event: Event): EventResponse
  object EventExists : EventResponse


  private val msg = "    📩 {}"


  private fun createTicketSeller(name: String): ActorRef {
    return context.actorOf(TicketSeller.props(name), name)
  }

  override fun createReceive(): Receive =
    receiveBuilder()
      .match(CreateEvent::class.java) { createEvent->
        log().debug(msg, createEvent)

        context.findChild(createEvent.name)
          .ifPresentOrElse({

          getContext().sender().tell( EventExists, self());
        } , {
          val eventTickets = createTicketSeller(createEvent.name)
          val newTickets =
          IntStream.rangeClosed(1, createEvent.tickets)
            .mapToObj{ TicketSeller.Ticket(it) }
          .collect(Collectors.toList());

          eventTickets.tell( TicketSeller.Add(newTickets), self);
          getContext().sender().tell(EventCreated(Event(createEvent.name, createEvent.tickets)), self);
        })


      }
      .match(GetTickets::class.java) { getTickets ->
        log().debug(msg, getTickets)

        context.findChild(getTickets.event)
          .ifPresentOrElse({
            it.forward(TicketSeller.Buy(getTickets.tickets), context)
          }, {
            context.sender().tell(TicketSeller.Tickets(getTickets.event), self)
          }        )
      }
      .match(GetEvent::class.java) { getEvent ->
        log().debug(msg, getEvent)

        context.findChild(getEvent.name)
          .ifPresentOrElse({
            it.forward(TicketSeller.GetEvent, context)
          }, {
            context.sender().tell(Optional.empty<Any>(), self)
          }        )
      }
      .match(GetEvents::class.java) {getEvents->
        log().debug(msg, getEvents)

        // 子アクター（TicketSeller）に ask した結果のリストを作成
        val children = mutableListOf<CompletableFuture<Optional<Event>>>()

        context.children.forEach{child ->
        children.add(ask(self, GetEvent(child.path().name()), timeout)
          .thenApply{ it as Optional<Event> }
          .toCompletableFuture())
        }

        // List<CompletableFuture<Optional<Event>>> の children を CompletionStage<Events> に変換
        // Events は List<Event> を持つ
        val futureEvents = CompletableFuture
          .allOf(*children.toTypedArray())
          .thenApply{_ ->
        children.stream()
          .map{ it.join() }
          .filter{ it.isPresent }
          .map{ it.get() }
          .collect(Collectors.toList())
          .let { Events(it) }
      }

        pipe(futureEvents, context.dispatcher()).to(sender());

      }
      .match(CancelEvent::class.java) {cancelEvent->
        log().debug(msg, cancelEvent)

        context.findChild(cancelEvent.name)
          .ifPresentOrElse({
            it.forward(TicketSeller.Cancel, context);
          },
            {
              context.sender().tell(Optional.empty<Any>(), self);
            })

      }
      .build()
}