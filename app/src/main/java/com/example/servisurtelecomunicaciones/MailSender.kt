package com.example.servisurtelecomunicaciones

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class MailSender(
    private val username: String,   // Ej: tuCorreo@gmail.com
    private val password: String    // Contraseña o App Password
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
                // Asegúrate de usar javax.mail.PasswordAuthentication
                return PasswordAuthentication(username, password)
            }
        })
    }

    @Throws(MessagingException::class)
    fun sendMail(subject: String, body: String, recipient: String) {
        val message: Message = MimeMessage(session).apply {
            setFrom(InternetAddress(username))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
            this.subject = subject
            setText(body)
        }
        Transport.send(message)
    }
}
