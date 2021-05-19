package br.com.pix.integracao

import br.com.pix.registraChave.ContaUsuarioResponse
import br.com.pix.registraChave.TipoConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.erp.url}")
interface ErpClient {

    @Get("/{clientId}/contas?tipo={tipoConta}")
    fun consulta(@QueryValue clientId: String, @QueryValue tipoConta: TipoConta): HttpResponse<ContaUsuarioResponse>
}
