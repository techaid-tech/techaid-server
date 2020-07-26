package ju.ma.app.graphql

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLResolver
import com.google.api.services.gmail.model.Draft
import com.google.api.services.gmail.model.Label
import com.google.api.services.gmail.model.ListThreadsResponse
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import com.google.api.services.gmail.model.MessagePartBody
import com.google.api.services.gmail.model.MessagePartHeader
import com.google.api.services.gmail.model.Thread
import javax.mail.internet.MimeMessage
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import ju.ma.app.services.EmailFilter
import ju.ma.app.services.EmailPage
import ju.ma.app.services.MailService
import ju.ma.app.services.createEmail
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@PreAuthorize("hasAnyAuthority('read:emails')")
@Component
class GmailGraph(
    private val mailService: MailService
) : GraphQLQueryResolver {
    fun emails(filter: EmailFilter, id: String?): EmailPage {
        return mailService.emails(filter, id ?: mailService.address)
    }

    fun email(id: String): Message {
        return mailService.gmail.users().messages().get(mailService.address, id).execute()
    }

    fun thread(id: String): Thread {
        return mailService.gmail.users().threads().get(mailService.address, id).execute()
    }

    fun emailThreads(filter: EmailFilter): ListThreadsResponse {
        return mailService.threads(filter)
    }

    fun emailLabels(ids: List<String>?): List<Label> {
        return if (ids.isNullOrEmpty()) {
            mailService.gmail.users().labels().list(mailService.address).execute().labels.map {
                mailService.gmail.users().labels().get(mailService.address, it.id).execute()
            }
        } else {
            ids.map {
                mailService.gmail.users().labels().get(mailService.address, it).execute()
            }
        }
    }
}

@Component
@Validated
@PreAuthorize("hasAnyAuthority('write:emails')")
class GmailMutations(
    private val mailService: MailService
) : GraphQLMutationResolver {
    fun sendEmail(@Valid data: EmailInput): Message {
        return mailService.sendMessage(data.toMessage(mailService.address))
    }

    fun replyEmail(id: String, @Valid data: EmailInput): Message {
        val message = mailService.replyTo(id, data.toMessage(mailService.address))
        return mailService.sendMessage(message)
    }

    fun draftEmail(@Valid data: EmailInput): Draft {
        return mailService.createDraft(data.toMessage(mailService.address))
    }

    fun replyDraft(id: String, @Valid data: EmailInput): Draft {
        val message = mailService.replyTo(id, data.toMessage(mailService.address))
        return mailService.createDraft(message)
    }

    fun updateDraft(id: String, @Valid data: EmailInput): Draft {
        return mailService.updateDraft(id, data.toMessage(mailService.address))
    }

    fun deleteDraft(id: String): Boolean {
        mailService.deleteDraft(id)
        return true
    }

    fun sendDraft(id: String, @Valid data: EmailInput? = null): Message {
        return mailService.sendDraft(id, data?.toMessage(mailService.address))
    }
}

@Component
class MessagePartBodyResolver(
    private val mailService: MailService
) : GraphQLResolver<MessagePartBody> {
    fun decodedData(part: MessagePartBody): String? {
        return part.decodeData()?.let { it.toString(charset("UTF-8")) }
    }
}

@Component
class MessagePartResolver(
    private val mailService: MailService
) : GraphQLResolver<MessagePart> {
    fun headers(part: MessagePart, keys: List<String>?): List<MessagePartHeader> {
        keys ?: return part.headers
        return (part.headers ?: listOf<MessagePartHeader>()).filter { keys.contains(it.name) }
    }

    fun content(part: MessagePart, mimeType: String): MessagePart? {
        (part.parts ?: listOf<MessagePart>()).forEach {
            mimeType(mimeType, it)?.let { p ->
                return p
            }
        }
        return null
    }

    private fun mimeType(mimeType: String, part: MessagePart?): MessagePart? {
        part ?: return null
        if (part.mimeType == mimeType && part.body?.data != null) {
            return part
        }
        if (part.parts != null) {
            part.parts.forEach {
                mimeType(mimeType, it)?.let { p ->
                    return p
                }
            }
        }
        return null
    }
}

@Component
class ThreadResolver(
    private val mailService: MailService
) : GraphQLResolver<Thread> {
    fun messages(thread: Thread): List<Message> {
        return if (thread.messages.isNullOrEmpty()) {
            mailService.gmail.users().Threads().get(mailService.address, thread.id).execute().messages
        } else {
            thread.messages
        }
    }
}

data class EmailInput(
    val subject: String,
    @get:NotBlank
    val to: String,
    val body: String,
    val mimeType: String = "plain"
) {
    fun toMessage(from: String): MimeMessage {
        return createEmail(
            to = to,
            from = from,
            subject = subject,
            bodyText = body,
            mimeType = mimeType,
            charset = "UTF-8"
        )
    }
}
