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
import java.io.File
import java.io.FileOutputStream

class FormularioAvisoActivity : AppCompatActivity() {

    private val EMAIL_ORIGEN = "servisuravisosapp76@gmail.com"
    private val PASSWORD_EMAIL = "bnlwvwgrxbpfnqma"
    private val EMAIL_DESTINO = "telecomunicacionesservisur8@gmail.com"

    private lateinit var nombreEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var direccionEditText: EditText
    private lateinit var descripcionEditText: EditText
    private lateinit var btnEnviar: Button
    private lateinit var btnSelectImage: Button
    private lateinit var imageView: ImageView

    private var selectedImageUri: Uri? = null
    private var tempImageFile: File? = null
    private val IMAGE_PICK_CODE = 1001
    private val IMAGE_CAMERA_CODE = 1002
    private val CAMERA_PERMISSION_CODE = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_aviso)

        nombreEditText = findViewById(R.id.editTextNombre)
        phoneEditText = findViewById(R.id.editTextTelefono)
        direccionEditText = findViewById(R.id.editTextDireccion)
        descripcionEditText = findViewById(R.id.editTextDescripcion)
        btnEnviar = findViewById(R.id.btnEnviar)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        imageView = findViewById(R.id.imagePreview)

        imageView.visibility = ImageView.GONE
        setupPhoneEditText(phoneEditText)

        btnSelectImage.setOnClickListener {
            val opciones = arrayOf("Galería", "Cámara")
            AlertDialog.Builder(this)
                .setTitle("Seleccionar imagen")
                .setItems(opciones) { _, index ->
                    if (index == 0) pickImageFromGallery()
                    else checkCameraPermissionAndCapture()
                }.show()
        }

        btnEnviar.setOnClickListener {
            val nombre = nombreEditText.text.toString().trim()
            val telefono = phoneEditText.text.toString().trim()
            val direccion = direccionEditText.text.toString().trim()
            val descripcion = descripcionEditText.text.toString().trim()

            if (nombre.isEmpty() || telefono.isEmpty() || direccion.isEmpty() || descripcion.isEmpty()) {
                showCenteredToast("Por favor, rellene todos los campos")
            } else {
                val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Desconocido"
                val mensaje = """
                    Se ha registrado una nueva incidencia:
                    Usuario: $currentUserEmail
                    Nombre: $nombre
                    Teléfono: $telefono
                    Dirección: $direccion
                    Descripción: $descripcion
                """.trimIndent()

                Thread {
                    try {
                        val mailSender = MailSender(EMAIL_ORIGEN, PASSWORD_EMAIL)
                        if (tempImageFile != null && tempImageFile!!.exists()) {
                            mailSender.sendMailWithAttachment("Nueva Incidencia", mensaje, EMAIL_DESTINO, tempImageFile!!.absolutePath)
                        } else {
                            mailSender.sendMail("Nueva Incidencia", mensaje, EMAIL_DESTINO)
                        }
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
                    val file = File(cacheDir, "image_aviso.jpg")
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
                    val file = File(cacheDir, "captured_aviso.jpg")
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
                isEditing = false
            }
        })
    }

    private fun showCenteredToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.CENTER, 0, 500)
            show()
        }
    }
}
