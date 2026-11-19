package com.equinos.dieta.infraestructure.driver_adapters.jpa_repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "perfil_dieta", schema = "dietas")
public class PerfilDietaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "categoria_codigo", nullable = false, length = 20)
    private String categoriaCodigo;

    @Column(name = "estado_codigo", nullable = false, length = 30)
    private String estadoCodigo;

    @Column(name = "fuente", nullable = false, length = 20)
    private String fuente;

    @Column(name = "criterio_nutricional", nullable = false, columnDefinition = "text")
    private String criterioNutricional;

    @Column(name = "peso_ref_kg", nullable = false, precision = 7, scale = 2)
    private BigDecimal pesoRefKg;

    @Column(name = "ed_mcal_dia", nullable = false, precision = 7, scale = 2)
    private BigDecimal energiaDigestibleMcalDia;

    @Column(name = "pc_g_dia", nullable = false, precision = 8, scale = 2)
    private BigDecimal proteinaCrudaGDia;

    @Column(name = "ca_g_dia", nullable = false, precision = 7, scale = 2)
    private BigDecimal calcioGDia;

    @Column(name = "p_g_dia", nullable = false, precision = 7, scale = 2)
    private BigDecimal fosforoGDia;

    @Column(name = "pct_ms_peso", nullable = false, precision = 4, scale = 1)
    private BigDecimal pctMsPeso;

    @Column(name = "pct_forraje", nullable = false, precision = 5, scale = 2)
    private BigDecimal pctForraje;

    @Column(name = "pct_concentrado", nullable = false, precision = 5, scale = 2)
    private BigDecimal pctConcentrado;

    @Column(name = "manejo_agua", nullable = false, columnDefinition = "text")
    private String manejoAgua;

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    private List<PerfilDietaItemData> items = new ArrayList<>();

}
