package com.github.tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.github.listener.ExtentJUnit5Extension;
import com.github.model.UserProfile;
import com.github.service.GitHubApiClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(ExtentJUnit5Extension.class)
class UserProfileTest {

    @Autowired
    private GitHubApiClient gitHubApiClient;

    /**
     * Positive scenario: valid token => retrieve user profile => expect 200 status.
     */
    @Test
    @DisplayName("testGetUserProfile_Success")
    void testGetUserProfile_Success(ExtentTest test) {
        test.log(Status.INFO, "Retrieving user profile with a valid token.");
        Response response = gitHubApiClient.getUserProfileResponse(test);

        assertEquals(200, response.getStatusCode(), "Expected 200 OK for a valid user profile.");

        UserProfile profile = gitHubApiClient.getUserProfileAsObject(response);
        assertNotNull(profile, "UserProfile should not be null.");

        test.log(Status.PASS, "UserProfile retrieved successfully.");
        assertNotNull(profile.getLogin(), "'login' field should not be null.");
        test.log(Status.PASS, "User login is: " + profile.getLogin());
    }

    /**
     * Negative scenario: invalid token => attempt to retrieve user profile => expect 401 status.
     */
    @Test
    @DisplayName("testGetUserProfile_InvalidToken")
    void testGetUserProfile_InvalidToken(ExtentTest test) {
        test.log(Status.INFO, "Attempting to retrieve a user profile with an invalid token.");

        String originalToken = gitHubApiClient.getToken();
        gitHubApiClient.setToken("invalid-token");

        try {
            Response response = gitHubApiClient.getUserProfileResponse(test);
            assertEquals(401, response.getStatusCode(), "Expected 401 for an invalid token.");
            test.log(Status.PASS, "Received 401 Unauthorized status.");
        } finally {
            gitHubApiClient.setToken(originalToken);
        }
    }
}