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
@Table(name = "estado_salud", schema = "dietas")
public class EstadoSaludData {

    @Id
    @Column(name = "codigo", length = 30)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    public EstadoSaludData(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }
}
