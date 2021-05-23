package br.com.pix.remocaoChavePix

import br.com.pix.compartilhado.chavePix.ChavePixRepository
import br.com.pix.compartilhado.exception.ChavePixInexistenteException
import br.com.pix.compartilhado.integracao.BancoCentralClient
import br.com.pix.compartilhado.integracao.DeletePixKeyRequest.Companion.toRequest
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

@Singleton
class RemoveChavePixService(private val bancoCentralClient: BancoCentralClient,
                            private val repository: ChavePixRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun remove(idChavePix: UUID, idCliente: UUID) {
        val chavePix = repository.findByIdAndIdCliente(idChavePix, idCliente)
            ?: throw ChavePixInexistenteException("Chave pix não encontrada ou não pertencente ao cliente informado!")

        repository.deleteById(chavePix.id!!)

        val bcbResponse = bancoCentralClient.removeChave(chave = chavePix.chave, remocao = chavePix.toRequest())

        if(bcbResponse.status != HttpStatus.OK) throw IllegalStateException("Erro ao remover a chave no banco central")

        logger.info("Chave deletada com sucesso!")
    }
}