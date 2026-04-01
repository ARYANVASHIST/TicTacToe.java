/**
 * ============================================================
 *  TIC-TAC-TOE — Complete Java Swing Application
 *  Demonstrates: Arrays, Loops, Conditionals, GUI, Events
 * ============================================================
 *
 *  HOW TO COMPILE & RUN:
 *    javac TicTacToe.java
 *    java  TicTacToe
 *
 *  Java version: 8+
 * ============================================================
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

// ─────────────────────────────────────────────────────────────
//  ENTRY POINT
// ─────────────────────────────────────────────────────────────
public class TicTacToe {
    public static void main(String[] args) {
        // Launch GUI on the Event Dispatch Thread (EDT) — Swing best-practice
        SwingUtilities.invokeLater(() -> new GameFrame());
    }
}

// ─────────────────────────────────────────────────────────────
//  GameFrame  — top-level window
//  Responsibility: build the window, wire everything together
// ─────────────────────────────────────────────────────────────
class GameFrame extends JFrame {

    // ── Colour palette ────────────────────────────────────────
    static final Color BG          = new Color(15,  17,  26);   // near-black
    static final Color PANEL_BG    = new Color(22,  26,  40);
    static final Color ACCENT_X    = new Color(255, 100,  80);  // coral-red  → X
    static final Color ACCENT_O    = new Color( 80, 200, 255);  // sky-blue   → O
    static final Color CELL_IDLE   = new Color(32,  37,  56);
    static final Color CELL_HOVER  = new Color(42,  49,  74);
    static final Color WIN_CELL    = new Color(60,  220, 130);  // green highlight
    static final Color TEXT_MAIN   = new Color(230, 235, 255);
    static final Color TEXT_SUB    = new Color(130, 140, 170);

    // ── Game logic object ─────────────────────────────────────
    private GameLogic logic;

    // ── GUI components ────────────────────────────────────────
    private CellButton[][] cells;   // 3×3 grid of custom buttons
    private JLabel  statusLabel;    // shows whose turn / result
    private JLabel  scoreLabel;     // X wins  |  Draws  |  O wins
    private JButton resetBtn;

    // ── Scores ────────────────────────────────────────────────
    private int scoreX = 0, scoreO = 0, scoreDraw = 0;

    // ─────────────────────────────────────────────────────────
    GameFrame() {
        logic = new GameLogic();
        cells = new CellButton[3][3];

        buildWindow();
        buildUI();
        setVisible(true);
    }

    // Configure the JFrame itself
    private void buildWindow() {
        setTitle("Tic-Tac-Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG);
    }

    // Build all panels and lay them out
    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),     BorderLayout.NORTH);
        add(buildGrid(),       BorderLayout.CENTER);
        add(buildFooter(),     BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null); // center on screen
    }

    // ── Header: title + score ─────────────────────────────────
    private JPanel buildHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(28, 30, 12, 30));

        // Title
        JLabel title = new JLabel("TIC-TAC-TOE", SwingConstants.CENTER);
        title.setFont(new Font("Courier New", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Score row
        scoreLabel = new JLabel(buildScoreText(), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        scoreLabel.setForeground(TEXT_SUB);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreLabel.setBorder(new EmptyBorder(8, 0, 0, 0));

        panel.add(title);
        panel.add(scoreLabel);
        return panel;
    }

    // ── 3×3 grid ──────────────────────────────────────────────
    private JPanel buildGrid() {
        JPanel grid = new JPanel(new GridLayout(3, 3, 8, 8));
        grid.setBackground(BG);
        grid.setBorder(new EmptyBorder(10, 28, 10, 28));

        // Loop over rows and columns to create each cell button
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                CellButton btn = new CellButton(row, col, this);
                cells[row][col] = btn;
                grid.add(btn);
            }
        }
        return grid;
    }

    // ── Footer: status label + reset button ───────────────────
    private JPanel buildFooter() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(10, 30, 28, 30));

        // Status message
        statusLabel = new JLabel("Player X's turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Courier New", Font.BOLD, 16));
        statusLabel.setForeground(ACCENT_X);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Reset button
        resetBtn = new JButton("NEW GAME");
        resetBtn.setFont(new Font("Courier New", Font.BOLD, 13));
        resetBtn.setForeground(TEXT_MAIN);
        resetBtn.setBackground(new Color(40, 46, 68));
        resetBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(60, 70, 100), 1, true),
            new EmptyBorder(8, 24, 8, 24)
        ));
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hover effect on reset button (uses anonymous ActionListener)
        resetBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                resetBtn.setBackground(new Color(55, 64, 95));
            }
            public void mouseExited(MouseEvent e) {
                resetBtn.setBackground(new Color(40, 46, 68));
            }
        });

        // Reset button click → restart game
        resetBtn.addActionListener(e -> resetGame());

        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(14));
        panel.add(resetBtn);
        return panel;
    }

    // ─────────────────────────────────────────────────────────
    //  GAME ACTIONS  (called by CellButton)
    // ─────────────────────────────────────────────────────────

    /**
     * Called when a player clicks a cell.
     * Uses conditional statements to:
     *   1. Validate the move
     *   2. Check for win
     *   3. Check for draw
     *   4. Switch turns
     */
    void handleCellClick(int row, int col) {
        // ── Validate: ignore if game over or cell occupied ──
        if (logic.isGameOver() || !logic.isCellEmpty(row, col)) {
            if (!logic.isGameOver()) {
                // Cell already taken — flash a warning
                statusLabel.setText("Cell taken! Try another.");
                statusLabel.setForeground(new Color(255, 200, 60));
            }
            return;
        }

        // ── Place the current player's mark ──────────────────
        char current = logic.getCurrentPlayer();
        logic.makeMove(row, col);
        cells[row][col].setMark(current);

        // ── Check win condition ───────────────────────────────
        if (logic.checkWin()) {
            int[] winLine = logic.getWinLine(); // indices of winning cells

            // Highlight winning cells using a for-loop
            for (int i = 0; i < winLine.length; i += 2) {
                cells[winLine[i]][winLine[i + 1]].setWin();
            }

            // Update score
            if (current == 'X') {
                scoreX++;
                statusLabel.setText("Player X wins! 🎉");
                statusLabel.setForeground(ACCENT_X);
            } else {
                scoreO++;
                statusLabel.setText("Player O wins! 🎉");
                statusLabel.setForeground(ACCENT_O);
            }
            updateScore();
            logic.setGameOver(true);
            return;
        }

        // ── Check draw condition ──────────────────────────────
        if (logic.checkDraw()) {
            scoreDraw++;
            statusLabel.setText("It's a Draw!");
            statusLabel.setForeground(new Color(180, 180, 220));
            updateScore();
            logic.setGameOver(true);
            return;
        }

        // ── Switch turn ───────────────────────────────────────
        logic.switchPlayer();
        char next = logic.getCurrentPlayer();
        statusLabel.setText("Player " + next + "'s turn");
        statusLabel.setForeground(next == 'X' ? ACCENT_X : ACCENT_O);
    }

    /** Resets board state and UI for a new game */
    private void resetGame() {
        logic.reset();

        // Loop through all cells to clear them
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                cells[r][c].reset();
            }
        }

        statusLabel.setText("Player X's turn");
        statusLabel.setForeground(ACCENT_X);
    }

    private void updateScore() {
        scoreLabel.setText(buildScoreText());
    }

    private String buildScoreText() {
        return String.format("X  %d   ·   Draw  %d   ·   O  %d",
                             scoreX, scoreDraw, scoreO);
    }
}

