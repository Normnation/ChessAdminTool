package ClubParsing.ClubParsingGUI;

import ClubParsing.ClubParsingLogic.ChessStats;
import FileManagement.FileMenu;
import MatchParsing.MatchParsingGUI.MatchClubParsingGUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChessAdminGUI extends JFrame {
    private final Color BG = new Color(10, 10, 12);
    private final Color SURFACE = new Color(18, 20, 24);
    private final Color SURFACE_ALT = new Color(24, 26, 32);
    private final Color TEXT = new Color(230, 232, 236);
    private final Color SUBTEXT = new Color(180, 182, 186);
    private final Color ACCENT = new Color(120, 86, 255);
    private final Color WARN = new Color(255, 212, 64);
    private final Color GOOD = new Color(100, 230, 150);
    private final Font tableFont = new Font("Tahoma", Font.PLAIN, 18);
    private final Font tableHeaderFont = new Font("Tahoma", Font.BOLD, 18);
    private final DefaultComboBoxModel<String> clubModel = new DefaultComboBoxModel<>();
    private final List<String> allClubSlugs = new ArrayList<>();
    private final List<String> allClubSlugsLower = new ArrayList<>();
    private final DefaultListSelectionModel linkedSelection = new DefaultListSelectionModel();
    public JProgressBar progressBar;
    protected JScrollPane usernameScrollPane, timeoutScrollPane, ratingScrollPane, timeout960ScrollPane;
    protected JPanel usernamePanel, timeoutPanel, ratingPanel, timeout960Panel;
    JLabel usernameHeader = new JLabel("Players");
    JLabel timeoutHeader = new JLabel("Timeouts");
    JLabel ratingHeader = new JLabel("Ratings");
    JLabel timeout960Header = new JLabel("960 Timeouts");
    Font preferredFont = new Font("Tahoma", Font.PLAIN, 20);
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
    JTextField[] textFields = new JTextField[2]; // hidden but kept for compatibility
    JTextField playersCurrentlyParsed = new JTextField("Ready.");
    private long startTime;
    private MatchClubParsingGUI matchClubParsingGUI;
    private JPanel controlsCard;
    private volatile boolean paused = false;
    private volatile boolean stopped = false;
    private JComboBox<String> clubCombo;
    private Timer filterTimer;
    private JSpinner timeoutSpinner;
    private JSpinner minRatingSpinner;
    private JSpinner maxRatingSpinner;
    private String lastFilterQuery = "";
    private boolean updatingModel = false;
    private int hoverIndex = -1;

    private JTable rosterTable;
    private javax.swing.table.DefaultTableModel rosterModel;

    public ChessAdminGUI() {
        ImageIcon backgroundImage = new ImageIcon("resources/MainChessImage.jpg");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 760));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);
        setLayout(new BorderLayout());
        setIconImage(backgroundImage.getImage());
        setTitle("Admin chess tool.");
        getContentPane().setBackground(BG);

        FileMenu menuBar = new FileMenu();
        setJMenuBar(menuBar.getChessMenu());

        usernamePanel = roundedPanel();
        ratingPanel = roundedPanel();
        timeoutPanel = roundedPanel();
        timeout960Panel = roundedPanel();

        implementLists();

        JLabel sectionHint = new JLabel("Find players registered in a match above a certain timeout ratio.", SwingConstants.LEFT);
        sectionHint.setForeground(SUBTEXT);
        sectionHint.setFont(preferredFont);
        implementHeaders(sectionHint);

        JButton submitBtn = createTimeoutButton();
        ModernButton pauseButton = new ModernButton("Pause");
        ModernButton stopButton = new ModernButton("Stop parsing");
        JButton matchFrameButton = openClubParsingButton();

        pauseButton.addActionListener(e -> {
            togglePauseResume();
            pauseButton.setText(paused ? "Resume" : "Pause");
        });

        stopButton.addActionListener(e -> {
            stopParsing();
            JOptionPane.showMessageDialog(this, "Parsing has been stopped.", "Stopped parsing", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel backgroundPanel = new BackgroundPanel(backgroundImage.getImage());
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBorder(new EmptyBorder(12, 12, 16, 16));
        add(backgroundPanel, BorderLayout.CENTER);

        JComponent rosterPanel = buildRosterPanel();
        innerPanel = buildControlPanel(sectionHint, submitBtn, pauseButton, stopButton, matchFrameButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, rosterPanel, innerPanel);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.0);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        styleSplitPane(splitPane);

        backgroundPanel.add(splitPane, BorderLayout.CENTER);

        initProgressBar();
        loadClubsForCountry("CA");

        setVisible(true);

        SwingUtilities.invokeLater(() -> {
            int target = Math.min(580, Math.max(460, getWidth() / 3));
            splitPane.setDividerLocation(target);
        });
    }

    public static void synchronizeScrolling(JScrollPane... panes) {
        List<JScrollBar> bars = Arrays.stream(panes).map(JScrollPane::getVerticalScrollBar).toList();
        AtomicBoolean syncing = new AtomicBoolean(false);

        bars.forEach(bar -> bar.addAdjustmentListener(e -> {
            if (syncing.getAndSet(true)) return;

            int v = e.getValue();
            bars.forEach(other -> {
                if (other != bar) other.setValue(v);
            });

            syncing.set(false);
        }));
    }

    private JPanel buildControlPanel(JLabel sectionHint,
                                     JButton submitBtn,
                                     JButton pauseButton,
                                     JButton stopButton,
                                     JButton matchFrameButton) {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setOpaque(false);
        shell.setBorder(new EmptyBorder(26, 28, 0, 10));

        initFilterControls();
        makeClubComboSearchableFast();
        createHiddenLegacyTextFields();

        JPanel topWrapper = new JPanel(new GridBagLayout());
        topWrapper.setOpaque(false);

        controlsCard = new JPanel(new GridBagLayout());
        controlsCard.setOpaque(false);
        controlsCard.setBorder(new EmptyBorder(0, 12, 16, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel title = new JLabel("Admin timeout tool", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(GOOD);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        controlsCard.add(title, gbc);

        styleStatusField(playersCurrentlyParsed);
        gbc.gridy = 1;
        gbc.insets = new Insets(8, 8, 14, 8);
        controlsCard.add(playersCurrentlyParsed, gbc);

        JLabel clubLabel = createFormLabel("Club Name");
        JLabel timeoutLabel = createFormLabel("Timeout rate");

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 8, 0, 8);
        controlsCard.add(clubLabel, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        controlsCard.add(timeoutLabel, gbc);

        setControlSize(clubCombo, 430, 46);
        setControlSize(timeoutSpinner, 220, 46);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 8, 14, 8);
        controlsCard.add(clubCombo, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        controlsCard.add(timeoutSpinner, gbc);

        JLabel minLabel = createFormLabel("Min rating");
        JLabel maxLabel = createFormLabel("Max rating");

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 8, 0, 8);
        controlsCard.add(minLabel, gbc);

        gbc.gridx = 1;
        controlsCard.add(maxLabel, gbc);

        gbc.gridx = 2;
        controlsCard.add(Box.createHorizontalStrut(1), gbc);

        setControlSize(minRatingSpinner, 200, 46);
        setControlSize(maxRatingSpinner, 200, 46);
        setControlSize(submitBtn, 160, 46);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.insets = new Insets(4, 8, 16, 8);
        controlsCard.add(minRatingSpinner, gbc);

        gbc.gridx = 1;
        controlsCard.add(maxRatingSpinner, gbc);

        gbc.gridx = 2;
        controlsCard.add(submitBtn, gbc);

        JPanel parseButtonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        parseButtonRow.setOpaque(false);
        setControlSize(pauseButton, 170, 48);
        setControlSize(stopButton, 170, 48);
        parseButtonRow.add(pauseButton);
        parseButtonRow.add(stopButton);

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 8, 0, 8);
        controlsCard.add(parseButtonRow, gbc);

        GridBagConstraints wrapGbc = new GridBagConstraints();
        wrapGbc.gridx = 0;
        wrapGbc.gridy = 0;
        wrapGbc.weightx = 1;
        wrapGbc.anchor = GridBagConstraints.NORTH;
        wrapGbc.fill = GridBagConstraints.HORIZONTAL;
        topWrapper.add(controlsCard, wrapGbc);

        JPanel bottom = new JPanel(new BorderLayout(18, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(12, 0, 0, 0));

        sectionHint.setBorder(new CompoundBorder(
                new RoundedBorder(new Color(255, 255, 255, 75), 14, 1.2f),
                new EmptyBorder(8, 14, 8, 14)
        ));

        setControlSize(matchFrameButton, 220, 54);

        bottom.add(sectionHint, BorderLayout.CENTER);
        bottom.add(matchFrameButton, BorderLayout.EAST);

        shell.add(topWrapper, BorderLayout.NORTH);
        shell.add(bottom, BorderLayout.SOUTH);

        return shell;
    }

    private void createHiddenLegacyTextFields() {
        textFields[0] = new JTextField("Enter team name here", 28);
        textFields[1] = new JTextField("Enter timeout rate here", 28);

        for (JTextField tf : textFields) {
            tf.setVisible(false);
            tf.setFont(preferredFont);
            tf.setForeground(TEXT);
            tf.setBackground(SURFACE);
            tf.setBorder(new RoundedBorder(new Color(255, 255, 255, 70), 12, 1.2f));
        }
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(preferredFont);
        label.setForeground(WARN);
        return label;
    }

    private void styleStatusField(JTextField textField) {
        textField.setBackground(SURFACE);
        textField.setForeground(TEXT);
        textField.setCaretColor(TEXT);
        textField.setEditable(false);
        textField.setVisible(true);
        textField.setFont(new Font("Arial", Font.BOLD, 18));
        textField.setHorizontalAlignment(SwingConstants.LEFT);
        textField.setBorder(new CompoundBorder(
                new RoundedBorder(new Color(255, 255, 255, 60), 12, 1.2f),
                new EmptyBorder(4, 10, 4, 10)
        ));
    }

    private void setControlSize(JComponent component, int width, int height) {
        Dimension d = new Dimension(width, height);
        component.setPreferredSize(d);
        component.setMinimumSize(d);
    }

    private void styleSplitPane(JSplitPane splitPane) {
        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(new Color(255, 255, 255, 28));
                        g2.fillRoundRect(2, 0, getWidth() - 4, getHeight(), 8, 8);
                        g2.dispose();
                    }
                };
            }
        });
    }

    private void initProgressBar() {
        progressBar = implementProgressBar();
        add(progressBar, BorderLayout.NORTH);
    }

    private JPanel roundedPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        return p;
    }

    private JScrollPane wrap(JList<String> list) {
        JScrollPane sp = new JScrollPane(list);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(new LineBorder(new Color(255, 255, 255, 60), 1, true));
        return sp;
    }

    private void place(JComponent c, int x, int y, int w, int h) {
        c.setBounds(x, y, w, h);
    }

    private void initFilterControls() {
        clubCombo = new JComboBox<>(clubModel);
        clubCombo.setEditable(true);
        clubCombo.setFont(preferredFont);
        clubCombo.setForeground(TEXT);
        clubCombo.setBackground(SURFACE);
        clubCombo.setOpaque(false);
        clubCombo.setMaximumRowCount(18);
        clubCombo.setBorder(new RoundedBorder(new Color(255, 255, 255, 70), 12, 1.5f));
        clubCombo.setUI(new BasicComboBoxUI() {
            @Override
            protected ComboPopup createPopup() {
                return new BasicComboPopup(comboBox) {
                    @Override
                    protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
                        int popupW = Math.max(420, clubCombo.getWidth());
                        return super.computePopupBounds(px, py, popupW, ph);
                    }
                };
            }
        });

        clubCombo.setSelectedItem("");
        clubCombo.setKeySelectionManager((key, model) -> -1);

        JTextField editor = (JTextField) clubCombo.getEditor().getEditorComponent();
        editor.setText("");
        editor.setForeground(TEXT);
        editor.setCaretColor(TEXT);
        editor.setBackground(SURFACE);
        editor.setBorder(new EmptyBorder(6, 10, 6, 10));

        timeoutSpinner = new JSpinner(new SpinnerNumberModel(20.0, 0.0, 100.0, 0.5));
        styleSpinner(timeoutSpinner);

        minRatingSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 3000, 50));
        maxRatingSpinner = new JSpinner(new SpinnerNumberModel(1599, 0, 3000, 50));
        styleSpinner(minRatingSpinner);
        styleSpinner(maxRatingSpinner);
    }

    private JComponent buildRosterPanel() {
        rosterModel = new javax.swing.table.DefaultTableModel(
                new Object[]{"Username", "Timeout %", "Rating", "960 Timeout %"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 1, 3 -> String.class;
                    case 2 -> Integer.class;
                    default -> String.class;
                };
            }
        };

        rosterTable = new JTable(rosterModel);
        rosterTable.setAutoCreateRowSorter(true);
        rosterTable.setRowHeight(30);
        rosterTable.setFont(tableFont);
        rosterTable.setForeground(TEXT);
        rosterTable.setBackground(SURFACE_ALT);
        rosterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rosterTable.setShowGrid(false);
        rosterTable.setIntercellSpacing(new Dimension(0, 0));
        rosterTable.setFillsViewportHeight(true);
        rosterTable.setOpaque(true);

        javax.swing.table.JTableHeader hdr = rosterTable.getTableHeader();
        hdr.setReorderingAllowed(false);
        hdr.setResizingAllowed(true);
        hdr.setFont(tableHeaderFont);
        hdr.setForeground(WARN);
        hdr.setBackground(BG);
        hdr.setPreferredSize(new Dimension(10, 32));

        javax.swing.table.DefaultTableCellRenderer headerRenderer =
                (javax.swing.table.DefaultTableCellRenderer) rosterTable.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        javax.swing.table.DefaultTableCellRenderer cell = new javax.swing.table.DefaultTableCellRenderer() {
            private final Color stripeA = SURFACE_ALT;
            private final Color stripeB = new Color(21, 23, 29);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setBorder(new EmptyBorder(5, 10, 5, 10));
                lbl.setOpaque(true);

                if (!isSelected) {
                    lbl.setBackground((row % 2 == 0) ? stripeA : stripeB);
                    lbl.setForeground(TEXT);
                } else {
                    lbl.setBackground(new Color(120, 86, 255, 180));
                    lbl.setForeground(Color.WHITE);
                }

                lbl.setHorizontalAlignment((col == 1 || col == 2 || col == 3) ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return lbl;
            }
        };
        rosterTable.setDefaultRenderer(Object.class, cell);
        rosterTable.setDefaultRenderer(Integer.class, cell);

        rosterTable.getColumnModel().getColumn(0).setPreferredWidth(215);
        rosterTable.getColumnModel().getColumn(1).setPreferredWidth(105);
        rosterTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        rosterTable.getColumnModel().getColumn(3).setPreferredWidth(120);

        rosterTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = rosterTable.getSelectedRow();
                    if (viewRow >= 0) {
                        int modelRow = rosterTable.convertRowIndexToModel(viewRow);
                        String username = String.valueOf(rosterModel.getValueAt(modelRow, 0));
                        try {
                            Desktop.getDesktop().browse(new URI("https://www.chess.com/member/" + username));
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(ChessAdminGUI.this, "Unable to open player's profile page.",
                                    "Unable to open", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(rosterTable);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(true);
        sp.getViewport().setBackground(SURFACE_ALT);
        sp.setBorder(new RoundedBorder(new Color(255, 255, 255, 50), 14, 1.2f));

        JPanel shell = new JPanel(new BorderLayout());
        shell.setOpaque(false);
        shell.setBorder(new EmptyBorder(0, 0, 0, 12));
        shell.add(sp, BorderLayout.CENTER);

        shell.setPreferredSize(new Dimension(560, 10));
        shell.setMinimumSize(new Dimension(420, 10));

        return shell;
    }

    private void makeClubComboSearchableFast() {
        JTextField editor = (JTextField) clubCombo.getEditor().getEditorComponent();
        editor.setForeground(TEXT);
        editor.setBackground(SURFACE);
        editor.setCaretColor(TEXT);
        editor.setBorder(new EmptyBorder(6, 10, 6, 10));

        if (filterTimer != null) filterTimer.stop();
        filterTimer = new Timer(180, e -> applyClubFilter(editor.getText()));
        filterTimer.setRepeats(false);

        editor.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() {
                if (updatingModel) return;
                filterTimer.restart();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
        });

        clubCombo.setKeySelectionManager((key, model) -> -1);

        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    clubCombo.hidePopup();
                }
            }
        });
    }

    private JList<?> getPopupList() {
        try {
            Component accChild = (Component) clubCombo.getAccessibleContext().getAccessibleChild(0);
            if (accChild instanceof BasicComboPopup popup) return popup.getList();
        } catch (Exception ignored) {
        }
        return null;
    }

    private void applyClubFilter(String typed) {
        String q = typed == null ? "" : typed.toLowerCase();
        if (q.equals(lastFilterQuery)) return;
        lastFilterQuery = q;

        boolean wasVisible = clubCombo.isPopupVisible();
        JList<?> popupList = getPopupList();
        int topIndex = -1;
        if (wasVisible && popupList != null) topIndex = popupList.getFirstVisibleIndex();

        updatingModel = true;
        try {
            final int MAX_RESULTS = 300;
            Object editorItem = clubCombo.getEditor().getItem();
            clubModel.removeAllElements();

            if (q.isEmpty()) {
                int count = 0;
                for (String s : allClubSlugs) {
                    clubModel.addElement(s);
                    if (++count >= MAX_RESULTS) break;
                }
            } else {
                int added = 0;
                for (int i = 0; i < allClubSlugs.size() && added < MAX_RESULTS; i++) {
                    if (allClubSlugsLower.get(i).startsWith(q)) {
                        clubModel.addElement(allClubSlugs.get(i));
                        added++;
                    }
                }
                if (added < MAX_RESULTS) {
                    for (int i = 0; i < allClubSlugs.size() && added < MAX_RESULTS; i++) {
                        if (!allClubSlugsLower.get(i).startsWith(q) && allClubSlugsLower.get(i).contains(q)) {
                            clubModel.addElement(allClubSlugs.get(i));
                            added++;
                        }
                    }
                }
                if (clubModel.getSize() == 0) clubModel.addElement(typed);
            }

            clubCombo.getEditor().setItem(editorItem);
        } finally {
            updatingModel = false;
        }

        if (!wasVisible && clubModel.getSize() > 0) {
            clubCombo.showPopup();
        } else if (wasVisible && popupList != null && topIndex >= 0) {
            int last = Math.max(0, Math.min(topIndex, clubModel.getSize() - 1));
            if (clubModel.getSize() > 0) popupList.ensureIndexIsVisible(last);
        }
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(preferredFont);
        spinner.setForeground(TEXT);
        spinner.setBackground(SURFACE);
        spinner.setBorder(new RoundedBorder(new Color(255, 255, 255, 70), 12, 1.2f));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(SURFACE);
            de.getTextField().setForeground(TEXT);
            de.getTextField().setCaretColor(TEXT);
            de.getTextField().setHorizontalAlignment(SwingConstants.RIGHT);
            de.getTextField().setBorder(new EmptyBorder(6, 10, 6, 10));
        }
    }

    private void loadClubsForCountry(String iso) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("https://api.chess.com/pub/country/" + iso + "/clubs");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "ChessAdminGUI/1.0");
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(15_000);

                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    for (String line; (line = br.readLine()) != null; ) sb.append(line);

                    Matcher m = Pattern.compile("https://api\\.chess\\.com/pub/club/([^\"]+)").matcher(sb.toString());
                    List<String> slugs = new ArrayList<>();
                    while (m.find()) slugs.add(m.group(1));

                    SwingUtilities.invokeLater(() -> {
                        allClubSlugs.clear();
                        allClubSlugsLower.clear();

                        for (String s : slugs) {
                            allClubSlugs.add(s);
                            allClubSlugsLower.add(s.toLowerCase());
                        }

                        clubModel.removeAllElements();
                        int shown = 0;
                        for (String s : allClubSlugs) {
                            clubModel.addElement(s);
                            if (++shown >= 300) break;
                        }

                        clubCombo.setSelectedItem("");
                        JTextField ed = (JTextField) clubCombo.getEditor().getEditorComponent();
                        ed.setText("");
                    });
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                "Failed to load clubs for country " + iso + ": " + ex.getMessage(),
                                "Club list", JOptionPane.ERROR_MESSAGE));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }, "club-loader").start();
    }

    private void implementScrollpanes() {
        usernameScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, usernameList);
        timeoutScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, timeoutList);
        ratingScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, ratingList);
        timeout960ScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, timeout960List);

        if (usernameScrollPane == null || timeoutScrollPane == null || ratingScrollPane == null || timeout960ScrollPane == null)
            return;

        usernameScrollPane.setOpaque(false);
        usernameScrollPane.getViewport().setOpaque(false);
        timeoutScrollPane.setOpaque(false);
        timeoutScrollPane.getViewport().setOpaque(false);
        ratingScrollPane.setOpaque(false);
        ratingScrollPane.getViewport().setOpaque(false);
        timeout960ScrollPane.setOpaque(false);
        timeout960ScrollPane.getViewport().setOpaque(false);

        Border thinRound = new RoundedBorder(new Color(255, 255, 255, 50), 14, 1.2f);
        usernameScrollPane.setBorder(thinRound);
        timeoutScrollPane.setBorder(thinRound);
        ratingScrollPane.setBorder(thinRound);
        timeout960ScrollPane.setBorder(thinRound);

        synchronizeScrolling(usernameScrollPane, timeoutScrollPane, ratingScrollPane, timeout960ScrollPane);
    }

    private void implementHeaders(JLabel hint) {
        for (JLabel h : new JLabel[]{usernameHeader, timeoutHeader, ratingHeader, timeout960Header}) {
            h.setForeground(WARN);
            h.setFont(preferredFont.deriveFont(Font.BOLD));
            h.setBorder(new EmptyBorder(6, 10, 6, 10));
        }

        hint.setOpaque(false);
    }

    private void implementLists() {
        int top = 4, left = 2, bottom = 4, right = 2;
        Border innerPadding = new EmptyBorder(top, left, bottom, right);
        int fixedRowHeight = 28;

        for (JList<String> l : new JList[]{usernameList, timeoutList, ratingList, timeout960List}) {
            l.setFont(tableFont);
            l.setFixedCellHeight(fixedRowHeight);
            l.setForeground(TEXT);
            l.setBackground(SURFACE_ALT);
            l.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, 0), innerPadding));
            l.setSelectionModel(linkedSelection);
            l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        usernameList.setCellRenderer(new LinkedCellRenderer());
        timeoutList.setCellRenderer(new LinkedCellRenderer());
        ratingList.setCellRenderer(new LinkedCellRenderer());
        timeout960List.setCellRenderer(new LinkedCellRenderer());

        usernameList.setPrototypeCellValue("username_________");
        ratingList.setPrototypeCellValue("rating_____");
        timeoutList.setPrototypeCellValue("timeout rate");
        timeout960List.setPrototypeCellValue("960 timeout rate");

        usernameList.setVisibleRowCount(25);
        timeoutList.setVisibleRowCount(25);
        ratingList.setVisibleRowCount(25);
        timeout960List.setVisibleRowCount(25);

        installLinkedHover(usernameList, ratingList, timeoutList, timeout960List);
    }

    private void installLinkedHover(JList<?>... lists) {
        MouseMotionAdapter mover = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                JList<?> src = (JList<?>) e.getSource();
                int idx = src.locationToIndex(e.getPoint());
                if (idx != hoverIndex) {
                    hoverIndex = idx;
                    for (JList<?> l : lists) l.repaint();
                }
            }
        };

        MouseAdapter exit = new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoverIndex != -1) {
                    hoverIndex = -1;
                    for (JList<?> l : lists) l.repaint();
                }
            }
        };

        for (JList<?> l : lists) {
            l.addMouseMotionListener(mover);
            l.addMouseListener(exit);
        }
    }

    public void setProgressBarMax(int max) {
        Runnable update = () -> {
            progressBar.setMaximum(max);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            progressBar.setString(String.format("Starting... 0/%d (0%%)", max));
            progressBar.setForeground(ACCENT);
            progressBar.setBackground(SURFACE);
            startTime = System.currentTimeMillis();
        };

        if (SwingUtilities.isEventDispatchThread()) update.run();
        else SwingUtilities.invokeLater(update);
    }

    public void updateProgressBar(int value) throws IOException {
        Runnable update = () -> {
            progressBar.setValue(value);

            int max = Math.max(1, progressBar.getMaximum());
            int percent = (int) ((value * 100.0f) / max);

            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            long estimatedTotalTime = elapsedTime > 0 ? (long) ((elapsedTime / (double) Math.max(1, value)) * max) : 0;
            long remainingTime = Math.max(0, estimatedTotalTime - elapsedTime);

            String remainingTimeString = formatDuration(remainingTime);
            progressBar.setString(value + "/" + progressBar.getMaximum() + " (" + percent + "%) • ETA " + remainingTimeString);

            if (progressBar.getValue() == progressBar.getMaximum()) {
                progressBar.setString("Complete: " + value + "/" + progressBar.getMaximum() + " (100%)");
                JOptionPane.showMessageDialog(this, "Parsing complete. Processed " + value + " players.");
            }
        };

        if (SwingUtilities.isEventDispatchThread()) update.run();
        else SwingUtilities.invokeLater(update);
    }

    public JProgressBar implementProgressBar() {
        progressBar = new JProgressBar();
        progressBar.setBackground(SURFACE);
        progressBar.setForeground(ACCENT);
        progressBar.setBorder(new CompoundBorder(
                new EmptyBorder(3, 4, 3, 4),
                new RoundedBorder(new Color(255, 255, 255, 70), 10, 1.2f)
        ));
        progressBar.setPreferredSize(new Dimension(10, 30));
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Arial", Font.BOLD, 13));
        progressBar.setString("0%");
        UIManager.put("ProgressBar.selectionForeground", Color.BLACK);
        UIManager.put("ProgressBar.selectionBackground", TEXT);
        return progressBar;
    }

    public void addUser(String username, double timeout, int rating, double timeout960) {
        Runnable add = () -> {
            if (rosterModel != null) {
                rosterModel.addRow(new Object[]{
                        username,
                        String.format("%.0f%%", timeout),
                        rating,
                        String.format("%.0f%%", timeout960)
                });
            } else {
                usernameListModel.addElement(username);
                timeoutListModel.addElement(String.format("%.0f%%", timeout));
                ratingListModel.addElement(String.valueOf(rating));
                timeout960ListModel.addElement(String.format("%.0f%%", timeout960));
            }
        };

        if (SwingUtilities.isEventDispatchThread()) add.run();
        else SwingUtilities.invokeLater(add);
    }

    public JLabel addingTitle(JPanel innerPanel) {
        JLabel titleOfPage = new JLabel("Admin timeout tool", SwingConstants.CENTER);
        place(titleOfPage, 225, 33, 400, 50);
        innerPanel.add(titleOfPage);
        titleOfPage.setVisible(true);
        titleOfPage.setFont(new Font("Arial", Font.BOLD, 30));
        titleOfPage.setForeground(GOOD);
        return titleOfPage;
    }

    public JLabel[] addingHeadersForTextFields(JPanel innerPanel) {
        JLabel[] headers = new JLabel[2];
        headers[0] = new JLabel("Club Name", SwingConstants.CENTER);
        headers[1] = new JLabel("Timeout rate", SwingConstants.CENTER);

        place(headers[0], 170, 91, 420, 28);
        place(headers[1], 610, 91, 220, 28);

        for (JLabel h : headers) {
            h.setVisible(true);
            h.setFont(preferredFont);
            h.setForeground(WARN);
        }

        innerPanel.add(headers[0]);
        innerPanel.add(headers[1]);
        return headers;
    }

    public void addCurrentClubParsingTF(JPanel ignoredPanel, JTextField textField) {
        Runnable update = () -> {
            textField.setText("Checking: " + teamNameTF);
            textField.setVisible(true);
            styleStatusField(textField);

            if (textField.getParent() == null && controlsCard != null) {
                // In normal use this field is already in the layout.
                // This fallback keeps old external calls from silently doing nothing.
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.gridwidth = 3;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1;
                gbc.insets = new Insets(8, 8, 14, 8);
                controlsCard.add(textField, gbc);
            }

            textField.revalidate();
            textField.repaint();

            if (controlsCard != null) {
                controlsCard.revalidate();
                controlsCard.repaint();
            }
        };

        if (SwingUtilities.isEventDispatchThread()) update.run();
        else SwingUtilities.invokeLater(update);
    }

    public JTextField[] addingTextFields(JPanel innerPanel) {
        JTextField[] tfs = new JTextField[2];
        tfs[0] = new JTextField("Enter team name here", 28);
        tfs[1] = new JTextField("Enter timeout rate here", 28);

        FocusAdapter fa = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                JTextField tf = (JTextField) e.getSource();
                if (tf.getText().equals("Enter team name here") || tf.getText().equals("Enter timeout rate here"))
                    tf.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                JTextField tf = (JTextField) e.getSource();
                if (tf.getText().isEmpty()) {
                    if (tf == tfs[0]) tf.setText("Enter team name here");
                    else tf.setText("Enter timeout rate here");
                }
            }
        };

        for (JTextField tf : tfs) {
            tf.setHorizontalAlignment(JLabel.CENTER);
            tf.setFont(preferredFont);
            tf.setForeground(TEXT);
            tf.setBackground(SURFACE);
            tf.setBorder(new RoundedBorder(new Color(255, 255, 255, 70), 12, 1.2f));
            tf.addFocusListener(fa);
        }

        place(tfs[0], 200, 130, 250, 40);
        innerPanel.add(tfs[0]);

        place(tfs[1], 475, 130, 250, 40);
        innerPanel.add(tfs[1]);

        return tfs;
    }

    public JButton openClubParsingButton() {
        ModernButton matchParsingButton = new ModernButton("Match Parsing");

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

        return matchParsingButton;
    }

    public JButton createTimeoutButton() {
        ModernButton submitButton = new ModernButton("Submit");

        submitButton.addActionListener(e -> {
            stopped = false;
            paused = false;

            usernameListModel.clear();
            timeoutListModel.clear();
            ratingListModel.clear();
            timeout960ListModel.clear();

            if (rosterModel != null) rosterModel.setRowCount(0);

            String clubInput;
            if (clubCombo != null && clubCombo.isEditable() && clubCombo.getEditor() != null) {
                Object raw = clubCombo.getEditor().getItem();
                clubInput = raw == null ? "" : raw.toString().trim();
            } else if (clubCombo != null && clubCombo.getSelectedItem() != null) {
                clubInput = clubCombo.getSelectedItem().toString().trim();
            } else {
                clubInput = (textFields[0] != null ? textFields[0].getText().trim() : "");
            }
            teamNameTF = clubInput;

            String timeoutStr = (timeoutSpinner != null)
                    ? String.valueOf(((Number) timeoutSpinner.getValue()).doubleValue())
                    : (textFields[1] != null ? textFields[1].getText().trim() : "");
            timeoutTF = timeoutStr;

            addCurrentClubParsingTF(innerPanel, playersCurrentlyParsed);

            if (teamNameTF.isBlank()) {
                JOptionPane.showMessageDialog(this, "The club name field is empty.");
                return;
            }

            if (timeoutTF.isEmpty()) {
                JOptionPane.showMessageDialog(this, "The timeout rate field is empty.");
                return;
            }

            try {
                double maxTimeoutRate = Double.parseDouble(timeoutTF);
                if (maxTimeoutRate < 0 || maxTimeoutRate > 100) {
                    JOptionPane.showMessageDialog(this, "Invalid timeout rate. Please enter a value between 0 and 100.");
                    return;
                }

                Integer minR = (minRatingSpinner != null) ? (Integer) minRatingSpinner.getValue() : 0;
                Integer maxR = (maxRatingSpinner != null) ? (Integer) maxRatingSpinner.getValue() : 1299;

                if (minR > maxR) {
                    JOptionPane.showMessageDialog(this, "Min rating cannot be greater than Max rating.");
                    return;
                }

                ChessStats.fetchClubData(this, teamNameTF, maxTimeoutRate, minR, maxR);

                if (textFields[0] != null && textFields[0].isVisible()) textFields[0].setText("Enter team name here");
                if (textFields[1] != null && textFields[1].isVisible())
                    textFields[1].setText("Enter timeout rate here");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid timeout rate. Please enter a valid number.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Unable to fetch club data: " + ex.getMessage(),
                        "Fetch error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return submitButton;
    }

    public synchronized void togglePauseResume() {
        paused = !paused;
        if (!paused) notifyAll();
    }

    public synchronized void stopParsing() {
        stopped = true;
        notifyAll();
    }

    public synchronized void checkPausedAndStopped() throws InterruptedException {
        while (paused && !stopped) {
            wait();
        }

        if (stopped) throw new InterruptedException("Parsing stopped by user.");
    }

    private String formatDuration(long millis) {
        if (millis < 0) return "not available";

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(minutes)
                - TimeUnit.HOURS.toSeconds(hours);

        return (hours > 0 ? String.format("%dh ", hours) : "") + String.format("%dmin %dsec", minutes, seconds);
    }

    private static class CustomListCellRenderer extends DefaultListCellRenderer {
        private final Border pad = new EmptyBorder(8, 10, 8, 10);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(pad);
            return label;
        }
    }

    private static class RoundedBorder implements Border {
        private final Color color;
        private final int radius;
        private final float thickness;

        RoundedBorder(Color color, int radius, float thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius * 2, radius * 2);
            g2.dispose();
        }
    }

    private class LinkedCellRenderer extends DefaultListCellRenderer {
        private final Color stripeA = SURFACE_ALT;
        private final Color stripeB = new Color(21, 23, 29);
        private final Color hoverBg = new Color(255, 255, 255, 24);
        private final Color selBg = new Color(120, 86, 255, 180);
        private final Border pad = new EmptyBorder(6, 10, 6, 10);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lbl.setOpaque(true);
            lbl.setForeground(TEXT);
            lbl.setBorder(pad);

            Color base = (index % 2 == 0) ? stripeA : stripeB;
            lbl.setBackground(base);

            if (index == hoverIndex && !isSelected) lbl.setBackground(mix(base, hoverBg));
            if (isSelected) {
                lbl.setBackground(selBg);
                lbl.setForeground(Color.WHITE);
            }

            return lbl;
        }

        private Color mix(Color a, Color b) {
            int r = Math.min(255, a.getRed() + b.getRed());
            int g = Math.min(255, a.getGreen() + b.getGreen());
            int bl = Math.min(255, a.getBlue() + b.getBlue());
            int alpha = Math.min(255, a.getAlpha() + b.getAlpha());
            return new Color(r, g, bl, alpha);
        }
    }

    private class ModernButton extends JButton {
        private final int arc = 16;
        private boolean hover;
        private boolean press;

        ModernButton(String text) {
            super(text);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(TEXT);
            setFont(new Font("Arial", Font.BOLD, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 18, 8, 18));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    press = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    press = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color base = ACCENT;
            int a = press ? 220 : hover ? 235 : 200;
            Color fill = new Color(base.getRed(), base.getGreen(), base.getBlue(), a);

            Shape rr = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc * 2, arc * 2);

            g2.setColor(new Color(0, 0, 0, 120));
            g2.fill(new RoundRectangle2D.Double(2, 3, getWidth() - 4, getHeight() - 2, arc * 2, arc * 2));

            g2.setColor(fill);
            g2.fill(rr);

            if (isFocusOwner()) {
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(255, 255, 255, 120));
                g2.draw(rr);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class BackgroundPanel extends JPanel {
        private final Image image;

        BackgroundPanel(Image image) {
            this.image = image;
            setOpaque(true);
            setBackground(BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int iw = image == null ? -1 : image.getWidth(this);
            int ih = image == null ? -1 : image.getHeight(this);

            if (iw > 0 && ih > 0) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(BG);
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(
                    0, 0, new Color(0, 0, 0, 135),
                    0, getHeight(), new Color(0, 0, 0, 195)
            ));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
