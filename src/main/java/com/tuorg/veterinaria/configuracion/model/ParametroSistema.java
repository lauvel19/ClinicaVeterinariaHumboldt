package com.tuorg.veterinaria.configuracion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parametros_sistema", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParametroSistema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    private Long idParametro;

    @Column(name = "clave", nullable = false, unique = true, length = 150)
    private String clave;

    @Column(name = "valor", nullable = false, length = 500)
    private String valor;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "aplicacion", length = 50)
    private String aplicacion;
}
