package br.com.pix.registraChave

import br.com.pix.RegistroChaveRequest
import br.com.pix.integracao.ErpClient
import br.com.pix.registraChave.exception.PixExistenteException
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class CadastraChavePixService(private val erpClient: ErpClient, private val repository: ChavePixRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun registra(request: RegistroChaveRequest?): ChavePix {
        if (repository.existsByChave(request!!.chave)) throw PixExistenteException("Pix já existente no sistema")

        val tipoConta = requestParaTipoConta(request.tipoConta)
        val contaResponse = erpClient.consulta(request.idCliente, tipoConta)
        val conta = contaResponse.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado")

        val chavePix = NovaChavePix(request.idCliente, tipoConta, request.chave, requestParaTipoChave(request.tipoChave), conta).toModel()
        repository.save(chavePix)
        logger.info("Chave salva no banco")

        return chavePix
    }
}
