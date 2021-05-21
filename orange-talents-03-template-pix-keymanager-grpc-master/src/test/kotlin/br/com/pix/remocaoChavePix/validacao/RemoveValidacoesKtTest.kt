package br.com.pix.remocaoChavePix.validacao

import br.com.pix.RemocaoChaveRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class RemoveValidacoesKtTest {

    @Test
    fun `valida deve retornar null quando tudo Ok`() {
        val request = remocaoChaveRequest()
        val resultado = request.valida()
        assertNull(resultado)
    }

    @Test
    fun `valida deve retornar ErrorMessage se pix for branco`() {
        val request = remocaoChaveRequest(idPix = "")
        val resultado = request.valida()
        assertEquals("Id do pix deve ser informado", resultado!!.description)
    }

    @Test
    fun `valida deve retornar ErrorMessage se idCliente for branco`() {
        val request = remocaoChaveRequest(idCliente = "")
        val resultado = request.valida()
        assertEquals("Id do cliente deve ser informado", resultado!!.description)
    }


    @Test
    fun `valida deve retornar ErrorMessage se idCliente for formato diferente de UUID`() {
        val idCliente = "c56dfef4-7901"
        val request = remocaoChaveRequest(idCliente = idCliente)

        val result = request.valida()

        assertEquals("Id do cliente deve ter um formato UUID válido", result!!.description)
    }

    @Test
    fun `valida deve retornar ErrorMessage se idPix for formato diferente de UUID`() {
        val idPix = "324aa21c-3d23"
        val request = remocaoChaveRequest(idPix = idPix)
        val result = request.valida()
        assertEquals("Id do pix deve ter um formato UUID válido", result!!.description)
    }

    private fun remocaoChaveRequest(
        idPix: String = "324aa21c-3d23-472d-b1ff-7d855ede51fd",
        idCliente: String = "c56dfef4-7901-44fb-84e2-a2cefb157890"
    ): RemocaoChaveRequest {
        return RemocaoChaveRequest.newBuilder()
            .setIdPix(idPix)
            .setIdCliente(idCliente)
            .build()
    }
}