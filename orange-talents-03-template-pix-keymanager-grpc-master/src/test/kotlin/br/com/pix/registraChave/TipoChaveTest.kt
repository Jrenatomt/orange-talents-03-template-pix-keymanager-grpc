package br.com.pix.registraChave

import br.com.pix.validacao.ErrorMessage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoChaveTest {


    @Nested
    inner class TestesTipoChave {

        @Test
        fun `Nao deve validar chave Aleatoria quando preenchido a chave`() {
            val chave = "email@email"
            val response = TipoChave.ALEATORIA.valida(chave)
            assertTrue(response is ErrorMessage)
        }

        @Test
        fun `valida chave Aleatoria deve retornar null quando nao for preenchida`() {
            val chave = ""
            val response = TipoChave.ALEATORIA.valida(chave)
            assertEquals(null, response)
        }

        @Test
        fun `valida chave Celular deve retornar um ErrorMessage quando for celular invalido`() {
            val celular = "000"
            val response = TipoChave.CELULAR.valida(celular)
            assertTrue(response is ErrorMessage)
        }

        @Test
        fun `valida chave Celular deve retornar null quando for celular valido`() {
            val result = TipoChave.CELULAR.valida("+5585988714077")
            assertEquals(null, result)
        }


        @Test
        fun `valida chave CPF deve retornar um ErrorMessage quando for cpf invalido`(){
            val response = TipoChave.CPF.valida("1154343")
            assertTrue(response is ErrorMessage)
        }

        @Test
        fun `valida chave CPF deve retornar um ErrorMessage quando for cpf não for preenchido`(){
            val response = TipoChave.CPF.valida("")
            assertTrue(response is ErrorMessage)
        }

        @Test
        fun `valida do tipo CPF deve retornar null quando for cpf valido`() {
            val cpfValido = "18482952005"
            val response = TipoChave.CPF.valida(cpfValido)
            assertEquals(null, response)
        }

        @Test
        fun `valida chave Email deve retornar um ErrorMessage quando for email nao for informado`() {
            val chave = ""
            val response = TipoChave.EMAIL.valida(chave)
            assertTrue(response is ErrorMessage)
        }

        @Test
        fun `valida chave Email deve retornar um ErrorMessage quando for email invalido`() {
            val email = "renato"
            val response = TipoChave.EMAIL.valida(email)
            assertTrue(response is ErrorMessage)
        }

        @Test
        fun `valida do tipo Email deve retornar null quando for email valido`() {
            val email = "renato@email.com"
            val result = TipoChave.EMAIL.valida(email)
            assertEquals(null, result)
        }


        @Test
        fun `valida do tipo Invalida deve retornar sempre um ErrorMessage`() {
            val chave = "1234566"
            val result = TipoChave.INVALIDA.valida(chave)
            assertEquals("Tipo de chave inválido", result?.description)
            assertEquals("Por favor insira um tipo de chave válido", result?.augmentDescription)
            assertTrue(result is ErrorMessage)
        }
    }
}
