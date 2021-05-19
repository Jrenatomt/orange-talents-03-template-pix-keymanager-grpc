package br.com.pix.registraChave

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
        return ChavePix(idCliente, tipoConta, chave, tipoChave, conta)
    }

    init {
        if (tipoChave == TipoChave.ALEATORIA) chave = UUID.randomUUID().toString()
    }
}