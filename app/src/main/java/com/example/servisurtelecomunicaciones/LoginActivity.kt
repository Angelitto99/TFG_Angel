package com.example.servisurtelecomunicaciones

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.*
import de.hdodenhof.circleimageview.CircleImageView

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    companion object {
        private const val ADMIN_PIN = "1234"
        private val ADMIN_EMAILS = listOf(
            "admin@servisur.com",
            "jefefinca@servisur.com"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Referencias a vistas
        val emailEt     = findViewById<EditText>(R.id.mail)
        val passEt      = findViewById<EditText>(R.id.password)
        val switchAdmin = findViewById<SwitchCompat>(R.id.switchAdmin)
        val pinEt       = findViewById<EditText>(R.id.pinField)
        val loginBtn    = findViewById<Button>(R.id.loginButton)
        val registerBtn = findViewById<Button>(R.id.registerButton)
        val logoIv      = findViewById<ImageView>(R.id.logo)
        val ivGoogle    = findViewById<CircleImageView>(R.id.ivGoogle)
        val ivFacebook  = findViewById<CircleImageView>(R.id.ivFacebook)
        val ivTwitter   = findViewById<CircleImageView>(R.id.ivTwitter)
        val tvPrivacy   = findViewById<TextView>(R.id.tvPrivacy)

        // Cargo logo y redes
        Glide.with(this).load(R.drawable.ic_servisur).fitCenter().into(logoIv)
        Glide.with(this).load(R.drawable.ic_google).override(48,48).into(ivGoogle)
        Glide.with(this).load(R.drawable.ic_facebook).override(48,48).into(ivFacebook)
        Glide.with(this).load(R.drawable.ic_twitter).override(48,48).into(ivTwitter)

        // Mostrar u ocultar PIN según switch
        switchAdmin.setOnCheckedChangeListener { _, isAdmin ->
            pinEt.visibility = if (isAdmin) View.VISIBLE else View.GONE
        }

        // Pie de privacidad/ términos
        val html = """
             
            <a href="https://www.example.com/privacy">Política de Privacidad</a> y 
            <a href="https://www.example.com/terms">Términos de Servicio</a>.
        """.trimIndent()
        tvPrivacy.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        tvPrivacy.movementMethod = LinkMovementMethod.getInstance()

        // — INICIAR SESIÓN —
        loginBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val pwd   = passEt.text.toString()

            // Validaciones básicas
            if (email.isEmpty() || pwd.isEmpty()) {
                return@setOnClickListener toastCentered("Completa email y contraseña")
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return@setOnClickListener toastCentered("Email no válido")
            }

            if (switchAdmin.isChecked) {
                // **FLUJO ADMIN**
                if (email !in ADMIN_EMAILS) {
                    return@setOnClickListener toastCentered("Este correo no es de administrador")
                }
                val pin = pinEt.text.toString().trim()
                if (pin.isEmpty()) {
                    return@setOnClickListener toastCentered("Introduce el PIN de administrador")
                }
                if (pin != ADMIN_PIN) {
                    return@setOnClickListener toastCentered("PIN de administrador incorrecto")
                }
                // Si todo OK, vamos a Firebase:
                auth.signInWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, AdminHomeActivity::class.java))
                            finish()
                        } else {
                            handleAuthError(task.exception)
                        }
                    }
            } else {
                // **FLUJO CLIENTE NORMAL**
                auth.signInWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            handleAuthError(task.exception)
                        }
                    }
            }
        }

        // — REGISTRARSE —
        registerBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val pwd   = passEt.text.toString()
            if (email.isEmpty() || pwd.isEmpty()) {
                return@setOnClickListener toastCentered("Completa email y contraseña")
            }
            auth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this) { reg ->
                    if (reg.isSuccessful) {
                        toastCentered("Registro exitoso")
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        toastCentered("Error al registrar: ${reg.exception?.message}")
                    }
                }
        }

        // — OAUTH SIMULADO —
        ivGoogle  .setOnClickListener { openUrl("https://accounts.google.com/") }
        ivFacebook.setOnClickListener { openUrl("https://www.facebook.com/") }
        ivTwitter .setOnClickListener { openUrl("https://twitter.com/login") }
    }

    // Muestra un Toast centrado
    private fun toastCentered(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.CENTER, 0, 250)
            show()
        }
    }

    // Manejo de errores de FirebaseAuth
    private fun handleAuthError(ex: Exception?) {
        when (ex) {
            is FirebaseAuthInvalidUserException ->
                toastCentered("No existe ninguna cuenta con ese correo")
            is FirebaseAuthInvalidCredentialsException ->
                toastCentered("La contraseña es incorrecta")
            else ->
                toastCentered("Error de autenticación: ${ex?.localizedMessage}")
        }
    }

    // Abre URL en navegador
    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
