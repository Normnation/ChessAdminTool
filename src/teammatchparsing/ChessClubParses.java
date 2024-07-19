package teammatchparsing;

import com.google.gson.Gson;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map.Entry;

public class ChessClubParses {
	public static final String PUB_MATCH_ENDPOINT = "https://api.chess.com/pub/match/";

	public void fetchData(DefaultListModel<String> usernameModel, DefaultListModel<String> ratingModel,
			DefaultListModel<String> timeoutModel, String matchId, String interestedClubName) {
		try {
			URL url = new URL(PUB_MATCH_ENDPOINT + matchId);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			System.out.println("JSON Response: " + response.toString());

			Gson gson = new Gson();
			MatchWrapper matchWrapper = gson.fromJson(response.toString(), MatchWrapper.class);
			System.out.println(matchWrapper.getTeams());
			SwingUtilities.invokeLater(() -> {
				boolean foundTeam = false;
				for (Entry<String, ClubTeam> entry : matchWrapper.getTeams().entrySet()) {
					ClubTeam clubTeam = entry.getValue();
					int[] count = { 1 };
					if (clubTeam.getName().equalsIgnoreCase(interestedClubName)) {
						foundTeam = true;
						clubTeam.getPlayers().forEach(player -> {
							if (player.getTimeout_percent() >= 25) {
								usernameModel.addElement(player.getUsername());
								ratingModel.addElement(String.valueOf(player.getRating()));
								timeoutModel.addElement((player.getTimeout_percent()) + "%");
								// Output to the console
								System.out.println(count[0]++ + ": @" + player.getUsername() + " - Rating: "
										+ player.getRating() + ", Timeout: " + player.getTimeout_percent() + "%");
							}
						});
						break;
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
