package com.teco.bolao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "teams")
public class Team extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "fifa_code", nullable = false, length = 3, unique = true)
    private String fifaCode;

    @Column(name = "flag_url", length = 255)
    private String flagUrl;

    protected Team() {
    }

    public Team(String name, String fifaCode, String flagUrl) {
        this.name = name;
        this.fifaCode = fifaCode;
        this.flagUrl = flagUrl;
    }

    public String getName() {
        return name;
    }

    public String getFifaCode() {
        return fifaCode;
    }

    public String getFlagUrl() {
        return flagUrl;
    }
}
