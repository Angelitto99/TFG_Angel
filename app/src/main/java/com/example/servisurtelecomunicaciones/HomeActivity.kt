package com.example.servisurtelecomunicaciones
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Card para Antenas (antes Satélite TV)
        val cardAntenas = findViewById<CardView>(R.id.Antenas)
        val iconAntenas = findViewById<ImageView>(R.id.iconSatelliteTV)
        // Cargar logo de Antenas usando Glide
        Glide.with(this)
            .load(R.drawable.ic_satellite) // Asegúrate de que este drawable esté optimizado
            .override(48, 48)
            .centerInside()
            .into(iconAntenas)
        cardAntenas.setOnClickListener {
            startActivity(Intent(this, AntenaActivity::class.java))
        }

        // Card para Videoporteros
        val cardVideoporteros = findViewById<CardView>(R.id.cardVideoporteros)
        val iconVideoporteros = findViewById<ImageView>(R.id.iconVideoporteros)
        // Cargar logo de Videoporteros usando Glide
        Glide.with(this)
            .load(R.drawable.ic_videoportero)
            .override(48, 48)
            .centerInside()
            .into(iconVideoporteros)
        cardVideoporteros.setOnClickListener {
            startActivity(Intent(this, VideoporteroActivity::class.java))
        }

        // Card para Electricidad
        val cardElectricidad = findViewById<CardView>(R.id.cardElectricidad)
        val iconElectricidad = findViewById<ImageView>(R.id.iconElectricidad)
        // Cargar logo de Electricidad usando Glide
        Glide.with(this)
            .load(R.drawable.ic_electricidad)
            .override(48, 48)
            .centerInside()
            .into(iconElectricidad)
        cardElectricidad.setOnClickListener {
            startActivity(Intent(this, ElectricidadActivity::class.java))
        }

        // Card para CCTV
        val cardCCTV = findViewById<CardView>(R.id.cardCCTV)
        val iconCCTV = findViewById<ImageView>(R.id.iconCCTV)
        iconCCTV.setImageResource(R.drawable.ic_cctv)
        cardCCTV.setOnClickListener {
            startActivity(Intent(this, CCTVActivity::class.java))
        }

        // Card para Cerrajería
        val cardCerrajeria = findViewById<CardView>(R.id.cardCerrajeria)
        val iconCerrajeria = findViewById<ImageView>(R.id.iconCerrajeria)
        iconCerrajeria.setImageResource(R.drawable.ic_cerrajeria)
        cardCerrajeria.setOnClickListener {
            startActivity(Intent(this, CerrajeriaActivity::class.java))
        }

        // Card para Mantenimiento
        val cardMantenimiento = findViewById<CardView>(R.id.cardMantenimiento)
        val iconMantenimiento = findViewById<ImageView>(R.id.iconMantenimiento)
        iconMantenimiento.setImageResource(R.drawable.ic_mantenimiento)
        cardMantenimiento.setOnClickListener {
            startActivity(Intent(this, MantenimientoActivity::class.java))
        }

        // Botones inferiores (SOBRE NOSOTROS, VER PÁGINA WEB, DAR UN AVISO)
        val buttonAboutUs = findViewById<Button>(R.id.buttonAboutUs)
        buttonAboutUs.setOnClickListener {
            startActivity(Intent(this, AboutUsActivity::class.java))
        }

        val websiteButton = findViewById<Button>(R.id.websiteButton)
        websiteButton.setOnClickListener {
            val url = "https://www.servisurtelecomunicaciones.com/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage(null)
            startActivity(intent)
        }

        val buttonIncident = findViewById<Button>(R.id.buttonIncident)
        buttonIncident.setOnClickListener {
            startActivity(Intent(this, IncidentActivity::class.java))
        }

        // Botón Cerrar Sesión
        val cerrarSesionBtn = findViewById<Button>(R.id.btnCerrarSesion)
        cerrarSesionBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val toast = Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}