package clubAPIparsing;

public class UserData {

    private String username;
    private String chess960Rating;
    private String chessDailyRating;
    private Double timeoutPercent;

    public UserData(String username, String chess960Rating, String chessDailyRating, Double timeoutPercent) {
        this.username = username;
        this.chess960Rating = chess960Rating;
        this.chessDailyRating = chessDailyRating;
        this.timeoutPercent = timeoutPercent;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getChess960Rating() {
        return chess960Rating;
    }

    public void setChess960Rating(String chess960Rating) {
        this.chess960Rating = chess960Rating;
    }

    public String getChessDailyRating() {
        return chessDailyRating;
    }

    public void setChessDailyRating(String chessDailyRating) {
        this.chessDailyRating = chessDailyRating;
    }

    public Double getTimeoutPercent() {
        return timeoutPercent;
    }

    public void setTimeoutPercent(Double timeoutPercent) {
        this.timeoutPercent = timeoutPercent;
    }
}
