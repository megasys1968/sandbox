package quo.vadis.megasys.query

import org.springframework.data.jpa.repository.JpaRepository
import quo.vadis.megasys.core.*
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class InvoiceEntity(
  @field:Id
  override val invoiceId: UUID,
  override var invoiceTitle: String
) : IInvoice

interface InvoiceRepository: JpaRepository<InvoiceEntity, UUID>
