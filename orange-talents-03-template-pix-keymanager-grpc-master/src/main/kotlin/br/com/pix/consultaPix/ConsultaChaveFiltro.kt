package br.com.pix.consultaPix

import br.com.pix.compartilhado.chavePix.ChavePixRepository
import br.com.pix.compartilhado.exception.ChavePixInexistenteException
import br.com.pix.compartilhado.integracao.BancoCentralClient
import br.com.pix.compartilhado.integracao.PixKeyDetailsResponse.Companion.paraDetalhesChavePix
import br.com.pix.compartilhado.utils.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class ConsultaChaveFiltro {

    abstract fun consulta(repository: ChavePixRepository, bancoCentralClient: BancoCentralClient): DetalhesChavePix

    @Introspected
    data class PorPixEClientId(@field:NotBlank @ValidUUID val idCliente: String,
                               @field:NotBlank @ValidUUID val idPix: String) : ConsultaChaveFiltro() {

        override fun consulta(repository: ChavePixRepository, bancoCentralClient: BancoCentralClient): DetalhesChavePix {
            val idPixUUID = UUID.fromString(idPix)
            val idClientUUID = UUID.fromString(idCliente)

            val chavePix = repository.findByIdAndIdCliente(idPixUUID, idClientUUID)
                ?: throw ChavePixInexistenteException("Chave pix não encontrada")

            return DetalhesChavePix(chavePix)
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @field:Size(max = 77) val chave: String) : ConsultaChaveFiltro() {

        private val logger = LoggerFactory.getLogger(this::class.java)

        override fun consulta(repository: ChavePixRepository, bancoCentralClient: BancoCentralClient): DetalhesChavePix {
            return repository.findByChave(chave).map {
                DetalhesChavePix(it)
            }.orElseGet {
                logger.info("Realizando consulta chave $chave no Banco Central")

                val response = bancoCentralClient.consultaChave(chave)
                when (response.status) {
                    HttpStatus.OK -> response.body()!!.paraDetalhesChavePix()
                    else -> throw ChavePixInexistenteException("Chave pix não encontrada")
                }
            }
        }
    }

    @Introspected
    class Invalido : ConsultaChaveFiltro() {
        override fun consulta(repository: ChavePixRepository, bancoCentralClient: BancoCentralClient): DetalhesChavePix {
            throw IllegalArgumentException("Chave pix inválida ou não informada")
        }
    }
}