package com.equinos.dieta.infraestructure.driver_adapters.jpa_repository;

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

/** Entidad JPA de la tabla dietas.dieta. Solo la usa el DataGatewayImpl. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "dieta", schema = "dietas")
public class DietaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "caballo_id", nullable = false)
    private Long caballoId;

    @Column(name = "caballo_nombre", nullable = false, length = 120)
    private String caballoNombre;

    @Column(name = "caballo_codigo", nullable = false, length = 40)
    private String caballoCodigo;

    @Column(name = "perfil_id", nullable = false)
    private Long perfilId;

    @Column(name = "categoria", nullable = false, length = 20)
    private String categoria;

    @Column(name = "estado_salud", nullable = false, length = 30)
    private String estadoSalud;

    @Column(name = "peso_kg", nullable = false, precision = 7, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "ms_total_kg", nullable = false, precision = 7, scale = 2)
    private BigDecimal msTotalKg;

    @Column(name = "forraje_kg", nullable = false, precision = 7, scale = 2)
    private BigDecimal forrajeKg;

    @Column(name = "concentrado_kg", nullable = false, precision = 7, scale = 2)
    private BigDecimal concentradoKg;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

}