// ─────────────────────────────────────────────────────────────
//  GameLogic  — pure game rules (no GUI)
//  Responsibility: board array, win/draw detection, turn mgmt
// ─────────────────────────────────────────────────────────────
class GameLogic {

    // 3×3 board: '\0' means empty cell
    private char[][] board;
    private char     currentPlayer;
    private boolean  gameOver;

    // Stores the row/col indices of the three winning cells
    // Format: [r0,c0, r1,c1, r2,c2]
    private int[]    winLine;

    // All eight possible winning combinations (rows, cols, diagonals)
    private static final int[][] WIN_PATTERNS = {
        // Rows
        {0,0, 0,1, 0,2},
        {1,0, 1,1, 1,2},
        {2,0, 2,1, 2,2},
        // Columns
        {0,0, 1,0, 2,0},
        {0,1, 1,1, 2,1},
        {0,2, 1,2, 2,2},
        // Diagonals
        {0,0, 1,1, 2,2},
        {0,2, 1,1, 2,0}
    };

    GameLogic() {
        board = new char[3][3];
        reset();
    }

    /** Reset the board array and game state */
    void reset() {
        // Loop through every cell and clear it
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[r][c] = '\0';
            }
        }
        currentPlayer = 'X';
        gameOver      = false;
        winLine       = null;
    }

    boolean isCellEmpty(int r, int c) {
        return board[r][c] == '\0';
    }

    void makeMove(int r, int c) {
        board[r][c] = currentPlayer;
    }

    void switchPlayer() {
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }

    char getCurrentPlayer() { return currentPlayer; }
    boolean isGameOver()    { return gameOver; }
    void setGameOver(boolean v) { gameOver = v; }
    int[] getWinLine()      { return winLine; }

    /**
     * Check all win patterns using a for-loop.
     * Returns true and stores the winning cells if found.
     */
    boolean checkWin() {
        // Iterate over every pattern
        for (int[] p : WIN_PATTERNS) {
            char a = board[p[0]][p[1]];
            char b = board[p[2]][p[3]];
            char c = board[p[4]][p[5]];

            // All three cells match and are not empty → win!
            if (a != '\0' && a == b && b == c) {
                winLine = p;
                return true;
            }
        }
        return false;
    }

    /**
     * Draw: all 9 cells filled, no winner.
     * Uses a nested for-loop to scan every cell.
     */
    boolean checkDraw() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == '\0') return false; // empty cell found
            }
        }
        return true; // no empty cells → draw
    }
}

