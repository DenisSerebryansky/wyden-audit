package wyden.io.audit.service

import org.junit.jupiter.api.Test
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import wyden.io.audit.model.TaskStatus
import kotlin.test.assertEquals

class DefaultAuditTests {

    private val audit = DefaultAudit()

    private fun message(routingKey: String, body: String = "task"): Message {
        val props = MessageProperties()
        props.setReceivedRoutingKey(routingKey)
        return Message(body.toByteArray(), props)
    }

    @Test
    fun `should count produced tasks`() {
        audit.receiveInbound("task1")
        val counts = audit.getCounts()
        assertEquals(1, counts[TaskStatus.PRODUCED])
    }

    @Test
    fun `should count processed tasks`() {
        val msg = message("task.processed.RED")
        audit.receiveOutbound(msg)
        val counts = audit.getCounts()
        assertEquals(1, counts[TaskStatus.PROCESSED])
    }

    @Test
    fun `should count discarded tasks`() {
        val msg = message("task.discarded.RED")
        audit.receiveOutbound(msg)
        val counts = audit.getCounts()
        assertEquals(1, counts[TaskStatus.DISCARDED])
    }

    @Test
    fun `should count dead letter as discarded`() {
        // we send dead letters to outbound exchange with the same routing key as we received
        val msg = message("task.produced.BLUE")
        audit.receiveOutbound(msg)
        val counts = audit.getCounts()
        assertEquals(1, counts[TaskStatus.DISCARDED])
    }

    @Test
    fun `should count certified tasks`() {
        audit.receiveCertified("task1")
        val counts = audit.getCounts()
        assertEquals(1, counts[TaskStatus.CERTIFIED])
    }

    @Test
    fun `should accumulate counts correctly`() {
        audit.receiveInbound("t1")
        audit.receiveOutbound(message("task.processed.RED", "t1"))
        audit.receiveCertified("t1")

        audit.receiveInbound("t2")
        audit.receiveOutbound(message("task.discarded.RED", "t2"))

        val counts = audit.getCounts()

        assertEquals(2, counts[TaskStatus.PRODUCED])
        assertEquals(1, counts[TaskStatus.PROCESSED])
        assertEquals(1, counts[TaskStatus.DISCARDED])
        assertEquals(1, counts[TaskStatus.CERTIFIED])
    }
}
