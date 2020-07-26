package ju.ma.app.services

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.Base64
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Draft
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.ListThreadsResponse
import com.google.api.services.gmail.model.Message
import java.io.ByteArrayOutputStream
import java.util.Properties
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class MailService {
    @Value("\${gmail.client-id}")
    lateinit var clientId: String
    @Value("\${gmail.client-secret}")
    lateinit var clientSecret: String
    @Value("\${gmail.refresh-token}")
    lateinit var refreshToken: String
    @Value("\${gmail.address}")
    lateinit var address: String

    val gmail: Gmail by lazy {
        val jsonFactory = JacksonFactory.getDefaultInstance()
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val credential = GoogleCredential.Builder()
            .setJsonFactory(jsonFactory)
            .setTransport(transport)
            .setClientSecrets(clientId, clientSecret)
            .build()
            .setRefreshToken(refreshToken)
        credential.refreshToken()
        Gmail.Builder(transport, jsonFactory, credential).setApplicationName("Lambeth Techaid").build()
    }

    fun emails(filter: EmailFilter, userId: String = address): EmailPage {
        val response = filter.apply(
            gmail.users().messages()
                .list(userId)
        )
        return EmailPage(userId, response.execute(), this)
    }

    fun threads(filter: EmailFilter): ListThreadsResponse {
        if (filter.id.isNotBlank()) {
            val thread = gmail.users().threads().get(address, filter.id).execute()
            return ListThreadsResponse().apply {
                threads = mutableListOf(thread)
                resultSizeEstimate = 1
            }
        }
        return filter.apply(gmail.users().threads().list(address)).execute()
    }

    fun sendMessage(message: MimeMessage): Message = sendMessage(createMessageWithEmail(message))

    fun sendMessage(message: Message): Message = gmail.users().messages().send(address, message).execute()

    fun createDraft(message: Message): Draft {
        val draft = Draft()
        draft.message = message
        return gmail.users().drafts().create(address, draft).execute()
    }

    fun createDraft(message: MimeMessage) = createDraft(createMessageWithEmail(message))

    fun updateDraft(draftId: String, message: Message): Draft {
        val draft = Draft()
        draft.message = message
        return gmail.users().drafts().update(address, draftId, draft).execute()
    }

    fun updateDraft(draftId: String, message: MimeMessage) = updateDraft(draftId, createMessageWithEmail(message))

    fun sendDraft(draftId: String, message: Message? = null): Message {
        val draft = Draft()
        draft.id = draftId
        message?.let {
            draft.message = message
        }
        return gmail.users().drafts().send(address, draft).execute()
    }

    fun sendDraft(draftId: String, message: MimeMessage? = null): Message {
        return sendDraft(draftId, message?.let { createMessageWithEmail(message) })
    }

    fun deleteDraft(draftId: String) = gmail.users().drafts().delete(address, draftId).execute()

    fun replyTo(messageId: String, message: MimeMessage): Message {
        val source = gmail.users().messages().get(address, messageId).execute()
        val headers = source.payload.headers.map { it.name to it.value }.toMap()
        message.addHeader("In-Reply-To", headers["Message-ID"])
        message.addHeader("References", headers["References"])
        var subject = headers["Subject"] ?: message.subject
        if (!subject.startsWith("Re:")) {
            subject = "Re: $subject"
        }
        message.subject = subject
        val content = createMessageWithEmail(message)
        content.threadId = source.threadId
        return content
    }
}

class EmailPage(userId: String, response: ListMessagesResponse, private val mailService: MailService) {
    val resultSizeEstimate: Long = response.resultSizeEstimate ?: 0
    val nextPageToken: String = response.nextPageToken ?: ""
    val messages: List<Message> by lazy {
        response.messages?.map {
            mailService.gmail.users().messages().get(userId, it.id).execute()
        } ?: listOf()
    }
}

data class EmailFilter(
    val maxResults: Long = 10,
    val pageToken: String = "",
    val query: String = "",
    val userId: String = "",
    val id: String = "",
    val labelIds: List<String> = listOf()
) {
    fun apply(list: Gmail.Users.Messages.List): Gmail.Users.Messages.List {
        list.maxResults = maxResults
        if (pageToken.isNotBlank()) list.pageToken = pageToken
        if (query.isNotBlank()) list.q = query
        if (userId.isNotBlank()) list.userId = userId
        if (labelIds.isNotEmpty()) list.labelIds = labelIds
        return list
    }

    fun apply(list: Gmail.Users.Threads.List): Gmail.Users.Threads.List {
        list.maxResults = maxResults
        if (pageToken.isNotBlank()) list.pageToken = pageToken
        if (query.isNotBlank()) list.q = query
        if (userId.isNotBlank()) list.userId = userId
        if (labelIds.isNotEmpty()) list.labelIds = labelIds
        return list
    }
}

fun createEmail(
    to: String,
    from: String,
    subject: String,
    bodyText: String,
    mimeType: String = "plain",
    charset: String? = null
): MimeMessage {
    val props = Properties()
    val session = Session.getDefaultInstance(props, null)
    val email = MimeMessage(session)
    email.setFrom(InternetAddress(from))
    email.addRecipient(
        javax.mail.Message.RecipientType.TO,
        InternetAddress(to)
    )
    email.subject = subject
    email.setText(bodyText, charset, mimeType)
    return email
}

fun createMessageWithEmail(emailContent: MimeMessage): Message {
    val buffer = ByteArrayOutputStream()
    emailContent.writeTo(buffer)
    val bytes = buffer.toByteArray()
    val encodedEmail = Base64.encodeBase64URLSafeString(bytes)
    val message = Message()
    message.raw = encodedEmail
    return message
}
