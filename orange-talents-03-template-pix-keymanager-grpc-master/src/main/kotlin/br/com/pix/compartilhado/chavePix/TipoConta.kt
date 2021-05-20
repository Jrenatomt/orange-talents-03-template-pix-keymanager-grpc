package br.com.pix.compartilhado.chavePix

import br.com.pix.RegistroChaveRequest

enum class TipoConta {
    CONTA_CORRENTE, CONTA_POUPANCA, INVALIDA
}

fun requestParaTipoConta(tipo: RegistroChaveRequest.TipoConta?): TipoConta {
    return when (tipo) {
        RegistroChaveRequest.TipoConta.CONTA_CORRENTE -> TipoConta.CONTA_CORRENTE
        RegistroChaveRequest.TipoConta.CONTA_POUPANCA -> TipoConta.CONTA_POUPANCA
        else -> TipoConta.INVALIDA
    }
}


