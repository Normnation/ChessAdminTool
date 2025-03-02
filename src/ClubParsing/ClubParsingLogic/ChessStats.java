package ClubParsing.ClubParsingLogic;

import ClubParsing.ClubParsingGUI.ChessAdminGUI;
import ClubParsing.ClubParsingModels.ActiveMembers;
import ClubParsing.ClubParsingModels.PlayerStats;
import MatchParsing.MatchParsingModels.Player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChessStats {
    private static final Gson gson = new GsonBuilder().create();
    // Will update later for sorting.
//    private final List<Player> allUsers = new ArrayList<>();

    public static void fetchClubData(ChessAdminGUI gui, String teamName, double maxTimeoutRate) {
        new Thread(() -> {
            try {
                String clubMembersEndpoint = "https://api.chess.com/pub/club/" + teamName + "/members";
                URL url = new URL(clubMembersEndpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                ActiveMembers clubMembers = gson.fromJson(response.toString(), ActiveMembers.class);
                List<String> allUsernames = new ArrayList<>();

                if (clubMembers.getWeekly() != null) {
                    clubMembers.getWeekly().forEach(member -> allUsernames.add(member.getUsername()));
                }
                if (clubMembers.getMonthly() != null) {
                    clubMembers.getMonthly().forEach(member -> allUsernames.add(member.getUsername()));
                }
                if (clubMembers.getAllTime() != null) {
                    clubMembers.getAllTime().forEach(member -> allUsernames.add(member.getUsername()));
                }

                SwingUtilities.invokeLater(() -> gui.setProgressBarMax(allUsernames.size()));

                for (int i = 0; i < allUsernames.size(); i++) {
                    gui.checkPausedAndStopped();

                    String username = allUsernames.get(i);
                    parseAndProcessPlayerStats(gui, username, maxTimeoutRate);
                    int progress = i + 1;
                    SwingUtilities.invokeLater(() -> gui.updateProgressBar(progress));

                    if (gui.progressBar.getValue() == gui.progressBar.getMaximum()) {
                        SwingUtilities.invokeLater(() -> gui.progressBar.setVisible(false));
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                JOptionPane.showMessageDialog(gui, "Thread interrupted", "Interrupted", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(gui, "General parsing exception", "Unable to parse response", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }

    private static void parseAndProcessPlayerStats(ChessAdminGUI gui, String username, double maxTimeoutRate) {
        HttpURLConnection statsConnection = null;
        try {
            String playerStatsEndpoint = "https://api.chess.com/pub/player/" + username + "/stats";
            URL statsUrl = new URL(playerStatsEndpoint);
            statsConnection = (HttpURLConnection) statsUrl.openConnection();
            statsConnection.setRequestMethod("GET");

            int responseCode = statsConnection.getResponseCode();
            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:
                    BufferedReader statsIn = new BufferedReader(new InputStreamReader(statsConnection.getInputStream()));
                    String inputLine;
                    StringBuilder statsResponse = new StringBuilder();
                    while ((inputLine = statsIn.readLine()) != null) {
                        statsResponse.append(inputLine);
                    }
                    statsIn.close();

                    PlayerStats playerStats = gson.fromJson(statsResponse.toString(), PlayerStats.class);
                    double timeoutRate = 0;
                    int dailyRating = 0;
                    double timeoutRate960 = 0;

                    if (playerStats.getChessDaily() != null && playerStats.getChessDaily().getRecord() != null) {
                        timeoutRate = playerStats.getChessDaily().getRecord().getTimeoutPercent();
                        dailyRating = playerStats.getChessDaily().getLast() != null ? playerStats.getChessDaily().getLast().getRating() : 0;
                    }

                    if (playerStats.getChess960_daily() != null && playerStats.getChess960_daily().getRecord() != null) {
                        timeoutRate960 = playerStats.getChess960_daily().getRecord().getTimeoutPercent();
                    }

                    if (timeoutRate <= maxTimeoutRate) {
                        final double finalTimeoutRate = timeoutRate;
                        final int finalDailyRating = dailyRating;
                        final double finalTimeoutRate960 = timeoutRate960;


                        SwingUtilities.invokeLater(() -> {
                            gui.addUser(username, finalTimeoutRate, finalDailyRating, finalTimeoutRate960);
                        });
                    }
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    JOptionPane.showMessageDialog(gui, "No player data found for the specified username: " + username, "Player Not Found", JOptionPane.ERROR_MESSAGE);
                    break;
                case HttpURLConnection.HTTP_INTERNAL_ERROR:
                    JOptionPane.showMessageDialog(gui, "Server encountered an internal error. Please try again later.", "Internal Server Error", JOptionPane.ERROR_MESSAGE);
                    break;
                default:
                    JOptionPane.showMessageDialog(gui, "Failed to fetch player stats. HTTP error code: " + responseCode, "API Error", JOptionPane.ERROR_MESSAGE);
                    break;
            }
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(gui, "The URL for fetching player stats is malformed. Check the username.", "URL Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(gui, "Failed to connect to the Chess.com API. Check your internet connection or try again later.", "Connection Error", JOptionPane.ERROR_MESSAGE);
        } catch (JsonSyntaxException e) {
            JOptionPane.showMessageDialog(gui, "Failed to parse player statistics. The data format may have changed.", "Parsing Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (statsConnection != null) {
                statsConnection.disconnect();
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessAdminGUI::new);
    }
}