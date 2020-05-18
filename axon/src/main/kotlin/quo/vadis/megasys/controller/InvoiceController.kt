package quo.vadis.megasys.controller

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.MultipleInstancesResponseType
import org.axonframework.queryhandling.QueryGateway
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.view.UrlBasedViewResolver
import quo.vadis.megasys.core.*
import quo.vadis.megasys.query.InvoiceEntity
import quo.vadis.megasys.query.InvoiceService
import java.util.*

@Controller
class InvoiceController(val commandGateway: CommandGateway, val queryGateway: QueryGateway) {

  companion object {
    const val REDIRECT_URL = "${UrlBasedViewResolver.REDIRECT_URL_PREFIX}/"
  }

  data class InvoiceBody(override val invoiceTitle: String): IInvoiceBody
  data class InvoiceRequest(
    override val invoiceId: UUID,
    override val invoiceTitle: String
  ): IInvoiceHeader, IInvoiceBody

  @GetMapping("/")
  fun topPage(model: Model): String {
    val invoices = queryGateway.query(InvoiceService.AllInvoicesQuery(), MultipleInstancesResponseType(InvoiceEntity::class.java)).get()
    model.addAttribute("invoices", invoices)
    return "index"
  }

  @PostMapping("/invoices")
  fun createInvoice(invoice: InvoiceBody): String {
    commandGateway.sendAndWait<Any>(CreateInvoiceCommand(UUID.randomUUID(), invoice))
    return REDIRECT_URL
  }

  @PostMapping("/invoices/update")
  fun updateInvoice(invoice: InvoiceRequest): String {
    commandGateway.sendAndWait<Any>(UpdateInvoiceCommand(invoice.invoiceId, invoice))
    return REDIRECT_URL
  }

  @PostMapping("/invoices/delete")
  fun deleteInvoice(@RequestParam invoiceId: UUID): String {
    commandGateway.sendAndWait<Any>(RemoveInvoiceCommand(invoiceId))
    return REDIRECT_URL
  }

}