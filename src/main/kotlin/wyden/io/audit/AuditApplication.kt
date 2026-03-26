package wyden.io.audit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("wyden.io.audit.property")
class AuditApplication

fun main(args: Array<String>) {
    runApplication<AuditApplication>(*args)
}
