package ClubParsing.ClubParsingLogic;

import ClubParsing.ClubParsingGUI.ChessAdminGUI;
import ClubParsing.ClubParsingModels.ActiveMembers;
import ClubParsing.ClubParsingModels.PlayerStats;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;

public class ChessStats {
    private static final Gson gson = new GsonBuilder().create();

    private static final String CLUB_MEMBERS_ENDPOINT = "https://api.chess.com/pub/club/%s/members";
    private static final String PLAYER_STATS_ENDPOINT = "https://api.chess.com/pub/player/%s/stats";

    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 20_000;

    private static final String CSV_HEADER = "Username,Daily Timeout,Rating,Timeout Rate 960\n";

    /*
        Important:
        This uses ONE SwingWorker so the GUI does not freeze.
        The Chess.com requests are still sequential: one player request at a time.
        There are no concurrent API calls, no executor pool, and no parallel stream.
    */
    public static void fetchClubData(ChessAdminGUI gui,
                                     String teamName,
                                     double maxTimeoutRate,
                                     Integer minRating,
                                     Integer maxRating) throws IOException {

        SwingWorker<Void, PlayerResult> worker = new SwingWorker<>() {
            private final StringBuilder csv = new StringBuilder(CSV_HEADER);
            private int acceptedCount = 1;

            @Override
            protected Void doInBackground() throws Exception {
                String safeTeamName = teamName == null ? "" : teamName.trim();

                if (safeTeamName.isBlank()) {
                    throw new IllegalArgumentException("Club name is empty.");
                }

                Set<String> usernames = fetchClubMembers(safeTeamName);

                SwingUtilities.invokeLater(() -> gui.setProgressBarMax(usernames.size()));

                int progress = 0;

                for (String username : usernames) {
                    gui.checkPausedAndStopped();

                    PlayerResult result = fetchPlayerResult(
                            username,
                            maxTimeoutRate,
                            minRating == null ? 0 : minRating,
                            maxRating == null ? 3000 : maxRating
                    );

                    if (result != null) {
                        result.rowNumber = acceptedCount++;
                        csv.append(result.toCsvLine());
                        publish(result);
                    }

                    progress++;
                    int progressSnapshot = progress;

                    SwingUtilities.invokeLater(() -> {
                        try {
                            gui.updateProgressBar(progressSnapshot);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                }

                writeCsv(csv);
                return null;
            }

            @Override
            protected void process(java.util.List<PlayerResult> chunks) {
                for (PlayerResult result : chunks) {
                    gui.addUser(
                            result.username,
                            result.dailyTimeout,
                            result.displayRating,
                            result.timeout960
                    );
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (CancellationException ignored) {
                    JOptionPane.showMessageDialog(gui,
                            "Parsing was cancelled.",
                            "Cancelled",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(gui,
                            "Parsing was interrupted.",
                            "Interrupted",
                            JOptionPane.WARNING_MESSAGE);
                } catch (Exception e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    JOptionPane.showMessageDialog(gui,
                            "Parsing failed:\n" + cause.getMessage(),
                            "Parsing Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private static Set<String> fetchClubMembers(String teamName) throws IOException {
        String endpoint = String.format(CLUB_MEMBERS_ENDPOINT, teamName);
        HttpResult result = getJson(endpoint);

        if (result.responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to fetch club members. HTTP " + result.responseCode);
        }

        ActiveMembers clubMembers = gson.fromJson(result.body, ActiveMembers.class);
        Set<String> usernames = new LinkedHashSet<>();

        if (clubMembers == null) {
            return usernames;
        }

        if (clubMembers.getWeekly() != null) {
            clubMembers.getWeekly().forEach(member -> {
                if (member != null && member.getUsername() != null) {
                    usernames.add(member.getUsername());
                }
            });
        }

        if (clubMembers.getMonthly() != null) {
            clubMembers.getMonthly().forEach(member -> {
                if (member != null && member.getUsername() != null) {
                    usernames.add(member.getUsername());
                }
            });
        }

        if (clubMembers.getAllTime() != null) {
            clubMembers.getAllTime().forEach(member -> {
                if (member != null && member.getUsername() != null) {
                    usernames.add(member.getUsername());
                }
            });
        }

        return usernames;
    }

    private static PlayerResult fetchPlayerResult(String username,
                                                  double maxTimeoutRate,
                                                  int minRating,
                                                  int maxRating) throws IOException {
        try {
            String endpoint = String.format(PLAYER_STATS_ENDPOINT, username);
            HttpResult result = getJson(endpoint);

            if (result.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return null;
            }

            if (result.responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            PlayerStats playerStats = gson.fromJson(result.body, PlayerStats.class);

            if (playerStats == null) {
                return null;
            }

            double dailyTimeout = 0;
            double timeout960 = 0;
            int dailyRating = 0;
            int daily960Rating = 0;

            if (playerStats.getChessDaily() != null) {
                if (playerStats.getChessDaily().getRecord() != null) {
                    dailyTimeout = playerStats.getChessDaily().getRecord().getTimeoutPercent();
                }

                if (playerStats.getChessDaily().getLast() != null) {
                    dailyRating = playerStats.getChessDaily().getLast().getRating();
                }
            }

            if (playerStats.getChess960_daily() != null) {
                if (playerStats.getChess960_daily().getRecord() != null) {
                    timeout960 = playerStats.getChess960_daily().getRecord().getTimeoutPercent();
                }

                if (playerStats.getChess960_daily().getLast() != null) {
                    daily960Rating = playerStats.getChess960_daily().getLast().getRating();
                }
            }

            boolean timeoutOk = dailyTimeout <= maxTimeoutRate && timeout960 <= maxTimeoutRate;

            int displayRating = chooseDisplayRating(dailyRating, daily960Rating, minRating, maxRating);
            boolean ratingOk = displayRating > 0;

            if (!timeoutOk || !ratingOk) {
                return null;
            }

            PlayerResult accepted = new PlayerResult();
            accepted.username = username;
            accepted.dailyTimeout = dailyTimeout;
            accepted.timeout960 = timeout960;
            accepted.dailyRating = dailyRating;
            accepted.daily960Rating = daily960Rating;
            accepted.displayRating = displayRating;

            return accepted;
        } catch (MalformedURLException e) {
            return null;
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    private static int chooseDisplayRating(int dailyRating,
                                           int daily960Rating,
                                           int minRating,
                                           int maxRating) {
        boolean dailyOk = dailyRating >= minRating && dailyRating <= maxRating;
        boolean daily960Ok = daily960Rating >= minRating && daily960Rating <= maxRating;

        if (dailyRating > 0 && dailyOk) {
            return dailyRating;
        }

        if (daily960Rating > 0 && daily960Ok) {
            return daily960Rating;
        }

        return 0;
    }

    private static HttpResult getJson(String endpoint) throws IOException {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "ChessAdminTool/1.0");

            int responseCode = connection.getResponseCode();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                    StandardCharsets.UTF_8
            ))) {
                StringBuilder response = new StringBuilder();

                for (String line; (line = reader.readLine()) != null; ) {
                    response.append(line);
                }

                return new HttpResult(responseCode, response.toString());
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void writeCsv(StringBuilder csv) throws IOException {
        Path outputPath = resolveOutputPath();

        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write(csv.toString());
        }
    }

    private static Path resolveOutputPath() {
        String configuredPath = System.getenv("LocalFileEndPointChess");

        if (configuredPath != null && !configuredPath.isBlank()) {
            return Path.of(configuredPath);
        }

        return Path.of("chess_stats.csv");
    }

    private static class HttpResult {
        final int responseCode;
        final String body;

        HttpResult(int responseCode, String body) {
            this.responseCode = responseCode;
            this.body = body;
        }
    }

    private static class PlayerResult {
        int rowNumber;
        String username;
        double dailyTimeout;
        int dailyRating;
        int daily960Rating;
        int displayRating;
        double timeout960;

        String toCsvLine() {
            return String.format(
                    "%d: @%s | DR=%d | 960=%d | DisplayRating=%d | T=%.0f%% | T960=%.0f%%%n",
                    rowNumber,
                    username,
                    dailyRating,
                    daily960Rating,
                    displayRating,
                    dailyTimeout,
                    timeout960
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessAdminGUI::new);
    }
}
