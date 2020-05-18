package quo.vadis.megasys

import org.axonframework.config.EventProcessingConfigurer
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine
import org.axonframework.eventsourcing.eventstore.jdbc.PostgresEventTableFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class SampleAxonApplication {
  @Autowired
  fun configure(configurer: EventProcessingConfigurer) {
    configurer.usingSubscribingEventProcessors()
  }
}

fun main(args: Array<String>) {
  val context = runApplication<SampleAxonApplication>(*args)
}
