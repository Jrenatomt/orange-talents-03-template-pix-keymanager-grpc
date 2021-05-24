package br.com.pix.consultaPix

import br.com.pix.ConsultaChaveRequest
import br.com.pix.ConsultaChaveResponse
import br.com.pix.KeyManagerConsultaServiceGrpc
import br.com.pix.compartilhado.chavePix.ChavePixRepository
import br.com.pix.compartilhado.integracao.BancoCentralClient
import br.com.pix.compartilhado.utils.ConsultaChaveResponseConverter
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
class ConsultaGrpcEndpoint(private val repository: ChavePixRepository,
                           private val bancoCentralClient: BancoCentralClient,
                           private val validator: Validator
) : KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceImplBase() {

    override fun consultaChave(request: ConsultaChaveRequest, responseObserver: StreamObserver<ConsultaChaveResponse>) {

        val filtro = request.filtro(validator)
        val consulta = filtro.consulta(repository, bancoCentralClient)

        responseObserver.onNext(ConsultaChaveResponseConverter().converte(consulta))
        responseObserver.onCompleted()
    }
}