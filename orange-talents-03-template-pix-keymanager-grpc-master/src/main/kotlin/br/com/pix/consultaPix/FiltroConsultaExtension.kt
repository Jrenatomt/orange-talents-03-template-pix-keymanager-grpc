package br.com.pix.consultaPix

import br.com.pix.ConsultaChaveRequest
import br.com.pix.ConsultaChaveRequest.FiltroCase.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ConsultaChaveRequest.filtro(validator: Validator): ConsultaChaveFiltro {
    val filtro = when (filtroCase) {
        PIXECLIENTEID -> {
            pixEClienteId.idPix.let {
                ConsultaChaveFiltro.PorPixEClientId(
                    idCliente = pixEClienteId.idCliente,
                    idPix = pixEClienteId.idPix
                )
            }
        }
        CHAVEPIX -> ConsultaChaveFiltro.PorChave(chavePix)
        FILTRO_NOT_SET -> ConsultaChaveFiltro.Invalido()
    }

    val possiveisErros = validator.validate(filtro)
    if (possiveisErros.isNotEmpty()) {
        throw ConstraintViolationException(possiveisErros)
    }

    return filtro
}