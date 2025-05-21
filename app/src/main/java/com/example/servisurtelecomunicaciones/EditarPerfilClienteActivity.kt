    package com.example.servisurtelecomunicaciones

    import android.Manifest
    import android.app.Activity
    import android.content.Context
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.graphics.Bitmap
    import android.graphics.BitmapFactory
    import android.graphics.Matrix
    import android.net.Uri
    import android.os.Build
    import android.os.Bundle
    import android.provider.MediaStore
    import android.text.Editable
    import android.text.TextWatcher
    import android.view.Gravity
    import android.widget.*
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import androidx.exifinterface.media.ExifInterface
    import com.google.android.material.textfield.TextInputEditText
    import com.google.firebase.auth.FirebaseAuth
    import de.hdodenhof.circleimageview.CircleImageView
    import java.io.File
    import java.io.FileOutputStream

    class EditarPerfilClienteActivity : AppCompatActivity() {

        companion object {
            private const val PREFS = "prefs"
            private const val RC_PICK_PHOTO = 1001
            private const val RC_NOTIF_PERM = 2001
        }

        private lateinit var ivAvatar: CircleImageView
        private lateinit var etNombre: TextInputEditText
        private lateinit var etTelefono: TextInputEditText
        private lateinit var etEmpresa: TextInputEditText
        private lateinit var swNotifs: Switch
        private lateinit var btnGuardar: Button
        private lateinit var btnCambiarPass: Button
        private lateinit var tvHolaUsuario: TextView

        private val uid: String by lazy {
            FirebaseAuth.getInstance().currentUser?.uid ?: "default"
        }

        private fun key(key: String): String = "cliente_${uid}_$key"

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_editar_perfil_cliente)

            ivAvatar = findViewById(R.id.ivAvatar)
            etNombre = findViewById(R.id.etNombre)
            etTelefono = findViewById(R.id.etTelefono)
            etEmpresa = findViewById(R.id.etEmpresa)
            swNotifs = findViewById(R.id.swNotifs)
            btnGuardar = findViewById(R.id.btnGuardarPerfil)
            btnCambiarPass = findViewById(R.id.btnCambiarPass)
            tvHolaUsuario = findViewById(R.id.tvHolaUsuario)

            val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

            etNombre.setText(prefs.getString(key("name"), ""))
            etTelefono.setText(prefs.getString(key("phone"), ""))
            etEmpresa.setText(prefs.getString(key("company"), ""))
            swNotifs.isChecked = prefs.getBoolean(key("notifs"), false)
            tvHolaUsuario.text = "Hola, ${etNombre.text}"

            prefs.getString(key("avatar"), null)?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    ivAvatar.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_perfil)
                }
            }

            ivAvatar.setOnClickListener { pickImage() }

            etTelefono.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    s?.let {
                        if (it.length > 9) {
                            etTelefono.setText(it.substring(0, 9))
                            etTelefono.setSelection(9)
                        }
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            btnGuardar.setOnClickListener {
                prefs.edit()
                    .putString(key("name"), etNombre.text.toString())
                    .putString(key("phone"), etTelefono.text.toString())
                    .putString(key("company"), etEmpresa.text.toString())
                    .putBoolean(key("notifs"), swNotifs.isChecked)
                    .putBoolean("is_admin", false)
                    .apply()

                tvHolaUsuario.text = "Hola, ${etNombre.text}"

                if (swNotifs.isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            RC_NOTIF_PERM
                        )
                    } else {
                        AlarmManagerCliente.programarAlarmaSemanal(this)
                    }
                } else {
                    AlarmManagerCliente.cancelarAlarma(this)
                }

                toastConLogo("Perfil guardado correctamente")
                finish()
            }

            btnCambiarPass.setOnClickListener {
                startActivity(Intent(this, CambiarPasswordActivity::class.java))
            }
        }

        private fun pickImage() {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, RC_PICK_PHOTO)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == RC_PICK_PHOTO && resultCode == Activity.RESULT_OK) {
                data?.data?.let { uri ->
                    try {
                        val correctedBitmap = rotateImageIfRequired(this, uri)
                        val avatarFile = File(filesDir, "avatar_cliente_$uid.jpg")
                        val outputStream = FileOutputStream(avatarFile)
                        correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        outputStream.close()

                        ivAvatar.setImageBitmap(correctedBitmap)

                        getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                            .edit()
                            .putString(key("avatar"), avatarFile.absolutePath)
                            .apply()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == RC_NOTIF_PERM && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AlarmManagerCliente.programarAlarmaSemanal(this)
            }
        }

        private fun rotateImageIfRequired(context: Context, imageUri: Uri): Bitmap {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val exifStream = context.contentResolver.openInputStream(imageUri)
            val exif = ExifInterface(exifStream!!)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            exifStream.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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
