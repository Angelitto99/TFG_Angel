package com.example.servisurtelecomunicaciones

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView



class IncidentActivity : AppCompatActivity() {
    private val TAG = "IncidentSimpleActivity"

    // Configuración de Email (igual que antes)
    private val EMAIL_ORIGEN    = "servisuravisosapp76@gmail.com"
    private val PASSWORD_EMAIL  = "bnlwvwgrxbpfnqma"
    private val EMAIL_DESTINO   = "telecomunicacionesservisur8@gmail.com"

    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var imageView: ImageView
    private lateinit var btnEnviar: Button
    private var selectedImageUri: Uri? = null
    private val IMAGE_PICK_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incident)

        // Views
        nameEditText        = findViewById(R.id.nameField)
        phoneEditText       = findViewById(R.id.phoneField)
        locationEditText    = findViewById(R.id.locationField)
        descriptionEditText = findViewById(R.id.descriptionField)
        btnSelectImage      = findViewById(R.id.btnSelectImage)
        imageView           = findViewById(R.id.imagePreview)
        btnEnviar           = findViewById(R.id.btnEnviar)
        imageView.visibility = ImageView.GONE

        setupPhoneEditText()

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        btnEnviar.setOnClickListener {
            btnEnviar.isEnabled = false
            val nombre    = nameEditText.text.toString().trim()
            val telefono  = phoneEditText.text.toString().trim()
            val ubicacion = locationEditText.text.toString().trim()
            val desc      = descriptionEditText.text.toString().trim()

            if (nombre.isNotEmpty() && telefono.length == 12 && ubicacion.isNotEmpty() && desc.isNotEmpty()) {
                enviarCorreoYGuardar(nombre, telefono, ubicacion, desc)
            } else {
                showCenteredToast("Por favor completa todos los campos y el teléfono con +34 y 9 dígitos")
                btnEnviar.isEnabled = true
            }
        }
    }

    private fun enviarCorreoYGuardar(
        nombre: String, telefono: String,
        ubicacion: String, descripcion: String
    ) {
        Thread {
            try {
                // 1) Enviar email
                val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Desconocido"
                val mensaje = """
                    Se ha registrado una nueva incidencia:
                    Usuario: $currentUserEmail
                    Nombre: $nombre
                    Teléfono: $telefono
                    Ubicación: $ubicacion
                    Descripción: $descripcion
                """.trimIndent()
                MailSender(EMAIL_ORIGEN, PASSWORD_EMAIL)
                    .sendMail("Nueva Incidencia", mensaje, EMAIL_DESTINO)

                // 2) Guardar en Firebase
                val dbRef = FirebaseDatabase
                    .getInstance()
                    .getReference("incidencias")
                    .push()

                val nuevaIncidencia = Incidencia(
                    id             = dbRef.key ?: "",
                    usuarioEmail   = currentUserEmail,
                    nombre         = nombre,
                    telefono       = telefono,
                    ubicacion      = ubicacion,
                    descripcion    = descripcion
                )
                dbRef.setValue(nuevaIncidencia)

                // 3) Feedback UI
                runOnUiThread {
                    showCenteredToast("¡Incidencia enviada y guardada correctamente!")
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showCenteredToast("Error: ${e.localizedMessage}")
                    btnEnviar.isEnabled = true
                }
            }
        }.start()
    }

    private fun setupPhoneEditText() {
        phoneEditText.setText("+34")
        phoneEditText.setSelection(phoneEditText.text.length)
        phoneEditText.addTextChangedListener(object : TextWatcher {
            var editing = false
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (editing) return
                editing = true
                s?.let {
                    if (!it.startsWith("+34")) {
                        phoneEditText.setText("+34")
                        phoneEditText.setSelection(phoneEditText.text.length)
                    } else if (it.length > 12) {
                        val t = it.substring(0, 12)
                        phoneEditText.setText(t)
                        phoneEditText.setSelection(t.length)
                    }
                }
                editing = false
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE
            && resultCode == Activity.RESULT_OK
            && data?.data != null
        ) {
            selectedImageUri = data.data
            imageView.setImageURI(selectedImageUri)
            imageView.visibility = ImageView.VISIBLE
        }
    }

    private fun showCenteredToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.CENTER, 0, 500)
            show()
        }
    }
}
