package br.com.pix.compartilhado.integracao

import br.com.pix.compartilhado.chavePix.TipoConta
import br.com.pix.registraChave.ContaUsuarioResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.erp.url}")
interface ErpClient {

    @Get("/{clientId}/contas")
    fun consulta(@QueryValue clientId: String, @QueryValue tipo: TipoConta): HttpResponse<ContaUsuarioResponse>
}
