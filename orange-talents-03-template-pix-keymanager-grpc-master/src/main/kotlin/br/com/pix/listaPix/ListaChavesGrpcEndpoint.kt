package br.com.pix.listaPix

import br.com.pix.*
import br.com.pix.compartilhado.chavePix.ChavePixRepository
import br.com.pix.compartilhado.exception.ClienteNaoEncontradoException
import br.com.pix.compartilhado.exception.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavesGrpcEndpoint(private val repository: ChavePixRepository) :
    KeyManagerListaServiceGrpc.KeyManagerListaServiceImplBase() {

    override fun listaChaves(request: ListaChavesRequest, responseObserver: StreamObserver<ListaChavesResponse>) {

        if (request.idCliente.isNullOrBlank())
            throw IllegalArgumentException("Id cliente não pode ser nulo ou vazio!")

        val idCliente = UUID.fromString(request.idCliente)

        if (!repository.existsByIdCliente(idCliente))
            throw ClienteNaoEncontradoException("Cliente não encontrado!")

        val listaChavesPix = repository.findAllByIdCliente(idCliente).map {
            ListaChavesResponse.ChaveResponse.newBuilder()
                .setIdPix(it.id.toString())
                .setTipoChave(TipoChave.valueOf(it.tipoChavePix.name))
                .setChave(it.chave)
                .setTipoConta(TipoConta.valueOf(it.tipoConta.name))
                .setCriadoEm(it.criadoEm.let { criadoEm ->
                    val instantCriadoEm = criadoEm.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(instantCriadoEm.epochSecond)
                        .setNanos(instantCriadoEm.nano)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(ListaChavesResponse.newBuilder()
            .setIdCliente(idCliente.toString())
            .addAllChavesPix(listaChavesPix)
            .build()
        )

        responseObserver.onCompleted()
    }
}