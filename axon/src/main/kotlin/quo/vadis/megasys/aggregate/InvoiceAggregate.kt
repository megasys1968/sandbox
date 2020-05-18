package quo.vadis.megasys.aggregate

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.modelling.command.EntityId
import org.axonframework.spring.stereotype.Aggregate
import quo.vadis.megasys.core.*
import java.util.*

@Aggregate
class InvoiceAggregate(): IInvoice {
  @AggregateIdentifier
  override lateinit var invoiceId: UUID
  override lateinit var invoiceTitle: String

  @CommandHandler
  constructor(command: CreateInvoiceCommand): this() {
    AggregateLifecycle.apply(InvoiceCreatedEvent(command))
  }

  @EventSourcingHandler
  fun on(event: InvoiceCreatedEvent) {
    invoiceId = event.invoiceId
    invoiceTitle = event.invoiceTitle
  }

  @CommandHandler
  fun handle(command: UpdateInvoiceCommand) {
    AggregateLifecycle.apply(InvoiceUpdatedEvent(command))
  }

  @EventSourcingHandler
  fun on(event: InvoiceUpdatedEvent) {
    invoiceTitle = event.invoiceTitle
  }

  @CommandHandler
  fun handle(command: RemoveInvoiceCommand) {
    AggregateLifecycle.apply(InvoiceRemovedEvent(command.invoiceId))
  }

  @EventSourcingHandler
  fun on(event: InvoiceRemovedEvent) {
    AggregateLifecycle.markDeleted()
  }

}