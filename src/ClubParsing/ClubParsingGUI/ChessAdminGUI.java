package ClubParsing.ClubParsingGUI;

import FileManagement.FileMenu;
import ClubParsing.ClubParsingLogic.ChessStats;
import MatchParsing.MatchParsingGUI.MatchClubParsingGUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;

public class ChessAdminGUI extends JFrame {
    private MatchClubParsingGUI matchClubParsingGUI;
    public JProgressBar progressBar;
    protected JScrollPane usernameScrollPane, timeoutScrollPane, ratingScrollPane, timeout960ScrollPane;
    protected JPanel usernamePanel, timeoutPanel, ratingPanel, timeout960Panel;
    Color yellow = new Color(204, 204, 0);
    Color purple = new Color(186, 85, 211);
    JLabel usernameHeader = new JLabel("Usernames:");
    JLabel timeoutHeader = new JLabel("Timeouts:");
    JLabel ratingHeader = new JLabel("Ratings:");
    JLabel timeout960Header = new JLabel("960 Timeouts:");
    Font preferredFont = new Font("Tahoma ", Font.PLAIN, 24);
    DefaultListModel<String> usernameListModel = new DefaultListModel<>();
    DefaultListModel<String> timeoutListModel = new DefaultListModel<>();
    DefaultListModel<String> ratingListModel = new DefaultListModel<>();
    DefaultListModel<String> timeout960ListModel = new DefaultListModel<>();
    JList<String> usernameList = new JList<>(usernameListModel);
    JList<String> timeoutList = new JList<>(timeoutListModel);
    JList<String> ratingList = new JList<>(ratingListModel);
    JList<String> timeout960List = new JList<>(timeout960ListModel);
    JPanel innerPanel = new JPanel();
    String teamNameTF = "";
    String timeoutTF = "";
    JTextField[] textFields = new JTextField[2];
    JTextField playersCurrentlyParsed = new JTextField();

    private volatile boolean paused = false;
    private volatile boolean stopped = false;


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
        ratingPanel = new JPanel(new BorderLayout());
        timeoutPanel = new JPanel(new BorderLayout());
        timeout960Panel = new JPanel(new BorderLayout());
        JButton timeOutButton = createTimeoutButton();
        JButton matchFrameButton = openClubParsingButton();

        FileMenu menuBar = new FileMenu();
        // Generates menu bar
        setJMenuBar(menuBar.getChessMenu());

        usernamePanel.setOpaque(false);
        ratingPanel.setOpaque(false);
        timeoutPanel.setOpaque(false);
        timeout960Panel.setOpaque(false);

        // Sets up the Jlists for the 4 columns
        implementLists();

        JLabel matchParsingButtonHeader = new JLabel(
                "Find players registered in a match above a certain time out ratio.");
        // Sets up the headers
        implementHeaders(matchParsingButtonHeader);

        usernamePanel.add(usernameHeader, BorderLayout.NORTH);
        usernamePanel.add(new JScrollPane(usernameList), BorderLayout.CENTER);
        timeoutPanel.add(timeoutHeader, BorderLayout.NORTH);
        timeoutPanel.add(new JScrollPane(timeoutList), BorderLayout.CENTER);
        ratingPanel.add(ratingHeader, BorderLayout.NORTH);
        ratingPanel.add(new JScrollPane(usernameList), BorderLayout.CENTER);
        timeout960Panel.add(timeout960Header, BorderLayout.NORTH);
        timeout960Header.add(new JScrollPane(timeoutList), BorderLayout.CENTER);
        // Sets up scrollpanes

        implementScrollpanes();
        usernamePanel.add(usernameScrollPane, BorderLayout.CENTER);
        timeoutPanel.add(timeoutScrollPane, BorderLayout.CENTER);
        ratingPanel.add(ratingScrollPane, BorderLayout.CENTER);
        timeout960Panel.add(timeout960ScrollPane, BorderLayout.CENTER);

        addPosition(100, 700, 700, 50, matchParsingButtonHeader);

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
        listPanel.add(ratingPanel);
        listPanel.add(timeout960Panel);

        backgroundLabel.add(listPanel, BorderLayout.WEST);

        addingTitle(innerPanel);
        addingHeadersForTextFields(innerPanel);
        textFields = addingTextFields(innerPanel);
        addPosition(390, 200, 150, 50, timeOutButton);
        JButton pauseButton = new JButton("Pause/Resume");
        JButton stopButton = new JButton("Stop parsing");


        addPosition(325, 250, 150, 50, pauseButton);
        addPosition(475, 250, 150, 50, stopButton);

