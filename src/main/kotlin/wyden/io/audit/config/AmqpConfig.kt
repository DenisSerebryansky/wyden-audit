package wyden.io.audit.config

import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import wyden.io.audit.property.AuditProperties

@Configuration
class AmqpConfig(private val properties: AuditProperties) {

    @Bean
    fun workInboundExchange(): TopicExchange = TopicExchange(EXCHANGE_INBOUND)

    @Bean
    fun workOutboundExchange(): TopicExchange = TopicExchange(EXCHANGE_OUTBOUND)

    @Bean
    fun workCertifiedExchange(): TopicExchange = TopicExchange(EXCHANGE_CERTIFIED)

    @Bean
    fun auditInboundQueue(): Queue = QueueBuilder.durable(getQueueName(QUEUE_PREFIX_INBOUND)).build()

    @Bean
    fun auditOutboundQueue(): Queue = QueueBuilder.durable(getQueueName(QUEUE_PREFIX_OUTBOUND)).build()

    @Bean
    fun auditCertifiedQueue(): Queue = QueueBuilder.durable(getQueueName(QUEUE_PREFIX_CERTIFIED)).build()

    @Bean
    fun bindings(): Declarables =
        Declarables(
            BindingBuilder
                .bind(auditInboundQueue())
                .to(workInboundExchange())
                .with(getRoutingKey()),
            BindingBuilder
                .bind(auditOutboundQueue())
                .to(workOutboundExchange())
                .with(getRoutingKey()),
            BindingBuilder
                .bind(auditCertifiedQueue())
                .to(workCertifiedExchange())
                .with(getRoutingKey())
        )

    private fun getRoutingKey() = "$ROUTING_KEY_PREFIX.${properties.color.lowercase()}"

    private fun getQueueName(prefix: String) = "$prefix.${properties.color.lowercase()}"

    companion object {
        const val EXCHANGE_INBOUND = "work-inbound"
        const val EXCHANGE_OUTBOUND = "work-outbound"
        const val EXCHANGE_CERTIFIED = "certified-result"

        const val ROUTING_KEY_PREFIX = "task.*"
        const val ROUTING_KEY_PREFIX_DISCARDED = "task.discarded"

        // produced tasks in outbound exchange are considered as dead letters
        const val ROUTING_KEY_PREFIX_DEAD_LETTER = "task.produced"

        const val QUEUE_PREFIX_OUTBOUND = "work-outbound-audit"
        const val QUEUE_PREFIX_INBOUND = "work-inbound-audit"
        const val QUEUE_PREFIX_CERTIFIED = "work-certified-audit"
    }
}
