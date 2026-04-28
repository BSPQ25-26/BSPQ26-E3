// Clase Category
package com.example.restapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="post_id")
    private Post post;
    
    private String description;
    
    public Category() {
    }
    
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescripcion() {
        return description;
    }
    
    public void setDescripcion(String descripcion) {
        this.description = descripcion;
    }
    
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", nombre='" + name + '\'' +
                ", descripcion='" + description + '\'' +
                '}';
    }
}