package quo.vadis.megasys

import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import quo.vadis.megasys.aggregate.InvoiceAggregate
import quo.vadis.megasys.controller.InvoiceController
import quo.vadis.megasys.core.*
import java.util.*

@SpringBootTest
class SampleAxonApplicationTests {
  lateinit var fixture: FixtureConfiguration<InvoiceAggregate>

  @BeforeEach
  fun setup() {
    fixture = AggregateTestFixture(InvoiceAggregate::class.java)
  }

  @Test
  fun createRequestCommand() {
    val invoiceId = UUID.randomUUID()
    fixture
      .given()
      .`when`(CreateInvoiceCommand(invoiceId, InvoiceController.InvoiceBody("サンプル")))
      .expectEvents(InvoiceCreatedEvent(CreateInvoiceCommand(invoiceId, InvoiceController.InvoiceBody("サンプル"))))
  }

  @Test
  fun updateBudgetSubjectCommand() {
    val invoiceId = UUID.randomUUID()

    fixture
      .givenCommands(
        CreateInvoiceCommand(invoiceId, InvoiceController.InvoiceBody("サンプル"))
      )
      .`when`(UpdateInvoiceCommand(invoiceId, InvoiceController.InvoiceBody("お試し")))
      .expectEvents(InvoiceUpdatedEvent(UpdateInvoiceCommand(invoiceId, InvoiceController.InvoiceBody("お試し"))))
  }
}
