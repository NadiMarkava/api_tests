package api;

public class Comment {

    private int id;
    private int postId;
    private String name;
    private String email;
    private String body;

    public Comment() {
        super();
    }

    public int getId() {
        return id;
    }

    public int getPost_id() {
        return postId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBody() {
        return body;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPost_id(int post_id) {
        this.postId = post_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
