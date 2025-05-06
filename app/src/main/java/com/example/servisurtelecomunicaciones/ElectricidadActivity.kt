package com.example.servisurtelecomunicaciones

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ElectricidadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_electricidad)

        // Cargar imagen del cuadro eléctrico con Glide
        val imgCuadro = findViewById<ImageView>(R.id.imgCuadro)
        Glide.with(this)
            .load(R.drawable.ic_cuadro)  // ic_cuadro.png en res/drawable
            .override(1500, 1500)
            .fitCenter()
            .into(imgCuadro)

        // Cargar imagen para ic_cuadro2
        val imgCuadro2 = findViewById<ImageView>(R.id.imgCuadro2)
        Glide.with(this)
            .load(R.drawable.ic_cuadro2)  // ic_cuadro2.png en res/drawable
            .override(1500, 1500)
            .fitCenter()
            .into(imgCuadro2)

        // Cargar imagen para ic_cuadro3
        val imgCuadro3 = findViewById<ImageView>(R.id.imgCuadro3)
        Glide.with(this)
            .load(R.drawable.ic_cuadro3)  // ic_cuadro3.png en res/drawable
            .override(1500, 1500)
            .fitCenter()
            .into(imgCuadro3)


        // Cargar imagen de interruptores inteligentes con Glide
        val imgInterruptor = findViewById<ImageView>(R.id.imgInterruptor)
        Glide.with(this)
            .load(R.drawable.ic_interruptor)  // ic_interruptor.png en res/drawable
            .override(1500, 1500)
            .fitCenter()
            .into(imgInterruptor)

        // Configurar el botón "Pedir presupuesto" para abrir el formulario
        val btnPedirPresupuesto = findViewById<Button>(R.id.btnPedirPresupuesto)
        btnPedirPresupuesto.setOnClickListener {
            val intent = Intent(this, FormularioAvisoActivity::class.java)
            startActivity(intent)
        }
    }
}
