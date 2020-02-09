package quo.vadis.megasys

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.javadsl.ConnectHttp
import akka.http.javadsl.Http
import akka.http.javadsl.ServerBinding
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import java.time.Duration

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

  val app = RestApi(system, Duration.ofSeconds(5));

  val routeFlow = app.createRoute().flow(system, materializer);
  val binding = http.bindAndHandle(routeFlow,
  ConnectHttp.toHost(host, port), materializer); // HTTPサーバーの起動

  log.info("Server online at http://{}:{}", host, port);
  log.info("Press RETURN to stop...");

  System.`in`.read();

  log.info("presses return...");

  binding
    .thenCompose{ it.unbind() }
    .thenAccept{ system.terminate() }
}