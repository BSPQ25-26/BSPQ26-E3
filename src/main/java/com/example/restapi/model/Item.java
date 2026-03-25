package com.example.restapi.model;
import jakarta.persistence.*;

@Entity
@Table(name = "items")
public class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Boolean status;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Double amount;
    
    private String image_url;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Profile seller;
    
    // Constructor vacío (para JPA)
    public Item() {
    }
    
    // Constructor con parámetros
    public Item(String nombre, String descripcion, Double precio,
         String image_url, Integer cantidad, Category categoria, Boolean status) {
        this.title = nombre;
        this.description = descripcion;
        this.amount = precio;
        this.image_url = image_url;
        this.quantity = cantidad;
        this.category = categoria;
        this.status = status;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return title;
    }
    
    public void setName(String nombre) {
        this.title = nombre;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String descripcion) {
        this.description = descripcion;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double precio) {
        this.amount = precio;
    }
    
    public String getImage_URL() {
        return image_url;
    }
    
    public void setImagen(String image_url) {
        this.image_url = image_url;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }

    public Boolean getStatus(){
        return status;
    }

    public void setStatus(Boolean status){
        this.status = status;
    }

    public Profile getSeller() {
        return seller;
    }

    public void setSeller(Profile seller) {
        this.seller = seller;
    }
    
    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", nombre='" + title + '\'' +
                ", descripcion='" + description + '\'' +
                ", precio=" + amount +
                ", imagen='" + image_url + '\'' +
                ", cantidad=" + quantity +
                ", categoria='" + category + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
