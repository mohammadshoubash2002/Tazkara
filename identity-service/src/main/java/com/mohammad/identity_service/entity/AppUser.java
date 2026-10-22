package com.mohammad.identity_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Email
    private String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
    private String password;

    private String role;

    @Column(nullable = true, unique = true)
    private String organizationName;

    private boolean isOrganizer;

    public AppUser(){

    }

    public AppUser(String name, String email, String password, String role, boolean isOrganizer, String organizationName){
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isOrganizer = isOrganizer;
        this.organizationName = organizationName;
    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
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

    public String setName(String name) {
        return this.name = name;
    }

    public String setEmail(String email) {
        return this.email = email;
    }

    public String setPassword(String password) {
        return this.password = password;
    }

    public String setOrganization_name(String organization_name) {
        return this.organizationName = organizationName;
    }

    public boolean setIsOrganizer(boolean isOrganizer) {
        return this.isOrganizer = isOrganizer;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", organization_name='" + organizationName + '\'' +
                '}';
    }

    public static class Builder {
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
            return new AppUser(name, email, password, role, isOrganizer, organizationName);
        }
    }
}