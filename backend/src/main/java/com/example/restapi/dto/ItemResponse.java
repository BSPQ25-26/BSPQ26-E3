package com.example.restapi.dto;

public class ItemResponse {
    private Long id;
    private String title;
    private String description;
    private Double amount;
    private String image_url;
    private Integer quantity;
    private Boolean status;
    private String categoryName;
    private String sellerId;

    public ItemResponse(Long id, String title, String description, Double amount, 
                       String image_url, Integer quantity, Boolean status, 
                       String categoryName, String sellerId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.image_url = image_url;
        this.quantity = quantity;
        this.status = status;
        this.categoryName = categoryName;
        this.sellerId = sellerId;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Double getAmount() { return amount; }
    public String getImage_url() { return image_url; }
    public Integer getQuantity() { return quantity; }
    public Boolean getStatus() { return status; }
    public String getCategoryName() { return categoryName; }
    public String getSellerId() { return sellerId; }
}