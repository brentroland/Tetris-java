import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class TetrisGame extends JPanel implements ActionListener, KeyListener {
    private final int ROWS = 20;
    private final int COLS = 10;
    private final int TILE_SIZE = 30;
    private final Timer timer;
    private final int[][] grid = new int[ROWS][COLS];
    private Tetromino currentTetromino;
    private Tetromino nextTetromino;
    private int score = 0;
    private int level = 1;
    private boolean isPaused = false;
    private Color currentColor = Color.RED;

    public TetrisGame() {
        setPreferredSize(new Dimension(COLS * TILE_SIZE + 150, ROWS * TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(500, this);
        timer.start();
        nextTetromino = new Tetromino();
        spawnTetromino();
    }

    private void resetGame() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = 0;
            }
        }
        score = 0;
        level = 1;
        timer.setDelay(500);
        currentColor = Color.RED;
        nextTetromino = new Tetromino();
        spawnTetromino();
        timer.start();
        repaint();
    }

    private void spawnTetromino() {
        currentTetromino = nextTetromino;
        nextTetromino = new Tetromino();
        if (currentTetromino == null || !isPositionValid(currentTetromino.shape, currentTetromino.row, currentTetromino.col)) {
            timer.stop();
            int option = JOptionPane.showConfirmDialog(this, "Game Over!\nScore: " + score + "\nRestart?", "Game Over", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                resetGame();
            } else {
                System.exit(0);
            }
        }
    }

    private boolean isPositionValid(int[][] shape, int row, int col) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int newRow = row + r;
                    int newCol = col + c;
                    if (newRow < 0 || newRow >= ROWS || newCol < 0 || newCol >= COLS || grid[newRow][newCol] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void lockTetromino() {
        if (currentTetromino != null) {
            for (int r = 0; r < currentTetromino.shape.length; r++) {
                for (int c = 0; c < currentTetromino.shape[r].length; c++) {
                    if (currentTetromino.shape[r][c] != 0) {
                        grid[currentTetromino.row + r][currentTetromino.col + c] = currentTetromino.shape[r][c];
                    }
                }
            }
            clearLines();
            spawnTetromino();
        }
    }

    private void clearLines() {
        int linesCleared = 0;
        for (int r = ROWS - 1; r >= 0; r--) {
            boolean fullLine = true;
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == 0) {
                    fullLine = false;
                    break;
                }
            }
            if (fullLine) {
                for (int rr = r; rr > 0; rr--) {
                    System.arraycopy(grid[rr - 1], 0, grid[rr], 0, COLS);
                }
                grid[0] = new int[COLS];
                linesCleared++;
                r++; // Recheck this row after clearing
            }
        }

        if (linesCleared > 0) {
            score += linesCleared == 4 ? 500 : linesCleared * 100;
            level = score / 1000 + 1;
            timer.setDelay(Math.max(100, (int)(500 / Math.pow(1.15, level - 1))));

            // Change color randomly after a level is completed
            if (score % 1000 == 0) {
                currentColor = new Color(new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256));
            }
        }
    }

    private void moveTetromino(int dRow, int dCol) {
        if (currentTetromino != null && isPositionValid(currentTetromino.shape, currentTetromino.row + dRow, currentTetromino.col + dCol)) {
            currentTetromino.row += dRow;
            currentTetromino.col += dCol;
        } else if (dRow == 1 && currentTetromino != null) {
            lockTetromino();
        }
        repaint();
    }

    private void rotateTetromino(boolean clockwise) {
        if (currentTetromino != null) {
            int[][] rotatedShape = currentTetromino.getRotatedShape(clockwise);
            if (isPositionValid(rotatedShape, currentTetromino.row, currentTetromino.col)) {
                currentTetromino.shape = rotatedShape;
            }
        }
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isPaused) {
            moveTetromino(1, 0);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw border around play area
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, COLS * TILE_SIZE, ROWS * TILE_SIZE);

        // Draw grid
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] != 0) {
                    g.setColor(currentColor);
                    g.fillRect(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Draw current tetromino (if initialized)
        if (currentTetromino != null) {
            for (int r = 0; r < currentTetromino.shape.length; r++) {
                for (int c = 0; c < currentTetromino.shape[r].length; c++) {
                    if (currentTetromino.shape[r][c] != 0) {
                        int x = (currentTetromino.col + c) * TILE_SIZE;
                        int y = (currentTetromino.row + r) * TILE_SIZE;
                        g.setColor(currentColor);
                        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                        g.setColor(Color.BLACK);
                        g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }

        // Draw next tetromino
        g.setColor(Color.WHITE);
        g.drawString("Next Piece:", COLS * TILE_SIZE + 20, 20);
        if (nextTetromino != null) {
            for (int r = 0; r < nextTetromino.shape.length; r++) {
                for (int c = 0; c < nextTetromino.shape[r].length; c++) {
                    if (nextTetromino.shape[r][c] != 0) {
                        int x = COLS * TILE_SIZE + 20 + c * TILE_SIZE;
                        int y = 40 + r * TILE_SIZE;
                        g.setColor(currentColor);
                        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                        g.setColor(Color.BLACK);
                        g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }

        // Draw score and level
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, COLS * TILE_SIZE + 20, 150);
        g.drawString("Level: " + level, COLS * TILE_SIZE + 20, 170);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isPaused && e.getKeyCode() != KeyEvent.VK_P) {
            return;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> moveTetromino(0, -1);
            case KeyEvent.VK_RIGHT -> moveTetromino(0, 1);
            case KeyEvent.VK_DOWN -> moveTetromino(1, 0);
            case KeyEvent.VK_A -> rotateTetromino(false);
            case KeyEvent.VK_D -> rotateTetromino(true);
            case KeyEvent.VK_P -> isPaused = !isPaused;
            case KeyEvent.VK_R -> resetGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris");
        TetrisGame game = new TetrisGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Enable full-screen mode
        frame.setVisible(true);
    }

    class Tetromino {
        int[][] shape;
        int row, col;

        Tetromino() {
            int[][][] shapes = {
                {{1, 1, 1, 1}}, // I
                {{1, 1}, {1, 1}}, // O
                {{0, 1, 0}, {1, 1, 1}}, // T
                {{0, 1, 1}, {1, 1, 0}}, // S
                {{1, 1, 0}, {0, 1, 1}}, // Z
                {{1, 0, 0}, {1, 1, 1}}, // L
                {{0, 0, 1}, {1, 1, 1}}, // J
            };
            shape = shapes[new Random().nextInt(shapes.length)];
            row = 0;
            col = COLS / 2 - shape[0].length / 2;
        }

        int[][] getRotatedShape(boolean clockwise) {
            int rows = shape.length;
            int cols = shape[0].length;
            int[][] rotated = new int[cols][rows];
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (clockwise) {
                        rotated[c][rows - 1 - r] = shape[r][c];
                    } else {
                        rotated[cols - 1 - c][r] = shape[r][c];
                    }
                }
            }
            return rotated;
        }
    }
}
