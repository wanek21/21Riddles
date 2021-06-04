package martian.riddles.dto;

/*Класс-обертка данных о пользователе.
  Этот класс преобразовывается в json с помощью Retrofit и отправляется на сервер
  Может содержать неактуальные данные! Актуальные данные содержаться в классе Progress*/

public class DataOfUser {

    private String nickname = "";
    private String answer;
    private int level = 1;
    private String uniqueString;
    private String token;

    public String getUniqueString() {
        return uniqueString;
    }

    public void setUniqueString(String uniqueString) {
        this.uniqueString = uniqueString;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
