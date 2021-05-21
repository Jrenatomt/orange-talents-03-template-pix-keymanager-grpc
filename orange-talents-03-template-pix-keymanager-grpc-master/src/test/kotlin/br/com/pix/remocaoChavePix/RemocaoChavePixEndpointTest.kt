package br.com.pix.remocaoChavePix

import br.com.pix.KeyManagerRemoveServiceGrpc
import br.com.pix.RemocaoChaveRequest
import br.com.pix.compartilhado.chavePix.ChavePix
import br.com.pix.compartilhado.chavePix.ChavePixRepository
import br.com.pix.compartilhado.chavePix.TipoChave
import br.com.pix.compartilhado.chavePix.TipoConta
import br.com.pix.registraChave.ContaUsuario
import br.com.pix.registraChave.KeyManagerGrpcEndpointTest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class RemocaoChavePixEndpointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub
) {
    companion object {
        var idPix: UUID? = null
        var idCliente: UUID? = null
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()

        val chavePixModel = chavePixModel()
        repository.save(chavePixModel)
        idPix = chavePixModel.id
        idCliente = chavePixModel.idCliente
    }

    @Test
    fun `deve remover chave pix quando dados validos`() {
        grpcClient.removerChavePix(remocaoChaveRequest(idPix = idPix.toString(), idCliente = idCliente.toString()))
        assertNull(repository.findByIdAndIdCliente(idPix!!, idCliente!!))
    }

    @Test
    fun `nao deve remover chave pix com dados invalidos`() {
        val idClienteRequest = "c56dfef4-7901-44fb-84e2"

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.removerChavePix(remocaoChaveRequest(idPix = idPix.toString(), idCliente = idClienteRequest)
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertNotNull(status.description)
            assertNotNull(repository.findById(idPix!!))
        }
    }

    @Test
    fun `nao deve remover chave quando id da chave pix nao for encontrado`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.removerChavePix(remocaoChaveRequest("c56dfef4-7901-44fb-84e2-a2cefb157890"))
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertencente ao cliente informado!", status.description)
            assertNotNull(repository.findById(idPix!!))
        }
    }

    @Test
    fun `nao deve remover chave pix quando id do cliente nao pertencer ao cliente`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.removerChavePix(remocaoChaveRequest(idPix.toString(),"c56dfef4-7902-44fb-84e2-a2cefb157890"))
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertencente ao cliente informado!", status.description)
            assertNotNull(repository.findById(idPix!!))
        }
    }

    private fun chavePixModel(): ChavePix {
        return ChavePix(KeyManagerGrpcEndpointTest.CLIENTE_ID,
            TipoConta.CONTA_CORRENTE,
            "96498610093",
            TipoChave.CPF,
            ContaUsuario("ITAÚ",
                "60701190",
                "Renato Marques",
                "96498610093",
                "0001",
                "291900")
        )
    }

    private fun remocaoChaveRequest(idPix: String,
                                    idCliente: String = "c56dfef4-7901-44fb-84e2-a2cefb157890"): RemocaoChaveRequest {
        return RemocaoChaveRequest.newBuilder()
            .setIdPix(idPix)
            .setIdCliente(idCliente)
            .build()
    }

    @Factory
    class GrpcClient {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub {
            return KeyManagerRemoveServiceGrpc.newBlockingStub(channel)
        }
    }
}
