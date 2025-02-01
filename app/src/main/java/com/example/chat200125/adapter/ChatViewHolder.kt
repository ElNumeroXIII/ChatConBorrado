package com.example.chat200125.adapter

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.chat200125.R
import com.example.chat200125.databinding.ChatLayoutBinding
import com.example.chat200125.model.ChatModel
import java.text.SimpleDateFormat
import java.util.Date

class ChatViewHolder (v: View): RecyclerView.ViewHolder(v){
    val binding = ChatLayoutBinding.bind(v)
    fun render(item: ChatModel,emailUsuarioLogeado:String){
        val params=binding.cvChat.layoutParams as FrameLayout.LayoutParams
        if (emailUsuarioLogeado==item.email){
            binding.clChat.setBackgroundColor(binding.tvFecha.context.getColor(R.color.color_logeado))
            params.gravity=Gravity.END
            binding.imageButton.visibility=View.VISIBLE
        }else{
            binding.clChat.setBackgroundColor(binding.tvFecha.context.getColor(R.color.color_normal))
            params.gravity=Gravity.START
            binding.imageButton.visibility=View.GONE
        }
        binding.tvEmail.text = item.email
        binding.tvTexto.text = item.mensaje
        binding.tvFecha.text = fechaFormateada(item.fecha)

    }

    private fun fechaFormateada(fecha:Long):String{
        val date = Date(fecha)
        val format = SimpleDateFormat("dd/MM/YYYY HH:mm:ss")
        return format.format(date)
    }
}
