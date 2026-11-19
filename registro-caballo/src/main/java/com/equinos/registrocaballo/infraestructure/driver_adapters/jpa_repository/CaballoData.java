package com.equinos.registrocaballo.infraestructure.driver_adapters.jpa_repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Entidad JPA de la tabla caballos.caballo. Solo la usa el DataGatewayImpl. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "caballo", schema = "caballos")
public class CaballoData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "codigo", nullable = false, length = 40)
    private String codigo;

    @Column(name = "peso_kg", nullable = false, precision = 7, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "categoria", nullable = false, length = 20)
    private String categoria;

    @Column(name = "estado_salud", nullable = false, length = 30)
    private String estadoSalud;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

}
