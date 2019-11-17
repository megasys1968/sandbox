package quo.vadis.megasys

class BoxOffice {
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
}