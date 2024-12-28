package com.github.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "github")
public class GitHubProperties {
    private String springApplicationName;
    private Api api = new Api();
    private DefaultProps defaultProps = new DefaultProps();

    @Data
    public static class Api {
        private String baseurl;
        private String token;
    }

    @Data
    public static class DefaultProps {
        private String owner;
        private String repo;
    }
}