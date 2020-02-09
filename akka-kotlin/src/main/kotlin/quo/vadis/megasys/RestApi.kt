package quo.vadis.megasys

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.event.Logging;
import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.server.AllDirectives
import akka.http.javadsl.server.PathMatchers.segment
import akka.http.javadsl.server.Route
import java.time.Duration
import java.util.*
import akka.pattern.PatternsCS.ask


class RestApi(system: ActorSystem, val timeout: Duration) : AllDirectives() {
  private val log = Logging.getLogger(system, this)
  private val boxOfficeActor: ActorRef = system.actorOf(BoxOffice.props(timeout), "boxOfficeActor");
  private val msg = "      📩 {}"

  fun createRoute() = route(
    pathPrefix("events") {
      route(
        getEvents(),
        pathPrefix(segment()) { name ->
          route(
            getEvent(name),
            createEvent(name),
            cancelEvent(name)
          )
        },
        pathPrefix(segment().slash(segment("tickets"))) { event ->
          route(
            requestTickets(event)
          )
        }
      )
    }
  )

  private fun getEvents() =
    // [Get all events] GET /events/
    get {
      pathEndOrSingleSlash {
        log.debug("---------- GET /events/ ----------");

        val events = ask(boxOfficeActor, BoxOffice.GetEvents, timeout)
          .thenApply { it as BoxOffice.Events }

        return@pathEndOrSingleSlash onSuccess(
          { events },
          {
            log.debug(msg, it)
            return@onSuccess completeOK(it, Jackson.marshaller())
          })
      }
    }

  private fun getEvent(name: String) =
    // [Get an event] GET /events/:name/
    pathEndOrSingleSlash {
      get {
        log.debug("---------- GET /events/{}/ ----------", name);

        val futureEvent =
          ask(boxOfficeActor, BoxOffice.GetEvent(name), timeout)
            .thenApply { it as Optional<BoxOffice.Event> }

        return@get onSuccess({ futureEvent }, {
          log.debug(msg, it)
          return@onSuccess if (it.isPresent())
            completeOK(it.get(), Jackson.marshaller());
          else
            complete(StatusCodes.NOT_FOUND);
        })
      }
    }

  private fun createEvent(name: String): Route {
    // [Create an event] POST /events/:name/ tickets:=:tickets
    return pathEndOrSingleSlash {
      post {
        entity(Jackson.unmarshaller(EventDescription::class.java)) { event ->
          log.debug("---------- POST /events/{}/ {\"tickets\":{}} ----------", name, event.tickets);

          val futureEventResponse =
            ask(boxOfficeActor, BoxOffice.CreateEvent(name, event.tickets), timeout)
              .thenApply { it as BoxOffice.EventResponse }

          return@entity onSuccess({ futureEventResponse }, {
            log.debug(msg, it)
            if (it is BoxOffice.EventCreated) {
              val maybeEvent = it.event
              return@onSuccess complete(StatusCodes.CREATED, maybeEvent, Jackson.marshaller())
            } else {
              val err = EventError("$name exists already.")
              return@onSuccess complete(StatusCodes.BAD_REQUEST, err, Jackson.marshaller());
            }
          })
        }
      }
    }
  }

  private fun requestTickets(event: String): Route {
    // [Buy tickets] POST /events/:event/tickets/ tickets:=:request
    return pathEndOrSingleSlash {
      post {
        entity(Jackson.unmarshaller(TicketRequest::class.java)) { request ->
          log.debug("---------- POST /events/{}/tickets/ {\"tickets\":{}} ----------", event, request.tickets);

          val futureTickets = ask(boxOfficeActor, BoxOffice.GetTickets(event, request.tickets), timeout)
            .thenApply { it as TicketSeller.Tickets }

          return@entity onSuccess({ futureTickets }, {
            log.debug(msg, it)
            return@onSuccess if (it.entries.isEmpty()) {
              complete(StatusCodes.NOT_FOUND);
            } else {
              complete(StatusCodes.CREATED, it, Jackson.marshaller());
            }
          })
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private fun cancelEvent(name: String): Route {
    // [Cancel an event] DELETE /events/:name/
    return pathEndOrSingleSlash {
      delete {
        log.debug("---------- DELETE /events/${name}/ ----------")

        val futureEvent =
          ask(boxOfficeActor, BoxOffice.CancelEvent(name), timeout)
            .thenApply { it as Optional<BoxOffice.Event> }

        return@delete onSuccess({ futureEvent }, {
          log.debug(msg, it)

          return@onSuccess if (it.isPresent) {
            completeOK(it.get(), Jackson.marshaller())
          } else {
            return@onSuccess complete(StatusCodes.NOT_FOUND)
          }
        })
      }
    }
  }

}