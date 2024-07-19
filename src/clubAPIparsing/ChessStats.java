package clubAPIparsing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChessStats {
	private static final Gson gson = new GsonBuilder().create();
	static int returnedMembers = 0;

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
					for (ActiveMembers.Member member : clubMembers.getWeekly()) {
						allUsernames.add(member.getUsername());
					}
				}

				if (clubMembers.getMonthly() != null) {
					for (ActiveMembers.Member member : clubMembers.getMonthly()) {
						allUsernames.add(member.getUsername());
					}
				}

				if (clubMembers.getAllTime() != null) {
					for (ActiveMembers.Member member : clubMembers.getAllTime()) {
						allUsernames.add(member.getUsername());
					}
				}

				SwingUtilities.invokeLater(() -> gui.setProgressBarMax(allUsernames.size()));
				for (int i = 0; i < allUsernames.size(); i++) {
					String username = allUsernames.get(i);
					parseAndProcessPlayerStats(gui, username, maxTimeoutRate);
					int progress = i + 1;
					SwingUtilities.invokeLater(() -> gui.updateProgressBar(progress));
					if (gui.progressBar.getValue() == gui.progressBar.getMaximum()) {
						SwingUtilities.invokeLater(() -> gui.progressBar.setVisible(false));
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	private static void parseAndProcessPlayerStats(ChessAdminGUI gui, String username, double maxTimeoutRate) {
		try {
			String playerStatsEndpoint = "https://api.chess.com/pub/player/" + username + "/stats";
			URL statsUrl = new URL(playerStatsEndpoint);
			HttpURLConnection statsConnection = (HttpURLConnection) statsUrl.openConnection();
			statsConnection.setRequestMethod("GET");

			BufferedReader statsIn = new BufferedReader(new InputStreamReader(statsConnection.getInputStream()));
			String inputLine;
			StringBuilder statsResponse = new StringBuilder();
			while ((inputLine = statsIn.readLine()) != null) {
				statsResponse.append(inputLine);
			}
			statsIn.close();

			PlayerStats playerStats = gson.fromJson(statsResponse.toString(), PlayerStats.class);

			if (playerStats.getChessDaily() != null && playerStats.getChessDaily().getRecord() != null) {
				double timeoutRate = playerStats.getChessDaily().getRecord().getTimeoutPercent();

				if (timeoutRate <= maxTimeoutRate) {
					SwingUtilities.invokeLater(() -> gui.addUser(username, timeoutRate));
					returnedMembers++;
					// System.out.println("@" + username + " : " + timeoutRate );
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new ChessAdminGUI());
	}
}
