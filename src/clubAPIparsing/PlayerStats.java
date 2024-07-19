package clubAPIparsing;

import com.google.gson.annotations.SerializedName;

public class PlayerStats {

    @SerializedName("chess_daily")
    private ChessStat chessDaily;

    public ChessStat getChessDaily() {
        return chessDaily;
    }

    public static class ChessStat {
        @SerializedName("last")
        private Last last;

        @SerializedName("record")
        private Record record;

        public Last getLast() {
            return last;
        }

        public Record getRecord() {
            return record;
        }

        public static class Last {
            @SerializedName("rating")
            private int rating;

            public int getRating() {
                return rating;
            }
        }

        public static class Record {
            @SerializedName("timeout_percent")
            private double timeoutPercent;

            public double getTimeoutPercent() {
                return timeoutPercent;
            }
        }
    }
}
