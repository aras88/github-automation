# GitHub Automation Project

This project is a Java-based automation framework for interacting with GitHub's API. It supports functionalities like creating issues, managing repositories, listing repositories, and fetching user profiles. Built with **Spring Boot**, **Rest Assured**, and **JUnit 5**, it integrates **Extent Reports** for detailed HTML reporting.

---

## Features

- **Create Issue**: Programmatically create issues in repositories.
- **Create Repository**: Create GitHub repositories with various configurations.
- **List Repositories**: Retrieve a list of repositories for the authenticated user.
- **Get User Profile**: Fetch user profile details from the GitHub API.
- **Close Issues**: Close specific issues in repositories.
- **Integration with Extent Reports**: Detailed, visualized test reports in HTML.

---

## Project Structure

```
src
├── main
│   ├── java
│   │   ├── com.github
│   │   │   ├── config
│   │   │   │   └── GitHubProperties.java  # Configuration handler for GitHub.
│   │   │   ├── listener
│   │   │   │   └── ExtentJUnit5Extension.java  # Integrates Extent Reports with JUnit 5.
│   │   │   ├── model
│   │   │   │   ├── Repository.java  # Represents GitHub repositories.
│   │   │   │   └── UserProfile.java  # Represents a GitHub user profile.
│   │   │   ├── service
│   │   │   │   └── GitHubApiClient.java  # Handles API calls to GitHub.
│   │   │   └── Application.java  # Entry point for Spring Boot.
│   └── resources
│       └── application.yml  # Configuration file for GitHub credentials.
├── test
│   ├── java
│   │   ├── com.github.tests
│   │   │   ├── CreateIssueTest.java  # Tests for creating issues.
│   │   │   ├── CreateRepositoryTest.java  # Tests for creating repositories.
│   │   │   ├── DeleteRepositoryTest.java  # Tests for deleting repositories.
│   │   │   ├── ListRepositoriesTest.java  # Tests for listing repositories.
│   │   │   └── UserProfileTest.java  # Tests for fetching user profiles.
└── pom.xml  # Maven configuration for dependencies.
```

---

## Prerequisites

Before running this project, ensure you have:

1. **Java 21** or a compatible JDK version installed.
2. **Maven 3.8+** installed.
3. A **GitHub Personal Access Token (PAT)** with the required scopes:
   - `repo`: For repository-related operations.
   - `user`: For accessing user profiles.

---

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd github-automation
```

### 2. Configure GitHub Credentials

Edit the `application.yml` file in `src/main/resources`:

```yaml
github:
  api:
    baseurl: https://api.github.com
    token: your-github-token  # Replace with your GitHub Personal Access Token.
  default:
    owner: your-github-username  # Replace with your GitHub username.
    repo: your-default-repo  # Replace with the name of your default repository.
```

### 3. Install Dependencies

Run the following command:

```bash
mvn clean install
```

---

## Running the Tests

You can execute the tests using Maven. Results are saved in the `test-output` directory.

### Run All Tests

```bash
mvn test
```

### View Reports

After tests complete, open the HTML report:

```bash
open test-output/ExtentReports.html
```

---

## Test Suite

The framework includes the following tests:

| Test Case                                           | Description                                         |
|-----------------------------------------------------|-----------------------------------------------------|
| `testDeleteNonExistentRepository_Failure`           | Fails when attempting to delete a nonexistent repository. |
| `testDeleteRepository_Success`                      | Successfully deletes a repository.                 |
| `testDeleteRepositoryWithoutAuthorization_Failure`  | Ensures deletion fails without valid authorization. |
| `testCreateNewRepository_Success`                   | Successfully creates a new repository.             |
| `testCreateRepositoryWithInvalidName_Failure`       | Fails when creating a repository with an invalid name. |
| `testCreateRepositoryWithDuplicateName_Failure`     | Fails when creating a repository with a duplicate name. |
| `testGetUserProfile_Success`                        | Fetches user profile details successfully.         |
| `testCreateIssue_Success`                           | Creates an issue in an existing repository.        |
| `testCreateIssueWithoutTitle_Failure`               | Fails when creating an issue without a title.      |
| `testCreateIssueWithoutAuthorization_Failure`       | Ensures issue creation fails without authorization. |

---

## Key Files and Methods

### Core Files

- **`GitHubApiClient.java`**:
  - `createIssueResponse(String owner, String repo, String title, String bodyText, ExtentTest test)`
  - `deleteRepositoryResponse(String repoName, ExtentTest test)`
  - `listRepositoriesResponse(ExtentTest test)`
  - `closeIssueResponse(String owner, String repo, String issueNumber, ExtentTest test)`

- **`GitHubProperties.java`**:
  Handles credentials and default properties like `owner` and `repo`.

- **`ExtentJUnit5Extension.java`**:
  Integrates Extent Reports into JUnit 5 tests.

---

## Example Usage

### Creating an Issue

```java
Response response = gitHubApiClient.createIssueResponse("owner", "repo", "Issue Title", "Issue body text", test);
```

### Closing an Issue

```java
Response response = gitHubApiClient.closeIssueResponse("owner", "repo", "issue-number", test);
```

### Deleting a Repository

```java
Response response = gitHubApiClient.deleteRepositoryResponse("repo-name", test);
```

---

## Technologies Used

- **Spring Boot**: Dependency management and configuration.
- **Rest Assured**: API testing library.
- **JUnit 5**: Modern testing framework.
- **Extent Reports**: HTML-based reporting.
- **Maven**: Build and dependency management.

---

## License

This project is licensed under the [MIT License](LICENSE).

---

## Troubleshooting

1. **Missing Java or Maven**:
   - Ensure `JAVA_HOME` is set correctly.
   - Install Maven 3.8+.

2. **GitHub API Errors**:
   - Confirm your GitHub PAT has required scopes.

3. **View Logs**:
   - All requests and responses are logged to Extent Reports for easy debugging.
