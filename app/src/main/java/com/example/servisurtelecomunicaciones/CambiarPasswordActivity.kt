package com.example.servisurtelecomunicaciones

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class CambiarPasswordActivity : AppCompatActivity() {

    private lateinit var etNuevaPass: EditText
    private lateinit var etRepetirPass: EditText
    private lateinit var btnCambiar: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cambiar_password)

        etNuevaPass = findViewById(R.id.etNuevaPass)
        etRepetirPass = findViewById(R.id.etRepetirPass)
        btnCambiar = findViewById(R.id.btnCambiar)
        auth = FirebaseAuth.getInstance()

        btnCambiar.setOnClickListener {
            val nueva = etNuevaPass.text.toString()
            val repetir = etRepetirPass.text.toString()

            if (nueva.length < 6) {
                toastConLogo("La contraseña debe tener al menos 6 caracteres")
                return@setOnClickListener
            }
            if (nueva != repetir) {
                toastConLogo("Las contraseñas no coinciden")
                return@setOnClickListener
            }

            val user = auth.currentUser
            user?.updatePassword(nueva)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toastConLogo("Contraseña cambiada correctamente")
                    finish()
                } else {
                    toastConLogo("Error al cambiar la contraseña")
                }
            }
        }
    }

    private fun toastConLogo(msg: String) {
        val layout = layoutInflater.inflate(R.layout.toast_custom_logo, findViewById(android.R.id.content), false)
        layout.findViewById<TextView>(R.id.toastText).text = msg

        Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(android.view.Gravity.CENTER, 0, 250)
            show()
        }
    }
}
