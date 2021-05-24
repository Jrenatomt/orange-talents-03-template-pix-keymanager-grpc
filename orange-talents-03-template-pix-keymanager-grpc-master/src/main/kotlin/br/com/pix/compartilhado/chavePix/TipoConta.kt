package br.com.pix.compartilhado.chavePix

import br.com.pix.TipoConta as TipoContaGrpc

enum class TipoConta {
    CONTA_CORRENTE, CONTA_POUPANCA, INVALIDA
}

fun requestParaTipoConta(tipo: TipoContaGrpc?): TipoConta {
    return when (tipo) {
        TipoContaGrpc.CONTA_CORRENTE -> TipoConta.CONTA_CORRENTE
        TipoContaGrpc.CONTA_POUPANCA -> TipoConta.CONTA_POUPANCA
        else -> TipoConta.INVALIDA
    }
}


