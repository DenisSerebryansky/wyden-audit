package wyden.io.audit.service

import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import wyden.io.audit.config.AmqpConfig.Companion.ROUTING_KEY_PREFIX_DEAD_LETTER
import wyden.io.audit.config.AmqpConfig.Companion.ROUTING_KEY_PREFIX_DISCARDED
import wyden.io.audit.model.TaskStatus
import wyden.io.audit.util.LoggerDelegate
import java.util.concurrent.ConcurrentHashMap

@Service
class DefaultAudit : Audit {

    private val taskCounts = ConcurrentHashMap(TaskStatus.entries.associateWith { 0L })

    private val discardedStatusRoutingKeys = setOf(ROUTING_KEY_PREFIX_DISCARDED, ROUTING_KEY_PREFIX_DEAD_LETTER)

    override fun getCounts(): Map<TaskStatus, Long> = taskCounts.toMap()

    @RabbitListener(queues = ["#{auditInboundQueue.name}"])
    override fun receiveInbound(message: String) {
        log.info("Received inbound task {}", message)
        incrementTaskCount(TaskStatus.PRODUCED)
    }

    @RabbitListener(queues = ["#{auditOutboundQueue.name}"])
    override fun receiveOutbound(message: Message) {
        val routingKey = message.messageProperties.receivedRoutingKey ?: "<unknown>"
        val task = String(message.body, Charsets.UTF_8)

        log.info("Received outbound task {} with routing key {}", task, routingKey)
        val status = resolveOutboundStatus(routingKey)

        incrementTaskCount(status)
    }

    @RabbitListener(queues = ["#{auditCertifiedQueue.name}"])
    override fun receiveCertified(message: String) {
        log.info("Received certified task {}", message)
        incrementTaskCount(TaskStatus.CERTIFIED)
    }

    private fun resolveOutboundStatus(routingKey: String): TaskStatus =
        when {
            discardedStatusRoutingKeys.any { routingKey.startsWith(it) } -> TaskStatus.DISCARDED
            else -> TaskStatus.PROCESSED
        }

    private fun incrementTaskCount(status: TaskStatus) {
        taskCounts.merge(status, 1L, Long::plus)
    }

    companion object {
        private val log by LoggerDelegate()
    }
}
