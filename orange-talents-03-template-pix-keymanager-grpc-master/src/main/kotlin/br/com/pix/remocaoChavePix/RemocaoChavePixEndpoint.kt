package br.com.pix.remocaoChavePix

import br.com.pix.validacao.errorResponse
import br.com.pix.KeyManagerRemoveServiceGrpc
import br.com.pix.RemocaoChaveRequest
import br.com.pix.RemocaoChaveResponse
import br.com.pix.remocaoChavePix.validacao.valida
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Singleton

@Singleton
class RemocaoChavePixEndpoint(private val removeService: RemoveChavePixService) :
    KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceImplBase() {

    override fun removerChavePix(
        request: RemocaoChaveRequest?,
        responseObserver: StreamObserver<RemocaoChaveResponse>?
    ) {

        val possivelErroValidacao = request?.valida()
        possivelErroValidacao?.let {
            responseObserver?.errorResponse(Status.INVALID_ARGUMENT, it)
            return
        }

        val idChavePix = UUID.fromString(request!!.idPix)
        val idCliente = UUID.fromString(request.idCliente)

        removeService.remove(idChavePix, idCliente)

        responseObserver!!.onNext(
            RemocaoChaveResponse.newBuilder()
                .setIdPix(idChavePix.toString())
                .setIdCliente(idCliente.toString())
                .build()
        )
        responseObserver.onCompleted()
    }
}

