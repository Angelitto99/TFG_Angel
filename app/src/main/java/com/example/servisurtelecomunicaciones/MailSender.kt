package com.example.servisurtelecomunicaciones

import java.io.File
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class MailSender(
    private val username: String,
    private val password: String
) {
    private val session: Session

    init {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })
    }

    @Throws(MessagingException::class)
    fun sendMail(subject: String, body: String, recipient: String) {
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(username))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
            this.subject = subject
            setText(body)
        }
        Transport.send(message)
    }

    @Throws(MessagingException::class)
    fun sendMailWithAttachment(subject: String, body: String, recipient: String, attachmentPath: String?) {
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(username))
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
        message.subject = subject

        val multipart = MimeMultipart()

        val messageBodyPart = MimeBodyPart()
        messageBodyPart.setText(body)
        multipart.addBodyPart(messageBodyPart)

        if (!attachmentPath.isNullOrEmpty()) {
            val attachPart = MimeBodyPart()
            val source = FileDataSource(File(attachmentPath))
            attachPart.dataHandler = DataHandler(source)
            attachPart.fileName = source.name
            multipart.addBodyPart(attachPart)
        }

        message.setContent(multipart)
        Transport.send(message)
    }
}
