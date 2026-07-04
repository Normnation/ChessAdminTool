package ClubParsing.ClubParsingModels;

import com.google.gson.annotations.SerializedName;

public class PlayerProfile {

    @SerializedName("last_online")
    private long lastOnline; // unix seconds

    public long getLastOnline() {
        return lastOnline;
    }
}