package br.com.pix.compartilhado.chavePix

import br.com.pix.registraChave.ContaUsuario
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotBlank val idCliente: UUID,
    @field:NotNull @Enumerated(EnumType.STRING) @Column(nullable = false) val tipoConta: TipoConta,
    @field:NotBlank val chave: String,
    @field:NotNull @Enumerated(EnumType.STRING) @Column(nullable = false) val tipoChavePix: TipoChave,
    @field:NotNull @Embedded val conta: ContaUsuario
) {
    @Id
    @GeneratedValue
    var id: UUID? = null

    @Column(updatable = false, nullable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now()
}