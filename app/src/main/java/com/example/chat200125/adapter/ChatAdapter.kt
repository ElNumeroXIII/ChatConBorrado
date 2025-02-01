package com.example.chat200125.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chat200125.R
import com.example.chat200125.model.ChatModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatAdapter(
    var lista: MutableList<ChatModel>,
    private val emailUsuarioLogeado: String
) : RecyclerView.Adapter<ChatViewHolder>() {

    fun updateAdapter(listaNueva: MutableList<ChatModel>) {
        lista = listaNueva
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.chat_layout, parent, false)
        return ChatViewHolder(v)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val mensaje = lista[position]
        holder.render(mensaje, emailUsuarioLogeado)

        holder.binding.imageButton.setOnClickListener {
            if (mensaje.id.isNotEmpty()) {
                val databaseRef = FirebaseDatabase.getInstance().getReference("chat")

                // 🔍 Buscar el nodo padre que contiene el mensaje
                databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var mensajeEncontrado = false

                        for (nodoPadre in snapshot.children) {  // 🔍 Recorre todos los nodos principales (ej: 1738326141900)
                            val idFirebase = nodoPadre.child("id").value
                            Log.d("ChatAdapter", "Revisando nodo padre: ${nodoPadre.key}, id almacenado: $idFirebase")

                            // Compara si el ID del mensaje es el mismo que el guardado en Firebase
                            if (idFirebase == mensaje.id) {
                                Log.d("ChatAdapter", "Nodo padre encontrado: ${nodoPadre.key}, eliminando mensaje...")

                                // 🔥 Eliminar el mensaje completo en la ruta correcta
                                nodoPadre.ref.removeValue()
                                    .addOnSuccessListener {
                                        lista.removeAt(position)
                                        notifyItemRemoved(position)
                                        Log.d("ChatAdapter", "Mensaje eliminado correctamente en Firebase: ${mensaje.id}")
                                        Toast.makeText(
                                            holder.itemView.context,
                                            "Mensaje eliminado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Log.e("ChatAdapter", "Error al eliminar el mensaje", it)
                                        Toast.makeText(
                                            holder.itemView.context,
                                            "Error al eliminar",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                mensajeEncontrado = true
                                break
                            }
                        }

                        if (!mensajeEncontrado) {
                            Log.e("ChatAdapter", "Mensaje no encontrado en Firebase: ${mensaje.id}")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ChatAdapter", "Error en la consulta de Firebase", error.toException())
                    }
                })
            } else {
                Log.e("ChatAdapter", "Error: mensaje.id vacío")
            }
        }


    }
}