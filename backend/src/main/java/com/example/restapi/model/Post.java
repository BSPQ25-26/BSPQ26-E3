package com.example.restapi.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Profile author;

    private OffsetDateTime creationDate;

    @Column(nullable = false)
    private String title;

    @Size(max = 500)
    private String content;

    //or in other hands is it posted yet or are you drafting it
    @Column(nullable = false)
    private Boolean isPublic;

    @OneToMany(mappedBy = "post", cascade = CascadeType.DETACH, orphanRemoval = true)
    private List<Category> categories = new ArrayList<>();

    @JsonSetter(nulls = Nulls.SKIP)
    public void setAuthor(String authorId ){
        Profile profile = new Profile();
        profile.setId(UUID.fromString(authorId));
        this.author = profile;
    }

    public void setAuthor(Profile author) {
        this.author = author;
    }

}
