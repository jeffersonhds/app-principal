package com.jefferson.antenas.utils

/**
 * Utilitário centralizado para validações de entrada do usuário
 *
 * Usado em: AuthViewModel, SignUpScreen, LoginScreen
 */
object ValidationUtils {

    // Regex para validação de email
    // Aceita: user@example.com, user+tag@example.co.uk, etc
    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    /**
     * Valida se um email está no formato correto
     *
     * @param email String a validar
     * @return true se email é válido, false caso contrário
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && email.matches(EMAIL_REGEX)
    }

    /**
     * Valida se uma senha atende aos requisitos mínimos
     * Requisito: mínimo 6 caracteres
     *
     * @param password String a validar
     * @return true se senha é válida, false caso contrário
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Valida se o nome não está vazio
     *
     * @param name String a validar
     * @return true se nome é válido, false caso contrário
     */
    fun isValidName(name: String): Boolean {
        return name.isNotBlank()
    }

    /**
     * Retorna mensagem de erro para senha
     * Útil para exibir ao usuário
     *
     * @param password String a validar
     * @return null se válido, mensagem de erro caso contrário
     */
    fun getPasswordError(password: String): String? {
        return when {
            password.isEmpty() -> null // Campo vazio, não mostra erro ainda
            password.length < 6 -> "Senha deve ter pelo menos 6 caracteres"
            else -> null
        }
    }

    /**
     * Retorna mensagem de erro para email
     * Útil para exibir ao usuário
     *
     * @param email String a validar
     * @return null se válido, mensagem de erro caso contrário
     */
    fun getEmailError(email: String): String? {
        return when {
            email.isEmpty() -> null // Campo vazio, não mostra erro ainda
            !isValidEmail(email) -> "Email inválido"
            else -> null
        }
    }

    /**
     * Retorna mensagem de erro para nome
     *
     * @param name String a validar
     * @return null se válido, mensagem de erro caso contrário
     */
    fun getNameError(name: String): String? {
        return when {
            name.isEmpty() -> null // Campo vazio, não mostra erro ainda
            name.length < 3 -> "Nome deve ter pelo menos 3 caracteres"
            else -> null
        }
    }

    /**
     * Valida todos os campos de SignUp de uma vez
     * Retorna lista de erros (vazia se tudo ok)
     *
     * @param name Nome do usuário
     * @param email Email do usuário
     * @param password Senha do usuário
     * @return Lista com mensagens de erro (vazia se válido)
     */
    fun validateSignUp(name: String, email: String, password: String): List<String> {
        val errors = mutableListOf<String>()

        // Validar nome
        if (!isValidName(name)) {
            errors.add("Nome é obrigatório")
        } else if (name.length < 3) {
            errors.add("Nome deve ter pelo menos 3 caracteres")
        }

        // Validar email
        if (!isValidEmail(email)) {
            errors.add("Email inválido")
        }

        // Validar senha
        if (!isValidPassword(password)) {
            errors.add("Senha deve ter pelo menos 6 caracteres")
        }

        return errors
    }

    /**
     * Valida todos os campos de Login de uma vez
     *
     * @param email Email do usuário
     * @param password Senha do usuário
     * @return Lista com mensagens de erro (vazia se válido)
     */
    fun validateLogin(email: String, password: String): List<String> {
        val errors = mutableListOf<String>()

        if (!isValidEmail(email)) {
            errors.add("Email inválido")
        }

        if (password.isEmpty()) {
            errors.add("Senha é obrigatória")
        }

        return errors
    }
}