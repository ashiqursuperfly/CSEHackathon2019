package ashiqur.goriberfitbit.rest_api.api_models;

public class User {
    public String username;
    public String score;

    public User(String username, String score) {
        this.username = username;
        this.score = score;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", score='" + score + '\'' +
                '}';
    }
}
