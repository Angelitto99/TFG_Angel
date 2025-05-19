package com.example.servisurtelecomunicaciones

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.*
import de.hdodenhof.circleimageview.CircleImageView

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val ADMIN_PIN = "1234"
        private val ADMIN_EMAILS = listOf("admin@servisur.com", "jefefinca@servisur.com")
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val isAdmin = prefs.getBoolean("is_admin", false)
            val next = if (isAdmin) AdminHomeActivity::class.java else HomeActivity::class.java
            startActivity(Intent(this, next))
            finish()
            return
        }

        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        val emailEt = findViewById<EditText>(R.id.mail)
        val passEt = findViewById<EditText>(R.id.password)
        val switchAdmin = findViewById<SwitchCompat>(R.id.switchAdmin)
        val pinEt = findViewById<EditText>(R.id.pinField)
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val registerBtn = findViewById<Button>(R.id.registerButton)
        val btnGuest = findViewById<Button>(R.id.btnGuest)
        val logoIv = findViewById<ImageView>(R.id.logo)
        val tvPrivacy = findViewById<TextView>(R.id.tvPrivacy)

        Glide.with(this).load(R.drawable.ic_servisur).fitCenter().into(logoIv)

        switchAdmin.setOnCheckedChangeListener { _, isChecked ->
            pinEt.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        val html = """
            <a href="https://www.example.com/privacy">Política de Privacidad</a> y
            <a href="https://www.example.com/terms">Términos de Servicio</a>.
        """.trimIndent()
        tvPrivacy.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        tvPrivacy.movementMethod = LinkMovementMethod.getInstance()

        loginBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val pwd = passEt.text.toString()

            if (email.isEmpty() || pwd.isEmpty()) {
                toastConLogo("Completa email y contraseña")
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toastConLogo("Email no válido")
                return@setOnClickListener
            }

            if (switchAdmin.isChecked) {
                if (email !in ADMIN_EMAILS) {
                    toastConLogo("Este correo no es de administrador")
                    return@setOnClickListener
                }
                val pin = pinEt.text.toString().trim()
                if (pin.isEmpty()) {
                    toastConLogo("Introduce el PIN de administrador")
                    return@setOnClickListener
                }
                if (pin != ADMIN_PIN) {
                    toastConLogo("PIN de administrador incorrecto")
                    return@setOnClickListener
                }
                auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        prefs.edit().putBoolean("is_admin", true).putBoolean("is_guest", false).apply()
                        startActivity(Intent(this, AdminHomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                    } else {
                        handleAuthError(task.exception)
                    }
                }
            } else {
                auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        prefs.edit().putBoolean("is_admin", false).putBoolean("is_guest", false).apply()
                        startActivity(Intent(this, HomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                    } else {
                        handleAuthError(task.exception)
                    }
                }
            }
        }

        registerBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val pwd = passEt.text.toString()
            if (email.isEmpty() || pwd.isEmpty()) {
                toastConLogo("Completa email y contraseña")
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(this) { reg ->
                if (reg.isSuccessful) {
                    prefs.edit().putBoolean("is_admin", false).putBoolean("is_guest", false).apply()
                    toastConLogo("Registro exitoso")
                    startActivity(Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                } else {
                    toastConLogo("Error al registrar: ${reg.exception?.message}")
                }
            }
        }

        btnGuest.setOnClickListener {
            toastConLogo("Acceso como invitado")
            prefs.edit().putBoolean("is_admin", false).putBoolean("is_guest", true).apply()
            startActivity(Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun handleAuthError(ex: Exception?) {
        when (ex) {
            is FirebaseAuthInvalidUserException -> toastConLogo("No existe ninguna cuenta con ese correo")
            is FirebaseAuthInvalidCredentialsException -> toastConLogo("La contraseña es incorrecta")
            else -> toastConLogo("Error de autenticación: ${ex?.localizedMessage}")
        }
    }

    private fun toastConLogo(msg: String) {
        val layout = layoutInflater.inflate(R.layout.toast_custom_logo, findViewById(android.R.id.content), false)
        layout.findViewById<TextView>(R.id.toastText).text = msg

        Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.CENTER, 0, 250)
            show()
        }
    }
}
