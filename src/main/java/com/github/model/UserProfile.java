package com.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfile {
    private String login;
    private Long id;
    private String url;
    private String name;
}