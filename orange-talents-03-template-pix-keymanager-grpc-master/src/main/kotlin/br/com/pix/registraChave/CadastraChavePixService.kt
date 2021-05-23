package br.com.pix.registraChave

import br.com.pix.RegistroChaveRequest
import br.com.pix.compartilhado.chavePix.*
import br.com.pix.compartilhado.integracao.ErpClient
import br.com.pix.compartilhado.exception.PixExistenteException
import br.com.pix.compartilhado.integracao.BancoCentralClient
import br.com.pix.compartilhado.integracao.CreatePixKeyRequest.Companion.toRequest
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import io.netty.handler.codec.http.HttpResponseStatus.UNPROCESSABLE_ENTITY
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

@Validated
@Singleton
class CadastraChavePixService(
    private val bancoCentralClient: BancoCentralClient,
    private val erpClient: ErpClient,
    private val repository: ChavePixRepository
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(request: RegistroChaveRequest?): ChavePix {
        if (repository.existsByChave(request!!.chave)) throw PixExistenteException("Pix já existente no sistema")

        val contaResponse = erpClient.consulta(request.idCliente, requestParaTipoConta(request.tipoConta))
        val conta = contaResponse.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado")

        val chavePix = NovaChavePix(request, conta).toModel()
        repository.save(chavePix)

        logger.info("Registrando chave no banco central")
        try {
            val bancoCentralResponse = bancoCentralClient.cadastraChave(chavePix.toRequest()).body()
                ?: throw IllegalStateException("Erro ao realizar registro da chave pix no Banco Central")

            chavePix.atualizaChavePix(bancoCentralResponse.key!!)

            logger.info("Chave: ${chavePix.chave} salva no banco e registrada no Banco central")

            return chavePix

        } catch (e: HttpClientResponseException) {
            when {
                (e.status == UNPROCESSABLE_ENTITY) ->
                    throw PixExistenteException("Chave pix já existente no Banco Central")

                else ->
                    throw IllegalStateException("Erro ao realizar registro da chave pix no Banco Central")
            }
        }
    }
}

