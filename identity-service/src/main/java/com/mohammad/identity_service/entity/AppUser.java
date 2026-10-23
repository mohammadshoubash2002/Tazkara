package com.mohammad.identity_service.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String name;

    private String email;

    private String password;

    private String role;

    @Column(nullable = true, unique = true)
    private String organizationName;

    private boolean isOrganizer;

    public AppUser(){

    }

    public AppUser(String username ,String name, String email, String password, String role, boolean isOrganizer, String organizationName){
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isOrganizer = isOrganizer;
        this.organizationName = organizationName;
    }


    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public boolean getIsOrganizer() {
        return isOrganizer;
    }

    public String setUsername(String username) {
        return this.username = username;
    }

    public String setName(String name) {
        return this.name = name;
    }

    public String setEmail(String email) {
        return this.email = email;
    }

    public String setPassword(String password) {
        return this.password = password;
    }

    public String setOrganizationName(String organizationName) {
        return this.organizationName = organizationName;
    }

    public boolean setIsOrganizer(boolean isOrganizer) {
        return this.isOrganizer = isOrganizer;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "username='" + username + '\'' +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", organization_name='" + organizationName + '\'' +
                '}';
    }

    public static class Builder {
        private String username;
        private String name;
        private String email;
        private String password;
        private String role;
        private String organizationName;
        private boolean isOrganizer;

//        public Builder(String name, String email, String password, String role, String organizationName){
//            this.name = name;
//            this.email = email;
//            this.password = password;
//            this.role = role;
//            this.organizationName = organizationName;
//        }

        public Builder username(String username){
            this.username = username;
            return this;
        }

        public Builder name(String name){
            this.name = name;
            return this;
        }

        public Builder email(String email){
            this.email = email;
            return this;
        }

        public Builder password(String password){
            this.password = password;
            return this;
        }

        public Builder role(String role){
            this.role = role;
            return this;
        }

        public Builder organizationName(String organizationName){
            this.organizationName = organizationName;
            return this;
        }

        public Builder isOrganizer(boolean isOrganizer){
            this.isOrganizer = isOrganizer;
            return this;
        }

        public AppUser build(){
            return new AppUser(username, name, email, password, role, isOrganizer, organizationName);
        }
    }
}