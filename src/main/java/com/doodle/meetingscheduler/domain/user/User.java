package com.doodle.meetingscheduler.domain.user;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "created_at", nullable = false)
    private final Instant createdAt = Instant.now();

    protected User() {
    }

    public User(String name, String email) {
        this.name = Objects.requireNonNull(name).trim();
        this.email = Objects.requireNonNull(email).trim().toLowerCase();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}