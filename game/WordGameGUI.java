import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class WordGameGUI extends JFrame {
    private JLabel questionLabel, hintLabel, timerLabel, scoreLabel;
    private JTextField answerField;
    private JButton submitButton, viewScoreButton;
    private JTextArea feedbackArea;
    private JProgressBar timerBar;
    private List<QuestionData> questions = new ArrayList<>();
    private Map<String, List<Integer>> playerScores = new HashMap<>();
    private int currentQuestion, correctCount, totalCount, timeRemaining;
    private String playerName;
    private Timer gameTimer;
    
    public WordGameGUI() {
        setTitle("Word Journey");
        setSize(600, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        loadQuestions();
        loadScores();
        initComponents();
        showNameDialog();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 248, 255));
        
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(70, 130, 180));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("=== Word Journey ===", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        scoreLabel = new JLabel("Score: 0/0", SwingConstants.RIGHT);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(scoreLabel, BorderLayout.EAST);
        
        // Center Panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        questionLabel = createLabel("Question will appear here", Font.BOLD, 18);
        hintLabel = createLabel("Hint will appear here", Font.ITALIC, 14);
        hintLabel.setForeground(new Color(100, 100, 100));
        timerLabel = createLabel("Time: 20s", Font.BOLD, 16);
        timerLabel.setForeground(new Color(220, 20, 60));
        
        timerBar = new JProgressBar(0, 20);
        timerBar.setValue(20);
        timerBar.setStringPainted(true);
        timerBar.setForeground(new Color(60, 179, 113));
        timerBar.setMaximumSize(new Dimension(400, 25));
        timerBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        answerField = new JTextField(20);
        answerField.setFont(new Font("Arial", Font.PLAIN, 16));
        answerField.setMaximumSize(new Dimension(400, 35));
        answerField.setAlignmentX(Component.CENTER_ALIGNMENT);
        answerField.addActionListener(e -> checkAnswer());
        
        submitButton = createButton("Submit Answer", new Color(70, 130, 180));
        submitButton.addActionListener(e -> checkAnswer());
        
        feedbackArea = new JTextArea(3, 30);
        feedbackArea.setFont(new Font("Arial", Font.PLAIN, 14));
        feedbackArea.setEditable(false);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setBackground(new Color(255, 250, 205));
        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        scrollPane.setMaximumSize(new Dimension(400, 80));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(questionLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(hintLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(timerLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(timerBar);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(answerField);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(submitButton);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(scrollPane);
        
        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(Color.WHITE);
        viewScoreButton = createButton("View My Scores", new Color(100, 149, 237));
        viewScoreButton.setVisible(false);
        viewScoreButton.addActionListener(e -> showPlayerScores());
        JButton historyButton = createButton("All Players History", new Color(255, 140, 0));
        historyButton.addActionListener(e -> showAllPlayersHistory());
        bottomPanel.add(viewScoreButton);
        bottomPanel.add(historyButton);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JLabel createLabel(String text, int style, int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", style, size));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
    
    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.black);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
    
    private void showNameDialog() {
        playerName = JOptionPane.showInputDialog(this, "Enter your name:", "Player Name", JOptionPane.QUESTION_MESSAGE);
        if (playerName == null || playerName.trim().isEmpty()) playerName = "Player";
        
        if (playerScores.containsKey(playerName)) {
            viewScoreButton.setVisible(true);
            if (JOptionPane.showConfirmDialog(this, "Welcome back, " + playerName + "!\nView previous scores?", 
                "Returning Player", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                showPlayerScores();
            }
        }
        
        if (!questions.isEmpty()) startGame();
        else JOptionPane.showMessageDialog(this, "Could not load questions from word_answer.txt", "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void loadQuestions() {
        try (BufferedReader r = new BufferedReader(new FileReader("word_answer.txt"))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length == 3) questions.add(new QuestionData(p[0].trim(), p[1].trim(), p[2].trim()));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    private void loadScores() {
        File f = new File("result.txt");
        if (!f.exists()) return;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.trim().split("\\s+");
                if (p.length >= 2) {
                    try {
                        playerScores.putIfAbsent(p[0], new ArrayList<>());
                        playerScores.get(p[0]).add(Integer.parseInt(p[p.length - 1]));
                    } catch (NumberFormatException e) {}
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    private void showPlayerScores() {
        if (!playerScores.containsKey(playerName)) {
            JOptionPane.showMessageDialog(this, "No previous scores found for " + playerName);
            return;
        }
        
        List<Integer> scores = playerScores.get(playerName);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel t = new JLabel("Score History for " + playerName);
        t.setFont(new Font("Arial", Font.BOLD, 18));
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(15));
        
        JPanel stats = new JPanel(new GridLayout(3, 2, 10, 5));
        stats.setMaximumSize(new Dimension(400, 100));
        stats.setBorder(BorderFactory.createTitledBorder("Statistics"));
        stats.add(new JLabel("Games Played:"));
        stats.add(new JLabel(String.valueOf(scores.size())));
        stats.add(new JLabel("Best Score:"));
        stats.add(new JLabel(Collections.max(scores) + "/" + totalCount));
        stats.add(new JLabel("Average Score:"));
        stats.add(new JLabel(String.format("%.1f/%d", scores.stream().mapToInt(Integer::intValue).average().orElse(0), totalCount)));
        p.add(stats);
        
        JTextArea hist = new JTextArea(8, 30);
        hist.setEditable(false);
        hist.setFont(new Font("Monospaced", Font.PLAIN, 12));
        StringBuilder sb = new StringBuilder();
        for (int i = Math.max(0, scores.size() - 10); i < scores.size(); i++)
            sb.append(String.format("Game %d: %d/%d%n", i + 1, scores.get(i), totalCount));
        hist.setText(sb.toString());
        p.add(new JScrollPane(hist));
        
        JOptionPane.showMessageDialog(this, p, "Player Scores", JOptionPane.PLAIN_MESSAGE);
    }
    
    private void showAllPlayersHistory() {
        if (playerScores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No game history available yet.");
            return;
        }
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Leaderboard", createLeaderboard());
        tabs.addTab("All Games", createAllGames());
        tabs.addTab("Stats", createStats());
        
        JDialog d = new JDialog(this, "Game History", true);
        d.setContentPane(tabs);
        d.setSize(600, 500);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
    
    private JPanel createLeaderboard() {
        JPanel p = new JPanel(new BorderLayout());
        Map<String, Integer> best = new HashMap<>();
        playerScores.forEach((k, v) -> best.put(k, Collections.max(v)));
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(best.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        Object[][] data = new Object[sorted.size()][5];
        for (int i = 0; i < sorted.size(); i++) {
            String pl = sorted.get(i).getKey();
            List<Integer> sc = playerScores.get(pl);
            data[i][0] = String.valueOf(i + 1);
            data[i][1] = pl;
            data[i][2] = sorted.get(i).getValue() + "/" + totalCount;
            data[i][3] = sc.size();
            data[i][4] = String.format("%.1f", sc.stream().mapToInt(Integer::intValue).average().orElse(0));
        }
        
        JTable tbl = new JTable(data, new String[]{"Rank", "Player", "Best", "Games", "Avg"});
        tbl.setRowHeight(30);
        tbl.setEnabled(false);
        p.add(new JScrollPane(tbl));
        return p;
    }
    
    private JPanel createAllGames() {
        JPanel p = new JPanel(new BorderLayout());
        List<int[]> games = new ArrayList<>();
        playerScores.forEach((pl, sc) -> {
            for (int i = 0; i < sc.size(); i++) games.add(new int[]{pl.hashCode(), i + 1, sc.get(i)});
        });
        Collections.reverse(games);
        
        Object[][] data = new Object[Math.min(50, games.size())][4];
        for (int i = 0; i < data.length; i++) {
            final int index = i;
            String pl = playerScores.keySet().stream().filter(k -> k.hashCode() == games.get(index)[0]).findFirst().orElse("");
            data[i][0] = pl;
            data[i][1] = games.get(i)[1];
            data[i][2] = games.get(i)[2] + "/" + totalCount;
            data[i][3] = String.format("%.1f%%", (double) games.get(i)[2] / totalCount * 100);
        }
        
        JTable tbl = new JTable(data, new String[]{"Player", "Game #", "Score", "%"});
        tbl.setEnabled(false);
        p.add(new JScrollPane(tbl));
        return p;
    }
    
    private JPanel createStats() {
        JPanel p = new JPanel(new BorderLayout());
        StringBuilder sb = new StringBuilder("STATISTICS\n═════════════════════\n\n");
        sb.append("Players: ").append(playerScores.size()).append("\n");
        sb.append("Total Games: ").append(playerScores.values().stream().mapToInt(List::size).sum()).append("\n\n");
        
        playerScores.forEach((pl, sc) -> {
            sb.append(String.format("%s: Best=%d Avg=%.1f Games=%d\n", 
                pl, Collections.max(sc), sc.stream().mapToInt(Integer::intValue).average().orElse(0), sc.size()));
        });
        
        JTextArea txt = new JTextArea(sb.toString());
        txt.setEditable(false);
        txt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        p.add(new JScrollPane(txt));
        return p;
    }
    
    private void startGame() {
        currentQuestion = correctCount = 0;
        totalCount = questions.size();
        displayQuestion();
    }
    
    private void displayQuestion() {
        if (currentQuestion >= questions.size()) { endGame(); return; }
        QuestionData q = questions.get(currentQuestion);
        questionLabel.setText("Q" + (currentQuestion + 1) + ": " + q.question);
        hintLabel.setText("Hint: " + q.hint);
        answerField.setText("");
        answerField.setEnabled(true);
        submitButton.setEnabled(true);
        feedbackArea.setText("");
        scoreLabel.setText("Score: " + correctCount + "/" + totalCount);
        startTimer();
    }
    
    private void startTimer() {
        timeRemaining = 20;
        timerBar.setValue(20);
        timerLabel.setText("Time: 20s");
        timerBar.setForeground(new Color(60, 179, 113));
        if (gameTimer != null) gameTimer.stop();
        
        gameTimer = new Timer(1000, e -> {
            timeRemaining--;
            timerLabel.setText("Time: " + timeRemaining + "s");
            timerBar.setValue(timeRemaining);
            if (timeRemaining <= 5) timerBar.setForeground(new Color(220, 20, 60));
            else if (timeRemaining <= 10) timerBar.setForeground(new Color(255, 165, 0));
            if (timeRemaining <= 0) { gameTimer.stop(); timeUp(); }
        });
        gameTimer.start();
    }
    
    private void timeUp() {
        answerField.setEnabled(false);
        submitButton.setEnabled(false);
        feedbackArea.setText("Time's up!\nCorrect: " + questions.get(currentQuestion).answer);
        Timer nextTimer = new Timer(2000, e -> { 
            currentQuestion++; 
            displayQuestion(); 
        });
        nextTimer.setRepeats(false);
        nextTimer.start();
    }
    
    private void checkAnswer() {
        if (gameTimer != null) gameTimer.stop();
        String ans = answerField.getText().trim();
        QuestionData q = questions.get(currentQuestion);
        answerField.setEnabled(false);
        submitButton.setEnabled(false);
        
        if (ans.equalsIgnoreCase(q.answer)) {
            correctCount++;
            feedbackArea.setText("Correct! (" + (20 - timeRemaining) + "s)");
            feedbackArea.setBackground(new Color(144, 238, 144));
        } else {
            feedbackArea.setText("Wrong!\nYour: " + ans + "\nCorrect: " + q.answer);
            feedbackArea.setBackground(new Color(255, 182, 193));
        }
        
        scoreLabel.setText("Score: " + correctCount + "/" + totalCount);
        Timer nextTimer = new Timer(2500, e -> { 
            feedbackArea.setBackground(new Color(255, 250, 205)); 
            currentQuestion++; 
            displayQuestion(); 
        });
        nextTimer.setRepeats(false);
        nextTimer.start();
    }
    
    private void endGame() {
        if (gameTimer != null) gameTimer.stop();
        saveResults();
        
        JPanel p = new JPanel(new GridLayout(4, 1, 10, 10));
        p.add(new JLabel("=== Game Over ===", SwingConstants.CENTER));
        p.add(new JLabel("Player: " + playerName, SwingConstants.CENTER));
        p.add(new JLabel("Score: " + correctCount + "/" + totalCount, SwingConstants.CENTER));
        p.add(new JLabel(String.format("Accuracy: %.1f%%", (double) correctCount / totalCount * 100), SwingConstants.CENTER));
        
        if (JOptionPane.showConfirmDialog(this, p, "Results", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            loadScores();
            showNameDialog();
        } else System.exit(0);
    }
    
    private void saveResults() {
        try (PrintWriter w = new PrintWriter(new FileWriter("result.txt", true))) {
            w.println(playerName + " " + correctCount);
        } catch (IOException e) { e.printStackTrace(); }
        playerScores.putIfAbsent(playerName, new ArrayList<>());
        playerScores.get(playerName).add(correctCount);
    }
    
    class QuestionData {
        String question, hint, answer;
        QuestionData(String q, String h, String a) { question = q; hint = h; answer = a; }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
            new WordGameGUI().setVisible(true);
        });
    }
}