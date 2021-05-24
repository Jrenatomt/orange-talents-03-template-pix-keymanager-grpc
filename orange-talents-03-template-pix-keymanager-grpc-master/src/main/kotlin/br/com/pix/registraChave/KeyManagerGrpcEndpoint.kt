package br.com.pix.registraChave

import br.com.pix.KeyManagerServiceGrpc
import br.com.pix.RegistroChaveRequest
import br.com.pix.RegistroChaveResponse
import br.com.pix.compartilhado.exception.ErrorHandler
import br.com.pix.registraChave.validacao.valida
import br.com.pix.validacao.errorResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class KeyManagerGrpcEndpoint(private val cadastraChavePixService: CadastraChavePixService) :
    KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    override fun cadastroChavePix(
        request: RegistroChaveRequest?,
        responseObserver: StreamObserver<RegistroChaveResponse>?
    ) {
        val possibleValidationError = request.valida()
        possibleValidationError?.let {
            responseObserver?.errorResponse(Status.INVALID_ARGUMENT, it)
            return
        }


        val chavePix = cadastraChavePixService.registra(request)
        responseObserver!!.onNext(
            RegistroChaveResponse.newBuilder()
                .setIdPix(chavePix.id.toString())
                .setChavePix(chavePix.chave)
                .build()
        )
        responseObserver.onCompleted()

    }
}