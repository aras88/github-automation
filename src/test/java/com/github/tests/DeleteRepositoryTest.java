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
class DeleteRepositoryTest {

    @Autowired
    private GitHubApiClient gitHubApiClient;

    @BeforeEach
    void setUp() {
        // No setup required as each test manages its own repository
    }

    @Test
    @DisplayName("testDeleteRepository_Success")
    void testDeleteRepository(ExtentTest test) {
        String randomName = "test-repo-" + UUID.randomUUID().toString().substring(0, 6);
        test.log(Status.INFO, "Creating repository: " + randomName);

        try {
            // Create repository
            Response createResponse = gitHubApiClient.createRepositoryResponse(
                randomName,
                "Repository created for deletion test",
                false,
                test
            );
            assertEquals(201, createResponse.getStatusCode(), "Repository creation failed.");

            Repository createdRepo = gitHubApiClient.createRepositoryAsObject(createResponse);
            assertNotNull(createdRepo, "Created repository is null.");
            test.log(Status.PASS, "Repository created: " + randomName);

            test.log(Status.INFO, "Deleting the repository: " + randomName);

            // Delete repository
            Response deleteResponse = gitHubApiClient.deleteRepositoryResponse(randomName, test);
            assertEquals(204, deleteResponse.getStatusCode(), "Repository should be deleted successfully");
            test.log(Status.PASS, "Repository was deleted successfully");

            // Verify deletion by listing repositories
            test.log(Status.INFO, "Verifying deletion by listing repositories");
            Response listResp = gitHubApiClient.listRepositoriesResponse(test);
            assertEquals(200, listResp.getStatusCode(), "Failed to list repositories.");
            List<Repository> repositories = gitHubApiClient.listRepositoriesAsObjects(listResp);
            assertNotNull(repositories, "Repository list is null.");
            assertFalse(repositories.stream().anyMatch(repo -> repo.getName().equals(randomName)),
                "Deleted repository should not be present in the list");
            test.log(Status.PASS, "Verified that repository is no longer present");
        } finally {
            // Cleanup in case deletion failed
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

    @Test
    @DisplayName("testDeleteNonExistentRepository_Failure")
    void testDeleteNonExistentRepository(ExtentTest test) {
        // Precondition: Delete all repositories with 'test-repo' in the name to ensure no leftovers
        test.log(Status.INFO, "Precondition: Deleting all repositories with 'test-repo' in the name");
        Response listResp = gitHubApiClient.listRepositoriesResponse(test);
        assertEquals(200, listResp.getStatusCode(), "Failed to list repositories.");

        List<Repository> existingRepos = gitHubApiClient.listRepositoriesAsObjects(listResp);
        if (existingRepos != null) {
            existingRepos.stream()
                .filter(repo -> repo.getName().contains("test-repo"))
                .forEach(repo -> {
                    test.log(Status.INFO, "Deleting existing repository: " + repo.getName());
                    Response deleteResp = gitHubApiClient.deleteRepositoryResponse(repo.getName(), test);
                    if (deleteResp.getStatusCode() == 204) {
                        test.log(Status.PASS, "Repository deleted: " + repo.getName());
                    } else if (deleteResp.getStatusCode() == 404) {
                        test.log(Status.WARNING, "Repository already deleted or does not exist: " + repo.getName());
                    } else {
                        test.log(Status.WARNING, "Failed to delete repository " + repo.getName() + " (status " 
                            + deleteResp.getStatusCode() + ").");
                    }
                });
        }

        String nonExistentRepo = "non-existent-repo-" + UUID.randomUUID().toString().substring(0, 6);
        test.log(Status.INFO, "Executing test: Attempting to delete non-existent repository: " + nonExistentRepo);

        // Attempt to delete a non-existent repository
        Response deleteResponse = gitHubApiClient.deleteRepositoryResponse(nonExistentRepo, test);
        assertEquals(404, deleteResponse.getStatusCode(), "Deleting a non-existent repository should return 404");
        test.log(Status.PASS, "Deletion of non-existent repository failed as expected with status 404");
    }

    @Test
    @DisplayName("testDeleteRepositoryWithoutAuthorization_Failure")
    void testDeleteRepositoryWithoutAuthorization(ExtentTest test) {
        String randomName = "test-repo-" + UUID.randomUUID().toString().substring(0, 6);
        test.log(Status.INFO, "Precondition: Creating repository for unauthorized deletion test: " + randomName);

        try {
            // Create repository
            Response createResponse = gitHubApiClient.createRepositoryResponse(
                randomName,
                "Repository created for unauthorized deletion test",
                false,
                test
            );
            assertEquals(201, createResponse.getStatusCode(), "Repository creation failed.");

            Repository createdRepo = gitHubApiClient.createRepositoryAsObject(createResponse);
            assertNotNull(createdRepo, "Created repository is null.");
            test.log(Status.PASS, "Repository created: " + randomName);

            test.log(Status.INFO, "Attempting to delete repository without authorization: " + randomName);

            // Attempt unauthorized deletion
            Response deleteResponse = gitHubApiClient.deleteRepositoryUnauthorizedResponse(randomName, test);
            assertEquals(401, deleteResponse.getStatusCode(), "Deleting without authorization should return 401");
            test.log(Status.PASS, "Unauthorized deletion attempt failed as expected with status 401");
        } finally {
            // Cleanup repository if it wasn't deleted
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