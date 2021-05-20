package br.com.pix.registraChave

import br.com.pix.RegistroChaveRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TipoContaTest{


    @Test
    fun `requestParaTipoConta deve transformar TipoConta request para TipoConta model`() {
        val requestString = RegistroChaveRequest.TipoConta.CONTA_CORRENTE
        val resultadoEsperado = TipoConta.CONTA_CORRENTE

        val resultado = requestParaTipoConta(requestString)

        assertEquals(resultadoEsperado, resultado)
    }
}
