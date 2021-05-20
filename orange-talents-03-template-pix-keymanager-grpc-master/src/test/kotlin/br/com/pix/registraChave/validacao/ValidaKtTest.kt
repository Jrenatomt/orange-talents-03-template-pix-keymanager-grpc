package br.com.pix.registraChave.validacao

import br.com.pix.RegistroChaveRequest
import br.com.pix.validacao.ErrorMessage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ValidaKtTest{

    @Test
    fun `validaIdCliente deve retornar um ErrorMessage quando nao for um uuid valido`() {
        val uuid = ""
        val resultado = validaIdCliente(uuid)
        assertTrue(resultado is ErrorMessage)
    }

    @Test
    fun `validaIdCliente deve retornar null quando for um uuid valido`() {
    val uuid = "8d91cebf-c17b-4ba3-ac3e-d26dcfa7d041"
        val resultado = validaIdCliente(uuid)
        assertEquals(null, resultado?.description)
    }

    @Test
    fun `validaTipoConta deve retornar um ErrorMessage quando nao for um Tipo Conta valido`() {
        val result = validaTipoConta(RegistroChaveRequest.TipoConta.CONTA_DESCONHECIDA)
        assertEquals("Tipo de conta deve ser válida", result?.description)
        assertTrue(result is ErrorMessage)
    }

    @Test
    fun `validaTipoConta deve retornar um ErrorMessage quando for null`() {
        val result = validaTipoConta(null)
        assertEquals("Tipo de conta é obrigatório", result?.description)
        assertTrue(result is ErrorMessage)
    }

    @Test
    fun `validaTipoConta deve retornar null quando tipo conta valido`() {
        val tipoContaString ="CONTA_POUPANCA"
        val tipoConta = RegistroChaveRequest.TipoConta.valueOf(tipoContaString)
        val result = validaTipoConta(tipoConta)
        assertEquals(null, result?.description)
    }
    @Test
    fun `validaTipoChave deve retornar um ErrorMessage quando nao for um Tipo Chave valido`() {
        val result = validaTipoChave(RegistroChaveRequest.TipoChave.CHAVE_DESCONHECIDA)
        assertEquals("Tipo de chave deve ser válida", result?.description)
        assertTrue(result is ErrorMessage)
    }
    @Test
    fun `validaTipoChave deve retornar um ErrorMessage quando for null`() {
        val result = validaTipoChave(null)
        assertEquals("Tipo de chave é obrigatório", result?.description)
        assertTrue(result is ErrorMessage)
    }

    @Test
    fun `validaTipoChave deve retornar null quando tipo chave valido`() {
        val tipoChaveString = "CPF"
        val tipoChave = RegistroChaveRequest.TipoChave.valueOf(tipoChaveString)
        val result = validaTipoChave(tipoChave)
        assertEquals(null, result?.description)
    }

    @Test
    fun `validaRequest deve retornar null quando request for valido`() {
        val request = RegistroChaveRequest.newBuilder()
            .setIdCliente("8d91cebf-c17b-4ba3-ac3e-d26dcfa7d041")
            .setTipoChave(RegistroChaveRequest.TipoChave.EMAIL)
            .setChave("teste@teste.com")
            .setTipoConta(RegistroChaveRequest.TipoConta.CONTA_POUPANCA)
            .build()
        val result = request.valida()
        assertEquals(null, result)
    }

    @Test
    fun `validaRequest deve retornar ErrorResponse quando request for invalido`() {
        val request = RegistroChaveRequest.newBuilder()
            .setIdCliente("idCliente")
            .setTipoChave(RegistroChaveRequest.TipoChave.valueOf("CPF"))
            .setChave("chave")
            .setTipoConta(RegistroChaveRequest.TipoConta.valueOf("CONTA_CORRENTE"))
            .build()
        val result = request.valida()
        assertTrue(result is ErrorMessage)
    }
}