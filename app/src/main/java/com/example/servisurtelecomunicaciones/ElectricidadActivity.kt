package com.example.servisurtelecomunicaciones

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ElectricidadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_electricidad)

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isGuest = prefs.getBoolean("is_guest", false)

        // Cargar imagen del cuadro eléctrico con Glide
        val imgCuadro = findViewById<ImageView>(R.id.imgCuadro)
        Glide.with(this)
            .load(R.drawable.ic_cuadro)
            .override(1500, 1500)
            .fitCenter()
            .into(imgCuadro)

        val imgCuadro2 = findViewById<ImageView>(R.id.imgCuadro2)
        Glide.with(this)
            .load(R.drawable.ic_cuadro2)
            .override(1500, 1500)
            .fitCenter()
            .into(imgCuadro2)

        val imgCuadro3 = findViewById<ImageView>(R.id.imgCuadro3)
        Glide.with(this)
            .load(R.drawable.ic_cuadro3)
            .override(1500, 1500)
            .fitCenter()
            .into(imgCuadro3)

        val imgInterruptor = findViewById<ImageView>(R.id.imgInterruptor)
        Glide.with(this)
            .load(R.drawable.ic_interruptor)
            .override(1500, 1500)
            .fitCenter()
            .into(imgInterruptor)

        val btnPedirPresupuesto = findViewById<Button>(R.id.btnPedirPresupuesto)
        btnPedirPresupuesto.setOnClickListener {
            if (isGuest) {
                toastConLogo("Debes registrarte para usar esta función")
            } else {
                startActivity(Intent(this, FormularioAvisoActivity::class.java))
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
