package com.jefferson.antenas.utils

/**
 * Converte mensagens de erro do Firebase (inglês) para português profissional
 *
 * Uso:
 * catch (e: Exception) {
 *     val mensagem = ErrorMessageHandler.tratarErro(e)
 *     Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show()
 * }
 */
object ErrorMessageHandler {

    /**
     * Traduz exceções do Firebase para mensagens em português
     *
     * @param exception A exceção capturada
     * @return Mensagem amigável em português
     */
    fun tratarErro(exception: Exception?): String {
        if (exception == null) {
            return "Erro desconhecido. Tente novamente."
        }

        val mensagem = exception.message ?: ""

        // Ordena por likelihood (mais comuns primeiro)
        return when {
            // ❌ LOGIN / AUTENTICAÇÃO
            mensagem.contains("incorrect", ignoreCase = true) ||
                    mensagem.contains("malformed", ignoreCase = true) ||
                    mensagem.contains("auth credential", ignoreCase = true) -> {
                "Email ou senha incorretos. Verifique e tente novamente."
            }

            mensagem.contains("no user", ignoreCase = true) ||
                    mensagem.contains("There is no user record", ignoreCase = true) -> {
                "Usuário não encontrado. Verifique o email ou cadastre-se."
            }

            mensagem.contains("password is invalid", ignoreCase = true) ||
                    mensagem.contains("wrong password", ignoreCase = true) -> {
                "Senha incorreta. Tente novamente."
            }

            // ❌ CADASTRO
            mensagem.contains("already in use", ignoreCase = true) ||
                    mensagem.contains("email already exists", ignoreCase = true) -> {
                "Este email já está cadastrado. Faça login ou use outro email."
            }

            mensagem.contains("invalid email", ignoreCase = true) ||
                    mensagem.contains("badly formatted", ignoreCase = true) -> {
                "Email inválido. Verifique o formato."
            }

            mensagem.contains("password too short", ignoreCase = true) ||
                    mensagem.contains("weak password", ignoreCase = true) -> {
                "Senha muito fraca. Use pelo menos 6 caracteres."
            }

            // ❌ CONECTIVIDADE
            mensagem.contains("network", ignoreCase = true) ||
                    mensagem.contains("connection", ignoreCase = true) ||
                    mensagem.contains("timeout", ignoreCase = true) ||
                    mensagem.contains("unreachable", ignoreCase = true) -> {
                "Sem conexão com a internet. Verifique sua conexão."
            }

            mensagem.contains("User not found", ignoreCase = true) -> {
                "Usuário não encontrado. Cadastre-se primeiro."
            }

            // ❌ THROTTLING / RATE LIMIT
            mensagem.contains("too many requests", ignoreCase = true) ||
                    mensagem.contains("too many failed login", ignoreCase = true) -> {
                "Muitas tentativas. Aguarde alguns minutos e tente novamente."
            }

            // ❌ SERVIDOR
            mensagem.contains("internal error", ignoreCase = true) ||
                    mensagem.contains("server error", ignoreCase = true) -> {
                "Erro no servidor. Tente novamente mais tarde."
            }

            // ❌ GENÉRICO
            else -> {
                "Erro ao processar sua solicitação. Tente novamente."
            }
        }
    }

    /**
     * Versão simplificada que retorna uma descrição curta
     */
    fun obterMensagemCurta(exception: Exception?): String {
        val mensagem = tratarErro(exception)
        // Se for muito longa, trunca
        return if (mensagem.length > 60) {
            mensagem.take(57) + "..."
        } else {
            mensagem
        }
    }
}