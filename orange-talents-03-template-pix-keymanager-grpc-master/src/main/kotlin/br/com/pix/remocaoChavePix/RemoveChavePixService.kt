package br.com.pix.remocaoChavePix

import br.com.pix.compartilhado.chavePix.ChavePixRepository
import br.com.pix.compartilhado.exception.ChavePixInexistenteException
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

@Singleton
class RemoveChavePixService(private val repository: ChavePixRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun remove(idChavePix: UUID, idCliente: UUID) {
        val chavePix = repository.findByIdAndIdCliente(idChavePix, idCliente)
            ?: throw ChavePixInexistenteException("Chave pix não encontrada ou não pertencente ao cliente informado!")

        repository.deleteById(chavePix.id!!)
        logger.info("Chave deletada com sucesso!")
    }
}