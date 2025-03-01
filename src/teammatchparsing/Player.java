package teammatchparsing;

public class Player {
    public void setUsername(String username) {
        this.username = username;
    }

    private String username;
    private double timeout_percent;
    private int rating;
    private double timeout960_percent;

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Player() {
    }

    public String getUsername() {
        return username;
    }

    public double getTimeout_percent() {
        return timeout_percent;
    }

    public int getRating() {
        return rating;
    }

    public double getTimeout960_percent() {
        return timeout960_percent;
    }

    public void setTimeout_percent(double timeout_percent) {
        this.timeout_percent = timeout_percent;
    }

    public void setTimeout960_percent(double timeout960_percent) {
        this.timeout960_percent = timeout960_percent;
    }


}
