package com.example.servisurtelecomunicaciones

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.servisurtelecomunicaciones.MailSender

class  FormularioAvisoActivity : AppCompatActivity() {

    // Configuración de Email (remitente y receptor)
    private val EMAIL_ORIGEN = "servisuravisosapp76@gmail.com"   // Cuenta remitente
    private val PASSWORD_EMAIL = "bnlw vwgr xbpf nqma"                     // Contraseña o App Password
    private val EMAIL_DESTINO = "telecomunicacionesservisur8@gmail.com"  // Cuenta que recibe el aviso

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_aviso)

        val nombreEditText = findViewById<EditText>(R.id.editTextNombre)
        val phoneEditText = findViewById<EditText>(R.id.editTextTelefono) // Nuevo campo de teléfono
        val direccionEditText = findViewById<EditText>(R.id.editTextDireccion)
        val descripcionEditText = findViewById<EditText>(R.id.editTextDescripcion)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        val logoImageView = findViewById<ImageView>(R.id.logoImage)
        // Si deseas cargar el logo con Glide, descomenta la siguiente línea:
        // Glide.with(this).load(R.drawable.ic_servisur).into(logoImageView)

        // Configurar el campo de teléfono para forzar +34 y limitar a 9 dígitos adicionales
        setupPhoneEditText(phoneEditText)

        btnEnviar.setOnClickListener {
            val nombre = nombreEditText.text.toString().trim()
            val telefono = phoneEditText.text.toString().trim()  // Debe tener el formato "+34XXXXXXXXX"
            val direccion = direccionEditText.text.toString().trim()
            val descripcion = descripcionEditText.text.toString().trim()

            if (nombre.isEmpty() || telefono.isEmpty() || direccion.isEmpty() || descripcion.isEmpty()) {
                showCenteredToast("Por favor, rellene todos los campos")
            } else {
                // Obtener el email del usuario logueado (si no, se mostrará "Desconocido")
                val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Desconocido"

                // Prepara el cuerpo del correo; notar que ahora indicamos "Nueva Incidencia"
                val mensaje = """
                    Se ha registrado una nueva incidencia:
                    Usuario: $currentUserEmail
                    Nombre: $nombre
                    Teléfono: $telefono
                    Dirección: $direccion
                    Descripción: $descripcion
                """.trimIndent()

                // Enviar el correo en un hilo secundario para no bloquear la UI
                Thread {
                    try {
                        val mailSender = MailSender(EMAIL_ORIGEN, PASSWORD_EMAIL)
                        // Cambiar el asunto a "Nueva Incidencia"
                        mailSender.sendMail("Nueva Incidencia", mensaje, EMAIL_DESTINO)
                        runOnUiThread {
                            showCenteredToast("Incidencia enviada correctamente")
                            finish()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            showCenteredToast("Error al enviar la incidencia: ${e.localizedMessage}")
                        }
                    }
                }.start()
            }
        }
    }

    // Función para configurar el campo de teléfono con el prefijo +34 y limitar a 12 caracteres en total
    private fun setupPhoneEditText(phoneEditText: EditText) {
        phoneEditText.setText("+34")
        phoneEditText.setSelection(phoneEditText.text.length)
        phoneEditText.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                if (s != null) {
                    // Forzar que siempre inicie con "+34"
                    if (!s.toString().startsWith("+34")) {
                        phoneEditText.setText("+34")
                        phoneEditText.setSelection(phoneEditText.text.length)
                    } else if (s.length > 12) { // "+34" + 9 dígitos = 12 caracteres
                        val trimmed = s.substring(0, 12)
                        phoneEditText.setText(trimmed)
                        phoneEditText.setSelection(trimmed.length)
                    }
                }
                isEditing = false
            }
        })
    }

    private fun showCenteredToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 500)
        toast.show()
    }
}
