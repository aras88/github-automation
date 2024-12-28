package com.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {
    private long id;
    private String name;
    private String description;
    private boolean privateRepo;
    private String htmlUrl;
}
