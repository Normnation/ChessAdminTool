package clubAPIparsing;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ActiveMembers {

    @SerializedName("weekly")
    private List<Member> weekly;
    @SerializedName("monthly")
    private List<Member> monthly;
    @SerializedName("all_time")
    private List<Member> allTime;

    public List<Member> getWeekly() {
        return weekly;
    }

    public List<Member> getMonthly() {
        return monthly;
    }

    public List<Member> getAllTime() {
        return allTime;
    }

    public static class Member {
        @SerializedName("username")
        private String username;
        @SerializedName("joined")
        private long joined;

        public String getUsername() {
            return username;
        }

    }
}
