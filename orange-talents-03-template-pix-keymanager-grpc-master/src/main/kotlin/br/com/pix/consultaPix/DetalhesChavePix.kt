package br.com.pix.consultaPix

import br.com.pix.compartilhado.chavePix.ChavePix
import br.com.pix.compartilhado.chavePix.TipoChave
import br.com.pix.compartilhado.chavePix.TipoConta
import br.com.pix.registraChave.ContaUsuario
import java.time.LocalDateTime

class DetalhesChavePix(val idPix: String? = "",
                       val idCliente: String? = "",
                       val tipoChave: TipoChave?,
                       val chavePix: String,
                       val tipoConta: TipoConta,
                       val conta: ContaUsuario,
                       val criadoEm: LocalDateTime
) {

    constructor(entidade: ChavePix): this(
        idPix = entidade.id.toString(),
        idCliente = entidade.idCliente.toString(),
        tipoChave = entidade.tipoChavePix,
        chavePix = entidade.chave,
        tipoConta = entidade.tipoConta,
        conta = entidade.conta,
        criadoEm = entidade.criadoEm,
    )

}