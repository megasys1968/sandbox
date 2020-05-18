package quo.vadis.megasys.core

import java.util.*

data class InvoiceCreatedEvent(private val invoice: IInvoice): IInvoice by invoice

data class InvoiceUpdatedEvent(private val invoice: IInvoice): IInvoice by invoice

data class InvoiceRemovedEvent(
  override val invoiceId: UUID
): IInvoiceHeader
