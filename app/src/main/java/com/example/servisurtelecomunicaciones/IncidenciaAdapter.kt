package com.example.servisurtelecomunicaciones

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class IncidenciaAdapter(
    private var items: MutableList<Incidencia>,
    private val onEstadoChanged: (Incidencia) -> Unit,
    private val onDelete: (Incidencia) -> Unit
) : RecyclerView.Adapter<IncidenciaAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo  = view.findViewById<TextView>(R.id.tvIncidenciaTitulo)
        val tvFecha   = view.findViewById<TextView>(R.id.tvIncidenciaFecha)
        val tvEstado  = view.findViewById<TextView>(R.id.tvIncidenciaEstado)
        val ivDelete  = view.findViewById<ImageView>(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incidencia, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val inc = items[position]
        val ctx = holder.itemView.context

        // Formatear fecha
        val fechaStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(inc.timestamp)

        // Mostrar tarjeta
        holder.tvTitulo.text = inc.nombre
        holder.tvFecha .text = fechaStr
        holder.tvEstado.text = inc.estado.replaceFirstChar { it.uppercase() }
        holder.tvEstado.setTextColor(
            ctx.getColor(
                when (inc.estado.lowercase()) {
                    "abierta"   -> android.R.color.holo_green_dark
                    "pendiente" -> android.R.color.holo_orange_dark
                    "cerrada"   -> android.R.color.darker_gray
                    else        -> android.R.color.black
                }
            )
        )

        // Click para detalle
        holder.itemView.setOnClickListener {
            showDetailDialog(ctx, inc, fechaStr)
        }

        // Papelera
        holder.ivDelete.setOnClickListener {
            confirmarBorrado(ctx, inc, position)
        }
    }

    private fun showDetailDialog(ctx: Context, inc: Incidencia, fechaStr: String) {
        val dialogView = LayoutInflater.from(ctx)
            .inflate(R.layout.dialog_incidencia_detail, null)

        dialogView.findViewById<TextView>(R.id.tvDialogUsuarioVal).apply {
            text = inc.usuarioEmail
            setTextColor(ctx.getColor(android.R.color.black))
        }
        dialogView.findViewById<TextView>(R.id.tvDialogFechaVal).apply {
            text = fechaStr
            setTextColor(ctx.getColor(android.R.color.black))
        }
        dialogView.findViewById<TextView>(R.id.tvDialogTelefonoVal).apply {
            text = inc.telefono
            setTextColor(ctx.getColor(android.R.color.black))
        }
        dialogView.findViewById<TextView>(R.id.tvDialogUbicacionVal).apply {
            text = inc.ubicacion
            setTextColor(ctx.getColor(android.R.color.black))
        }
        dialogView.findViewById<TextView>(R.id.tvDialogDescripcionVal).apply {
            text = inc.descripcion
            setTextColor(ctx.getColor(android.R.color.black))
        }

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
        val opciones = arrayOf("Abierta","Pendiente","Cerrada")
        val current  = opciones.indexOfFirst { it.equals(inc.estado,ignoreCase=true) }
        AlertDialog.Builder(ctx)
            .setTitle("Cambiar estado")
            .setSingleChoiceItems(opciones, if(current>=0)current else 0) { dlg,which->
                inc.estado = opciones[which].lowercase()
                onEstadoChanged(inc)
                notifyItemChanged(pos)
                dlg.dismiss()
            }
            .setNegativeButton("Cancelar",null)
            .show()
    }

    private fun confirmarBorrado(ctx: Context, inc: Incidencia, pos: Int) {
        fun doDelete() {
            onDelete(inc)
            items.removeAt(pos)
            notifyItemRemoved(pos)
            Toast.makeText(ctx,"Incidencia borrada",Toast.LENGTH_SHORT).show()
        }
        if(inc.estado.lowercase() in listOf("abierta","pendiente")) {
            AlertDialog.Builder(ctx)
                .setTitle("Atención")
                .setMessage("La incidencia está ${inc.estado}. ¿Borrar de todos modos?")
                .setPositiveButton("Sí"){d,_-> d.dismiss(); doDelete()}
                .setNegativeButton("No",null)
                .show()
        } else {
            AlertDialog.Builder(ctx)
                .setTitle("Borrar incidencia")
                .setMessage("¿Confirmas borrado?")
                .setPositiveButton("Sí"){d,_-> d.dismiss(); doDelete()}
                .setNegativeButton("No",null)
                .show()
        }
    }

    fun updateList(nueva: List<Incidencia>) {
        items.clear()
        items.addAll(nueva)
        notifyDataSetChanged()
    }
}
