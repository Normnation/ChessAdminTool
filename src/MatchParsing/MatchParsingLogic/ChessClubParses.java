package MatchParsing.MatchParsingLogic;

import MatchParsing.MatchParsingModels.ClubTeam;
import MatchParsing.MatchParsingModels.MatchWrapper;
import MatchParsing.MatchParsingModels.Player;
import MatchParsing.MatchParsingGUI.MatchClubParsingGUI;
import ClubParsing.ClubParsingModels.PlayerStats;
import com.google.gson.Gson;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class ChessClubParses {
    private static final String PUB_MATCH_ENDPOINT = "https://api.chess.com/pub/match/";
    private final List<Player> allPlayers = new ArrayList<>();

    public void fetchData(
            MatchClubParsingGUI gui,
            DefaultListModel<String> usernameModel,
            DefaultListModel<String> ratingModel,
            DefaultListModel<String> timeoutModel,
            DefaultListModel<String> timeout960Model,
            String matchId,
            String interestedClubName
    ) {
        SwingWorker<Void, Player> worker = new SwingWorker<>() {
//            private int progress = 0;

            @Override
            protected Void doInBackground() throws Exception {
                URL url = new URL(PUB_MATCH_ENDPOINT + matchId);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                int matchResponseCode = con.getResponseCode();
                if (matchResponseCode != HttpURLConnection.HTTP_OK) {
                    System.err.println("Could not fetch match. HTTP code: " + matchResponseCode);
                    return null;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                Gson gson = new Gson();
                MatchWrapper matchWrapper = gson.fromJson(response.toString(), MatchWrapper.class);
                boolean foundTeam = false;
                for (Entry<String, ClubTeam> entry : matchWrapper.getTeams().entrySet()) {
                    ClubTeam clubTeam = entry.getValue();
                    if (clubTeam.getName().equalsIgnoreCase(interestedClubName)) {
                        foundTeam = true;
                        final int totalPlayers = clubTeam.getPlayers().size();

                        SwingUtilities.invokeLater(() -> {
                            gui.setProgressBarMax(totalPlayers);
                            gui.progressBar.setValue(0);
                            gui.progressBar.setVisible(true);
                        });

                        AtomicInteger progress = new AtomicInteger(0);
                        clubTeam.getPlayers().forEach(player -> {
                            processPlayer(player, gson);
                            int currentProgress = progress.incrementAndGet();

                            SwingUtilities.invokeLater(() -> {
                                gui.updateProgressBar(currentProgress);
                                gui.progressBar.setString(currentProgress + "/" + totalPlayers + " (" + (int) ((currentProgress * 100.0f) / totalPlayers) + "%)");
                                if (currentProgress == totalPlayers) {
                                    gui.progressBar.setVisible(false);
                                }
                            });
                        });
                        break;
                    }
                }

                if (!foundTeam) {
                    System.err.println("Team '" + interestedClubName + "' not found in match ID " + matchId);
                }
                return null;

            }

            private void processPlayer(Player player, Gson gson) {
                try {
                    String statsUrl = "https://api.chess.com/pub/player/"
                            + player.getUsername().toLowerCase() + "/stats";
                    HttpURLConnection statsCon = (HttpURLConnection) new URL(statsUrl).openConnection();
                    statsCon.setRequestMethod("GET");
                    int statsCode = statsCon.getResponseCode();
                    if (statsCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader statsReader = new BufferedReader(new InputStreamReader(statsCon.getInputStream()));
                        StringBuilder statsResponse = new StringBuilder();
                        String line;
                        while ((line = statsReader.readLine()) != null) {
                            statsResponse.append(line);
                        }
                        statsReader.close();
                        PlayerStats pStats = gson.fromJson(statsResponse.toString(), PlayerStats.class);
                        updatePlayerStats(player, pStats);
                    }
                } catch (Exception ex) {
                    System.out.println("Unable to map player details from endpoint.");
                }
            }

            private void updatePlayerStats(Player player, PlayerStats pStats) {
                if (pStats.getChessDaily() != null && pStats.getChessDaily().getLast() != null) {
                    player.setRating(pStats.getChessDaily().getLast().getRating());
                }
                if (pStats.getChessDaily().getRecord() != null) {
                    player.setTimeout_percent(pStats.getChessDaily().getRecord().getTimeoutPercent());
                }
                if (pStats.getChess960_daily() != null && pStats.getChess960_daily().getRecord() != null) {
                    player.setTimeout960_percent(pStats.getChess960_daily().getRecord().getTimeoutPercent());
                }
                publish(player);
            }

            @Override
            protected void process(List<Player> chunks) {
                allPlayers.addAll(chunks);
                allPlayers.sort((p1, p2) -> Integer.compare(p2.getRating(), p1.getRating()));

                SwingUtilities.invokeLater(() -> {
                    usernameModel.clear();
                    ratingModel.clear();
                    timeoutModel.clear();
                    if (timeout960Model != null) {
                        timeout960Model.clear();
                    }

                    for (Player player : allPlayers) {
                        if (player.getTimeout_percent() >= 25) {
                            usernameModel.addElement(player.getUsername());
                            ratingModel.addElement(String.valueOf(player.getRating()));
                            timeoutModel.addElement(player.getTimeout_percent() + "%");
                            if (timeout960Model != null) {
                                timeout960Model.addElement(player.getTimeout960_percent() + "%");
                            }
                        }
                    }
                });
            }


            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(gui,
                            "Error during parsing: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }


}
