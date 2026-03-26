package wyden.io.audit.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("audit")
data class AuditProperties(val color: String)
