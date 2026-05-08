package com.example.restapi.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Profile getAuthor() {
        return author;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

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
