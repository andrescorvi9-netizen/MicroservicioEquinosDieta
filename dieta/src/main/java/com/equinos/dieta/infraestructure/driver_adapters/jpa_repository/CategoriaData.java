package com.equinos.dieta.infraestructure.driver_adapters.jpa_repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "categoria", schema = "dietas")
public class CategoriaData {

    @Id
    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion", nullable = false, columnDefinition = "text")
    private String descripcion;

    @Column(name = "notas_nrc", nullable = false, columnDefinition = "text")
    private String notasNrc;

    public CategoriaData(String codigo, String nombre, String descripcion, String notasNrc) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.notasNrc = notasNrc;
    }
}
