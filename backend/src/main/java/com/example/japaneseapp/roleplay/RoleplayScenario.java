package com.example.japaneseapp.roleplay;

import jakarta.persistence.*;

@Entity
@Table(name = "roleplay_scenario")
public class RoleplayScenario {
    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false)
    private RoleplayCharacter character;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    protected RoleplayScenario() {
    }

    public String getId() { return id; }
    public RoleplayCharacter getCharacter() { return character; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
