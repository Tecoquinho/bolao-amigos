package com.teco.bolao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "participants")
public class Participant extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    protected Participant() {
    }

    public Participant(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
