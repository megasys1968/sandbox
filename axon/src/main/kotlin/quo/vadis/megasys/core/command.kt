package quo.vadis.megasys.core

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

interface IInvoiceBody {
  val invoiceTitle: String
}

interface IInvoiceHeader {
  val invoiceId: UUID
}

interface IInvoice: IInvoiceHeader, IInvoiceBody

data class CreateInvoiceCommand(
  @field:TargetAggregateIdentifier
  override val invoiceId: UUID,
  val body: IInvoiceBody): IInvoiceBody by body, IInvoice

data class UpdateInvoiceCommand(
  @field:TargetAggregateIdentifier
  override val invoiceId: UUID,
  val body: IInvoiceBody): IInvoiceBody by body, IInvoice

data class RemoveInvoiceCommand(
  @field:TargetAggregateIdentifier
  override val invoiceId: UUID
): IInvoiceHeader
