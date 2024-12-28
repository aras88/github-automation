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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(ExtentJUnit5Extension.class)
class ListRepositoriesTest {

    @Autowired
    private GitHubApiClient gitHubApiClient;

    @BeforeEach
    void setUp() {
        // No setup required as each test manages its own repository
    }

    @Test
    @DisplayName("testListUserRepositories_Success")
    void testListUserRepositories(ExtentTest test) {
        String randomName = "test-repo-" + UUID.randomUUID().toString().substring(0, 6);
        test.log(Status.INFO, "Creating repository: " + randomName);

        try {
            Response createResponse = gitHubApiClient.createRepositoryResponse(
                randomName,
                "Repository created for list test",
                false,
                test
            );
            assertEquals(201, createResponse.getStatusCode(), "Repository creation failed.");

            Repository createdRepo = gitHubApiClient.createRepositoryAsObject(createResponse);
            assertNotNull(createdRepo, "Created repository is null.");
            test.log(Status.PASS, "Repository created: " + randomName);

            test.log(Status.INFO, "Listing user repositories.");

            Response listResp = gitHubApiClient.listRepositoriesResponse(test);
            assertEquals(200, listResp.getStatusCode(), "Failed to list repositories.");

            List<Repository> repositories = gitHubApiClient.listRepositoriesAsObjects(listResp);
            assertNotNull(repositories, "Repository list is null.");
            assertFalse(repositories.isEmpty(), "Repository list is empty.");
            assertTrue(repositories.stream().anyMatch(repo -> repo.getName().equals(randomName)),
                "Created repository not found in the list.");

            test.log(Status.PASS, "Repositories listed successfully and created repository is present.");
        } finally {
            test.log(Status.INFO, "Cleaning up repository: " + randomName);
            Response deleteResponse = gitHubApiClient.deleteRepositoryResponse(randomName, test);

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
    @DisplayName("testListRepositories_WhenEmpty")
    void testListRepositoriesWhenEmpty(ExtentTest test) {
        // Delete all repositories with 'test-repo' in the name
        test.log(Status.INFO, "Deleting all repositories with 'test-repo' in the name.");
        Response listResp = gitHubApiClient.listRepositoriesResponse(test);
        assertEquals(200, listResp.getStatusCode(), "Failed to list repositories.");

        List<Repository> existingRepos = gitHubApiClient.listRepositoriesAsObjects(listResp);
        if (existingRepos != null) {
            existingRepos.stream()
                .filter(repo -> repo.getName().contains("test-repo"))
                .forEach(repo -> {
                    test.log(Status.INFO, "Deleting repository: " + repo.getName());
                    Response deleteResponse = gitHubApiClient.deleteRepositoryResponse(repo.getName(), test);
                    if (deleteResponse.getStatusCode() == 204) {
                        test.log(Status.PASS, "Repository deleted: " + repo.getName());
                    } else if (deleteResponse.getStatusCode() == 404) {
                        test.log(Status.WARNING, "Repository already deleted or does not exist: " + repo.getName());
                    } else {
                        test.log(Status.WARNING, "Failed to delete repository " + repo.getName() + " (status " 
                            + deleteResponse.getStatusCode() + ").");
                    }
                });
        }

        String randomName = "test-repo-" + UUID.randomUUID().toString().substring(0, 6);
        test.log(Status.INFO, "Creating repository: " + randomName);

        try {
            Response createResponse = gitHubApiClient.createRepositoryResponse(
                randomName,
                "Repository created for list test",
                false,
                test
            );
            assertEquals(201, createResponse.getStatusCode(), "Repository creation failed.");

            Repository createdRepo = gitHubApiClient.createRepositoryAsObject(createResponse);
            assertNotNull(createdRepo, "Created repository is null.");
            test.log(Status.PASS, "Repository created: " + randomName);

            test.log(Status.INFO, "Deleting repository to simulate empty repository list.");

            Response deleteResp = gitHubApiClient.deleteRepositoryResponse(randomName, test);
            assertEquals(204, deleteResp.getStatusCode(), "Failed to delete repository.");
            test.log(Status.PASS, "Repository deleted successfully.");

            test.log(Status.INFO, "Listing user repositories.");

            Response finalListResp = gitHubApiClient.listRepositoriesResponse(test);
            assertEquals(200, finalListResp.getStatusCode(), "Failed to list repositories.");

            List<Repository> repositories = gitHubApiClient.listRepositoriesAsObjects(finalListResp);
            assertNotNull(repositories, "Repository list is null.");
            assertTrue(repositories.stream().noneMatch(repo -> repo.getName().equals(randomName)),
                "Deleted repository should not be present in the list.");

            test.log(Status.PASS, "Repository list is empty as expected.");
        } finally {
            test.log(Status.INFO, "Cleaning up repository: " + randomName);
            Response deleteResponse = gitHubApiClient.deleteRepositoryResponse(randomName, test);

            if (deleteResponse.getStatusCode() == 204) {
                test.log(Status.PASS, "Repository cleaned up successfully.");
            } else if (deleteResponse.getStatusCode() == 404) {
                test.log(Status.PASS, "Repository already deleted or does not exist.");
            } else {
                test.log(Status.WARNING, "Failed to clean up repository (status " 
                    + deleteResponse.getStatusCode() + ").");
            }
        }
    }

    @AfterEach
    void tearDown() {
        // No teardown required as cleanup is handled within each test
    }
}