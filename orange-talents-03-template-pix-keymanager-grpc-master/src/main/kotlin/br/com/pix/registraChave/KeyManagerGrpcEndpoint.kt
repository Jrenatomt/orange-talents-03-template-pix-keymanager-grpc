package br.com.pix.registraChave

import br.com.pix.KeyManagerServiceGrpc
import br.com.pix.RegistroChaveRequest
import br.com.pix.RegistroChaveResponse
import br.com.pix.registraChave.exception.PixExistenteException
import br.com.pix.registraChave.validacao.validaRequest
import br.com.pix.validacao.ErrorMessage
import br.com.pix.validacao.errorResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class KeyManagerGrpcEndpoint(private val cadastraChavePixService: CadastraChavePixService) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {
    override fun cadastroChavePix(
        request: RegistroChaveRequest?,
        responseObserver: StreamObserver<RegistroChaveResponse>?
    ) {
        val possibleValidationError = validaRequest(request)
        possibleValidationError?.let {
            responseObserver?.errorResponse(Status.INVALID_ARGUMENT, it)
            return
        }

        try {
            val chavePix = cadastraChavePixService.registra(request)

            responseObserver!!.onNext(
                RegistroChaveResponse.newBuilder()
                    .setIdPix(chavePix.id.toString())
                    .setChavePix(chavePix.chave)
                    .build())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            when (e) {
                is PixExistenteException -> responseObserver?.errorResponse(Status.ALREADY_EXISTS, ErrorMessage(e.message))
                is java.lang.IllegalStateException -> responseObserver?.errorResponse(Status.INVALID_ARGUMENT, ErrorMessage(e.message))
                else -> responseObserver?.errorResponse(Status.INTERNAL, ErrorMessage(e.message))
            }
        }
    }
}