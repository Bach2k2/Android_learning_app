package com.example.japaneseapp.roleplay;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roleplay_character")
public class RoleplayCharacter {
    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "supported_levels_json", nullable = false)
    private String supportedLevelsJson;

    protected RoleplayCharacter() {
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getSupportedLevelsJson() { return supportedLevelsJson; }
}
