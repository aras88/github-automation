package com.github;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;

@SpringBootApplication(exclude = {
    JpaRepositoriesAutoConfiguration.class,
    MongoAutoConfiguration.class
})
public class GitHubAutomationApplication {
    public static void main(String[] args) {
        SpringApplication.run(GitHubAutomationApplication.class, args);
    }
}