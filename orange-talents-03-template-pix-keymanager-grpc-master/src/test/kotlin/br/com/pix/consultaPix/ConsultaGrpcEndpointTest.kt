package br.com.pix.consultaPix

import br.com.pix.ConsultaChaveRequest
import br.com.pix.KeyManagerConsultaServiceGrpc
import br.com.pix.compartilhado.chavePix.ChavePix
import br.com.pix.compartilhado.chavePix.ChavePixRepository
import br.com.pix.compartilhado.chavePix.TipoChave
import br.com.pix.compartilhado.chavePix.TipoConta
import br.com.pix.compartilhado.integracao.*
import br.com.pix.registraChave.ContaUsuario
import br.com.pix.registraChave.KeyManagerGrpcEndpointTest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ConsultaGrpcEndpointTest(private val repository: ChavePixRepository,
                                        private val grpcClient: KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceBlockingStub) {

    @Inject
    lateinit var bancoCentralClient: BancoCentralClient

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve consultar chave pix localmente quando informao idCliente e idPix`() {
        val chavePix = chavePixModel()
        repository.save(chavePix)

        val result = grpcClient.consultaChave(
            ConsultaChaveRequest.newBuilder()
            .setPixEClienteId(ConsultaChaveRequest.ConsultaPorPixEClienteId.newBuilder()
                .setIdCliente(chavePix.idCliente.toString())
                .setIdPix(chavePix.id.toString())
            )
            .build())

        with(result) {
            assertEquals(chavePix.id.toString(), result.idPix)
            assertEquals(chavePix.tipoChavePix.toString(), chave.tipoChave.toString())
            assertEquals(chavePix.chave, chave.chavePix)
            assertEquals(chavePix.idCliente.toString(), idClient)
            assertEquals(chavePix.tipoConta.toString(), chave.conta.tipoConta.toString())
            assertEquals(chavePix.conta.nomeTitular, chave.conta.nomeTitular)
            assertEquals(chavePix.conta.cpfTitular, chave.conta.cpfTitular)
            assertEquals(chavePix.conta.agencia, chave.conta.agencia)
            assertEquals(chavePix.conta.numero, chave.conta.numero)
            assertEquals(chavePix.conta.instituicaoNome, chave.conta.instituicao)
            assertNotNull(result.chave.criadoEm)
        }
    }

    @Test
    fun `deve consultar chave pix localmente quando informado a chave`() {
        val chavePix = chavePixModel()
        repository.save(chavePix)

        val result = grpcClient.consultaChave(ConsultaChaveRequest.newBuilder()
            .setChavePix(chavePix.chave)
            .build())

        with(result) {
            assertEquals(chavePix.id.toString(), result.idPix)
            assertEquals(chavePix.tipoChavePix.toString(), chave.tipoChave.toString())
            assertEquals(chavePix.chave, chave.chavePix)
            assertEquals(chavePix.idCliente.toString(), idClient)
            assertEquals(chavePix.tipoConta.toString(), chave.conta.tipoConta.toString())
            assertEquals(chavePix.conta.nomeTitular, chave.conta.nomeTitular)
            assertEquals(chavePix.conta.cpfTitular, chave.conta.cpfTitular)
            assertEquals(chavePix.conta.agencia, chave.conta.agencia)
            assertEquals(chavePix.conta.numero, chave.conta.numero)
            assertEquals(chavePix.conta.instituicaoNome, chave.conta.instituicao)
            assertNotNull(result.chave.criadoEm)
        }
    }

    @Test
    fun `deve consultar chave pix no bcb quando for passado apenas ela e nao existir no banco`() {
        val chavePix = chavePixModel()

        `when`(bancoCentralClient.consultaChave(chavePix.chave))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        val result = grpcClient.consultaChave(ConsultaChaveRequest.newBuilder()
            .setChavePix(chavePix.chave)
            .build())

        with(result) {
            assertEquals(chavePix.tipoChavePix.toString(), chave.tipoChave.toString())
            assertEquals(chavePix.chave, chave.chavePix)
            assertEquals(chavePix.tipoConta.toString(), chave.conta.tipoConta.toString())
            assertEquals(chavePix.conta.nomeTitular, chave.conta.nomeTitular)
            assertEquals(chavePix.conta.cpfTitular, chave.conta.cpfTitular)
            assertEquals(chavePix.conta.agencia, chave.conta.agencia)
            assertEquals(chavePix.conta.numero, chave.conta.numero)
            assertEquals(chavePix.conta.instituicaoNome, chave.conta.instituicao)
            assertNotNull(result.chave.criadoEm)
        }
    }

    @Test
    fun `deve retornar NOT_FOUND quando for passado chave pix que nao existi`() {
        `when`(bancoCentralClient.consultaChave("1234567890"))
            .thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChave(ConsultaChaveRequest.newBuilder()
                .setChavePix("1234567890")
                .build())
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertNotNull(status.description)
        }
    }

    @Test
    fun `deve retornar NOT_FOUND quando for passado idCliente e idPix que nao existir local`() {
        val idInexistente = UUID.randomUUID().toString()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChave(ConsultaChaveRequest.newBuilder()
                .setPixEClienteId(ConsultaChaveRequest.ConsultaPorPixEClienteId.newBuilder()
                    .setIdPix(idInexistente)
                    .setIdCliente(idInexistente)
                )
                .build())
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertNotNull(status.description)
        }
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando filtro for invalido`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChave(ConsultaChaveRequest.newBuilder().build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertNotNull(status.description)
        }
    }


    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            KeyType.CPF,
            "96498610093",
            BankAccount(
                "60701190",
                "0001",
                "291900",
                BankAccount.AccountType.CACC
            ),
            Owner(
                Owner.Type.NATURAL_PERSON,
                "Renato marques",
                "96498610093"
            ),
            createdAt = LocalDateTime.MIN
        )
    }

    private fun chavePixModel(): ChavePix {
        return ChavePix(
            KeyManagerGrpcEndpointTest.CLIENTE_ID,
            TipoConta.CONTA_CORRENTE,
            "96498610093",
            TipoChave.CPF,
            ContaUsuario("ITAÚ UNIBANCO S.A.",
                "60701190",
                "Renato marques",
                "96498610093",
                "0001",
                "291900")
        )
    }

    @MockBean(BancoCentralClient::class)
    fun bancoCentralClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class GrpcClient {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceBlockingStub {
            return KeyManagerConsultaServiceGrpc.newBlockingStub(channel)
        }
    }
}