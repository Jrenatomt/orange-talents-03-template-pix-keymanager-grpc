package br.com.pix.registraChave

import br.com.pix.TipoConta as TipoContaGrpc
import br.com.pix.TipoChave as TipoChaveGrpc
import br.com.pix.KeyManagerServiceGrpc
import br.com.pix.RegistroChaveRequest
import br.com.pix.compartilhado.chavePix.ChavePix
import br.com.pix.compartilhado.chavePix.ChavePixRepository
import br.com.pix.compartilhado.chavePix.TipoChave
import br.com.pix.compartilhado.chavePix.TipoConta
import br.com.pix.compartilhado.integracao.*
import br.com.pix.compartilhado.integracao.CreatePixKeyRequest.Companion.toRequest
import br.com.pix.registraChave.utils.MockitoHelper
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.util.*
import javax.inject.Inject


@MicronautTest(transactional = false)
internal class KeyManagerGrpcEndpointTest(
    private val repository: ChavePixRepository,
    val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
) {
    @Inject
    lateinit var erpClient: ErpClient

    @Inject
    lateinit var bancoCentralClient: BancoCentralClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar uma nova chave pix`() {
        //Cenario
        `when`(erpClient.consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.ok(contaUsuarioResponse()))

        `when`(bancoCentralClient.cadastraChave(chavePixModel("35122922080", "CPF").toRequest()))
            .thenReturn(HttpResponse.ok(createPixKeyResponse()))

        //Ação
        val response = grpcClient.cadastroChavePix(
            RegistroChaveRequest.newBuilder()
                .setIdCliente(CLIENTE_ID.toString())
                .setTipoChave(TipoChaveGrpc.CPF)
                .setChave("35122922080")
                .setTipoConta(TipoContaGrpc.CONTA_CORRENTE)
                .build()
        )

        //verificação
        with(response) {
            verify(erpClient, times(1)).consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE)
            verify(bancoCentralClient, times(1)).cadastraChave(chavePixModel("35122922080", "CPF").toRequest())
            assertEquals("35122922080", chavePix)
            assertNotNull(idPix)
            assertNotNull(repository.findById(UUID.fromString(response.idPix)))
        }
    }

    @Test
    fun `nao deve registrar chave pix quando chave existir`() {
        //Cenario
        val chave = "45723279041"
        val chavePix = ChavePix(
            CLIENTE_ID, TipoConta.CONTA_CORRENTE, "45723279041", TipoChave.CPF,
            ContaUsuario(
                "ITAÚ UNIBANCO S.A.", "60701190",
                "renato", "45723279041", "0001", "291900"
            )
        )
        repository.save(chavePix)

        //Ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastroChavePix(
                RegistroChaveRequest.newBuilder()
                    .setIdCliente(CLIENTE_ID.toString())
                    .setTipoChave(TipoChaveGrpc.CPF)
                    .setChave(chave)
                    .setTipoConta(TipoContaGrpc.CONTA_CORRENTE)
                    .build()
            )
        }

        //verificação
        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertNotNull(status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando os parametros forem invalidos`() {
        val chavePix = "jrenatomt@gmail.com"

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastroChavePix(
                RegistroChaveRequest.newBuilder()
                    .setIdCliente(CLIENTE_ID.toString())
                    .setTipoChave(TipoChaveGrpc.CPF)
                    .setChave(chavePix)
                    .setTipoConta(TipoContaGrpc.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertNotNull(status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando nao encontrar os dados do cliente`() {
        `when`(erpClient.consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastroChavePix(
                RegistroChaveRequest.newBuilder()
                    .setIdCliente(CLIENTE_ID.toString())
                    .setTipoChave(TipoChaveGrpc.CPF)
                    .setChave("22847450084")
                    .setTipoConta(TipoContaGrpc.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertNotNull(status.description)
        }
    }

    @Test
    fun `deve gerar chave aletoria pelo sistema do Banco central`() {
        val chaveBcb = UUID.randomUUID()

        `when`(erpClient.consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.ok(contaUsuarioResponse()))

        `when`(bancoCentralClient.cadastraChave(chavePixModel(MockitoHelper.anyObject()).toRequest()))
            .thenReturn(HttpResponse.ok(createPixKeyResponse("RANDOM", chaveBcb.toString())))

        val result = grpcClient.cadastroChavePix(RegistroChaveRequest.newBuilder()
            .setIdCliente(CLIENTE_ID.toString())
            .setTipoChave(TipoChaveGrpc.ALEATORIA)
            .setTipoConta(TipoContaGrpc.CONTA_CORRENTE)
            .build()
        )

        with(result) {
            verify(erpClient, times(1)).consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE)
            verify(bancoCentralClient, times(1)).cadastraChave(chavePixModel(MockitoHelper.anyObject()).toRequest())
            assertEquals(chaveBcb.toString(), chavePix)
            assertNotNull(idPix)
            assertNotNull(repository.findById(chaveBcb))
        }
    }

    @Test
    fun `nao deve registrar chave e lancar INTERNAL quando der algum erro no Banco Central`() {
        val chave = "96498610093"

        `when`(erpClient.consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.ok(contaUsuarioResponse()))

        `when`(bancoCentralClient.cadastraChave(chavePixModel(chave, "CPF").toRequest()))
            .thenThrow(HttpClientResponseException("Erro", HttpResponse.serverError<Any>()))

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastroChavePix(RegistroChaveRequest.newBuilder()
                .setIdCliente(CLIENTE_ID.toString())
                .setTipoChave(TipoChaveGrpc.CPF)
                .setChave(chave)
                .setTipoConta(TipoContaGrpc.CONTA_CORRENTE)
                .build()
            )
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertNotNull(status.description)
        }
    }

    private fun contaUsuarioResponse(): ContaUsuarioResponse {
        return ContaUsuarioResponse(
            TitularResponse("renato", "35122922080"),
            InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190"),
            "0001",
            "291900"
        )
    }


    private fun chavePixModel(chave: String?, tipoChave: String = "ALEATORIA"): ChavePix {
        return ChavePix(
            CLIENTE_ID,
            TipoConta.CONTA_CORRENTE,
            chave ?: UUID.randomUUID().toString(),
            TipoChave.valueOf(tipoChave),
            ContaUsuario(
                "ITAÚ UNIBANCO S.A.",
                "60701190",
                "renato",
                "35122922080",
                "0001",
                "291900"
            )
        )
    }

    private fun createPixKeyResponse(keyType: String = "CPF", key: String = "35122922080"): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = KeyType.valueOf(keyType),
            key = key,
            bankAccount = BankAccount(
                participant = "60701190",
                branch = "0001",
                accountType = BankAccount.AccountType.CACC,
                accountNumber = "291900",
            ),
            owner = Owner(
                type = Owner.Type.NATURAL_PERSON,
                name = "renato",
                taxIdNumber = "35122922080"
            )
        )
    }

    @MockBean(BancoCentralClient::class)
    fun bancoCentralClient(): BancoCentralClient? {
        return mock(BancoCentralClient::class.java)
    }

    @MockBean(ErpClient::class)
    fun erpClient(): ErpClient? {
        return Mockito.mock(ErpClient::class.java)
    }

    @Factory
    class GrpcClient {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerServiceGrpc.KeyManagerServiceBlockingStub? {
            return KeyManagerServiceGrpc.newBlockingStub(channel)
        }
    }
}