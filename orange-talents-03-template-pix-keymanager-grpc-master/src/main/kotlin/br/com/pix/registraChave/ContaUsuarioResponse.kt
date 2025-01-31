package br.com.pix.registraChave

import javax.validation.constraints.NotBlank

data class ContaUsuarioResponse(
    @field:NotBlank val titular: TitularResponse,
    @field:NotBlank val instituicao: InstituicaoResponse,
    @field:NotBlank val agencia: String,
    @field:NotBlank val numero: String,
) {
    fun toModel(): ContaUsuario {
        return ContaUsuario(
            instituicaoNome = this.instituicao.nome,
            instituicaoIspb = this.instituicao.ispb,
            nomeTitular = this.titular.nome,
            cpfTitular = this.titular.cpf,
            agencia = this.agencia,
            numero = this.numero
        )
    }
}
data class TitularResponse(
    @field:NotBlank val nome: String,
    @field:NotBlank val cpf: String
)

data class InstituicaoResponse(
    @field:NotBlank val nome: String,
    @field:NotBlank val ispb: String
)
