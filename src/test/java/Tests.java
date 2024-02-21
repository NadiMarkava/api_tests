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

    private static final String TOKEN = "";
    private static final String usersUrl = "https://gorest.co.in/public/v2/users";
    private static final String commentsUrl = "https://gorest.co.in/public/v2/comments";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void getUsers() throws IOException, InterruptedException, URISyntaxException {
        File file = new File("src/test/resources/api/users/_get/rs.json");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(usersUrl))
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .build();
        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        List<User> userListFromResponse = objectMapper.readValue(response.body(), new TypeReference<List<User>>(){});
        for (User user : userListFromResponse) {
            SoftAssert softAssert = new SoftAssert();
            softAssert.assertTrue(String.valueOf(user.getId()).matches("^\\d{7}$"), "Id does not match" + user.getId());
            softAssert.assertTrue(user.getName().matches("[a-zA-Z]+(\\.)?\\s[a-zA-Z]+(\\.)?(\\s[a-zA-Z]+(\\.)?)?+(\\s[a-zA-Z])?"), "Name does not match" + user.getName());
            softAssert.assertTrue(user.getEmail().matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,9}$"), "Email does not match" + user.getEmail());
            softAssert.assertTrue(user.getGender().matches("(?:male|female)$"), "Gender does not match" + user.getGender());
            softAssert.assertTrue(user.getStatus().matches("(?:inactive|active)$"), "Status does not match" + user.getStatus());
            softAssert.assertAll();
        }
    }

    @Test
    public void getUserById() throws IOException, InterruptedException, URISyntaxException {
        String rqId  = "5913765";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(usersUrl + "/" + rqId))
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .build();
        HttpResponse<String> response = buildResponse(request);
        int rsId = getIdFromResponse(response);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        assertEquals(rsId, Integer.valueOf(rqId));
    }

    @Test
    public void postUser() throws IOException, InterruptedException, URISyntaxException {
        String postPath = "src/test/resources/api/users/_post/rq.json";
        File file = new File(postPath);
        User rqUser = objectMapper.readValue(file, User.class);
        HttpRequest request = buildPostRequest(usersUrl, postPath);
        HttpResponse<String> response = buildResponse(request);
        String res =response.body();
        User rsUser = objectMapper.readValue(res, User.class);
        int id = rsUser.getId();
        rqUser.setId(id);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        assertTrue(rqUser.equals(rsUser), "Users are not equal");
    }

    @Test
    public void putUser() throws IOException, InterruptedException, URISyntaxException {
        String putPath = "src/test/resources/api/users/_put/rq.json";
        String postPath = "src/test/resources/api/users/_post_for_put/rq.json";
        HttpRequest request = buildPostRequest(usersUrl, postPath);
        HttpResponse<String> response = buildResponse(request);
        int id = getIdFromResponse(response);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        request = buildPutRequest(usersUrl, id, putPath);
        response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        String resPut =response.body();
        User rsPutUser = objectMapper.readValue(resPut, User.class);
        User rqPutUser = objectMapper.readValue(new File(putPath), User.class);
        rqPutUser.setId(id);
        assertTrue(rqPutUser.equals(rsPutUser), "Users are not equal");
    }

    @Test
    public void deleteUser() throws IOException, InterruptedException, URISyntaxException {
        String postPath = "src/test/resources/api/users/_post_for_put/rq.json";
        HttpRequest request = buildPostRequest(usersUrl, postPath);
        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        int id = getIdFromResponse(response);
        String newUrl = usersUrl + "/" + String.valueOf(id);
        request = HttpRequest.newBuilder()
                .uri(new URI(newUrl))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .DELETE()
                .build();
        response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertTrue(response.body().isEmpty());
    }

    @Test
    public void postUserWithMissingFields() throws IOException, InterruptedException, URISyntaxException {
        String postPath = "src/test/resources/api/users/_post_miss_fields/rq.json";
        HttpRequest request = buildPostRequest(usersUrl, postPath);
        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), 422);
        String rsBody = response.body();
        assertTrue(rsBody.contains("can't be blank, can be male of female"), "ErrorMessage ");
        assertTrue(rsBody.contains("{\"field\":\"status\",\"message\":\"can't be blank\"}]"), "ErrorMessage ");
    }

    @Test
    public void postComment() throws IOException, InterruptedException, URISyntaxException {
        String postPath = "src/test/resources/api/users/_post_comment/rq.json";
        Comment rqComment = objectMapper.readValue(new File(postPath), Comment.class);
        HttpRequest request = buildPostRequest(commentsUrl, postPath);
        HttpResponse<String> response = buildResponse(request);
        String res =response.body();
        Comment rsComment = objectMapper.readValue(res, Comment.class);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        assertEquals(rqComment.getName(), rqComment.getName(), "Names are not equal");
        assertEquals(rqComment.getBody(), rqComment.getBody(), "Bodies are not equal");
    }

    @Test
    public void putComment() throws IOException, InterruptedException, URISyntaxException {
        String postPath = "src/test/resources/api/users/_post_comment/rq.json";
        String commentUrl = "https://gorest.co.in/public/v2/comments";
        String putPath = "src/test/resources/api/users/_put_comment/rq.json";
        HttpRequest request = buildPostRequest(commentsUrl, postPath);
        HttpResponse<String> response = buildResponse(request);
        Comment rsComment = objectMapper.readValue(response.body(), Comment.class);
        int id = rsComment.getId();
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        request = buildPutRequest(commentUrl, id, putPath);
        response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);
        String resPut =response.body();
        Comment rsPutComment = objectMapper.readValue(resPut, Comment.class);
        Comment rqPutComment = objectMapper.readValue(new File(putPath), Comment.class);
        assertEquals(rqPutComment.getBody(), rsPutComment.getBody(), "Comment does not update");
    }

    @Test
    public void deleteComment() throws IOException, InterruptedException, URISyntaxException {
        String postPath = "src/test/resources/api/users/_post_comment/rq.json";
        String commentUrl = "https://gorest.co.in/public/v2/comments";
        HttpRequest request = buildPostRequest(commentsUrl, postPath);
        HttpResponse<String> response = buildResponse(request);
        Comment rsComment = objectMapper.readValue(response.body(), Comment.class);
        int id = rsComment.getId();
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        String newUrl = commentUrl + "/" + String.valueOf(id);
        request = HttpRequest.newBuilder()
                .uri(new URI(newUrl))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .DELETE()
                .build();
        response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertTrue(response.body().isEmpty());
    }

    public int getIdFromResponse(HttpResponse<String> response) throws JsonProcessingException {
        String res = response.body();
        User rsUser = objectMapper.readValue(res, User.class);
        int id = rsUser.getId();
        return id;
    }

    public HttpRequest buildPostRequest(String url, String postPath) throws URISyntaxException, FileNotFoundException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofFile(Paths.get(postPath)))
                .build();
        return request;
    }

    public HttpRequest buildPutRequest(String url, int id, String putPath) throws URISyntaxException, FileNotFoundException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url + "/" + String.valueOf(id)))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .PUT(HttpRequest.BodyPublishers.ofFile(Paths.get(putPath)))
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
}
