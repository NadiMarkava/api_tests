package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GraphqlTests {


    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void getUsers() throws IOException, InterruptedException, URISyntaxException {
        String rq = Files.readString(Path.of(Constants.BASIC_PATH_GRAPHQL + "_get/rq.json"));
        HttpRequest request = buildRequest(rq);
        HttpResponse<String> response = sendRequest(request);
        List<Map<String, Object>> userList = JsonPath.from(response.body()).getList("data.users.nodes");
        for (Map<String, Object> userData : userList) {
            verifyValidData(userData);
        }
    }

    @Test
    public void getUserById() throws IOException, InterruptedException, URISyntaxException {
        String getByIdPath = Constants.BASIC_PATH_GRAPHQL + "_get_by_id/rq.json";
        String requestTmp = Files.readString(Path.of(getByIdPath));
        String id = "5850445";
        String rq = String.format(requestTmp, id);
        HttpRequest request = buildRequest(rq);
        HttpResponse<String> response = sendRequest(request);
        Map<String, Object> userData = JsonPath.from(response.body()).getMap("data.user");
        verifyValidData(userData);
    }

    @Test
    public void postUser() throws IOException, InterruptedException, URISyntaxException {
        String name = "Tomas";
        String email = "tom@kruz.example";
        String gender = "male";
        String status = "active";
        String postPath = Constants.BASIC_PATH_GRAPHQL + "_post/rq.json";
        String rq = getPath(postPath, name, email, gender, status);
        HttpRequest request = buildRequest(rq);
        HttpResponse<String> response = sendRequest(request);
        String idFromResponse = JsonPath.from(response.body()).getString("data.createUser.user.id");
        int id = Integer.valueOf(idFromResponse);
        User requestUser = new User(id, name, email, gender, status);
        Map<String, String> userParams = JsonPath.from(response.body()).getMap("data.createUser.user");
        User responseUser = new User();
        responseUser.setId(id);
        responseUser.setName(userParams.get("name"));
        responseUser.setEmail(userParams.get("email"));
        responseUser.setGender(userParams.get("gender"));
        responseUser.setStatus(userParams.get("status"));
        assertTrue(requestUser.equals(responseUser), "Users are not equal");
    }

    @Test
    public void putUser() throws IOException, InterruptedException, URISyntaxException {
        String putPath = Constants.BASIC_PATH_GRAPHQL + "_put/rq.json";
        String userId = postUser("Andreas", "tom@kruz.example", "male", "active");
        String requestTmp = Files.readString(Path.of(putPath));
        String putRq = String.format(requestTmp, userId, "Sara", "sara@hermas.donut", "female", "inactive");
        HttpRequest request = buildRequest(putRq);
        HttpResponse<String> response = sendRequest(request);
        String rs = response.body();
        String rsPath = "src/test/resources/api/graphql/_put/rs.json";
        String expectedRs = Files.readString(Path.of(rsPath));
        assertEquals(expectedRs, rs, "Responses are not equal");
    }

    @Test
    public void deleteUser() throws IOException, InterruptedException, URISyntaxException {
        String deletePath = Constants.BASIC_PATH_GRAPHQL + "_delete/rq.json";
        String userId = postUser("Rayn", "rayn@red.example", "male", "inactive");
        String requestTmp = Files.readString(Path.of(deletePath));
        String rq = String.format(requestTmp, userId);
        HttpRequest request = buildRequest(rq);
        HttpResponse<String> response = sendRequest(request);
        String rsPath = Constants.BASIC_PATH_GRAPHQL + "_delete/rs.json";
        String expectedRs = Files.readString(Path.of(rsPath));
        assertEquals(expectedRs, response.body(), "Responses are not equal");
    }

    public HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        return response;
    }

    public HttpRequest buildRequest(String path) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(Constants.URL_GRAPHQL))
                .header("Authorization", "Bearer " + Constants.TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(path))
                .build();
        return request;
    }

    public String getPath(String pathTmp, String name, String email, String gender, String status) throws IOException {
        String requestTmp = Files.readString(Path.of(pathTmp));
        String path = String.format(requestTmp, name, email, gender, status);
        return path;
    }

    public String postUser(String name, String email, String gender, String status) throws IOException, URISyntaxException, InterruptedException {
        String pathTmp = Constants.BASIC_PATH_GRAPHQL + "_post/rq.json";
        String postRq = getPath(pathTmp, name, email, gender, status);
        HttpRequest request = buildRequest(postRq);
        HttpResponse<String> response = sendRequest(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        String userId = JsonPath.from(response.body()).getString("data.createUser.user.id");
        return userId;
    }

    public void verifyValidData(Map<String, Object> user) {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(user.get("id").toString().matches("^\\d{7}$"), "Id does not match");
        softAssert.assertTrue(user.get("name").toString().matches("[a-zA-Z]+(\\.)?\\s[a-zA-Z]+(\\.)?(\\s[a-zA-Z]+(\\.)?)?+(\\s[a-zA-Z])?"), "Name does not match");
        softAssert.assertTrue(user.get("email").toString().matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,9}$"), "Email does not match");
        softAssert.assertTrue(user.get("gender").toString().matches("(?:male|female)$"), "Gender does not match");
        softAssert.assertTrue(user.get("status").toString().matches("(?:inactive|active)$"), "Status does not match");
        softAssert.assertAll();
    }
}
