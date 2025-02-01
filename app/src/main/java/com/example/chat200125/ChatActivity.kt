package com.example.chat200125

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chat200125.adapter.ChatAdapter
import com.example.chat200125.databinding.ActivityChatBinding
import com.example.chat200125.model.ChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    var emailUsuarioLogeado = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private var listadoChats = mutableListOf<ChatModel>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        emailUsuarioLogeado = auth.currentUser?.email.toString()

        val firebaseDatabase=FirebaseDatabase.getInstance()
        firebaseDatabase.setPersistenceEnabled(true)

        databaseRef = FirebaseDatabase.getInstance().getReference("chat")
        setRecycler()
        setListeners()
    }

    private fun setRecycler() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvChat.layoutManager = layoutManager
        adapter = ChatAdapter(listadoChats, emailUsuarioLogeado)
        binding.rvChat.adapter = adapter
    }

    private fun setListeners() {
        binding.imageView.setOnClickListener {
            enviar()
        }

        //Ponemos un listener a la base de datos para recuperar todos los mensajes
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listadoChats.clear()
                for (nodo in snapshot.children) {
                    val chatNodo = nodo.getValue(ChatModel::class.java)
                    if (chatNodo != null) {
                        listadoChats.add(chatNodo)

                    }
                }
                listadoChats.sortBy { it.fecha }
                //adapter.lista=listadoChats
                //adapter.notifyDataSetChanged()
                adapter.updateAdapter(listadoChats)

                // hacemos scroll del recycler para que aparezca su final

                binding.rvChat.scrollToPosition(listadoChats.size - 1)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ChatActivity,
                    "Error al recuperar los chats",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })


        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_logout -> {
                    auth.signOut()
                    finish()
                    true
                }

                R.id.item_cerrar -> {
                    finishAffinity()
                    true
                }

                R.id.item_borrar -> {
                    borrarChatUsuario()
                    true
                }

                else -> {
                    false
                }
            }
        }
        // Listener para enviar chat cuando le da a intro

        binding.etMensaje.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                enviar()
                ocultarTeclado()
                true
            } else {
                false
            }
        }



    }

    private fun borrarChatUsuario() {
        databaseRef.get().addOnSuccessListener {
            for (nodo in it.children) {
                val correo = nodo.child("email").getValue(String::class.java)
                if (correo == emailUsuarioLogeado) {
                    nodo.ref.removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Chat borrado", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al borrar el chat", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
        }
    }

 /*   private fun borrarChatEspecifico(chat: ChatModel) {
        databaseRef.child(chat.id).removeValue()
            .addOnSuccessListener {
                val position = adapter.lista.indexOf(chat)
                if (position!=1){
                    adapter.lista.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    Toast.makeText(this, "Chat borrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al borrar el chat", Toast.LENGTH_SHORT).show()
            }

    }*/

    private fun enviar() {
        val texto = binding.etMensaje.text.toString()
        if (texto.isNotEmpty()) {
            val mensajeId = databaseRef.push().key ?: return
            val chat = ChatModel(mensajeId,emailUsuarioLogeado, texto, System.currentTimeMillis())

            databaseRef.child(System.currentTimeMillis().toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        databaseRef.child(System.currentTimeMillis().toString()).setValue(chat)
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener {

                                Toast.makeText(
                                    this@ChatActivity,
                                    "No se pudo guardar el mensaje",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

            binding.etMensaje.setText("")
        } else return
    }

    private fun ocultarTeclado() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

    }

}