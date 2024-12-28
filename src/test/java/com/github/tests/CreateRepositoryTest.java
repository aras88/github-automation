package com.github.tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.github.listener.ExtentJUnit5Extension;
import com.github.model.Repository;
import com.github.service.GitHubApiClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(ExtentJUnit5Extension.class)
class CreateRepositoryTest {

    @Autowired
    private GitHubApiClient gitHubApiClient;

    @BeforeEach
    void setUp() {
        // No setup required as each test manages its own repository
    }

    @Test
    @DisplayName("testCreateNewRepository_Success")
    void testCreateNewRepository(ExtentTest test) {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString().substring(0, 6);
        test.log(Status.INFO, "Creating repository: " + repositoryName);

        try {
            // Create repository
            Response createResponse = gitHubApiClient.createRepositoryResponse(
                repositoryName,
                "Repository created by test",
                false,
                test
            );
            assertEquals(201, createResponse.getStatusCode(), "Repository creation failed.");
            test.log(Status.PASS, "Received expected HTTP 201 status code for repository creation.");

            // Convert response to Repository object
            Repository createdRepo = gitHubApiClient.createRepositoryAsObject(createResponse);
            assertNotNull(createdRepo, "Repository should not be null after creation.");
            test.log(Status.PASS, "Repository object created successfully.");

            // Validate repository name
            assertEquals(repositoryName, createdRepo.getName(),
                "Repository name should match the requested name.");
            test.log(Status.PASS, "Repository name matches the expected name.");

            // Log repository URL
            test.log(Status.INFO, "Created repository URL: " + createdRepo.getHtmlUrl());
        } finally {
            // Cleanup repository
            test.log(Status.INFO, "Cleaning up repository: " + repositoryName);
            Response deleteResponse = gitHubApiClient.deleteRepositoryResponse(repositoryName, test);
            if (deleteResponse.getStatusCode() == 204) {
                test.log(Status.PASS, "Repository cleaned up successfully.");
            } else if (deleteResponse.getStatusCode() == 404) {
                test.log(Status.WARNING, "Repository already deleted or does not exist.");
            } else {
                test.log(Status.WARNING, "Failed to clean up repository (status " 
                    + deleteResponse.getStatusCode() + ").");
            }
        }
    }

    @Test
    @DisplayName("testCreateRepositoryWithInvalidName_Failure")
    void testCreateRepositoryWithInvalidName(ExtentTest test) {
        String invalidName = ""; // Invalid repository name
        test.log(Status.INFO, "Attempting to create a repository with an invalid name.");

        // Attempt to create repository with invalid name
        Response createResponse = gitHubApiClient.createRepositoryResponse(
            invalidName,
            "Repository created with invalid name",
            false,
            test
        );

        // Validate the response
        assertEquals(422, createResponse.getStatusCode(),
            "Expected HTTP 422 Unprocessable Entity for invalid repository name.");
        test.log(Status.PASS, "Received expected HTTP 422 status code for invalid repository name.");

        // Validate error message
        String responseBody = createResponse.getBody().asString();
        assertTrue(responseBody.contains("name"),
            "Response should indicate the issue with the repository name.");
        test.log(Status.PASS, "Response indicates the issue with the repository name.");
    }

    @Test
    @DisplayName("testCreateRepositoryWithDuplicateName_Failure")
    void testCreateRepositoryWithDuplicateName(ExtentTest test) {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString().substring(0, 6);
        test.log(Status.INFO, "Creating repository with name: " + repositoryName);

        try {
            // Create initial repository
            Response createResponse = gitHubApiClient.createRepositoryResponse(
                repositoryName,
                "Initial repository creation for duplicate test",
                false,
                test
            );
            assertEquals(201, createResponse.getStatusCode(), "Initial repository creation failed.");
            test.log(Status.PASS, "Initial repository created successfully.");

            // Attempt to create duplicate repository
            test.log(Status.INFO, "Attempting to create a duplicate repository with name: " + repositoryName);
            Response duplicateCreateResponse = gitHubApiClient.createRepositoryResponse(
                repositoryName,
                "Duplicate repository creation attempt",
                false,
                test
            );

            // Validate the response
            assertEquals(422, duplicateCreateResponse.getStatusCode(),
                "Expected HTTP 422 Unprocessable Entity for duplicate repository name.");
            test.log(Status.PASS, "Received expected HTTP 422 status code for duplicate repository name.");

            // Validate error message
            String responseBody = duplicateCreateResponse.getBody().asString();
            assertTrue(responseBody.contains("already exists"),
                "Response should indicate that the repository already exists.");
            test.log(Status.PASS, "Response indicates that the repository already exists.");
        } finally {
            // Cleanup repository
            test.log(Status.INFO, "Cleaning up repository: " + repositoryName);
            Response deleteResponse = gitHubApiClient.deleteRepositoryResponse(repositoryName, test);
            if (deleteResponse.getStatusCode() == 204) {
                test.log(Status.PASS, "Repository cleaned up successfully.");
            } else if (deleteResponse.getStatusCode() == 404) {
                test.log(Status.WARNING, "Repository already deleted or does not exist.");
            } else {
                test.log(Status.WARNING, "Failed to clean up repository (status " 
                    + deleteResponse.getStatusCode() + ").");
            }
        }
    }

    @Test
    @DisplayName("testCreateRepositoryWithoutAuthorization_Failure")
    void testCreateRepositoryWithoutAuthorization(ExtentTest test) {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString().substring(0, 6);
        test.log(Status.INFO, "Attempting to create a repository without proper authorization.");

        // Attempt to create repository with invalid token
        Response createResponse = gitHubApiClient.createRepositoryUnauthorizedResponse(
            repositoryName,
            test
        );

        // Validate the response
        assertEquals(401, createResponse.getStatusCode(),
            "Expected HTTP 401 Unauthorized for repository creation without authorization.");
        test.log(Status.PASS, "Received expected HTTP 401 status code for unauthorized repository creation.");

        // Validate error message
        String responseBody = createResponse.getBody().asString();
        assertTrue(responseBody.contains("Bad credentials") || responseBody.contains("Unauthorized"),
            "Response should indicate unauthorized access.");
        test.log(Status.PASS, "Response indicates unauthorized access.");
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        // No teardown required as cleanup is handled within each test
    }
}