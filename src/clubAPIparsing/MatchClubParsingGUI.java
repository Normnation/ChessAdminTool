package clubAPIparsing;

import teammatchparsing.ChessClubParses;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class MatchClubParsingGUI extends JFrame {
    String matchID = "";
    String teamName = "";

    private final JProgressBar progressBar = new JProgressBar();

    private final JList<String> usernameList = new JList<>();
    private final JList<String> timeoutList = new JList<>();
    private final JList<String> ratingList = new JList<>();
    private final JList<String> timeout960List = new JList<>();

    private final DefaultListModel<String> usernameListModel = new DefaultListModel<>();
    private final DefaultListModel<String> timeoutListModel = new DefaultListModel<>();
    private final DefaultListModel<String> ratingListModel = new DefaultListModel<>();
    private final DefaultListModel<String> timeout960ListModel = new DefaultListModel<>();

    private final Color yellow = Color.YELLOW;
    private final Color purple = new Color(128, 0, 128);
    private final Font headerFont = new Font("Arial", Font.BOLD, 24);
    private final Font labelFont = new Font("Arial", Font.BOLD, 18);
    private final Font listFont = new Font("Arial", Font.BOLD, 16);

    private final ChessClubParses chessClubParses = new ChessClubParses();

    // Methods are referencing TOTAL members so it's never ending when it's done parsing.
    public void setProgressBarMax(int max) {
        progressBar.setMaximum(max);
        progressBar.setValue(0);
    }

    public void updateProgressBar(int value) {
        progressBar.setValue(value);
        if (progressBar.getValue() == progressBar.getMaximum()) {
            JOptionPane.showMessageDialog(this,
                    "Parsing complete. Processed " + value + " players.");
        }
    }

    public MatchClubParsingGUI() {
        super("Match Club Parsing Tool");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        getContentPane().setBackground(Color.BLACK);
        setLayout(new BorderLayout(0, 0));
        ImageIcon backgroundImage = new ImageIcon("resources/MainChessImage.jpg");
        setIconImage(backgroundImage.getImage());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(Color.BLACK);

        JLabel titleLabel = new JLabel("Match Club Parsing Tool", SwingConstants.CENTER);
        titleLabel.setForeground(yellow);
        titleLabel.setFont(headerFont);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(5));

        progressBar.setVisible(true);
        progressBar.setPreferredSize(new Dimension(400, 20));
        progressBar.setBackground(Color.DARK_GRAY);
        progressBar.setForeground(yellow);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(progressBar);
        topPanel.add(Box.createVerticalStrut(10));

        add(topPanel, BorderLayout.NORTH);

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.BLACK);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel matchIDLabel = new JLabel("Match ID:");
        matchIDLabel.setForeground(yellow);
        matchIDLabel.setFont(labelFont);
        matchIDLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField matchIDField = new JTextField();
        matchIDField.setFont(listFont);
        matchIDField.setForeground(yellow);
        matchIDField.setBackground(Color.BLACK);
        matchIDField.setCaretColor(yellow);
        matchIDField.setAlignmentX(Component.LEFT_ALIGNMENT);
        matchIDField.setMaximumSize(new Dimension(250, 30));

        JLabel clubNameLabel = new JLabel("Club Name:");
        clubNameLabel.setForeground(yellow);
        clubNameLabel.setFont(labelFont);
        clubNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField clubNameField = new JTextField();
        clubNameField.setFont(listFont);
        clubNameField.setForeground(yellow);
        clubNameField.setBackground(Color.BLACK);
        clubNameField.setCaretColor(yellow);
        clubNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        clubNameField.setMaximumSize(new Dimension(250, 30));

        JButton parseButton = new JButton("Parse");
        parseButton.setFont(labelFont);
        parseButton.setForeground(purple);
        parseButton.setBackground(Color.BLACK);
        parseButton.setBorder(BorderFactory.createLineBorder(yellow, 2));
        parseButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        parseButton.setPreferredSize(new Dimension(200, 60));

        parseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                matchID = matchIDField.getText().trim();
                teamName = clubNameField.getText().trim();

                JOptionPane.showMessageDialog(MatchClubParsingGUI.this,
                        "Parsing complete. Returned " + ChessClubParses.totalMembersParsed + " players from " + teamName + " using match ID: " + matchID);
                if (matchID.isEmpty() && teamName.isEmpty()) {
                    JOptionPane.showMessageDialog(MatchClubParsingGUI.this,
                            "Both Match ID and Club Name fields are empty.");
                } else if (matchID.isEmpty()) {
                    JOptionPane.showMessageDialog(MatchClubParsingGUI.this,
                            "Match ID is empty.");
                } else if (teamName.isEmpty()) {
                    JOptionPane.showMessageDialog(MatchClubParsingGUI.this,
                            "Club Name is empty.");
                } else {
                    usernameListModel.clear();
                    timeoutListModel.clear();
                    ratingListModel.clear();
                    timeout960ListModel.clear();

                    chessClubParses.fetchData(MatchClubParsingGUI.this, usernameListModel,
                            ratingListModel,
                            timeoutListModel,
                            timeout960ListModel,
                            matchID,
                            teamName);

                }
                matchIDField.setText("");
                clubNameField.setText("");
            }
        });

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
                        JOptionPane.showMessageDialog(MatchClubParsingGUI.this, "Unable to open profile page for player " + clickedUsername, "Unable to open profile", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        rightPanel.add(matchIDLabel);
        rightPanel.add(matchIDField);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(clubNameLabel);
        rightPanel.add(clubNameField);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(parseButton);

        add(rightPanel, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new GridLayout(1, 4, 5, 0));
        centerPanel.setBackground(Color.BLACK);

        JPanel usernamePanel = buildColumnPanel("Username", usernameList, usernameListModel);
        centerPanel.add(usernamePanel);

        JPanel timeoutPanel = buildColumnPanel("Timeout", timeoutList, timeoutListModel);
        centerPanel.add(timeoutPanel);

        JPanel ratingPanel = buildColumnPanel("Rating", ratingList, ratingListModel);
        centerPanel.add(ratingPanel);

        JPanel timeout960Panel = buildColumnPanel("960 Timeout", timeout960List, timeout960ListModel);
        centerPanel.add(timeout960Panel);

        add(centerPanel, BorderLayout.CENTER);

        synchronizeScrolling(
                (JScrollPane) usernamePanel.getComponent(1),
                (JScrollPane) timeoutPanel.getComponent(1),
                (JScrollPane) ratingPanel.getComponent(1),
                (JScrollPane) timeout960Panel.getComponent(1)
        );

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildColumnPanel(String title, JList<String> list, DefaultListModel<String> model) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);

        JLabel headerLabel = new JLabel(title, SwingConstants.CENTER);
        headerLabel.setForeground(yellow);
        headerLabel.setFont(labelFont);
        headerLabel.setBorder(BorderFactory.createLineBorder(yellow, 2));

        list.setModel(model);
        list.setFont(listFont);
        list.setForeground(yellow);
        list.setBackground(Color.BLACK);
        list.setBorder(BorderFactory.createLineBorder(Color.white, 1));
        list.setVisibleRowCount(20);

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(null);

        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void synchronizeScrolling(JScrollPane sp1, JScrollPane sp2, JScrollPane sp3, JScrollPane sp4) {
        JScrollBar sb1 = sp1.getVerticalScrollBar();
        JScrollBar sb2 = sp2.getVerticalScrollBar();
        JScrollBar sb3 = sp3.getVerticalScrollBar();
        JScrollBar sb4 = sp4 != null ? sp4.getVerticalScrollBar() : null;

        sb1.addAdjustmentListener(e -> {
            sb2.setValue(e.getValue());
            sb3.setValue(e.getValue());
            if (sb4 != null) sb4.setValue(e.getValue());
        });
        sb2.addAdjustmentListener(e -> {
            sb1.setValue(e.getValue());
            sb3.setValue(e.getValue());
            if (sb4 != null) sb4.setValue(e.getValue());
        });
        sb3.addAdjustmentListener(e -> {
            sb1.setValue(e.getValue());
            sb2.setValue(e.getValue());
            if (sb4 != null) sb4.setValue(e.getValue());
        });
        if (sb4 != null) {
            sb4.addAdjustmentListener(e -> {
                sb1.setValue(e.getValue());
                sb2.setValue(e.getValue());
                sb3.setValue(e.getValue());
            });
        }
    }
}