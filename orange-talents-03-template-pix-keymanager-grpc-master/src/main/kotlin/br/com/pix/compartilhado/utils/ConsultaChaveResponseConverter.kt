package br.com.pix.compartilhado.utils

import br.com.pix.ConsultaChaveResponse
import br.com.pix.TipoChave
import br.com.pix.TipoConta
import br.com.pix.consultaPix.DetalhesChavePix
import com.google.protobuf.Timestamp
import java.time.ZoneId

class ConsultaChaveResponseConverter {

    fun converte(consulta: DetalhesChavePix): ConsultaChaveResponse =
        ConsultaChaveResponse.newBuilder()
            .setIdPix(consulta.idPix)
            .setIdClient(consulta.idCliente)
            .setChave(ConsultaChaveResponse.ChavePixInfo.newBuilder()
                .setTipoChave(TipoChave.valueOf(consulta.tipoChave.toString()))
                .setChavePix(consulta.chavePix)
                .setConta(ConsultaChaveResponse.ChavePixInfo.Conta.newBuilder()
                    .setInstituicao(consulta.conta.instituicaoNome)
                    .setNomeTitular(consulta.conta.nomeTitular)
                    .setCpfTitular(consulta.conta.cpfTitular)
                    .setTipoConta(TipoConta.valueOf(consulta.tipoConta.toString()))
                    .setAgencia(consulta.conta.agencia)
                    .setNumero(consulta.conta.numero)
                )
                .setCriadoEm(consulta.criadoEm.let {
                    val instantCriadoEm = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(instantCriadoEm.epochSecond)
                        .setNanos(instantCriadoEm.nano)
                        .build()
                })
            )
            .build()
}