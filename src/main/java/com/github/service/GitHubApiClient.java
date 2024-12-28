package com.github.service;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.github.model.Repository;
import com.github.model.UserProfile;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@Getter
@Setter
@Service
public class GitHubApiClient {

    private final String baseUrl;
    private String token;
    private final String defaultOwner;

    public GitHubApiClient(
        @Value("${github.api.baseurl}") String baseUrl,
        @Value("${github.api.token}") String token,
        @Value("${github.default.owner}") String defaultOwner
    ) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.defaultOwner = defaultOwner;
    }

    /**
     * Retrieves the raw Response for GET /user.
     */
    public Response getUserProfileResponse(ExtentTest test) {
        String method = "GET";
        String endpoint = "/user";
        String requestBody = null;  // GET typically has no request body

        RequestSpecification request = given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer " + token)
            .header("Accept", "application/vnd.github.v3+json");

        Response response = request.get(endpoint);
        logRequestAndResponse(test, method, baseUrl + endpoint, requestBody, request, response);
        return response;
    }

    /**
     * Converts a GET /user Response into a UserProfile object.
     */
    public UserProfile getUserProfileAsObject(Response response) {
        return response.as(UserProfile.class);
    }

    /**
     * Retrieves the raw Response for GET /user/repos.
     */
    public Response listRepositoriesResponse(ExtentTest test) {
        String method = "GET";
        String endpoint = "/user/repos";
        String requestBody = null;

        RequestSpecification request = given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer " + token)
            .header("Accept", "application/vnd.github.v3+json");

        Response response = request.get(endpoint);
        logRequestAndResponse(test, method, baseUrl + endpoint, requestBody, request, response);
        return response;
    }

    /**
     * Converts the Response from GET /user/repos into a List<Repository>.
     */
    public List<Repository> listRepositoriesAsObjects(Response response) {
        return response.jsonPath().getList("$", Repository.class);
    }

    /**
     * Returns the raw Response from POST /repos/{owner}/{repo}/issues.
     */
    public Response createIssueResponse(String owner, String repo, String title, String bodyText, ExtentTest test) {
        String method = "POST";
        String endpoint = String.format("/repos/%s/%s/issues", owner, repo);

        JSONObject issueDetails = new JSONObject();
        issueDetails.put("title", title);
        issueDetails.put("body", bodyText);

        String requestBody = issueDetails.toString();

        RequestSpecification request = given()
            .header("Authorization", "Bearer " + token)
            .header("Accept", "application/vnd.github.v3+json")
            .contentType("application/json")
            .body(requestBody);

        Response response = request.post(baseUrl + endpoint);
        logRequestAndResponse(test, method, baseUrl + endpoint, requestBody, request, response);
        return response;
    }

    /**
     * Converts the Response from creating an issue into a JSONObject.
     */
    public JSONObject createIssueAsJson(Response response) {
        return new JSONObject(response.asString());
    }

    /**
     * Returns the raw Response from POST /user/repos.
     */
    public Response createRepositoryResponse(String name, String description, boolean isPrivate, ExtentTest test) {
        String method = "POST";
        String endpoint = "/user/repos";

        String payload = String.format(
            "{\"name\":\"%s\",\"description\":\"%s\",\"private\":%b}",
            name, description, isPrivate
        );

        RequestSpecification request = given()
            .header("Authorization", "Bearer " + token)
            .header("Accept", "application/vnd.github.v3+json")
            .contentType("application/json")
            .body(payload);

        Response response = request.post(baseUrl + endpoint);
        logRequestAndResponse(test, method, baseUrl + endpoint, payload, request, response);
        return response;
    }

    /**
     * Converts the Response from creating a repository into a Repository object.
     */
    public Repository createRepositoryAsObject(Response response) {
        return response.as(Repository.class);
    }

    /**
     * Returns the raw Response from DELETE /repos/{owner}/{repo}.
     */
    public Response deleteRepositoryResponse(String repoName, ExtentTest test) {
        String method = "DELETE";
        String endpoint = String.format("/repos/%s/%s", defaultOwner, repoName);
        String requestBody = null;

        RequestSpecification request = given()
            .header("Authorization", "Bearer " + token)
            .header("Accept", "application/vnd.github.v3+json");

        Response response = request.delete(baseUrl + endpoint);
        logRequestAndResponse(test, method, baseUrl + endpoint, requestBody, request, response);
        return response;
    }

    /**
     * Simulates an unauthorized deletion call using INVALID_TOKEN.
     */
    public Response deleteRepositoryUnauthorizedResponse(String repoName, ExtentTest test) {
        String method = "DELETE";
        String endpoint = String.format("/repos/%s/%s", defaultOwner, repoName);
        String requestBody = null;

        test.log(Status.INFO, "Attempting to delete repository without authorization: " + repoName);

        RequestSpecification request = given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer INVALID_TOKEN")
            .header("Accept", "application/vnd.github.v3+json");

        Response response = request.delete(baseUrl + endpoint);
        logRequestAndResponse(test, method, baseUrl + endpoint, requestBody, request, response);
        return response;
    }

     /**
     * Simulates an unauthorized creation call using INVALID_TOKEN.
     */
    public Response createRepositoryUnauthorizedResponse(String repoName, ExtentTest test) {
        String method = "POST";
        String endpoint = "/user/repos";

        String payload = String.format(
            "{\"name\":\"%s\",\"description\":\"Repository created without authorization\",\"private\":false}",
            repoName
        );

        test.log(Status.INFO, "Attempting to create repository without authorization: " + repoName);

        RequestSpecification request = given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer INVALID_TOKEN")
            .header("Accept", "application/vnd.github.v3+json")
            .contentType("application/json")
            .body(payload);

        Response response = request.post(baseUrl + endpoint);
        logRequestAndResponse(test, method, baseUrl + endpoint, payload, request, response);
        return response;
    }

    /**
     * Logs request/response details into a single code block in Extent.
     */
    private void logRequestAndResponse(
        ExtentTest test,
        String method,
        String uri,
        String requestBody,
        RequestSpecification requestSpec,
        Response response
    ) {
        String finalRequestBody = (requestBody == null) ? "No body" : requestBody;
        int statusCode = response.getStatusCode();
        String responseBody = response.prettyPrint();

        StringBuilder logBuilder = new StringBuilder()
            .append("Request:\n")
            .append("Method: ").append(method).append("\n")
            .append("URI: ").append(uri).append("\n")
            .append("Body:\n").append(finalRequestBody).append("\n\n")
            .append("Response:\n")
            .append("Status Code: ").append(statusCode).append("\n")
            .append("Body:\n").append(responseBody);

        test.info(MarkupHelper.createCodeBlock(logBuilder.toString()));
    }

    /**
     * Simulates unauthorized issue creation by using an invalid token.
     */
    public Response createIssueUnauthorizedResponse(String owner, String repo, String title, String bodyText, ExtentTest test) {
        String method = "POST";
        String endpoint = String.format("/repos/%s/%s/issues", owner, repo);

        JSONObject issueDetails = new JSONObject();
        issueDetails.put("title", title);
        issueDetails.put("body", bodyText);

        String requestBody = issueDetails.toString();

        test.log(Status.INFO, "Attempting to create issue without authorization: " + title);

        RequestSpecification request = given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer INVALID_TOKEN")
            .header("Accept", "application/vnd.github.v3+json")
            .contentType("application/json")
            .body(requestBody);

        Response response = request.post(baseUrl + endpoint);
        logRequestAndResponse(test, method, baseUrl + endpoint, requestBody, request, response);
        return response;
    }

    public Response closeIssueResponse(String owner, String repo, String issueNumber, ExtentTest test) {
        String method = "PATCH";
        String endpoint = String.format("/repos/%s/%s/issues/%s", owner, repo, issueNumber);

        JSONObject issueDetails = new JSONObject();
        issueDetails.put("state", "closed");

        String requestBody = issueDetails.toString();

        RequestSpecification request = given()
            .header("Authorization", "Bearer " + token)
            .header("Accept", "application/vnd.github.v3+json")
            .contentType("application/json")
            .body(requestBody);

        Response response = request.patch(baseUrl + endpoint);
        logRequestAndResponse(test, method, baseUrl + endpoint, requestBody, request, response);
        return response;
    }
}