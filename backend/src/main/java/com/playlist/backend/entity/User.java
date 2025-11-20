package com.playlist.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "USER_TABLE")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // PK

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    protected User() {}   // JPA 기본 생성자

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // getter / setter
    public Long getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }
}