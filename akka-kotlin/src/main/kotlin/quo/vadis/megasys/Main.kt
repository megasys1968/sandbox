package quo.vadis.megasys

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.javadsl.Http
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory

class Main

fun main() {
  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  val system = ActorSystem.create("go-ticks")

  val log = Logging.getLogger(system, Main::class.java)
  log.info("start actor system: {}", system.name())

  val http = Http.get(system)
  val materializer = Materializer.matFromSystem { system }


}