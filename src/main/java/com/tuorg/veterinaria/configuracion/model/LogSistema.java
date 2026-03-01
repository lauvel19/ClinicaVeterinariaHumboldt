package com.tuorg.veterinaria.configuracion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs_sistema", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogSistema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "componente", length = 100)
    private String componente;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "nivel", nullable = false, length = 20)
    private String nivel;

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;
}
