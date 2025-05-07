package com.example.servisurtelecomunicaciones

import android.content.Context
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

    companion object {
        private const val ADMIN_PIN = "1234"
        private val ADMIN_EMAILS = listOf(
            "admin@servisur.com",
            "jefefinca@servisur.com"
        )
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ AUTOLOGIN si ya hay usuario
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isAdmin = prefs.getBoolean("is_admin", false)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this, if (isAdmin) AdminHomeActivity::class.java else HomeActivity::class.java))
            finish()
            return
        }

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

        // Cargar imágenes
        Glide.with(this).load(R.drawable.ic_servisur).fitCenter().into(logoIv)
        Glide.with(this).load(R.drawable.ic_google).override(48, 48).into(ivGoogle)
        Glide.with(this).load(R.drawable.ic_facebook).override(48, 48).into(ivFacebook)
        Glide.with(this).load(R.drawable.ic_twitter).override(48, 48).into(ivTwitter)

        // Mostrar u ocultar PIN
        switchAdmin.setOnCheckedChangeListener { _, isChecked ->
            pinEt.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Pie con links legales
        val html = """
            <a href="https://www.example.com/privacy">Política de Privacidad</a> y 
            <a href="https://www.example.com/terms">Términos de Servicio</a>.
        """.trimIndent()
        tvPrivacy.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        tvPrivacy.movementMethod = LinkMovementMethod.getInstance()

        // BOTÓN LOGIN
        loginBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val pwd   = passEt.text.toString()

            if (email.isEmpty() || pwd.isEmpty()) {
                toastCentered("Completa email y contraseña")
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toastCentered("Email no válido")
                return@setOnClickListener
            }

            if (switchAdmin.isChecked) {
                // FLUJO ADMIN
                if (email !in ADMIN_EMAILS) {
                    toastCentered("Este correo no es de administrador")
                    return@setOnClickListener
                }
                val pin = pinEt.text.toString().trim()
                if (pin.isEmpty()) {
                    toastCentered("Introduce el PIN de administrador")
                    return@setOnClickListener
                }
                if (pin != ADMIN_PIN) {
                    toastCentered("PIN de administrador incorrecto")
                    return@setOnClickListener
                }
                auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        prefs.edit().putBoolean("is_admin", true).apply()
                        startActivity(Intent(this, AdminHomeActivity::class.java))
                        finish()
                    } else {
                        handleAuthError(task.exception)
                    }
                }
            } else {
                // FLUJO CLIENTE
                auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        prefs.edit().putBoolean("is_admin", false).apply()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        handleAuthError(task.exception)
                    }
                }
            }
        }

        // BOTÓN REGISTRO
        registerBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val pwd   = passEt.text.toString()
            if (email.isEmpty() || pwd.isEmpty()) {
                toastCentered("Completa email y contraseña")
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(this) { reg ->
                if (reg.isSuccessful) {
                    prefs.edit().putBoolean("is_admin", false).apply()
                    toastCentered("Registro exitoso")
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    toastCentered("Error al registrar: ${reg.exception?.message}")
                }
            }
        }

        // BOTONES DE REDES (simulados → SOLO CLIENTES)
        val socialClick = View.OnClickListener {
            toastCentered("Acceso como cliente")
            prefs.edit().putBoolean("is_admin", false).apply()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        ivGoogle.setOnClickListener(socialClick)
        ivFacebook.setOnClickListener(socialClick)
        ivTwitter.setOnClickListener(socialClick)
    }

    private fun toastCentered(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.CENTER, 0, 250)
            show()
        }
    }

    private fun handleAuthError(ex: Exception?) {
        when (ex) {
            is FirebaseAuthInvalidUserException -> toastCentered("No existe ninguna cuenta con ese correo")
            is FirebaseAuthInvalidCredentialsException -> toastCentered("La contraseña es incorrecta")
            else -> toastCentered("Error de autenticación: ${ex?.localizedMessage}")
        }
    }
}
