package ClubParsing.ClubParsingModels;

public class UserData {

    private String username;
    private String chess960Rating;
    private String chessDailyRating;
    private Double timeoutPercent;

    // Will be used later if we need to write the records to a file for quicker retrieval.
    public UserData(String username, String chess960Rating, String chessDailyRating, Double timeoutPercent) {
        this.username = username;
        this.chess960Rating = chess960Rating;
        this.chessDailyRating = chessDailyRating;
        this.timeoutPercent = timeoutPercent;
    }
}
