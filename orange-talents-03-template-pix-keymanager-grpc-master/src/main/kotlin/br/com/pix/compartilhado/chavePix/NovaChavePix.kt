package br.com.pix.compartilhado.chavePix

import br.com.pix.RegistroChaveRequest
import br.com.pix.registraChave.ContaUsuario
import io.micronaut.core.annotation.Introspected
import org.hibernate.validator.constraints.Length
import java.util.*
import javax.persistence.Embedded
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
class NovaChavePix(
    @field:NotBlank val idCliente: String,
    @field:NotNull val tipoConta: TipoConta,
    @field:Length(max = 77) var chave: String,
    @field:NotNull val tipoChave: TipoChave,
    @field:NotNull @Embedded val conta: ContaUsuario
) {
    fun toModel(): ChavePix {
        return ChavePix(UUID.fromString(idCliente), tipoConta, chave, tipoChave, conta)
    }

    constructor(request: RegistroChaveRequest?, conta: ContaUsuario) :
            this(
                idCliente = request!!.idCliente,
                tipoConta = requestParaTipoConta(request.tipoConta),
                chave = request.chave,
                tipoChave = requestParaTipoChave(request.tipoChave),
                conta = conta
            )

    init {
        if (tipoChave == TipoChave.ALEATORIA) chave = UUID.randomUUID().toString()
    }
}