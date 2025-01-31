package br.com.pix.compartilhado.chavePix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {

    fun existsByIdCliente(idCliente: UUID): Boolean

    fun existsByChave(chave: String): Boolean

    fun findByIdAndIdCliente(id: UUID, idCliente: UUID): ChavePix?

    fun findByChave(chave: String): Optional<ChavePix>

    fun findAllByIdCliente(idCliente: UUID): List<ChavePix>
}