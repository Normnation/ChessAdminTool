package clubAPIparsing;

import teammatchparsing.ChessClubParses;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentListener;

public class MatchClubParsingGUI extends ChessAdminGUI {
	Color yellow = new Color(255, 255, 0);
	Color purple = new Color(128, 0, 128);
	ChessClubParses chessClubParses = new ChessClubParses();
	private JPanel listPanel;
	private JScrollPane ratingScrollPane;
	private DefaultListModel<String> ratingListModel = new DefaultListModel<>();
	JList<String> ratingList = new JList<>(ratingListModel);

	public MatchClubParsingGUI() {
		super();

		progressBar.setVisible(false);
		ImageIcon backgroundPanel = new ImageIcon("/ChessStats/resources/kingchessBackground.jpg");
		backgroundPanel.setImage(getIconImage());
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setTitle("Match Club Parsing Tool");
		setBackground(Color.black);
		usernameHeader.setForeground(yellow);
		timeoutHeader.setForeground(yellow);
		usernameHeader.setBackground(Color.BLACK);
		timeoutHeader.setBackground(Color.BLACK);
		addRatingPanel();
		addComponents();
		synchronizeScrolling(usernameScrollPane, timeoutScrollPane, ratingScrollPane);
		pack();

	}

	public static void synchronizeScrolling(JScrollPane sp1, JScrollPane sp2, JScrollPane sp3) {
		JScrollBar sb1 = sp1.getVerticalScrollBar();
		JScrollBar sb2 = sp2.getVerticalScrollBar();
		JScrollBar sb3 = sp3.getVerticalScrollBar();

		AdjustmentListener listener = e -> {
			JScrollBar sourceScrollBar = (JScrollBar) e.getSource();
			int value = sourceScrollBar.getValue();

			if (sourceScrollBar != sb1 && sb1.getValue() != value) {
				sb1.setValue(value);
			}
			if (sourceScrollBar != sb2 && sb2.getValue() != value) {
				sb2.setValue(value);
			}
			if (sourceScrollBar != sb3 && sb3.getValue() != value) {
				sb3.setValue(value);
			}
		};

		sb1.addAdjustmentListener(listener);
		sb2.addAdjustmentListener(listener);
		sb3.addAdjustmentListener(listener);
	}

	private void addRatingPanel() {
		JPanel ratingPanel = new JPanel(new BorderLayout());
		ratingPanel.setBackground(Color.BLACK);
		ratingPanel.setOpaque(true);
		configureList(ratingList, "Rating");

		ratingScrollPane = new JScrollPane(ratingList);
		JLabel ratingHeader = new JLabel("Ratings:");
		setupHeader(ratingHeader);
		ratingHeader.setForeground(yellow);
		ratingHeader.setBackground(Color.BLACK);
		ratingPanel.add(ratingHeader, BorderLayout.NORTH);
		ratingPanel.add(ratingScrollPane, BorderLayout.CENTER);

		listPanel = (JPanel) getContentPane();
		listPanel.setLayout(new GridLayout(1, 3));
		listPanel.setBackground(Color.BLACK);
		listPanel.add(usernamePanel);
		listPanel.add(timeoutPanel);
		listPanel.add(ratingPanel);
	}

	private void addComponents() {
		JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
		inputPanel.setBackground(Color.BLACK);

		JPanel fieldsPanel = new JPanel();
		fieldsPanel.setBackground(Color.black);
		fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
		fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel matchIDLabel = new JLabel("Match ID:");
		matchIDLabel.setForeground(yellow);
		matchIDLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		JTextField matchIDField = new JTextField(20);

		matchIDField.setMaximumSize(new Dimension(Integer.MAX_VALUE, matchIDField.getPreferredSize().height));
		matchIDField.setAlignmentX(Component.CENTER_ALIGNMENT);
		matchIDField.setText("Enter Match ID");

		JLabel clubNameLabel = new JLabel("Club Name:");
		clubNameLabel.setForeground(yellow);
		clubNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JTextField clubNameField = new JTextField(20);
		clubNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, clubNameField.getPreferredSize().height));
		clubNameField.setAlignmentX(Component.CENTER_ALIGNMENT);
		clubNameField.setText("Enter Club Name");

		fieldsPanel.add(matchIDLabel);
		fieldsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		fieldsPanel.add(matchIDField);
		fieldsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		fieldsPanel.add(clubNameLabel);
		fieldsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		fieldsPanel.add(clubNameField);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(Color.DARK_GRAY);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		JButton parseButton = new JButton("Parse match details");
		parseButton.setFont(preferredFont);
		parseButton.setForeground(purple);
		parseButton.setBackground(Color.black);
		parseButton.setPreferredSize(new Dimension(300, 50));
		parseButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));
		buttonPanel.add(parseButton);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		parseButton.addActionListener(e -> {
			String matchID = matchIDField.getText();
			String teamName = clubNameField.getText();
			Boolean isMatchIDEmpty = matchID.isBlank();
			Boolean isTeamMatchEmpty = teamName.isEmpty();
			if (isMatchIDEmpty && isTeamMatchEmpty) {
				JOptionPane.showMessageDialog(this, "The Match ID field is empty as well as theTeam Match field.");
			} else if (matchID.isEmpty()) {
				JOptionPane.showMessageDialog(this, "The Match ID field is empty.");
			} else if (isTeamMatchEmpty) {
				JOptionPane.showMessageDialog(this, "The Team match field is empty.");
			} else {
				chessClubParses.fetchData(usernameListModel, ratingListModel, timeoutListModel, matchID, teamName);
				usernameListModel.clear();
				timeoutListModel.clear();
				ratingListModel.clear();
			}
			JOptionPane.showMessageDialog(this,
					"Checking timeout rate for players on Team " + teamName + " with matchID: " + matchID);
			matchIDField.setText("");
			clubNameField.setText("");
		});
		inputPanel.add(fieldsPanel, BorderLayout.CENTER);
		inputPanel.add(buttonPanel, BorderLayout.SOUTH);

		listPanel.add(inputPanel, BorderLayout.NORTH);
	}

	private void configureList(JList<String> list, String prototypeValue) {
		list.setCellRenderer(new CustomListCellRenderer());
		list.setPrototypeCellValue(prototypeValue);
		list.setFont(preferredFont);
		list.setVisibleRowCount(25);
		list.setForeground(yellow);
		list.setBackground(Color.BLACK);
		list.setBorder(BorderFactory.createLineBorder(Color.white, 2));
	}

	private void setupHeader(JLabel header) {
		header.setBorder(BorderFactory.createLineBorder(Color.white, 2));
		header.setForeground(yellow);
		header.setFont(preferredFont);
	}

}
