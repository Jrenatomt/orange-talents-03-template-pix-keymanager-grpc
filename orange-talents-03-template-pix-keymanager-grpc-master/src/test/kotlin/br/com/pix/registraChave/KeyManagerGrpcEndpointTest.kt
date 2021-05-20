package br.com.pix.registraChave

import br.com.pix.KeyManagerServiceGrpc
import br.com.pix.RegistroChaveRequest
import br.com.pix.compartilhado.chavePix.ChavePix
import br.com.pix.compartilhado.chavePix.ChavePixRepository
import br.com.pix.compartilhado.chavePix.TipoChave
import br.com.pix.compartilhado.chavePix.TipoConta
import br.com.pix.compartilhado.integracao.ErpClient
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
import java.util.*
import javax.inject.Inject


@MicronautTest(transactional = false)
internal class KeyManagerGrpcEndpointTest(
    private val repository: ChavePixRepository,
    val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
) {
    @Inject
    lateinit var erpClient: ErpClient

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
        val titularResponse = TitularResponse("renato", "35122922080")
        val instituicaoResponse = InstituicaoResponse("Itau", "60701190")
        val contaUsuarioResponse = ContaUsuarioResponse(titularResponse, instituicaoResponse, "0001", "084329")
        `when`(erpClient.consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.ok(contaUsuarioResponse))

        //Ação
        val response = grpcClient.cadastroChavePix(
            RegistroChaveRequest.newBuilder()
                .setIdCliente(CLIENTE_ID.toString())
                .setTipoChave(RegistroChaveRequest.TipoChave.CPF)
                .setChave("35122922080")
                .setTipoConta(RegistroChaveRequest.TipoConta.CONTA_CORRENTE)
                .build()
        )

        //verificação
        with(response) {
            assertEquals("35122922080", chavePix)
            assertNotNull(idPix)
            assertNotNull(repository.findById(UUID.fromString(response.idPix)))
        }
    }

    @Test
    fun `nao deve registrar chave pix quando chave existir`() {
        //Cenario
        val chave = "45723279041"
        val chavePix  = ChavePix(CLIENTE_ID, TipoConta.CONTA_CORRENTE,"45723279041", TipoChave.CPF,
            ContaUsuario("ITAÚ UNIBANCO S.A.", "60701190",
                "renato", "45723279041", "0001", "291900"))
        repository.save(chavePix)

        //Ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastroChavePix(RegistroChaveRequest.newBuilder()
                .setIdCliente(CLIENTE_ID.toString())
                .setTipoChave(RegistroChaveRequest.TipoChave.CPF)
                .setChave(chave)
                .setTipoConta(RegistroChaveRequest.TipoConta.CONTA_CORRENTE)
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
            grpcClient.cadastroChavePix(RegistroChaveRequest.newBuilder()
                .setIdCliente(CLIENTE_ID.toString())
                .setTipoChave(RegistroChaveRequest.TipoChave.CPF)
                .setChave(chavePix)
                .setTipoConta(RegistroChaveRequest.TipoConta.CONTA_CORRENTE)
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
            grpcClient.cadastroChavePix(RegistroChaveRequest.newBuilder()
                .setIdCliente(CLIENTE_ID.toString())
                .setTipoChave(RegistroChaveRequest.TipoChave.CPF)
                .setChave("22847450084")
                .setTipoConta(RegistroChaveRequest.TipoConta.CONTA_CORRENTE)
                .build()
            )
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertNotNull(status.description)
        }
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