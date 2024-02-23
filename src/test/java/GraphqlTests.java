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

    private static final String usersUrl = "https://gorest.co.in/public/v2/graphql";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TOKEN = "";

    @Test
    public void getUsers() throws IOException, InterruptedException, URISyntaxException {
        String getPath = "src/test/resources/api/graphql/_get/rq.json";
        HttpRequest request = buildRequest(getPath);
        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        String r = response.body();
        List<Map<String, Object>>userList = JsonPath.from(response.body()).getList("data.users.nodes");
        for (Map<String, Object> map : userList) {
                SoftAssert softAssert = new SoftAssert();
                softAssert.assertTrue(map.get("id").toString().matches("^\\d{7}$"), "Id does not match");
                softAssert.assertTrue(map.get("name").toString().matches("[a-zA-Z]+(\\.)?\\s[a-zA-Z]+(\\.)?(\\s[a-zA-Z]+(\\.)?)?+(\\s[a-zA-Z])?"), "Name does not match");
                softAssert.assertTrue(map.get("email").toString().matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,9}$"), "Email does not match");
                softAssert.assertTrue(map.get("gender").toString().matches("(?:male|female)$"), "Gender does not match");
                softAssert.assertTrue(map.get("status").toString().matches("(?:inactive|active)$"), "Status does not match");
                softAssert.assertAll();
            }
    }

    @Test
    public void getUserById() throws IOException, InterruptedException, URISyntaxException {
            String getByIdPath = "src/test/resources/api/graphql/_get_by_id/rq.json";
            String requestTmp = Files.readString(Path.of(getByIdPath));
            String id = "5913607";
            String rq = String.format(requestTmp, id);
            HttpRequest request = buildRequest(rq);
            HttpResponse<String> response = buildResponse(request);
            assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
            Map<String, Object> userParams = JsonPath.from(response.body()).getMap("data.user");
            SoftAssert softAssert = new SoftAssert();
            int responseId = (int) userParams.get("id");
            softAssert.assertTrue(responseId == Integer.valueOf(id), "Id does not match");
            softAssert.assertTrue(userParams.get("name").toString().matches("[a-zA-Z]+(\\.)?\\s[a-zA-Z]+(\\.)?(\\s[a-zA-Z]+(\\.)?)?+(\\s[a-zA-Z])?"), "Name does not match");
            softAssert.assertTrue(userParams.get("email").toString().matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,9}$"), "Email does not match");
            softAssert.assertTrue(userParams.get("gender").toString().matches("(?:male|female)$"), "Gender does not match");
            softAssert.assertTrue(userParams.get("status").toString().matches("(?:inactive|active)$"), "Status does not match");
            softAssert.assertAll();
    }

    @Test
    public void postUser() throws IOException, InterruptedException, URISyntaxException {
        String postPath = "src/test/resources/api/graphql/_post/rq.json";
        String name = "Tomas";
        String email = "tom@kruz.example";
        String gender = "male";
        String status = "active";
        String rq = getPath(postPath, name, email, gender, status);
        HttpRequest request = buildRequest(rq);
        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        String rs = response.body();
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
        String postPath = "src/test/resources/api/graphql/_post/rq.json";
        String putPath = "src/test/resources/api/graphql/_put/rq.json";
        String name = "Andreas";
        String email = "tom@kruz.example";
        String gender = "male";
        String status = "active";
        String postRq = String.format(postPath, name, email, gender, status);
        HttpRequest request = buildRequest(postRq);
        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        String idFromResponse = JsonPath.from(response.body()).getString("data.createUser.user.id");
        String nameToUpdate = "Sara";
        String emailToUpdate = "sara@hermas.donut";
        String genderToUpdate = "female";
        String statusToUpdate = "inactive";
        String putRq = String.format(putPath, idFromResponse, nameToUpdate, emailToUpdate, genderToUpdate, statusToUpdate);
        request = buildRequest(putRq);
        response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        String rs = response.body();
        String rsPath = "src/test/resources/api/graphql/_put/rs.json";
        String expectedRs = Files.readString(Path.of(rsPath));
        assertEquals(expectedRs, rs, "Responses are not equal");
    }

    @Test
    public void deleteUser() throws IOException, InterruptedException, URISyntaxException {
        String deletePath = "src/test/resources/api/graphql/_delete/rq.json";
        String pathTmp = "src/test/resources/api/graphql/_post/rq.json";
        String postRq = getPath(pathTmp, "Rayn", "rayn@red.example", "male", "inactive");
        HttpRequest request = buildRequest(postRq);
        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        String idFromResponse = JsonPath.from(response.body()).getString("data.createUser.user.id");
        String requestTmp = Files.readString(Path.of(deletePath));
        String rq = String.format(requestTmp, idFromResponse);
        request = buildRequest(rq);
        response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        String rs = response.body();
        String rsPath = "src/test/resources/api/graphql/_delete/rs.json";
        String expectedRs = Files.readString(Path.of(rsPath));
        assertEquals(expectedRs, rs, "Responses are not equal");
    }

    public HttpResponse<String> buildResponse(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    public HttpRequest buildRequest(String path) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(usersUrl))
                .header("Authorization", "Bearer " + TOKEN)
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
}
