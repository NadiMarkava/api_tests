import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.List;

import static org.testng.Assert.*;

public class Tests {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void getUsers() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = buildGetRequest(Constants.URL_USERS);
        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        List<User> userListFromResponse = objectMapper.readValue(response.body(), new TypeReference<List<User>>() {});
        for (User user : userListFromResponse) {
            verifyValidData(user);
        }
    }

    @Test
    public void getUserById() throws IOException, InterruptedException, URISyntaxException {
        String rqId = "5850495";
        HttpRequest request = buildGetRequest(Constants.URL_USERS + "/" + rqId);
        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        int rsId = getUserIdFromResponse(response);
        User user = objectMapper.readValue(response.body(), new TypeReference<User>() {});
        assertEquals(rsId, Integer.valueOf(rqId));
        verifyValidData(user);
    }

    @Test
    public void postUser() throws IOException, InterruptedException, URISyntaxException {
        String postPath = Constants.BASIC_PATH_USERS + "_post/rq.json";
        User rqUser = objectMapper.readValue(new File(postPath), User.class);
        HttpRequest request = buildPostRequest(Constants.URL_USERS, postPath);
        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        User rsUser = objectMapper.readValue(response.body(), User.class);
        rqUser.setId(rsUser.getId());
        assertTrue(rqUser.equals(rsUser), "Users are not equal");
    }

    @Test
    public void putUser() throws IOException, InterruptedException, URISyntaxException {
        String putPath = Constants.BASIC_PATH_USERS + "_put/rq.json";
        String postPath = Constants.BASIC_PATH_USERS + "_post_for_put/rq.json";
        HttpRequest request = buildPostRequest(Constants.URL_USERS, postPath);
        HttpResponse<String> response = buildResponse(request);
        int id = getUserIdFromResponse(response);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        request = buildPutRequest(Constants.URL_USERS, id, putPath);
        response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        String resPut = response.body();
        User rsPutUser = objectMapper.readValue(resPut, User.class);
        User rqPutUser = objectMapper.readValue(new File(putPath), User.class);
        rqPutUser.setId(id);
        assertTrue(rqPutUser.equals(rsPutUser), "Users are not equal");
    }

    @Test
    public void deleteUser() throws IOException, InterruptedException, URISyntaxException {
        String postPath = Constants.BASIC_PATH_USERS + "_post_for_put/rq.json";
        HttpRequest request = buildPostRequest(Constants.URL_USERS, postPath);
        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        int id = getUserIdFromResponse(response);
        String newUrl = Constants.URL_USERS + "/" + String.valueOf(id);
        request = buildDeleteRequest(newUrl);
        response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertTrue(response.body().isEmpty());
    }

    @Test
    public void postUserWithMissingFields() throws IOException, InterruptedException, URISyntaxException {
        String postPath = Constants.BASIC_PATH_USERS + "_post_miss_fields/rq.json";
        HttpRequest request = buildPostRequest(Constants.URL_USERS, postPath);
        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), 422);
        String rsBody = response.body();
        assertTrue(rsBody.contains("can't be blank, can be male of female"), "ErrorMessage ");
        assertTrue(rsBody.contains("{\"field\":\"status\",\"message\":\"can't be blank\"}]"), "ErrorMessage ");
    }

    @Test
    public void postComment() throws IOException, InterruptedException, URISyntaxException {
        String postPath = Constants.BASIC_PATH_USERS + "_post_comment/rq.json";
        Comment rqComment = objectMapper.readValue(new File(postPath), Comment.class);
        HttpRequest request = buildPostRequest(Constants.URL_COMMENTS, postPath);
        HttpResponse<String> response = buildResponse(request);
        String res = response.body();
        Comment rsComment = objectMapper.readValue(res, Comment.class);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        assertEquals(rqComment.getName(), rsComment.getName(), "Names are not equal");
        assertEquals(rqComment.getBody(), rsComment.getBody(), "Bodies are not equal");
    }

    @Test
    public void putComment() throws IOException, InterruptedException, URISyntaxException {
        String postPath = Constants.BASIC_PATH_USERS + "_post_comment/rq.json";
        String putPath = Constants.BASIC_PATH_USERS + "_put_comment/rq.json";
        HttpRequest request = buildPostRequest(Constants.URL_COMMENTS, postPath);
        HttpResponse<String> response = buildResponse(request);
        Comment rsComment = objectMapper.readValue(response.body(), Comment.class);
        int id = rsComment.getId();
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        request = buildPutRequest(Constants.URL_COMMENTS, id, putPath);
        response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        String resPut = response.body();
        Comment rsPutComment = objectMapper.readValue(resPut, Comment.class);
        Comment rqPutComment = objectMapper.readValue(new File(putPath), Comment.class);
        assertEquals(rqPutComment.getBody(), rsPutComment.getBody(), "Comment does not update");
    }

    @Test
    public void deleteComment() throws IOException, InterruptedException, URISyntaxException {
        String postPath = Constants.BASIC_PATH_USERS + "_post_comment/rq.json";
        HttpRequest request = buildPostRequest(Constants.URL_COMMENTS, postPath);
        HttpResponse<String> response = buildResponse(request);
        Comment rsComment = objectMapper.readValue(response.body(), Comment.class);
        int id = rsComment.getId();
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        String newUrl = Constants.URL_COMMENTS + "/" + String.valueOf(id);
        request = buildDeleteRequest(newUrl);
        response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertTrue(response.body().isEmpty());
    }

    public int getUserIdFromResponse(HttpResponse<String> response) throws JsonProcessingException {
        String res = response.body();
        User rsUser = objectMapper.readValue(res, User.class);
        int id = rsUser.getId();
        return id;
    }

    public HttpRequest buildGetRequest(String url) throws URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .build();
        return request;
    }

    public HttpRequest buildPostRequest(String url, String postPath) throws URISyntaxException, FileNotFoundException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", "Bearer " + Constants.TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofFile(Paths.get(postPath)))
                .build();
        return request;
    }

    public HttpRequest buildPutRequest(String url, int id, String putPath) throws URISyntaxException, FileNotFoundException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url + "/" + String.valueOf(id)))
                .header("Authorization", "Bearer " + Constants.TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .PUT(HttpRequest.BodyPublishers.ofFile(Paths.get(putPath)))
                .build();
        return request;
    }

    public HttpRequest buildDeleteRequest(String url) throws URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", "Bearer " + Constants.TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .DELETE()
                .build();
        return request;
    }

    public HttpResponse<String> buildResponse(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    public void verifyValidData(User user) {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(String.valueOf(user.getId()).matches("^\\d{7}$"), "Id does not match" + user.getId());
        softAssert.assertTrue(user.getName().matches("[a-zA-Z]+(\\.)?\\s[a-zA-Z]+(\\.)?(\\s[a-zA-Z]+(\\.)?)?+(\\s[a-zA-Z])?"), "Name does not match" + user.getName());
        softAssert.assertTrue(user.getEmail().matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,9}$"), "Email does not match" + user.getEmail());
        softAssert.assertTrue(user.getGender().matches("(?:male|female)$"), "Gender does not match" + user.getGender());
        softAssert.assertTrue(user.getStatus().matches("(?:inactive|active)$"), "Status does not match" + user.getStatus());
        softAssert.assertAll();
    }
}
