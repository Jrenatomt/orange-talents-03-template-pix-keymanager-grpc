package br.com.pix.remocaoChavePix.validacao

import br.com.pix.RemocaoChaveRequest
import br.com.pix.validacao.ErrorMessage

fun RemocaoChaveRequest.valida(): ErrorMessage? {
    if(idPix.isNullOrBlank()) return ErrorMessage("Id do pix deve ser informado")
    if (idCliente.isNullOrBlank()) return ErrorMessage("Id do cliente deve ser informado")
    if (!idCliente.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"
            .toRegex())) return ErrorMessage("Id do cliente deve ter um formato UUID válido")

    return null
}