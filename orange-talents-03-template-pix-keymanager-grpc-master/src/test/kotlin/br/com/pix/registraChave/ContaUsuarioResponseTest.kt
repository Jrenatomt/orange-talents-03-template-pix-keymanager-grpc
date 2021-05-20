package br.com.pix.registraChave

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ContaUsuarioResponseTest{

    @Test
    fun `deve retornar ContaUsuarioResponse para classe modelo ContaUsuario`() {

        val titularResponse = TitularResponse("renato", "35122922080")
        val instituicaoResponse = InstituicaoResponse("Itau", "60701190")
        val contaUsuarioResponse = ContaUsuarioResponse(titularResponse, instituicaoResponse, "001", "002")

        val response = contaUsuarioResponse.toModel()

        assertEquals(titularResponse.nome, response.nomeTitular)
        assertEquals(titularResponse.cpf, response.cpfTitular)
        assertEquals(instituicaoResponse.nome, response.instituicaoNome)
        assertEquals(instituicaoResponse.ispb, response.instituicaoIspb)
        assertEquals("001", response.agencia)
        assertEquals("002", response.numero)
    }
}
