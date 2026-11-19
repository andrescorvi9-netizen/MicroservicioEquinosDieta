package com.equinos.dieta.infraestructure.driver_adapters.jpa_repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "perfil_dieta_item", schema = "dietas")
public class PerfilDietaItemData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfil_id", nullable = false)
    private PerfilDietaData perfil;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "orden", nullable = false)
    private int orden;

    @Column(name = "texto", nullable = false, columnDefinition = "text")
    private String texto;

    public PerfilDietaItemData(PerfilDietaData perfil, String tipo, int orden, String texto) {
        this.perfil = perfil;
        this.tipo = tipo;
        this.orden = orden;
        this.texto = texto;
    }
}
