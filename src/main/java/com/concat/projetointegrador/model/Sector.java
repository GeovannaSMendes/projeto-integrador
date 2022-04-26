package com.concat.projetointegrador.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter @Setter
@Entity
@Table(name = "sector")
public class Sector {
    //TODO add nullable = false em warehouseId

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(unique = true, nullable = false)
    @ManyToOne
    private Warehouse warehouse;

    @Column(nullable = false)
    private Integer capacity;
    private Boolean active = true;

    @OneToOne
    private Supervisor supervisor;

}