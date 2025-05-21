// LoginActivity.kt
package com.example.servisurtelecomunicaciones

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.*
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val ADMIN_PIN   = "1234"
        private val ADMIN_EMAILS     = listOf(
            "admin@servisur.com",
            "jefefinca@servisur.com",
            "admin2@servisur.com",
            "admin3@servisur.com"
        )
        private const val DUMMY_PWD   = "__dummy_pass__"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var usersRef: DatabaseReference
    private var intentosFallidos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth     = FirebaseAuth.getInstance()
        usersRef = FirebaseDatabase.getInstance().getReference("users")
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)

        // Si ya hay sesión abierta o last_uid, saltamos
        val alreadyUid = auth.currentUser?.uid ?: prefs.getString("last_uid", null)
        if (alreadyUid != null) {
            val next = if (prefs.getBoolean("is_admin", false))
                AdminHomeActivity::class.java
            else
                HomeActivity::class.java
            startActivity(Intent(this, next))
            finish()
            return
        }

        // Vistas
        val emailEt      = findViewById<EditText>(R.id.mail)
        val passEt       = findViewById<EditText>(R.id.password)
        val switchAdmin  = findViewById<SwitchCompat>(R.id.switchAdmin)
        val pinEt        = findViewById<EditText>(R.id.pinField)
        val loginBtn     = findViewById<Button>(R.id.loginButton)
        val registerBtn  = findViewById<Button>(R.id.registerButton)
        val btnGuest     = findViewById<Button>(R.id.btnGuest)
        val logoIv       = findViewById<ImageView>(R.id.logo)
        val tvPrivacy    = findViewById<TextView>(R.id.tvPrivacy)
        val tvForgotPass = findViewById<TextView>(R.id.tvForgotPassword)

        Glide.with(this).load(R.drawable.ic_servisur).fitCenter().into(logoIv)

        switchAdmin.setOnCheckedChangeListener { _, checked ->
            pinEt.visibility = if (checked) View.VISIBLE else View.GONE
        }

        tvPrivacy.text = Html.fromHtml(
            "<a href=\"https://www.example.com/privacy\">Política de Privacidad</a> y " +
                    "<a href=\"https://www.example.com/terms\">Términos de Servicio</a>.",
            Html.FROM_HTML_MODE_LEGACY
        )
        tvPrivacy.movementMethod = LinkMovementMethod.getInstance()

        tvForgotPass.setOnClickListener { mostrarDialogoRecuperacion() }

        loginBtn.setOnClickListener {
            val email       = emailEt.text.toString().trim().lowercase()
            val pwd         = passEt.text.toString()
            val isAdminLogin= switchAdmin.isChecked

            if (email.isEmpty() || pwd.isEmpty()) {
                toastConLogo("Completa email y contraseña"); return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toastConLogo("Email no válido"); return@setOnClickListener
            }
            if (isAdminLogin) {
                if (email !in ADMIN_EMAILS) {
                    toastConLogo("Este correo no es de administrador"); return@setOnClickListener
                }
                val pin = pinEt.text.toString().trim()
                if (pin.isEmpty() || pin != ADMIN_PIN) {
                    toastConLogo(if (pin.isEmpty()) "Introduce PIN" else "PIN incorrecto")
                    return@setOnClickListener
                }
            }

            auth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser!!.uid
                        // Guardar flags y last_uid
                        prefs.edit()
                            .putString("last_uid", uid)
                            .putBoolean("is_admin", isAdminLogin)
                            .putBoolean("is_guest", false)
                            .apply()
                        // Asegurar email en /users
                        usersRef.child(uid).child("email").setValue(email)

                        // Comprobar si necesita cambio de contraseña
                        usersRef.child(uid).child("needs_password_change")
                            .addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val needsChange = snapshot.getValue(Boolean::class.java) ?: false
                                    if (needsChange) {
                                        AlertDialog.Builder(this@LoginActivity)
                                            .setTitle("Debes cambiar tu contraseña")
                                            .setMessage("Tu contraseña aún no se ha guardado. Por favor, cámbiala ahora.")
                                            .setCancelable(false)
                                            .setPositiveButton("Cambiar") { _, _ ->
                                                startActivity(Intent(this@LoginActivity, CambiarPasswordActivity::class.java))
                                                finish()
                                            }
                                            .show()
                                    } else {
                                        val next = if (isAdminLogin)
                                            AdminHomeActivity::class.java
                                        else
                                            HomeActivity::class.java
                                        startActivity(Intent(this@LoginActivity, next).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        })
                                        finish()
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    // En caso de error, entramos de todos modos
                                    val next = if (isAdminLogin)
                                        AdminHomeActivity::class.java
                                    else
                                        HomeActivity::class.java
                                    startActivity(Intent(this@LoginActivity, next).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    })
                                    finish()
                                }
                            })
                    } else {
                        intentosFallidos++
                        if (intentosFallidos >= 4) {
                            mostrarDialogoRecuperacion()
                            intentosFallidos = 0
                        } else {
                            handleAuthError(task.exception)
                        }
                    }
                }
        }

        registerBtn.setOnClickListener {
            val email = emailEt.text.toString().trim().lowercase()
            val pwd   = passEt.text.toString()
            if (email.isEmpty() || pwd.isEmpty()) {
                toastConLogo("Completa email y contraseña"); return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this) { reg ->
                    if (reg.isSuccessful) {
                        val uid = auth.currentUser!!.uid
                        usersRef.child(uid).child("email").setValue(email)
                        prefs.edit()
                            .putString("last_uid", uid)
                            .putBoolean("is_admin", false)
                            .putBoolean("is_guest", false)
                            .apply()
                        toastConLogo("Registro exitoso")
                        startActivity(Intent(this, HomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                    } else {
                        toastConLogo("Error al registrar: ${reg.exception?.localizedMessage}")
                    }
                }
        }

        btnGuest.setOnClickListener {
            prefs.edit()
                .putBoolean("is_admin", false)
                .putBoolean("is_guest", true)
                .apply()
            toastConLogo("Acceso como invitado")
            startActivity(Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun mostrarDialogoRecuperacion() {
        val builder = AlertDialog.Builder(this)
        val view    = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password, null)
        val emailInput = view.findViewById<EditText>(R.id.etResetEmail)

        builder.setTitle("¿Has olvidado tu contraseña?")
            .setView(view)
            .setPositiveButton("Verificar", null)
            .setNegativeButton("Cancelar", null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val correo = emailInput.text.toString().trim().lowercase()
                if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                    toastConLogo("Email no válido"); return@setOnClickListener
                }
                // dummy-login para validar existencia
                auth.signInWithEmailAndPassword(correo, DUMMY_PWD)
                    .addOnSuccessListener {
                        auth.signOut()
                        mostrarDialogoNuevaContrasena(correo)
                    }
                    .addOnFailureListener { ex ->
                        when (ex) {
                            is FirebaseAuthInvalidUserException ->
                                toastConLogo("Usuario NO registrado")
                            is FirebaseAuthInvalidCredentialsException ->
                                mostrarDialogoNuevaContrasena(correo)
                            else ->
                                toastConLogo("Error: ${ex.localizedMessage}")
                        }
                    }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun mostrarDialogoNuevaContrasena(email: String) {
        val isAdminEmail = email in ADMIN_EMAILS
        val view    = LayoutInflater.from(this).inflate(R.layout.dialog_set_new_password, null)
        val etNew   = view.findViewById<EditText>(R.id.etNewPassword)
        val etConf  = view.findViewById<EditText>(R.id.etConfirmPassword)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Nueva contraseña para $email")
            .setView(view)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Cambiar", null)
            .create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val np = etNew.text.toString()
                val cp = etConf.text.toString()
                if (np.length < 6) {
                    etNew.error = "Mínimo 6 caracteres"; return@setOnClickListener
                }
                if (np != cp) {
                    etConf.error = "No coinciden"; return@setOnClickListener
                }
                // Guardar flags y last_uid
                val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
                val uid   = usersRef.push().key // no change to actual uid
                prefs.edit()
                    .putBoolean("is_admin", isAdminEmail)
                    .putBoolean("is_guest", false)
                    .apply()
                // Navegar al Home/Admin
                val next = if (isAdminEmail) AdminHomeActivity::class.java else HomeActivity::class.java
                startActivity(Intent(this, next).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                dialog.dismiss()
                finish()
            }
        }
        dialog.show()
    }

    private fun handleAuthError(ex: Exception?) {
        when (ex) {
            is FirebaseAuthInvalidUserException        -> toastConLogo("No existe cuenta con ese correo")
            is FirebaseAuthInvalidCredentialsException -> toastConLogo("Contraseña incorrecta")
            else                                      -> toastConLogo("Error autenticación: ${ex?.localizedMessage}")
        }
    }

    private fun toastConLogo(msg: String) {
        val layout = LayoutInflater.from(this)
            .inflate(R.layout.toast_custom_logo, findViewById(android.R.id.content), false)
        layout.findViewById<TextView>(R.id.toastText).text = msg
        Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view     = layout
            setGravity(Gravity.CENTER, 0, 250)
            show()
        }
    }
}
