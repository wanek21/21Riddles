package martian.riddles.dto;

public class ResponseFromServer { // ответ от сервера

    private String winner; // имя победителя
    private String prize; // приз
    private String season; // сезон
    private String email; // контакт для связи
    private String linkwinner; // ссылка на соц сеть победителя
    private String leaders; // инфа о каждом уровне записывается в таком виде: 'level'-'countPlayersOnThisLevel'-'firstPlayer'
    private String riddle;
    private String message;
    private int resultCode; // результат запроса
    private int existwinner; // наличие победителя
    private int place; // место в игре

    public String getLinktowinner() {
        return linkwinner;
    }

    public void setLinktowinner(String linktowinner) {
        this.linkwinner = linktowinner;
    }

    public String getRiddle() {
        return riddle;
    }

    public void setRiddle(String riddle) {
        this.riddle = riddle;
    }

    public int getExistWinner() {
        return existwinner;
    }

    public void setExistWinner(int existWinner) {
        this.existwinner = existWinner;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getLeaders() {
        return leaders;
    }

    public void setLeaders(String leaders) {
        this.leaders = leaders;
    }
}
