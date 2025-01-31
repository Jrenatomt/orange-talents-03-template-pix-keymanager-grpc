package br.com.pix.compartilhado.chavePix

import br.com.pix.TipoChave as TipoChaveGrpc
import br.com.pix.validacao.ErrorMessage
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoChave {

    CPF {
        override fun valida(chave: String?): ErrorMessage? {
            if (chave.isNullOrBlank()) {
                return ErrorMessage(
                    description = "CPF é Obrigatório"
                )
            }

            val isCpfValido = CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }

            if (!chave.matches("^[0-9]{11}\$".toRegex()) || !isCpfValido) {
                return ErrorMessage(
                    description = "Por favor insira um CPF válido",
                    augmentDescription = "Formato esperado é 12345678901 e deve ser válido"
                )
            }

            return null
        }

    },
    CELULAR {
        override fun valida(chave: String?): ErrorMessage? {
            if (chave.isNullOrBlank() || !chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) {
                return ErrorMessage(
                    description = "Por favor insira um formato de telefone celular válido",
                    augmentDescription = "Formato esperado é +5585988714077"
                )
            }

            return null
        }
    },
    EMAIL {
        override fun valida(chave: String?): ErrorMessage? {
            if (chave.isNullOrBlank()) {
                return ErrorMessage(
                    description = "Email é obrigatório",
                    augmentDescription = "Formato esperado é email@email.com")
            }

            val isEmailValido = EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }

            if (!isEmailValido) {
                return ErrorMessage(
                    description = "Por favor insira um formato de email válido",
                    augmentDescription = "Formato esperado é email@email.com")
            }

            return null
        }

    },
    ALEATORIA {
        override fun valida(chave: String?): ErrorMessage? {
            if (!chave.isNullOrBlank()) {
                return ErrorMessage(
                    description = "Chave aleatório não deve ter chave preenchida",
                    augmentDescription = "Tente novamente sem preencher a chave"
                )
            }
            return null
        }

    },

    INVALIDA {
        override fun valida(chave: String?): ErrorMessage? {
            return ErrorMessage(
                description = "Tipo de chave inválido",
                augmentDescription = "Por favor insira um tipo de chave válido"
            )
        }

    };

    abstract fun valida(chave: String?): ErrorMessage?
}

fun requestParaTipoChave(message: TipoChaveGrpc?): TipoChave {
    return when (message) {
        TipoChaveGrpc.CPF -> TipoChave.CPF
        TipoChaveGrpc.CELULAR -> TipoChave.CELULAR
        TipoChaveGrpc.EMAIL -> TipoChave.EMAIL
        TipoChaveGrpc.ALEATORIA -> TipoChave.ALEATORIA
        else -> TipoChave.INVALIDA
    }
}