        pauseButton.addActionListener(e -> {
            togglePauseResume();
            pauseButton.setText(paused ? "Resume" : "Pause");
        });

        stopButton.addActionListener(e -> stopParsing());

        styleButton(pauseButton);
        styleButton(stopButton);

        innerPanel.add(timeOutButton);
        innerPanel.add(matchFrameButton);
        innerPanel.add(pauseButton);
        innerPanel.add(stopButton);

        addPosition(390, 200, 150, 50, timeOutButton);
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
                        JOptionPane.showMessageDialog(ChessAdminGUI.this, "Unable to open player's profile page.", "Unable to open", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        setMinimumSize(new Dimension(250, 250));

        JProgressBar progressBar = implementProgressBar();
        add(progressBar, BorderLayout.NORTH);

        setVisible(true);

    }


    private void implementScrollpanes() {
        usernameScrollPane = new JScrollPane(usernameList);
        timeoutScrollPane = new JScrollPane(timeoutList);
        ratingScrollPane = new JScrollPane(ratingList);
        timeout960ScrollPane = new JScrollPane(timeout960List);

        synchronizeScrolling(usernameScrollPane, timeoutScrollPane, ratingScrollPane, timeout960ScrollPane);

        usernameScrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
        timeoutScrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
        ratingScrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
        timeout960ScrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));

        usernameScrollPane.setPreferredSize(new Dimension(150, usernameScrollPane.getHeight()));
        ratingScrollPane.setPreferredSize(new Dimension(20, ratingScrollPane.getHeight()));
        timeoutScrollPane.setPreferredSize(new Dimension(10, timeoutScrollPane.getHeight()));
        timeout960ScrollPane.setPreferredSize(new Dimension(10, timeout960ScrollPane.getHeight()));
    }

    private void implementHeaders(JLabel matchParsingButtonHeader) {
        usernameHeader.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));
        timeoutHeader.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));
        ratingHeader.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));
        timeout960Header.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));

        matchParsingButtonHeader.setBorder(BorderFactory.createLineBorder(Color.white, 3));

        usernameHeader.setForeground(yellow);
        timeoutHeader.setForeground(yellow);
        ratingHeader.setForeground(yellow);
        timeout960Header.setForeground(yellow);

        matchParsingButtonHeader.setForeground(yellow);

        usernameHeader.setFont(preferredFont);
        timeoutHeader.setFont(preferredFont);
        ratingHeader.setFont(preferredFont);
        timeout960Header.setFont(preferredFont);

        matchParsingButtonHeader.setFont(preferredFont);
    }

    private void implementLists() {
        int top = 4;
        int left = 2;
        int bottom = 4;
        int right = 2;
        Border innerPadding = new EmptyBorder(top, left, bottom, right);

        Border line = BorderFactory.createLineBorder(Color.gray, 2);


        usernameList.setBorder(new CompoundBorder(line, innerPadding));
        timeoutList.setBorder(new CompoundBorder(line, innerPadding));
        ratingList.setBorder(new CompoundBorder(line, innerPadding));
        timeout960List.setBorder(new CompoundBorder(line, innerPadding));

        usernameList.setCellRenderer(new CustomListCellRenderer());
        timeoutList.setCellRenderer(new CustomListCellRenderer());
        ratingList.setCellRenderer(new CustomListCellRenderer());
        timeout960List.setCellRenderer(new CustomListCellRenderer());

        usernameList.setBorder(BorderFactory.createLineBorder(Color.gray));
        timeoutList.setBorder(BorderFactory.createLineBorder(Color.gray));
        ratingList.setBorder(BorderFactory.createLineBorder(Color.gray));
        timeout960List.setBorder(BorderFactory.createLineBorder(Color.gray));

        usernameList.setPrototypeCellValue("Username");
        ratingList.setPrototypeCellValue("Rating");
        timeoutList.setPrototypeCellValue("Timeout rate");
        timeout960List.setPrototypeCellValue("960 Timeout rate");

        usernameList.setFont(preferredFont);
        timeoutList.setFont(preferredFont);
        ratingList.setFont(preferredFont);
        timeout960List.setFont(preferredFont);

        usernameList.setVisibleRowCount(25);
        timeoutList.setVisibleRowCount(25);
        ratingList.setVisibleRowCount(25);
        timeout960List.setVisibleRowCount(25);

        usernameList.setForeground(yellow);
        timeoutList.setForeground(yellow);
        ratingList.setForeground(yellow);
        timeout960List.setForeground(yellow);

        usernameList.setBackground(Color.black);
        timeoutList.setBackground(Color.black);
        ratingList.setBackground(Color.black);
        timeout960List.setBackground(Color.black);

        usernameList.setBorder(BorderFactory.createLineBorder(Color.white, 2));
        timeoutList.setBorder(BorderFactory.createLineBorder(Color.white, 2));
        ratingList.setBorder(BorderFactory.createLineBorder(Color.white, 2));
        timeout960List.setBorder(BorderFactory.createLineBorder(Color.white, 2));
    }

    public static void synchronizeScrolling(JScrollPane sp1, JScrollPane sp2, JScrollPane sp3, JScrollPane sp4) {
        JScrollBar sb1 = sp1.getVerticalScrollBar();
        JScrollBar sb2 = sp2.getVerticalScrollBar();
        JScrollBar sb3 = sp3.getVerticalScrollBar();
        JScrollBar sb4 = sp4.getVerticalScrollBar();

        sb1.addAdjustmentListener(e -> {
            int value = e.getValue();
            sb2.setValue(value);
            sb3.setValue(value);
            sb4.setValue(value);
        });

        sb2.addAdjustmentListener(e -> {
            int value = e.getValue();
            sb1.setValue(value);
            sb3.setValue(value);
            sb4.setValue(value);
        });

        sb3.addAdjustmentListener(e -> {
            int value = e.getValue();
            sb1.setValue(value);
            sb2.setValue(value);
            sb4.setValue(value);
        });

        sb4.addAdjustmentListener(e -> {
            int value = e.getValue();
            sb1.setValue(value);
            sb2.setValue(value);
            sb3.setValue(value);
        });
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.YELLOW);
        button.setFont(new Font("Arial", Font.BOLD, 16));
    }

    public synchronized void togglePauseResume() {
        paused = !paused;
        if (!paused) {
            notifyAll();
        }
    }

    public synchronized void stopParsing() {
        stopped = true;
        notifyAll();
    }

    public synchronized void checkPausedAndStopped() throws InterruptedException {
        while (paused && !stopped) {
            wait();
        }
        if (stopped) {
            throw new InterruptedException("Parsing stopped by user.");
        }
    }


    public void setProgressBarMax(int max) {
        progressBar.setMaximum(max);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("0/" + max + " (0%)");
    }

    public void updateProgressBar(int value) {
        progressBar.setValue(value);
        int percent = (int) ((value * 100.0f) / progressBar.getMaximum());
        progressBar.setString(value + "/" + progressBar.getMaximum() + " (" + percent + "%)");

        if (progressBar.getValue() == progressBar.getMaximum()) {
            progressBar.setString("Complete: " + value + "/" + progressBar.getMaximum() + " (100%)");
            JOptionPane.showMessageDialog(this,
                    "Parsing complete. Processed " + value + " players.");
        }
    }

    public JProgressBar implementProgressBar() {
        progressBar = new JProgressBar();
        progressBar.setBackground(Color.BLACK);
        progressBar.setForeground(Color.GREEN);
        progressBar.setBorderPainted(true);
        progressBar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        progressBar.setStringPainted(true);
        UIManager.put("ProgressBar.selectionForeground", Color.black);
        return progressBar;
    }

    public void addUser(String username, double timeout, int rating, double timeout960) {
        usernameListModel.addElement(username);
        timeoutListModel.addElement(timeout + "%");
        ratingListModel.addElement(String.valueOf(rating));
        timeout960ListModel.addElement(timeout960 + "%");
    }

    public JLabel addingTitle(JPanel innerPanel) {
        JLabel titleOfPage = new JLabel("Admin timeout tool");

        addPosition(225, 33, 400, 50, titleOfPage);
        innerPanel.add(titleOfPage);

        titleOfPage.setVisible(true);
        titleOfPage.setHorizontalAlignment(JLabel.CENTER);
        titleOfPage.setFont(new Font("Arial", Font.BOLD, 30));
        titleOfPage.setForeground(Color.GREEN);
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

        addPosition(200, 91, 250, 40, usernameTimeoutRateHeaderTF[0]);
        innerPanel.add(usernameTimeoutRateHeaderTF[0]);

        addPosition(450, 91, 250, 40, usernameTimeoutRateHeaderTF[1]);
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

    public void addCurrentClubParsingTF(JPanel innerPanel, JTextField textField) {
        textField.setBackground(Color.BLACK);
        textField.setForeground(Color.GREEN);
        textField.setText("Checking: " + teamNameTF);
        textField.setEditable(false);
        textField.setVisible(true);
        textField.setFont(new Font("Arial", Font.BOLD, 20));

        FontMetrics metrics = textField.getFontMetrics(textField.getFont());
        int textWidth = metrics.stringWidth(textField.getText()) + 20;
        int textHeight = metrics.getHeight() + 5;
        textField.setPreferredSize(new Dimension(textWidth, textHeight));
        textField.setMinimumSize(new Dimension(textWidth, textHeight));

        innerPanel.setLayout(new BorderLayout());
        innerPanel.add(textField, BorderLayout.NORTH);

        innerPanel.revalidate();
        innerPanel.repaint();

    }


    public JTextField[] addingTextFields(JPanel innerPanel) {
        JTextField[] textFields = new JTextField[2];
        textFields[0] = new JTextField("Enter team name here", 28);
        textFields[1] = new JTextField("Enter timeout rate here", 28);

        FocusAdapter focusAdapter = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                JTextField textField = (JTextField) e.getSource();
                if (textField.getText().equals("Enter team name here") || textField.getText().equals("Enter timeout rate here")) {
                    textField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                JTextField textField = (JTextField) e.getSource();
                if (textField.getText().isEmpty()) {
                    if (textField == textFields[0]) {
                        textField.setText("Enter team name here");
                    } else if (textField == textFields[1]) {
                        textField.setText("Enter timeout rate here");
                    }
                }
            }
        };

        for (JTextField textField : textFields) {
            textField.setHorizontalAlignment(JLabel.CENTER);
            textField.setFont(preferredFont);
            textField.setForeground(yellow);
            textField.setBackground(Color.BLACK);
            textField.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            textField.addFocusListener(focusAdapter);
        }

        addPosition(200, 130, 250, 40, textFields[0]);
        innerPanel.add(textFields[0]);

        addPosition(475, 130, 250, 40, textFields[1]);
        innerPanel.add(textFields[1]);

        return textFields;
    }

    public JButton openClubParsingButton() {
        JButton matchParsingButton = new JButton("Match Parsing");
        addPosition(300, 750, 250, 50, matchParsingButton);
        matchParsingButton.setContentAreaFilled(true);
        matchParsingButton.setVisible(true);

        matchParsingButton.setFont(preferredFont);
        matchParsingButton.setForeground(purple);
        matchParsingButton.setBackground(Color.black);
        matchParsingButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        matchParsingButton.setEnabled(true);
        matchParsingButton.addActionListener(e -> {
            if (matchClubParsingGUI == null) {
                matchClubParsingGUI = new MatchClubParsingGUI();
                matchClubParsingGUI.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        matchClubParsingGUI = null;
                        matchParsingButton.setEnabled(true);
                    }
                });
            } else {
                matchClubParsingGUI.setVisible(true);
            }
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
            ratingListModel.clear();
            timeout960ListModel.clear();

            teamNameTF = textFields[0].getText();
            timeoutTF = textFields[1].getText();
            addCurrentClubParsingTF(innerPanel, playersCurrentlyParsed);
            boolean isTeamNameEmpty = teamNameTF.isBlank();
            boolean isTimeoutEmpty = timeoutTF.isEmpty();

            if (isTeamNameEmpty && isTimeoutEmpty) {
                JOptionPane.showMessageDialog(this, "The team match and timeout rate fields are both empty.");
            } else if (isTeamNameEmpty) {
                JOptionPane.showMessageDialog(this, "The team match field is empty.");
            } else if (isTimeoutEmpty) {
                JOptionPane.showMessageDialog(this, "The timeout rate field is empty.");
            } else {
                try {
                    double maxTimeoutRate = Double.parseDouble(timeoutTF);
                    if (maxTimeoutRate < 0 || maxTimeoutRate > 100) {
                        JOptionPane.showMessageDialog(this, "Invalid timeout rate. Please enter a value between 0 and 100.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Checking timeout rate for players from the club  " + teamNameTF
                                + " with a timeout rate less than or equal to " + timeoutTF + "%");
                        ChessStats.fetchClubData(this, teamNameTF, maxTimeoutRate);

                        textFields[0].setText("Enter team name here");
                        textFields[1].setText("Enter timeout rate here");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid timeout rate. Please enter a valid number.");
                }
            }
        });

        return submitButton;
    }

    static class CustomListCellRenderer extends DefaultListCellRenderer {
        private final Border outerPadding = BorderFactory.createEmptyBorder(7, 2, 7, 2);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(outerPadding);
            return label;
        }
    }
}