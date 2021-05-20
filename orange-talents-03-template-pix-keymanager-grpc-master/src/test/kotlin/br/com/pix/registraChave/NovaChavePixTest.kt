package br.com.pix.registraChave

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class NovaChavePixTest{

    @Test
    fun `deve retornar NovaChavePix para classe modelo ChavePix`() {

    }

    @Test
    fun `deve gerar chave uuid se for do tipo Aleatoria`() {
        val result = NovaChavePix(
            UUID.randomUUID().toString(),
            TipoConta.CONTA_POUPANCA,
            "",
            TipoChave.ALEATORIA,
            ContaUsuario("itau", "0001", "renato", "11111111111", "00000", "000")
        )

        assertNotEquals("", result.chave);
    }
}