import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.File;
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
    private static final String baseUrl = "https://gorest.co.in/public/v2/users";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void getUsers() throws IOException, InterruptedException, URISyntaxException {
        File file = new File("src/test/resources/api/users/_get/rs.json");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl))
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .build();

        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);

        List<User> userListFromResponse = objectMapper.readValue(response.body(), new TypeReference<List<User>>(){});
        for (User user : userListFromResponse) {
            SoftAssert softAssert = new SoftAssert();
            softAssert.assertTrue(String.valueOf(user.getId()).matches("^\\d{7}$"), "Id does not match" + user.getId());
            softAssert.assertTrue(user.getName().matches("[a-zA-Z]+(\\.)?\\s[a-zA-Z]+(\\.)?(\\s[a-zA-Z]+(\\.)?)?"), "Name does not match" + user.getName());
            softAssert.assertTrue(user.getEmail().matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,9}$"), "Email does not match" + user.getEmail());
            softAssert.assertTrue(user.getGender().matches("(?:male|female)$"), "Gender does not match" + user.getGender());
            softAssert.assertTrue(user.getStatus().matches("(?:inactive|active)$"), "Status does not match" + user.getStatus());
            softAssert.assertAll();
        }
    }

    @Test
    public void getUserById() throws IOException, InterruptedException, URISyntaxException {
        String rqId  = "6313246";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/" + rqId))
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

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofFile(Paths.get(postPath)))
                .build();

        HttpResponse<String> response = buildResponse(request);

        String res =response.body();
        User rsUser = objectMapper.readValue(res, User.class);
        int id = rsUser.getId();
        rqUser.setId(id);

        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        compareUserData(rqUser, rsUser);
    }

    @Test
    public void putUser() throws IOException, InterruptedException, URISyntaxException {
        String putPath = "src/test/resources/api/users/_put/rq.json";
        String postPath = "src/test/resources/api/users/_post_for_put/rq.json";
        //post user
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofFile(Paths.get(postPath)))
                .build();

        HttpResponse<String> response = buildResponse(request);
        int id = getIdFromResponse(response);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);

        //put methode
        request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/" + String.valueOf(id)))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .PUT(HttpRequest.BodyPublishers.ofFile(Paths.get(putPath)))
                .build();

        response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);

        String resPut =response.body();
        User rsPutUser = objectMapper.readValue(resPut, User.class);
        User rqPutUser = objectMapper.readValue(new File(putPath), User.class);
        rqPutUser.setId(id);
        compareUserData(rqPutUser, rsPutUser);
    }

    @Test
    public void deleteUser() throws IOException, InterruptedException, URISyntaxException {
        String postPath = "src/test/resources/api/users/_post/rq.json";
        //post user
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofFile(Paths.get(postPath)))
                .build();

        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        int id = getIdFromResponse(response);

        String newUrl = baseUrl + "/" + String.valueOf(id);
        //delete user
        request = HttpRequest.newBuilder()
                .uri(new URI(newUrl))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .DELETE()
                .build();

        response = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertTrue(response.body().isEmpty());
    }

    @Test
    public void postUserWithMissingFields() throws IOException, InterruptedException, URISyntaxException {
        String errorMessage = "[{\"field\":\"gender\",\"message\":\"can't be blank, can be male of female\"},{\"field\":\"status\",\"message\":\"can't be blank\"}]";
        //post user
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofFile(new File("src/test/resources/api/users/_post_miss_fields/rq.json").toPath()))
                .build();

        HttpResponse<String> response = buildResponse(request);
        assertEquals(response.statusCode(), 422);
        assertEquals(response.body(), errorMessage);
    }

    @Test
    public void postComment() throws IOException, InterruptedException, URISyntaxException {
        String postPath = "src/test/resources/api/users/_post_comment/rq.json";
        File file = new File(postPath);
        Comment rqComment = objectMapper.readValue(file, Comment.class);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://gorest.co.in/public/v2/comments"))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofFile(Paths.get(postPath)))
                .build();

        HttpResponse<String> response = buildResponse(request);

        String res =response.body();
        Comment rsComment = objectMapper.readValue(res, Comment.class);

        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);
        assertEquals(rqComment.getName(), rqComment.getName());
        assertEquals(rqComment.getBody(), rqComment.getBody());
    }

    @Test
    public void putComment() throws IOException, InterruptedException, URISyntaxException {
        String postPath = "src/test/resources/api/users/_post_comment/rq.json";
        String commentUrl = "https://gorest.co.in/public/v2/comments";
        String comment = "{\n" +
                "   \"body\":\"This is a wonderful story! Author wrote amazing\"\n" +
                "}";
        //post user
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(commentUrl))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofFile(Paths.get(postPath)))
                .build();

        HttpResponse<String> response = buildResponse(request);
        Comment rsComment = objectMapper.readValue(response.body(), Comment.class);
        int id = rsComment.getId();
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);

        //put methode
        request = HttpRequest.newBuilder()
                .uri(new URI(commentUrl + "/" + String.valueOf(id)))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .PUT(HttpRequest.BodyPublishers.ofString(comment))
                .build();

        response = buildResponse(request);
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK);

        String resPut =response.body();
        Comment rsPutComment = objectMapper.readValue(resPut, Comment.class);
        assertTrue(comment.contains(rsPutComment.getBody()), "Comment does not update");
    }

    @Test
    public void deleteComment() throws IOException, InterruptedException, URISyntaxException {
        String postPath = "src/test/resources/api/users/_post_comment/rq.json";
        String commentUrl = "https://gorest.co.in/public/v2/comments";
        //post user
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(commentUrl))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofFile(Paths.get(postPath)))
                .build();

        HttpResponse<String> response = buildResponse(request);
        Comment rsComment = objectMapper.readValue(response.body(), Comment.class);
        int id = rsComment.getId();
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED);

        String newUrl = commentUrl + "/" + String.valueOf(id);
        //delete user
        request = HttpRequest.newBuilder()
                .uri(new URI(newUrl))
                .header("Authorization", "Bearer " + TOKEN)
                .version(HttpClient.Version.HTTP_2)
                .DELETE()
                .build();

        response = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertTrue(response.body().isEmpty());
    }

    public int getIdFromResponse(HttpResponse<String> response) throws JsonProcessingException {
        String res = response.body();
        User rsUser = objectMapper.readValue(res, User.class);
        int id = rsUser.getId();
        return id;
    }

    public HttpResponse<String> buildResponse(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    public void compareUserData(User rqUser, User rsUser) {
        assertEquals(rqUser.getId(), rsUser.getId());
        assertEquals(rqUser.getName(), rsUser.getName());
        assertEquals(rqUser.getEmail(), rsUser.getEmail());
        assertEquals(rqUser.getGender(), rsUser.getGender());
        assertEquals(rqUser.getStatus(), rsUser.getStatus());
    }
}
