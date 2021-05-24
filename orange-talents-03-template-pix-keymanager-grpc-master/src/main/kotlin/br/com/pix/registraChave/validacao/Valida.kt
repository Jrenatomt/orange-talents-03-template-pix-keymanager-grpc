package br.com.pix.registraChave.validacao

import br.com.pix.TipoConta as TipoContaGrpc
import br.com.pix.TipoChave as TipoChaveGrpc
import br.com.pix.RegistroChaveRequest
import br.com.pix.compartilhado.chavePix.TipoChave
import br.com.pix.compartilhado.chavePix.TipoConta
import br.com.pix.compartilhado.chavePix.requestParaTipoChave
import br.com.pix.compartilhado.chavePix.requestParaTipoConta

import br.com.pix.validacao.ErrorMessage

fun RegistroChaveRequest?.valida(): ErrorMessage? {
    var possibleErrorMessage = validaIdCliente(this?.idCliente)
    possibleErrorMessage?.let {
        return it
    }

    possibleErrorMessage = validaTipoChave(this?.tipoChave)
    possibleErrorMessage?.let {
        return it
    }

    possibleErrorMessage = requestParaTipoChave(this?.tipoChave).valida(this?.chave)
    possibleErrorMessage?.let {
        return it
    }

    possibleErrorMessage = validaTipoConta(this?.tipoConta)
    possibleErrorMessage?.let {
        return it
    }

    return null
}

fun validaIdCliente(clientId: String?): ErrorMessage? {
    if (clientId.isNullOrBlank()) {
        return ErrorMessage(description = "Id do cliente é obrigatório")
    }

    if (!clientId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$".toRegex())) {
        return ErrorMessage(description = "Id do cliente deve conter um formato UUID válido")
    }

    clientId.let {
        return null
    }
}

fun validaTipoConta(requestTipoConta: TipoContaGrpc?): ErrorMessage? {
    if (requestTipoConta == null) {
        return ErrorMessage(description = "Tipo de conta é obrigatório")
    }

    if (requestParaTipoConta(requestTipoConta) == TipoConta.INVALIDA) {
        return ErrorMessage(description = "Tipo de conta deve ser válida")
    }

    return null
}

fun validaTipoChave(requestTipoChave: TipoChaveGrpc?): ErrorMessage? {
    if (requestTipoChave == null) {
        return ErrorMessage(description = "Tipo de chave é obrigatório")
    }
    if (requestParaTipoChave(requestTipoChave) == TipoChave.INVALIDA) {
        return ErrorMessage(description = "Tipo de chave deve ser válida")
    }

    return null
}
