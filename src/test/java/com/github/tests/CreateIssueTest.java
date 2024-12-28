package com.github.tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.github.config.GitHubProperties;
import com.github.listener.ExtentJUnit5Extension;
import com.github.model.Repository;
import com.github.service.GitHubApiClient;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(ExtentJUnit5Extension.class)
class CreateIssueTest {

    @Autowired
    private GitHubApiClient gitHubApiClient;

    @Autowired
    private GitHubProperties gitHubProperties;

    @Test
    @DisplayName("testCreateIssue_Success")
    void testCreateIssue(ExtentTest test) {
        String repoName = "test-repo-" + UUID.randomUUID().toString().substring(0, 8);
        String title = "Test Issue JUnit 5 - " + UUID.randomUUID().toString().substring(0, 6);
        String bodyText = "This is a sample issue created by a JUnit 5 test.";

        test.log(Status.INFO, "Creating repository: " + repoName);

        try {
            Response createRepoResponse = gitHubApiClient.createRepositoryResponse(
                    repoName,
                    "Repository created for issue test",
                    false,
                    test
            );
            assertEquals(201, createRepoResponse.getStatusCode(), "Repository creation failed.");
            test.log(Status.PASS, "Repository created successfully: " + repoName);

            Repository createdRepo = gitHubApiClient.createRepositoryAsObject(createRepoResponse);
            assertNotNull(createdRepo, "Created repository is null.");
            assertEquals(repoName, createdRepo.getName(), "Repository name mismatch.");
            test.log(Status.PASS, "Verified repository name: " + repoName);

            String owner = gitHubApiClient.getDefaultOwner();

            test.log(Status.INFO, "Creating an issue in repository: " + repoName);
            Response issueResponse = gitHubApiClient.createIssueResponse(owner, repoName, title, bodyText, test);
            assertEquals(201, issueResponse.getStatusCode(), "Issue creation failed.");
            test.log(Status.PASS, "Issue created successfully with title: " + title);

            JSONObject issueJson = gitHubApiClient.createIssueAsJson(issueResponse);
            assertNotNull(issueJson, "Issue JSON should not be null.");
            assertEquals(title, issueJson.optString("title"), "Issue title mismatch.");
            test.log(Status.PASS, "Verified issue title: " + title);

            String issueNumber = String.valueOf(issueJson.optInt("number"));
            String issueUrl = issueJson.optString("html_url");
            test.log(Status.PASS, "Issue URL: " + issueUrl);

            Response closeIssueResponse = gitHubApiClient.closeIssueResponse(owner, repoName, issueNumber, test);
            assertEquals(200, closeIssueResponse.getStatusCode(), "Closing issue failed.");
            test.log(Status.PASS, "Issue closed successfully: #" + issueNumber);

        } finally {
            test.log(Status.INFO, "Cleaning up repository: " + repoName);
            Response deleteResponse = gitHubApiClient.deleteRepositoryResponse(repoName, test);
            if (deleteResponse.getStatusCode() == 204) {
                test.log(Status.PASS, "Repository deleted successfully: " + repoName);
            } else if (deleteResponse.getStatusCode() == 404) {
                test.log(Status.PASS, "Repository already deleted or does not exist: " + repoName);
            } else {
                test.log(Status.WARNING, "Failed to delete repository (status " 
                    + deleteResponse.getStatusCode() + ").");
            }
        }
    }

