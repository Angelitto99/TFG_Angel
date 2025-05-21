package com.example.servisurtelecomunicaciones

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import java.io.File
import java.io.FileOutputStream

class IncidentActivity : AppCompatActivity() {

    companion object {
        private const val BASE_URL        = "https://servisurtelecomunicacion-223b0-default-rtdb.firebaseio.com"
        private const val EMAIL_ORIGEN    = "servisuravisosapp76@gmail.com"
        private const val PASSWORD_EMAIL  = "bnlwvwgrxbpfnqma"
        private const val EMAIL_DESTINO   = "telecomunicacionesservisur8@gmail.com"
        private const val IMAGE_PICK_CODE = 1001
        private const val IMAGE_CAMERA_CODE = 1002
        private const val CAMERA_PERMISSION_CODE = 2001
    }

    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var imageView: ImageView
    private lateinit var btnEnviar: Button
    private var tempImageFile: File? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incident)

        // Vistas
        nameEditText = findViewById(R.id.nameField)
        phoneEditText = findViewById(R.id.phoneField)
        locationEditText = findViewById(R.id.locationField)
        descriptionEditText = findViewById(R.id.descriptionField)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        imageView = findViewById(R.id.imagePreview)
        btnEnviar = findViewById(R.id.btnEnviar)

        imageView.visibility = ImageView.GONE
        setupPhoneEditText()

        btnSelectImage.setOnClickListener {
            val opciones = arrayOf("Galería", "Cámara")
            AlertDialog.Builder(this)
                .setTitle("Seleccionar imagen")
                .setItems(opciones) { _, index ->
                    if (index == 0) pickImageFromGallery() else checkCameraPermissionAndCapture()
                }
                .show()
        }

        btnEnviar.setOnClickListener {
            btnEnviar.isEnabled = false
            val nombre = nameEditText.text.toString().trim()
            val telefono = phoneEditText.text.toString().trim()
            val ubicacion = locationEditText.text.toString().trim()
            val desc = descriptionEditText.text.toString().trim()

            if (nombre.isNotEmpty() && telefono.length == 12 && ubicacion.isNotEmpty() && desc.isNotEmpty()) {
                enviarCorreoYGuardar(nombre, telefono, ubicacion, desc)
            } else {
                toastConLogo("Por favor completa todos los campos correctamente.")
                btnEnviar.isEnabled = true
            }
        }
    }

    private fun checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else captureFromCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                captureFromCamera()
            else toastConLogo("Permiso de cámara denegado")
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun captureFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, IMAGE_CAMERA_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            IMAGE_PICK_CODE -> data?.data?.let { uri ->
                val file = File(cacheDir, "image_attachment.jpg")
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                tempImageFile = file
                selectedImageUri = uri
                imageView.setImageURI(uri)
                imageView.visibility = ImageView.VISIBLE
            }

            IMAGE_CAMERA_CODE -> (data?.extras?.get("data") as? android.graphics.Bitmap)?.let { bmp ->
                val file = File(cacheDir, "captured_image.jpg")
                FileOutputStream(file).use { out -> bmp.compress(
                    android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
                tempImageFile = file
                selectedImageUri = Uri.fromFile(file)
                imageView.setImageBitmap(bmp)
                imageView.visibility = ImageView.VISIBLE
            }
        }
    }

    private fun enviarCorreoYGuardar(
        nombre: String,
        telefono: String,
        ubicacion: String,
        descripcion: String
    ) {
        Thread {
            try {
                // Envío de correo
                val currentUser = FirebaseAuth.getInstance().currentUser
                val usuarioEmail = currentUser?.email ?: "Desconocido"
                val usuarioId = currentUser?.uid ?: ""
                val mensaje = """
                    Se ha registrado una nueva incidencia:
                    Usuario: $usuarioEmail
                    Nombre: $nombre
                    Teléfono: $telefono
                    Ubicación: $ubicacion
                    Descripción: $descripcion
                """.trimIndent()
                val sender = MailSender(EMAIL_ORIGEN, PASSWORD_EMAIL)
                tempImageFile?.takeIf { it.exists() }
                    ?.let { sender.sendMailWithAttachment("Nueva Incidencia", mensaje, EMAIL_DESTINO, it.absolutePath) }
                    ?: sender.sendMail("Nueva Incidencia", mensaje, EMAIL_DESTINO)

                // Guardar en RTDB usando la misma base URL
                val ref: DatabaseReference = FirebaseDatabase
                    .getInstance(BASE_URL)
                    .getReference("incidencias")
                    .push()
                val incidencia = Incidencia(
                    id = ref.key ?: "",
                    usuarioId = usuarioId,
                    usuarioEmail = usuarioEmail,
                    nombre = nombre,
                    telefono = telefono,
                    ubicacion = ubicacion,
                    descripcion = descripcion,
                    timestamp = System.currentTimeMillis()
                )
                ref.setValue(incidencia)

                runOnUiThread {
                    toastConLogo("¡Incidencia enviada correctamente!")
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    toastConLogo("Error: ${e.localizedMessage}")
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
            override fun afterTextChanged(s: Editable?) {
                if (editing) return
                editing = true
                s?.let {
                    when {
                        !it.startsWith("+34") -> phoneEditText.setText("+34")
                        it.length > 12        -> phoneEditText.setText(it.substring(0, 12))
                    }
                    phoneEditText.setSelection(phoneEditText.text.length)
                }
                editing = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun toastConLogo(msg: String) {
        val layout = layoutInflater.inflate(R.layout.toast_custom_logo,
            findViewById(android.R.id.content), false)
        layout.findViewById<TextView>(R.id.toastText).text = msg
        Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.CENTER, 0, 400)
            show()
        }
    }
}
