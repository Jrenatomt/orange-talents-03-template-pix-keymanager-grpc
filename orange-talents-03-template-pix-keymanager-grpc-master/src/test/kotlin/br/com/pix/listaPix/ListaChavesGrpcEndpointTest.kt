package br.com.pix.listaPix

import br.com.pix.KeyManagerListaServiceGrpc
import br.com.pix.ListaChavesRequest
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
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class ListaChavesGrpcEndpointTest(private val repository: ChavePixRepository,
                                           private val grpcClient: KeyManagerListaServiceGrpc.KeyManagerListaServiceBlockingStub) {

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve listar todas as chaves quando for passado um id existente`() {
        val chavePix = chavePixModel()
        repository.save(chavePix)

        val result = grpcClient.listaChaves(ListaChavesRequest.newBuilder()
            .setIdCliente(chavePix.idCliente.toString())
            .build()
        )

        with(result) {
            assertEquals(chavePix.idCliente.toString(), idCliente)
            assertEquals(1, chavesPixCount)
            assertEquals(chavePix.id.toString(), chavesPixList[0].idPix)
            assertEquals(chavePix.tipoChavePix.name, chavesPixList[0].tipoChave.name)
            assertEquals(chavePix.tipoConta.name, chavesPixList[0].tipoConta.name)
            assertNotNull(chavesPixList[0].criadoEm)
        }
    }

    @Test
    fun `deve devolver lista de chaves vazia quando a chave nao for encontrada`() {
        val idInexistente = UUID.randomUUID().toString()

        val result = grpcClient.listaChaves(ListaChavesRequest.newBuilder()
            .setIdCliente(idInexistente)
            .build()
        )

        with(result) {
            assertEquals(idInexistente, idCliente)
            assertTrue(chavesPixList.isEmpty())
        }
    }

    @Test
    fun `deve devolver INVALID_ARGUMENT quando id do cliente informado for nulo ou vazio`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.listaChaves(
                ListaChavesRequest.newBuilder()
                .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Id cliente não pode ser nulo ou vazio!", status.description)
        }
    }

    private fun chavePixModel(): ChavePix {
        return ChavePix(
            KeyManagerGrpcEndpointTest.CLIENTE_ID,
            TipoConta.CONTA_CORRENTE,
            "96498610093",
            TipoChave.CPF,
            ContaUsuario("ITAÚ UNIBANCO S.A.",
                "60701190",
                "Renato Marques",
                "96498610093",
                "0001",
                "291900")
        )
    }

    @Factory
    class GrpcClient {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerListaServiceGrpc.KeyManagerListaServiceBlockingStub {
            return KeyManagerListaServiceGrpc.newBlockingStub(channel)
        }
    }
}