    @Test
    @DisplayName("testCreateIssueWithoutTitle_Failure")
    void testCreateIssueWithoutTitle(ExtentTest test) {
        String repoName = "test-repo-" + UUID.randomUUID().toString().substring(0, 8);
        String title = "";
        String bodyText = "Issue without a title should fail.";

        test.log(Status.INFO, "Creating repository: " + repoName);

        try {
            Response createRepoResponse = gitHubApiClient.createRepositoryResponse(
                    repoName,
                    "Repository created for issue test",
                    false,
                    test
            );
            assertEquals(201, createRepoResponse.getStatusCode(), "Repository creation failed.");
            test.log(Status.PASS, "Repository created successfully: " + repoName);

            Repository createdRepo = gitHubApiClient.createRepositoryAsObject(createRepoResponse);
            assertNotNull(createdRepo, "Created repository is null.");
            assertEquals(repoName, createdRepo.getName(), "Repository name mismatch.");
            test.log(Status.PASS, "Verified repository name: " + repoName);

            String owner = gitHubApiClient.getDefaultOwner();

            test.log(Status.INFO, "Attempting to create an issue without a title in repository: " + repoName);
            Response issueResponse = gitHubApiClient.createIssueResponse(owner, repoName, title, bodyText, test);
            assertEquals(422, issueResponse.getStatusCode(), "Expected HTTP 422 Unprocessable Entity for missing title.");
            test.log(Status.PASS, "Received expected HTTP 422 status code for missing title.");

            String responseBody = issueResponse.getBody().asString();
            assertTrue(responseBody.contains("title"), "Response should indicate the issue with the missing title.");
            test.log(Status.PASS, "Response indicates the issue with the missing title.");

        } finally {
            test.log(Status.INFO, "Cleaning up repository: " + repoName);
            Response deleteResponse = gitHubApiClient.deleteRepositoryResponse(repoName, test);
            if (deleteResponse.getStatusCode() == 204) {
                test.log(Status.PASS, "Repository deleted successfully: " + repoName);
            } else if (deleteResponse.getStatusCode() == 404) {
                test.log(Status.PASS, "Repository already deleted or does not exist: " + repoName);
            } else {
                test.log(Status.WARNING, "Failed to delete repository (status " 
                    + deleteResponse.getStatusCode() + ").");
            }
        }
    }

    @Test
    @DisplayName("testCreateIssueInNonExistentRepository_Failure")
    void testCreateIssueInNonExistentRepository_Failure(ExtentTest test) {
        String owner = gitHubProperties.getDefaultProps().getOwner();
        String nonExistentRepo = "non-existent-repo-" + UUID.randomUUID().toString().substring(0, 6);
        String title = "Issue in Non-Existent Repo";
        String bodyText = "This issue creation should fail as the repository does not exist.";

        test.log(Status.INFO, "Executing test: Attempting to create an issue in non-existent repository: " + nonExistentRepo);

        Response createResponse = gitHubApiClient.createIssueResponse(owner, nonExistentRepo, title, bodyText, test);
        assertEquals(404, createResponse.getStatusCode(), "Creating issue in non-existent repository should return 404");
        test.log(Status.PASS, "Issue creation in non-existent repository failed as expected with status 404");
    }

    @Test
    @DisplayName("testCreateIssueWithoutAuthorization_Failure")
    void testCreateIssueWithoutAuthorization_Failure(ExtentTest test) {
        String repoName = "test-repo-" + UUID.randomUUID().toString().substring(0, 6);
        String owner = gitHubProperties.getDefaultProps().getOwner();
        String title = "Unauthorized Issue Creation";
        String bodyText = "This issue creation should fail due to lack of authorization.";

        test.log(Status.INFO, "Creating repository: " + repoName);

        try {
            Response createRepoResponse = gitHubApiClient.createRepositoryResponse(
                    repoName,
                    "Repository created for unauthorized issue creation test",
                    false,
                    test
            );
            assertEquals(201, createRepoResponse.getStatusCode(), "Repository creation failed.");
            test.log(Status.PASS, "Repository created successfully: " + repoName);

            Repository createdRepo = gitHubApiClient.createRepositoryAsObject(createRepoResponse);
            assertNotNull(createdRepo, "Created repository is null.");
            assertEquals(repoName, createdRepo.getName(), "Repository name mismatch.");
            test.log(Status.PASS, "Verified repository name: " + repoName);

            test.log(Status.INFO, "Attempting to create an issue without authorization in repository: " + repoName);
            Response issueResponse = gitHubApiClient.createIssueUnauthorizedResponse(owner, repoName, title, bodyText, test);
            assertEquals(401, issueResponse.getStatusCode(), "Creating issue without authorization should return 401");
            test.log(Status.PASS, "Issue creation without authorization failed as expected with status 401");

        } finally {
            test.log(Status.INFO, "Cleaning up repository: " + repoName);
            Response deleteResponse = gitHubApiClient.deleteRepositoryResponse(repoName, test);
            if (deleteResponse.getStatusCode() == 204) {
                test.log(Status.PASS, "Repository deleted successfully: " + repoName);
            } else if (deleteResponse.getStatusCode() == 404) {
                test.log(Status.PASS, "Repository already deleted or does not exist: " + repoName);
            } else {
                test.log(Status.WARNING, "Failed to delete repository (status " 
                    + deleteResponse.getStatusCode() + ").");
            }
        }
    }
}