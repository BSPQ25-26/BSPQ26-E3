package com.example.restapi.dto;

import com.example.restapi.model.Category;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PostRequest {

    private UUID authorId;
    private String title;
    private String content;
    private List<Category> categories;

    public PostRequest (UUID authorId, String title, String content, List<Category> categories) {
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.categories = categories;
    }

}
