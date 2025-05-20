package com.example.servisurtelecomunicaciones

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class FacturaAdapter(
    private val items: MutableList<Factura>,
    private val onEstadoChanged: (Factura) -> Unit,
    private val onDelete: (Factura) -> Unit,
    private val onEdit: (Factura) -> Unit
) : RecyclerView.Adapter<FacturaAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNum = view.findViewById<TextView>(R.id.tvFacturaNumero)
        val tvCli = view.findViewById<TextView>(R.id.tvFacturaCliente)
        val tvFec = view.findViewById<TextView>(R.id.tvFacturaFecha)
        val tvEst = view.findViewById<TextView>(R.id.tvFacturaEstado)
        val ivDel = view.findViewById<ImageView>(R.id.ivFacturaDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_factura, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val f = items[position]
        val ctx = holder.itemView.context

        holder.tvNum.text = "Factura #${f.numero}"
        holder.tvCli.text = f.clienteNombre
        holder.tvFec.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(f.fecha))

        holder.tvEst.text = f.estado.replaceFirstChar { it.uppercase() }
        holder.tvEst.setTextColor(
            ctx.getColor(
                when (f.estado.lowercase()) {
                    "pagada" -> android.R.color.holo_green_dark
                    "pendiente" -> android.R.color.holo_orange_dark
                    else -> android.R.color.black
                }
            )
        )

        holder.ivDel.setOnClickListener {
            AlertDialog.Builder(ctx)
                .setTitle("Borrar factura")
                .setMessage("¿Seguro que quieres borrar la factura #${f.numero}?")
                .setPositiveButton("Sí") { d, _ ->
                    onDelete(f)
                    items.removeAt(position)
                    notifyItemRemoved(position)
                    d.dismiss()
                }
                .setNegativeButton("No", null)
                .show()
        }

        holder.itemView.setOnClickListener {
            showDetailDialog(ctx, f, position)
        }
    }

    private fun showDetailDialog(ctx: Context, f: Factura, pos: Int) {
        val dialogView = LayoutInflater.from(ctx)
            .inflate(R.layout.dialog_factura_detail, null)

        dialogView.findViewById<TextView>(R.id.tvDetNumero).text = f.numero
        dialogView.findViewById<TextView>(R.id.tvDetCliente).text = f.clienteNombre
        dialogView.findViewById<TextView>(R.id.tvDetNIF).text = f.clienteNIF
        dialogView.findViewById<TextView>(R.id.tvDetDireccion).text = f.clienteDireccion
        dialogView.findViewById<TextView>(R.id.tvDetFecha).text =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(f.fecha))
        dialogView.findViewById<TextView>(R.id.tvDetBase).text =
            String.format(Locale.getDefault(), "%.2f €", f.baseImponible)
        dialogView.findViewById<TextView>(R.id.tvDetIvaPct).text = "${f.tipoIva}%"
        dialogView.findViewById<TextView>(R.id.tvDetIvaCuota).text =
            String.format(Locale.getDefault(), "%.2f €", f.cuotaIva)
        dialogView.findViewById<TextView>(R.id.tvDetTotal).text =
            String.format(Locale.getDefault(), "%.2f €", f.total)
        dialogView.findViewById<TextView>(R.id.tvDetPago).text = f.formaPago
        dialogView.findViewById<TextView>(R.id.tvDetObs).text = f.observaciones

        dialogView.findViewById<TextView>(R.id.tvDetEstado).apply {
            text = f.estado.replaceFirstChar { it.uppercase() }
            setTextColor(
                ctx.getColor(
                    when (f.estado.lowercase()) {
                        "pagada" -> android.R.color.holo_green_dark
                        "pendiente" -> android.R.color.holo_orange_dark
                        else -> android.R.color.black
                    }
                )
            )
        }

        AlertDialog.Builder(ctx)
            .setView(dialogView)
            .setNegativeButton("Cambiar estado") { dlg, _ ->
                dlg.dismiss()
                changeEstadoDialog(ctx, f, pos)
            }
            .setNeutralButton("Editar") { dlg, _ ->
                dlg.dismiss()
                onEdit(f)
            }
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun changeEstadoDialog(ctx: Context, f: Factura, pos: Int) {
        val opts = arrayOf("Pendiente", "Pagada")
        val sel = opts.indexOfFirst { it.equals(f.estado, ignoreCase = true) }.coerceAtLeast(0)

        AlertDialog.Builder(ctx, R.style.MyAlertDialogTheme)
            .setTitle("Cambiar estado")
            .setSingleChoiceItems(opts, sel) { d, which ->
                f.estado = opts[which].lowercase()
                onEstadoChanged(f)
                notifyItemChanged(pos)
                ctx.toastConLogo("Estado cambiado correctamente")
                d.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
