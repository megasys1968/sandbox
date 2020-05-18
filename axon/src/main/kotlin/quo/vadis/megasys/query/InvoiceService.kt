package quo.vadis.megasys.query

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Service
import quo.vadis.megasys.core.*

@Service
class InvoiceService(val invoiceRepo: InvoiceRepository) {
  class AllInvoicesQuery

  @QueryHandler
  fun handle(query: AllInvoicesQuery): List<InvoiceEntity> {
    return invoiceRepo.findAll()
  }

  @EventHandler
  fun on(event: InvoiceCreatedEvent) {
    invoiceRepo.save(InvoiceEntity(event.invoiceId, event.invoiceTitle))
  }
  @EventHandler
  fun on(event: InvoiceUpdatedEvent) {
    invoiceRepo.save(InvoiceEntity(event.invoiceId, event.invoiceTitle))
  }
  @EventHandler
  fun on(event: InvoiceRemovedEvent) {
    invoiceRepo.deleteById(event.invoiceId)
  }

}