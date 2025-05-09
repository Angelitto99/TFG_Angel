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
import java.io.File
import java.io.FileOutputStream

class IncidentActivity : AppCompatActivity() {

    private val EMAIL_ORIGEN = "servisuravisosapp76@gmail.com"
    private val PASSWORD_EMAIL = "bnlwvwgrxbpfnqma"
    private val EMAIL_DESTINO = "telecomunicacionesservisur8@gmail.com"

    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var imageView: ImageView
    private lateinit var btnEnviar: Button

    private var selectedImageUri: Uri? = null
    private var tempImageFile: File? = null
    private val IMAGE_PICK_CODE = 1001
    private val IMAGE_CAMERA_CODE = 1002
    private val CAMERA_PERMISSION_CODE = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incident)

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
                }.show()
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
                showCenteredToast("Por favor completa todos los campos correctamente.")
                btnEnviar.isEnabled = true
            }
        }
    }

    private fun checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            captureFromCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureFromCamera()
            } else {
                showCenteredToast("Permiso de cámara denegado")
            }
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
            IMAGE_PICK_CODE -> {
                data?.data?.let { uri ->
                    val file = File(cacheDir, "image_attachment.jpg")
                    contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempImageFile = file
                    selectedImageUri = uri
                    imageView.setImageURI(uri)
                    imageView.visibility = ImageView.VISIBLE
                }
            }

            IMAGE_CAMERA_CODE -> {
                val photo = data?.extras?.get("data") as? android.graphics.Bitmap
                photo?.let {
                    val file = File(cacheDir, "captured_image.jpg")
                    FileOutputStream(file).use { out ->
                        it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    tempImageFile = file
                    selectedImageUri = Uri.fromFile(file)
                    imageView.setImageBitmap(it)
                    imageView.visibility = ImageView.VISIBLE
                }
            }
        }
    }

    private fun enviarCorreoYGuardar(nombre: String, telefono: String, ubicacion: String, descripcion: String) {
        Thread {
            try {
                val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Desconocido"
                val mensaje = """
                    Se ha registrado una nueva incidencia:
                    Usuario: $currentUserEmail
                    Nombre: $nombre
                    Teléfono: $telefono
                    Ubicación: $ubicacion
                    Descripción: $descripcion
                """.trimIndent()

                val sender = MailSender(EMAIL_ORIGEN, PASSWORD_EMAIL)
                if (tempImageFile != null && tempImageFile!!.exists()) {
                    sender.sendMailWithAttachment("Nueva Incidencia", mensaje, EMAIL_DESTINO, tempImageFile!!.absolutePath)
                } else {
                    sender.sendMail("Nueva Incidencia", mensaje, EMAIL_DESTINO)
                }

                val dbRef = FirebaseDatabase.getInstance().getReference("incidencias").push()
                val incidencia = Incidencia(
                    id = dbRef.key ?: "",
                    usuarioEmail = currentUserEmail,
                    nombre = nombre,
                    telefono = telefono,
                    ubicacion = ubicacion,
                    descripcion = descripcion
                )
                dbRef.setValue(incidencia)

                runOnUiThread {
                    showCenteredToast("¡Incidencia enviada correctamente!")
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
            override fun afterTextChanged(s: Editable?) {
                if (editing) return
                editing = true
                s?.let {
                    if (!it.startsWith("+34")) {
                        phoneEditText.setText("+34")
                        phoneEditText.setSelection(phoneEditText.text.length)
                    } else if (it.length > 12) {
                        val trimmed = it.substring(0, 12)
                        phoneEditText.setText(trimmed)
                        phoneEditText.setSelection(trimmed.length)
                    }
                }
                editing = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun showCenteredToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.CENTER, 0, 400)
            show()
        }
    }
}
