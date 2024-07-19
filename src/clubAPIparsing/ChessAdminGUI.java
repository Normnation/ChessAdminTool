package clubAPIparsing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class ChessAdminGUI extends JFrame {
	public JProgressBar progressBar;
	protected JScrollPane usernameScrollPane, timeoutScrollPane;
	protected JPanel usernamePanel, timeoutPanel;
	Color yellow = new Color(204, 204, 0);
	Color purple = new Color(186, 85, 211);
	JLabel usernameHeader = new JLabel("Usernames:");
	JLabel timeoutHeader = new JLabel("Timeouts:");
	Font preferredFont = new Font("Tahoma ", Font.PLAIN, 24);
	DefaultListModel<String> usernameListModel = new DefaultListModel<>();
	DefaultListModel<String> timeoutListModel = new DefaultListModel<>();
	JPanel innerPanel = new JPanel();
	String teamNameTF = "";
	String timeoutTF = "";
	JTextField[] textFields = new JTextField[2];

	public ChessAdminGUI() {

		ImageIcon backgroundImage = new ImageIcon("resources/MainChessImage.jpg");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(1250, 900));
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setResizable(true);
		setLayout(new BorderLayout());
		setIconImage(backgroundImage.getImage());
		setTitle("Admin chess tool.");

		usernamePanel = new JPanel(new BorderLayout());
		timeoutPanel = new JPanel(new BorderLayout());

		JButton timeOutButton = createTimeoutButton();
		JButton matchFrameButton = openClubParsingButton();

		FileMenu menuBar = new FileMenu();
		setJMenuBar(menuBar.getChessMenu());

		usernamePanel.setOpaque(false);
		timeoutPanel.setOpaque(false);

		int top = 4;
		int left = 2;
		int bottom = 4;
		int right = 2;
		Border innerPadding = new EmptyBorder(top, left, bottom, right);

		Border line = BorderFactory.createLineBorder(Color.gray, 2);

		JList<String> usernameList = new JList<>(usernameListModel);
		JList<String> timeoutList = new JList<>(timeoutListModel);

		usernameList.setBorder(new CompoundBorder(line, innerPadding));
		timeoutList.setBorder(new CompoundBorder(line, innerPadding));

		usernameList.setCellRenderer(new CustomListCellRenderer());
		timeoutList.setCellRenderer(new CustomListCellRenderer());

		usernameList.setBorder(BorderFactory.createLineBorder(Color.gray));
		timeoutList.setBorder(BorderFactory.createLineBorder(Color.gray));

		usernameList.setPrototypeCellValue("Username");
		timeoutList.setPrototypeCellValue("Timeout rate");

		usernameList.setFont(preferredFont);
		timeoutList.setFont(preferredFont);

		usernameList.setVisibleRowCount(25);
		timeoutList.setVisibleRowCount(25);

		usernameList.setForeground(yellow);
		timeoutList.setForeground(yellow);

		usernameList.setBackground(Color.black);
		timeoutList.setBackground(Color.black);

		usernameList.setBorder(BorderFactory.createLineBorder(Color.white, 2));
		timeoutList.setBorder(BorderFactory.createLineBorder(Color.white, 2));

		JLabel matchParsingButtonHeader = new JLabel(
				"Find players registered in a match above a certain time out ratio.");

		usernameHeader.setBorder(BorderFactory.createLineBorder(Color.white, 2));
		timeoutHeader.setBorder(BorderFactory.createLineBorder(Color.white, 2));
		matchParsingButtonHeader.setBorder(BorderFactory.createLineBorder(Color.white, 3));

		usernameHeader.setForeground(yellow);
		timeoutHeader.setForeground(yellow);
		matchParsingButtonHeader.setForeground(yellow);
		usernameHeader.setFont(preferredFont);
		timeoutHeader.setFont(preferredFont);
		matchParsingButtonHeader.setFont(preferredFont);

		usernamePanel.add(usernameHeader, BorderLayout.NORTH);
		usernamePanel.add(new JScrollPane(usernameList), BorderLayout.CENTER);
		timeoutPanel.add(timeoutHeader, BorderLayout.NORTH);
		timeoutPanel.add(new JScrollPane(timeoutList), BorderLayout.CENTER);

		usernameScrollPane = new JScrollPane(usernameList);
		timeoutScrollPane = new JScrollPane(timeoutList);

		synchronizeScrolling(usernameScrollPane, timeoutScrollPane);

		usernameScrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
		timeoutScrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
		usernameScrollPane.setPreferredSize(new Dimension(200, usernameScrollPane.getHeight()));
		timeoutScrollPane.setPreferredSize(new Dimension(50, timeoutScrollPane.getHeight()));

		usernamePanel.add(usernameScrollPane, BorderLayout.CENTER);
		timeoutPanel.add(timeoutScrollPane, BorderLayout.CENTER);
		addPosition(325, 750, 700, 50, matchParsingButtonHeader);

		JLabel backgroundLabel = new JLabel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
			}
		};

		backgroundLabel.setLayout(new BorderLayout());
		add(backgroundLabel);
		JPanel listPanel = new JPanel(new GridLayout(1, 2));
		listPanel.setOpaque(false);
		listPanel.setBorder(BorderFactory.createLineBorder(Color.white, 2));

		innerPanel.setOpaque(false);
		innerPanel.setLayout(null);
		innerPanel.setBorder(BorderFactory.createLineBorder(Color.white, 2));
		backgroundLabel.add(innerPanel);

		listPanel.add(usernamePanel);
		listPanel.add(timeoutPanel);
		backgroundLabel.add(listPanel, BorderLayout.WEST);

		addingTitle(innerPanel);
		addingHeadersForTextFields(innerPanel);
		textFields = addingTextFields(innerPanel);

		innerPanel.add(timeOutButton);
		innerPanel.add(matchFrameButton);
		addPosition(500, 200, 150, 50, timeOutButton);
		innerPanel.add(matchParsingButtonHeader);

		usernameList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				@SuppressWarnings("unchecked")
				JList<String> list = (JList<String>) evt.getSource();
				if (evt.getClickCount() == 2) {
					int index = list.locationToIndex(evt.getPoint());
					String clickedUsername = usernameListModel.getElementAt(index);
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(new URI("https://www.chess.com/member/" + clickedUsername));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		setMinimumSize(new Dimension(250, 250));

		// Adding details for the progress bar
		JProgressBar progressBar = implementProgressBar();
		add(progressBar, BorderLayout.NORTH);

		setVisible(true);
	}

	public static void synchronizeScrolling(JScrollPane sp1, JScrollPane sp2) {
		JScrollBar sb1 = sp1.getVerticalScrollBar();
		JScrollBar sb2 = sp2.getVerticalScrollBar();

		sb1.addAdjustmentListener(e -> sb2.setValue(e.getValue()));

		sb2.addAdjustmentListener(e -> sb1.setValue(e.getValue()));
	}

	public void setProgressBarMax(int max) {
		progressBar.setMaximum(max);
	}

	public void updateProgressBar(int value) {
		progressBar.setValue(value);
		if (progressBar.getValue() == progressBar.getMaximum()) {
			JOptionPane.showMessageDialog(this, "Parsing complete. Returned " + ChessStats.returnedMembers
					+ " users from " + teamNameTF + " with a time out rate at or below " + timeoutTF + "%");
		}
	}

	public JProgressBar implementProgressBar() {
		progressBar = new JProgressBar();
		progressBar.setBackground(Color.DARK_GRAY);
		progressBar.setForeground(Color.GREEN);
		progressBar.setBorderPainted(true);
		progressBar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		progressBar.setStringPainted(true);
		UIManager.put("ProgressBar.selectionForeground", Color.black);
		return progressBar;
	}

	public void addUser(String username, double timeout) {
		usernameListModel.addElement(username);
		timeoutListModel.addElement(timeout + "%");
	}

	public JLabel addingTitle(JPanel innerPanel) {
		JLabel titleOfPage = new JLabel("Admin timeout tool");

		addPosition(440, 33, 250, 50, titleOfPage);
		innerPanel.add(titleOfPage);

		titleOfPage.setVisible(true);
		titleOfPage.setHorizontalAlignment(JLabel.CENTER);
		titleOfPage.setFont(new Font("Arial", Font.BOLD, 24));
		titleOfPage.setForeground(yellow);
		titleOfPage.setBackground(Color.black);

		return titleOfPage;
	}

	private void addPosition(int x, int y, int width, int height, JComponent component) {
		component.setBounds(x, y, width, height);
	}

	public JLabel[] addingHeadersForTextFields(JPanel innerPanel) {
		JLabel[] usernameTimeoutRateHeaderTF = new JLabel[2];
		usernameTimeoutRateHeaderTF[0] = new JLabel("Club Name");
		usernameTimeoutRateHeaderTF[1] = new JLabel("Timeout rate");

		addPosition(317, 91, 250, 40, usernameTimeoutRateHeaderTF[0]);
		innerPanel.add(usernameTimeoutRateHeaderTF[0]);

		addPosition(583, 91, 250, 40, usernameTimeoutRateHeaderTF[1]);
		innerPanel.add(usernameTimeoutRateHeaderTF[1]);

		usernameTimeoutRateHeaderTF[0].setVisible(true);
		usernameTimeoutRateHeaderTF[0].setHorizontalAlignment(JLabel.CENTER);
		usernameTimeoutRateHeaderTF[0].setFont(preferredFont);
		usernameTimeoutRateHeaderTF[0].setForeground(yellow);
		usernameTimeoutRateHeaderTF[0].setBackground(Color.black);

		usernameTimeoutRateHeaderTF[1].setVisible(true);
		usernameTimeoutRateHeaderTF[1].setHorizontalAlignment(JLabel.CENTER);
		usernameTimeoutRateHeaderTF[1].setFont(preferredFont);
		usernameTimeoutRateHeaderTF[1].setForeground(yellow);
		usernameTimeoutRateHeaderTF[1].setBackground(Color.black);

		return usernameTimeoutRateHeaderTF;
	}

	public JTextField[] addingTextFields(JPanel innerPanel) {
		JTextField[] textFields = new JTextField[2];
		textFields[0] = new JTextField(28);
		textFields[1] = new JTextField(28);

		textFields[0].setText("Enter team name here");
		textFields[1].setText("Enter timeout rate here");

		for (JTextField textField : textFields) {
			textField.setHorizontalAlignment(JLabel.CENTER);
			textField.setFont(preferredFont);
			textField.setForeground(yellow);
			textField.setBackground(Color.BLACK);
			textField.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
		}

		addPosition(314, 130, 250, 40, textFields[0]);
		innerPanel.add(textFields[0]);

		addPosition(583, 130, 250, 40, textFields[1]);
		innerPanel.add(textFields[1]);

		return textFields;
	}

	public JButton openClubParsingButton() {
		JButton matchParsingButton = new JButton("Match Parsing");
		addPosition(550, 800, 250, 50, matchParsingButton);
		matchParsingButton.setContentAreaFilled(true);
		matchParsingButton.setVisible(true);

		matchParsingButton.setFont(preferredFont);
		matchParsingButton.setForeground(purple);
		matchParsingButton.setBackground(Color.black);
		matchParsingButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
		matchParsingButton.setEnabled(true);
		matchParsingButton.addActionListener(e -> {
			new MatchClubParsingGUI();
			matchParsingButton.setEnabled(false);
		});
		matchParsingButton.setOpaque(true);
		return matchParsingButton;
	}

	public JButton createTimeoutButton() {
		JButton submitButton = new JButton("Submit");

		submitButton.setFont(new Font("Arial", Font.BOLD, 16));
		submitButton.setForeground(purple);
		submitButton.setBackground(Color.BLACK);
		submitButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

		submitButton.addActionListener(e -> {
			usernameListModel.clear();
			timeoutListModel.clear();

			teamNameTF = textFields[0].getText();
			timeoutTF = textFields[1].getText();
			boolean isTeamNameEmpty = teamNameTF.isBlank();
			boolean isTimeOutEmpty = timeoutTF.isEmpty();
			if (isTeamNameEmpty && isTimeOutEmpty) {
				JOptionPane.showMessageDialog(this, "The team match and timeout rate fields are both empty.");
			} else if (isTeamNameEmpty) {
				JOptionPane.showMessageDialog(this, "The team match field is empty.");
			} else if (isTimeOutEmpty) {
				JOptionPane.showMessageDialog(this, "The time out rate field is empty.");
			} else {
				JOptionPane.showMessageDialog(this, "Checking timeout rate for players on Team " + teamNameTF
						+ " with a timeout rate less than or equal to " + timeoutTF + "%");
				try {
					double maxTimeoutRate = Double.parseDouble(timeoutTF);
					ChessStats.fetchClubData(this, teamNameTF, maxTimeoutRate);

					// Reset text fields after fetching data
					textFields[0].setText("Enter team name here");
					textFields[1].setText("Enter timeout rate here");
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this, "Invalid timeout rate. Please enter a valid number.");
				}
			}
		});

		return submitButton;
	}

	class CustomListCellRenderer extends DefaultListCellRenderer {
		private Border outerPadding = BorderFactory.createEmptyBorder(7, 2, 7, 2);

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			label.setBorder(outerPadding);
			return label;
		}
	}
}