// ─────────────────────────────────────────────────────────────
//  CellButton  — one clickable cell on the board
//  Responsibility: paint X/O/win state, report clicks upward
// ─────────────────────────────────────────────────────────────
class CellButton extends JButton {

    private final int   row, col;
    private final GameFrame frame;

    private char    mark    = '\0'; // 'X', 'O', or empty
    private boolean isWin   = false;
    private boolean hovered = false;

    CellButton(int row, int col, GameFrame frame) {
        this.row   = row;
        this.col   = col;
        this.frame = frame;

        setPreferredSize(new Dimension(108, 108));
        setBackground(GameFrame.CELL_IDLE);
        setBorder(new LineBorder(new Color(50, 58, 85), 2, true));
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false); // we paint manually in paintComponent

        // ── ActionListener: forwards click to GameFrame ──────
        addActionListener(e -> frame.handleCellClick(row, col));

        // ── Hover effects ─────────────────────────────────────
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    /** Set the mark ('X' or 'O') and trigger a repaint */
    void setMark(char m) {
        this.mark = m;
        repaint();
    }

    /** Mark this cell as part of the winning line */
    void setWin() {
        this.isWin = true;
        repaint();
    }

    /** Clear mark and win state for new game */
    void reset() {
        mark   = '\0';
        isWin  = false;
        hovered = false;
        repaint();
    }

    // ── Custom painting ───────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int pad = 20; // inner padding for X/O symbol

        // ── Cell background ───────────────────────────────────
        Color bg;
        if      (isWin)   bg = new Color(30, 80, 55);      // dark green for win
        else if (hovered && mark == '\0') bg = GameFrame.CELL_HOVER;
        else              bg = GameFrame.CELL_IDLE;

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, 14, 14);

        // ── Draw mark ─────────────────────────────────────────
        if (mark == 'X') {
            drawX(g2, pad, w, h);
        } else if (mark == 'O') {
            drawO(g2, pad, w, h);
        }

        g2.dispose();
    }

    /** Draw a stylised X using two diagonal lines */
    private void drawX(Graphics2D g2, int pad, int w, int h) {
        Color col = isWin ? GameFrame.WIN_CELL : GameFrame.ACCENT_X;
        g2.setColor(col);
        g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(pad, pad, w - pad, h - pad);
        g2.drawLine(w - pad, pad, pad, h - pad);
    }

    /** Draw a stylised O using an ellipse outline */
    private void drawO(Graphics2D g2, int pad, int w, int h) {
        Color col = isWin ? GameFrame.WIN_CELL : GameFrame.ACCENT_O;
        g2.setColor(col);
        g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval(pad, pad, w - 2 * pad, h - 2 * pad);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        Color borderColor = isWin
            ? GameFrame.WIN_CELL.darker()
            : new Color(55, 65, 95);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
        g2.dispose();
    }
}
