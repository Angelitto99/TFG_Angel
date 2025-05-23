package com.example.servisurtelecomunicaciones

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class IncidenciaAdapter(
    private var items: MutableList<Incidencia>,
    private val onEstadoChanged: (Incidencia) -> Unit,
    private val onDelete: (Incidencia) -> Unit
) : RecyclerView.Adapter<IncidenciaAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo  : TextView  = view.findViewById(R.id.tvIncidenciaTitulo)
        val tvFecha   : TextView  = view.findViewById(R.id.tvIncidenciaFecha)
        val tvEstado  : TextView  = view.findViewById(R.id.tvIncidenciaEstado)
        val ivDelete  : ImageView = view.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incidencia, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val inc = items[position]
        val ctx = holder.itemView.context
        val fechaStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(inc.timestamp)

        holder.tvTitulo.text  = inc.nombre
        holder.tvFecha.text   = fechaStr
        holder.tvEstado.text  = inc.estado.replaceFirstChar { it.uppercase() }
        holder.tvEstado.setTextColor(
            ctx.getColor(
                when (inc.estado.lowercase()) {
                    "abierta"   -> android.R.color.holo_green_dark
                    "pendiente" -> android.R.color.holo_orange_dark
                    "cerrada"   -> android.R.color.holo_red_dark
                    else        -> android.R.color.black
                }
            )
        )

        holder.itemView.setOnClickListener {
            showDetailDialog(ctx, inc, fechaStr)
        }

        holder.ivDelete.setOnClickListener {
            confirmarBorrado(ctx, inc, position)
        }
    }

    private fun showDetailDialog(ctx: Context, inc: Incidencia, fechaStr: String) {
        val dialogView = LayoutInflater.from(ctx)
            .inflate(R.layout.dialog_incidencia_detail, null)

        dialogView.findViewById<TextView>(R.id.tvDialogUsuarioVal).text     = inc.usuarioEmail
        dialogView.findViewById<TextView>(R.id.tvDialogFechaVal).text       = fechaStr
        dialogView.findViewById<TextView>(R.id.tvDialogTelefonoVal).text    = inc.telefono
        dialogView.findViewById<TextView>(R.id.tvDialogUbicacionVal).text   = inc.ubicacion
        dialogView.findViewById<TextView>(R.id.tvDialogDescripcionVal).text = inc.descripcion

        AlertDialog.Builder(ctx)
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .setNeutralButton("Cambiar estado") { dlg, _ ->
                dlg.dismiss()
                mostrarCambioEstado(ctx, inc, items.indexOf(inc))
            }
            .show()
    }

    private fun mostrarCambioEstado(ctx: Context, inc: Incidencia, pos: Int) {
        val opciones = arrayOf("Abierta", "Pendiente", "Cerrada")
        val current = opciones.indexOfFirst { it.equals(inc.estado, ignoreCase = true) }

        val dialog = AlertDialog.Builder(ctx)
            .setTitle("Cambiar estado")
            .setSingleChoiceItems(opciones, if (current >= 0) current else 0) { dlg, which ->
                inc.estado = opciones[which].lowercase()
                onEstadoChanged(inc)
                notifyItemChanged(pos)
                ctx.toastConLogo("Estado cambiado correctamente")
                dlg.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun confirmarBorrado(ctx: Context, inc: Incidencia, pos: Int) {
        fun doDelete() {
            onDelete(inc)
            items.removeAt(pos)
            notifyItemRemoved(pos)
            ctx.toastConLogo("Incidencia borrada")
        }

        if (inc.estado.lowercase() in listOf("abierta", "pendiente")) {
            AlertDialog.Builder(ctx)
                .setTitle("Atención")
                .setMessage("La incidencia está ${inc.estado}. ¿Borrar de todos modos?")
                .setPositiveButton("Sí") { d, _ -> d.dismiss(); doDelete() }
                .setNegativeButton("No", null)
                .show()
        } else {
            AlertDialog.Builder(ctx)
                .setTitle("Borrar incidencia")
                .setMessage("¿Confirmas borrado?")
                .setPositiveButton("Sí") { d, _ -> d.dismiss(); doDelete() }
                .setNegativeButton("No", null)
                .show()
        }
    }

    fun updateList(nueva: List<Incidencia>) {
        items.clear()
        items.addAll(nueva)
        notifyDataSetChanged()
    }
}

// Extensión para mostrar Toast con logo
fun Context.toastConLogo(msg: String) {
    val layout = LayoutInflater.from(this)
        .inflate(R.layout.toast_custom_logo, null)
    layout.findViewById<TextView>(R.id.toastText).text = msg

    Toast(this).apply {
        duration = Toast.LENGTH_SHORT
        view = layout
        setGravity(Gravity.CENTER, 0, 250)
        show()
    }
}
