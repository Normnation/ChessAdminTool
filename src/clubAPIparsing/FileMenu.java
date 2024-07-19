package clubAPIparsing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileMenu implements ActionListener {

	private static final String DEFAULT_UNKNOWN = "This is an unknown command.";

	private static final String EDITLIST = "You can edit the list by";

	private static final String BUG_REPORT_FEATURE_REQUEST = "To report a bug or request a feature, contact me at https://www.chess.com/member/or through Email at chessEmail.com";

	private static final String PRIMARYMESSAGE = "This application is designed for chess.com administrators to streamline the process of inviting members to team matches and other events.\n"
			+ "It uses the chess.com API to fetch member data from a specified club, including their activity levels (weekly, monthly, all-time).\n"
			+ "The application then filters members based on the selected timeout rate, using criteria set in a text box.";

	private static final String CONTACT = "You can contact me at: https://www.chess.com/member/or through Email at chessEmail.com";

	private static final String INPUT_FIELDS_PAGE_1 = "There are two fields below. The one on the left is meant for the name of the team as it is shown in the url.\n"
			+ "As an example, team-canada shows up in that format. If you do not know the name of your team, go to their url. As an example, https://www.chess.com/club/team-canada shows the name of Team Canada.\n"
			+ "The second field is for the maximum timeout rate. This is a percentage value. As an example, if you want to filter out all members with a timeout rate of 25% or higher, you would enter 25 in this field.";

	private static final String POPULATED_LIST_PAGE_1 = "The lists below will get populated with the usernames and timeout rates of players that match the criteria you specified.\n"
			+ "The usernames themselves can be double clicked and will bring you to the profile page of the user in case you'd like to message them to invite them.";

	private static final String MATCH_PARSING_PAGE_2 = "The two fields below will accept a match id and a team name (since the match takes place between two teams). The match id is the number that shows up in the url of the match.\n"
			+ "As an example, if the url is https://www.chess.com/club/matches/team-canada/1637489, the match id is 1637489. The team name is the name of the team that you are interested in.\n"
			+ "The tricky element of this is that these endpoints are not the same as the ones used on page one. Team Canada in this case will be just like that. It will be Team Canada, not team-canada.";

	private static final String LIST_PAGE_2 = "The list shows is similar to the list from page 1 with the addition of the ratings of the players. This is based on the stats endpoint\n"
			+ "which comes from the user's profile page. Keep in mind that the timeout rate only covers the last 90 days because that is how chess.com has it set up.";

	private static final String USAGE_INFO = "Chess.com TOS do not restrict usage with the exception of multi-threading. This application does not use multi-threading and because of this it may\n"
			+ "take a while for some results to be populated. For example, if you request ALL users from team canada and you set the timeout rate 100 and you ignore activity level,\n"
			+ "it can take close to an hour to return everything. It's best to find specific subsets. This of course only applies to page 1 as page 2 match parsing will always be quite low in terms of players.";

	private static final String FUTURE_PLANS = "In the future, I may add the option to save lists of players to an excel file or notepad file in case you want to keep records of something.\n"
			+ "I may also add a dropdown to filter greater than, less than or equal to for the timeout rate.";

	JMenuBar chessMenu;

	public FileMenu() {
		chessMenu = new JMenuBar();
		JMenu File = new JMenu("File");
		JMenu Edit = new JMenu("Edit");
		JMenu Info = new JMenu("Info");
		JMenu Help = new JMenu("Help");
		JMenu Contact = new JMenu("Contact");

		JMenuItem Save = new JMenuItem("Save list");
		JMenuItem aboutThisProgram = new JMenuItem("About this application");
		JMenuItem ContactMe = new JMenuItem("Contact");
		JMenuItem helpOption = new JMenuItem("Report a Bug");

		JMenu featureExplanation = new JMenu("Feature explanation");
		JMenuItem inputFieldsPage1 = new JMenuItem("Input fields (Page 1)");
		JMenuItem populatedListPage1 = new JMenuItem("Populated list (Page 1)");
		JMenuItem matchParsingPage2 = new JMenuItem("Match parsing (Page 2)");
		JMenuItem listPage2 = new JMenuItem("List (Page 2)");
		JMenuItem usageInfo = new JMenuItem("Usage Info");
		JMenuItem futurePlans = new JMenuItem("Future Plans");

		featureExplanation.add(inputFieldsPage1);
		featureExplanation.add(populatedListPage1);
		featureExplanation.add(matchParsingPage2);
		featureExplanation.add(listPage2);
		featureExplanation.add(usageInfo);
		featureExplanation.add(futurePlans);

		JMenuItem editSomething = new JMenuItem("Edit list");

		Save.addActionListener(this);
		aboutThisProgram.addActionListener(this);
		ContactMe.addActionListener(this);
		helpOption.addActionListener(this);
		inputFieldsPage1.addActionListener(this);
		populatedListPage1.addActionListener(this);
		matchParsingPage2.addActionListener(this);
		listPage2.addActionListener(this);
		usageInfo.addActionListener(this);
		futurePlans.addActionListener(this);
		editSomething.addActionListener(this);

		File.add(Save);
		Info.add(aboutThisProgram);
		Contact.add(ContactMe);
		Edit.add(editSomething);
		Help.add(helpOption);
		Help.add(featureExplanation);

		chessMenu.add(File);
		chessMenu.add(Edit);
		chessMenu.add(Info);
		chessMenu.add(Help);
		chessMenu.add(Contact);

		this.setChessMenu(chessMenu);
	}

	public JMenuBar getChessMenu() {
		return chessMenu;
	}

	public void setChessMenu(JMenuBar chessMenu) {
		this.chessMenu = chessMenu;
	}

	public JPanel createCustomPanel(String message) {
		JPanel customPanel = new JPanel();
		customPanel.setLayout(new BorderLayout());
		customPanel.setBackground(Color.black);
		customPanel.setPreferredSize(new Dimension(750, 450));
		JLabel titleLabel = new JLabel("About this Application");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		customPanel.add(titleLabel, BorderLayout.NORTH);

		JTextArea messageArea = new JTextArea(message);
		messageArea.setBackground(Color.black);
		messageArea.setForeground(Color.green);
		messageArea.setWrapStyleWord(true);
		messageArea.setLineWrap(true);
		messageArea.setEditable(false);
		messageArea.setFont(new Font("Tahoma", Font.PLAIN, 20));

		JScrollPane scrollPane = new JScrollPane(messageArea);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setBackground(Color.black);
		scrollPane.getVerticalScrollBar().setForeground(Color.green);
		customPanel.add(scrollPane, BorderLayout.CENTER);

		return customPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String issuedCommand = e.getActionCommand();
		JPanel customPanel = null;

		switch (issuedCommand) {
		case "Save list":
			JOptionPane.showMessageDialog(null, "List has been saved!");
			return; // Exit the method early
		case "About this application":
			customPanel = createCustomPanel(PRIMARYMESSAGE);
			break;
		case "Contact":
			JOptionPane.showMessageDialog(null, CONTACT);
			return; // Exit the method early
		case "Report a Bug":
			JOptionPane.showMessageDialog(null, BUG_REPORT_FEATURE_REQUEST);
			return; // Exit the method early
		case "Input fields (Page 1)":
			customPanel = createCustomPanel(INPUT_FIELDS_PAGE_1);
			break;
		case "Populated list (Page 1)":
			customPanel = createCustomPanel(POPULATED_LIST_PAGE_1);
			break;
		case "Match parsing (Page 2)":
			customPanel = createCustomPanel(MATCH_PARSING_PAGE_2);
			break;
		case "List (Page 2)":
			customPanel = createCustomPanel(LIST_PAGE_2);
			break;
		case "Usage Info":
			customPanel = createCustomPanel(USAGE_INFO);
			break;
		case "Future Plans":
			customPanel = createCustomPanel(FUTURE_PLANS);
			break;
		case "Edit list":
			JOptionPane.showMessageDialog(null, EDITLIST);
			return;
		default:
			JOptionPane.showMessageDialog(null, DEFAULT_UNKNOWN);
			return;
		}

		if (customPanel != null) {
			JOptionPane.showMessageDialog(null, customPanel, issuedCommand, JOptionPane.PLAIN_MESSAGE);
		}
	}
}
