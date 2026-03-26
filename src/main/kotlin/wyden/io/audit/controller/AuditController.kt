package wyden.io.audit.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import wyden.io.audit.model.TaskStatus
import wyden.io.audit.property.AuditProperties
import wyden.io.audit.service.Audit

@RestController
@Tag(name = "queries")
class AuditController(
    private val audit: Audit,
    private val auditProperties: AuditProperties,
) {

    @GetMapping("/color")
    fun getColor(): String = auditProperties.color

    @GetMapping("/count")
    fun getCounts(): Map<TaskStatus, Long> = audit.getCounts()
}
