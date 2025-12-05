package com.tuorg.veterinaria.configuracion.repository;

import com.tuorg.veterinaria.configuracion.model.ConfiguracionAvanzada;
import com.tuorg.veterinaria.configuracion.model.ConfiguracionAvanzada.TipoDato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para ConfiguracionAvanzada (parámetros del sistema).
 */
@Repository
public interface ConfiguracionAvanzadaRepository extends JpaRepository<ConfiguracionAvanzada, Long> {

    /**
     * Obtiene una configuración por su clave.
     * 
     * @param clave Clave de la configuración
     * @return Configuración encontrada
     */
    Optional<ConfiguracionAvanzada> findByClave(String clave);

    /**
     * Obtiene todas las configuraciones activas.
     * 
     * @return Lista de configuraciones activas
     */
    List<ConfiguracionAvanzada> findByActivoTrueOrderByCategoria();

    /**
     * Obtiene configuraciones por categoría.
     * 
     * @param categoria Categoría de configuración
     * @return Lista de configuraciones de esa categoría
     */
    @Query("SELECT ca FROM ConfiguracionAvanzada ca WHERE ca.categoria = :categoria AND ca.activo = true ORDER BY ca.clave")
    List<ConfiguracionAvanzada> findByCategoriaAndActivoTrue(@Param("categoria") String categoria);

    /**
     * Obtiene configuraciones editables.
     * 
     * @return Lista de configuraciones que pueden editarse desde la UI
     */
    @Query("SELECT ca FROM ConfiguracionAvanzada ca WHERE ca.editable = true AND ca.activo = true ORDER BY ca.categoria, ca.clave")
    List<ConfiguracionAvanzada> findEditables();

    /**
     * Obtiene todas las categorías distintas.
     * 
     * @return Lista de categorías únicas
     */
    @Query("SELECT DISTINCT ca.categoria FROM ConfiguracionAvanzada ca WHERE ca.activo = true AND ca.categoria IS NOT NULL ORDER BY ca.categoria")
    List<String> findDistinctCategorias();

    /**
     * Obtiene configuraciones por tipo de dato.
     * 
     * @param tipoDato Tipo de dato
     * @return Lista de configuraciones de ese tipo
     */
    List<ConfiguracionAvanzada> findByTipoDatoAndActivoTrue(TipoDato tipoDato);

    /**
     * Verifica si existe una clave de configuración.
     * 
     * @param clave Clave a verificar
     * @return true si existe
     */
    boolean existsByClave(String clave);
}
