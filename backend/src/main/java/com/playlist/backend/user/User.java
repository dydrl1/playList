package com.playlist.backend.user;

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

    @Column(nullable = false, length = 255)
    private String password;

    protected User() {}   // JPA 기본 생성자

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }


    // getter / setter
    public Long getId() { return id; }

    public String getName() { return name; }

    public String getPassword() { return password; }

    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public void setPassword(String password) { this.password = password; }

    public void updateName(String newName){ this.name = newName; }
}