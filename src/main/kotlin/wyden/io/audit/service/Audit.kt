package wyden.io.audit.service

import org.springframework.amqp.core.Message
import wyden.io.audit.model.TaskStatus

interface Audit {

    fun getCounts(): Map<TaskStatus, Long>

    fun receiveInbound(message: String)

    fun receiveOutbound(message: Message)

    fun receiveCertified(message: String)
}
