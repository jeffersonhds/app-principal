package com.jefferson.antenas.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppHelper {

    fun openWhatsApp(context: Context, phoneNumber: String, message: String) {
        try {
            // Garante que o número de telefone está no formato correto (com código do país, sem +)
            val formattedPhoneNumber = phoneNumber.replace("+", "").replace(" ", "")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhoneNumber&text=${Uri.encode(message)}")
                // O pacote "com.whatsapp" garante que abrirá o WhatsApp diretamente
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Se o WhatsApp não estiver instalado, informa ao usuário.
            Toast.makeText(context, "WhatsApp não encontrado.", Toast.LENGTH_SHORT).show()
        }
    }
}
