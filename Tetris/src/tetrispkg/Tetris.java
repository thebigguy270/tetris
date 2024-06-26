package tetrispkg;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class Tetris extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 22;
    private static final int SQUARE_SIZE = 30;
    private static final int BOARD_BORDER = 5;

    private static final int NEXT_PIECE_PREVIEW_WIDTH = 5;

    private static final int PANEL_WIDTH = BOARD_WIDTH * SQUARE_SIZE + 2 * BOARD_BORDER + NEXT_PIECE_PREVIEW_WIDTH * SQUARE_SIZE;
    private static final int PANEL_HEIGHT = BOARD_HEIGHT * SQUARE_SIZE + 2 * BOARD_BORDER;
    private static final Color BOARD_BACKGROUND_COLOR = new Color(220, 220, 220); // Une couleur de fond douce
    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isPaused = false;
    private boolean isAnimating = false;
    private int curX = 0;
    private int curY = 0;
    private int curScore = 0;
    private JLabel statusBar;
    private Shape curPiece;
    private Tetrominoes[] board;
    private MainMenu mainmenu;
    private static final int NORMAL_DROP_DELAY = 400;
    private static final int ACCELERATED_DROP_DELAY = 200;
    private int dropDelay = NORMAL_DROP_DELAY;
    private JLabel levelLabel;
    private JLabel prochainTetro;
    private int level = 1;
    private int linesCleared = 0;
    private Shape nextPiece;

    private JLabel countdownLabel;
    private Timer countdownTimer;
    private int countdownSeconds = 3; // Countdown duration in seconds
    
    
    public Tetris(MainMenu mainMenu) {
        this.mainmenu = mainmenu;
        initBoard();
        statusBar = new JLabel("Score: 0");
        levelLabel = new JLabel("Level: 1");
        prochainTetro = new JLabel("Prochain:");
        countdownLabel = new JLabel("Starting in: " + countdownSeconds, JLabel.CENTER);
        
        setLayout(new BorderLayout());
        
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(1, 3)); // Adjusted GridLayout to organize labels
        statusPanel.add(statusBar);
        statusPanel.add(levelLabel);
        statusPanel.add(prochainTetro);
        
        add(statusPanel, BorderLayout.NORTH);
        add(countdownLabel, BorderLayout.CENTER); // Add countdown label to the center
        
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        
        startCountdown();
    }

    private void startCountdown() {
        countdownLabel.setText("Starting in: " + countdownSeconds);
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdownSeconds--;
                if (countdownSeconds > 0) {
                    countdownLabel.setText("Starting in: " + countdownSeconds);
                } else {
                    countdownTimer.stop();
                    countdownLabel.setText("");
                    start();
                }
            }
        });
        countdownTimer.start();
    }

    
    
    private void initBoard() {
        setFocusable(true);
        curPiece = new Shape();
        nextPiece = new Shape();
        board = new Tetrominoes[BOARD_WIDTH * BOARD_HEIGHT];
        clearBoard();
        addKeyListener(new TAdapter());
        level = 1;
        linesCleared = 0;
    }

    
    
    private int squareWidth() {
        return SQUARE_SIZE;
    }

    private int squareHeight() {
        return SQUARE_SIZE;
    }

    private Tetrominoes shapeAt(int x, int y) {
        return board[(y * BOARD_WIDTH) + x];
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
            board[i] = Tetrominoes.NoShape;
        }
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            if (x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT) {
                board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
            }
        }
        removeFullLines();
        
        // Check if the piece is not falling anymore naturally
        if (!isFallingFinished) {
            newPiece(); // Start a new piece or handle game over
        } else {
            curScore += 10;
            statusBar.setText("Score: " + curScore);
            System.out.println("score: " + curScore); 
        }
    }
    private void newPiece() {
        curPiece = nextPiece;
        curX = BOARD_WIDTH / 2 + curPiece.minX();
        curY = BOARD_HEIGHT - 1 + curPiece.minY();
        if (!tryMove(curPiece, curX, curY)) {
            curPiece.setShape(Tetrominoes.NoShape);
            timer.stop();
            statusBar.setText("Game Over");
            returnToMainMenu();
        }

        nextPiece = new Shape();
        nextPiece.setRandomShape();
        while (nextPiece.getShape() == Tetrominoes.NoShape) {
            nextPiece.setRandomShape(); // Ensure it's not NoShape
        }
        repaint();
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
                return false;
            }
            if (shapeAt(x, y) != Tetrominoes.NoShape) {
                return false;
            }
        }
        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();
        return true;
    }

    private void increaseLevel() {
        level++;
        levelLabel.setText("Level: " + level); // Mise à jour du label de niveau
        dropDelay = calculateDropDelay(level);
        timer.setDelay(dropDelay);
    }


    private int calculateDropDelay(int level) {
        return Math.max(NORMAL_DROP_DELAY - (level * 20), 100);
    }

    private void removeFullLines() {
        List<Integer> fullLines = new ArrayList<>();
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineIsFull = true;
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (shapeAt(j, i) == Tetrominoes.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                fullLines.add(i);
            }
        }

        if (!fullLines.isEmpty()) {
            isAnimating = true;
            timer.stop();
            animateLineClear(fullLines);
        } else {
            if (isFallingFinished) {
                isFallingFinished = false;
                newPiece();
            } else {
                oneLineDown();
            }
        }
    }


    private int calculateScore(int lines) {
        switch (lines) {
            case 1:
                return 100;
            case 2:
                return 300;
            case 3:
                return 500;
            case 4:
                return 800;
            default:
                return 0;
        }
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoes shape) {
        Color colors[] = {
            new Color(0, 0, 0), new Color(204, 102, 102),
            new Color(102, 204, 102), new Color(102, 102, 204),
            new Color(204, 204, 102), new Color(204, 102, 204),
            new Color(102, 204, 204), new Color(218, 170, 0)
        };
        Color color = colors[shape.ordinal()];
        g.setColor(color);
        g.fillRect(x, y, squareWidth(), squareHeight());
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);
        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                   x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                   x + squareWidth() - 1, y + 1);
    }

    private void doDrawing(Graphics g) {
        var size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();
        
        g.setColor(BOARD_BACKGROUND_COLOR);
        g.fillRect(BOARD_BORDER, boardTop, BOARD_WIDTH * squareWidth(), BOARD_HEIGHT * squareHeight());
        
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                Tetrominoes shape = shapeAt(j, BOARD_HEIGHT - i - 1);
                if (shape != Tetrominoes.NoShape) {
                    drawSquare(g, 0 + j * squareWidth(), boardTop + i * squareHeight(), shape);
                }
            }
        }
        
        if (curPiece.getShape() != Tetrominoes.NoShape) {
            for (int i = 0; i < 4; i++) {
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                drawSquare(g, 0 + x * squareWidth(), boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(), curPiece.getShape());
            }
        }
    }

    private void drawNextPiece(Graphics g) {
        int startX = BOARD_WIDTH * squareWidth() + BOARD_BORDER * 2;
        int startY = BOARD_BORDER * 4;
        g.setColor(Color.BLACK);
        
        g.drawString("Next Piece:", startX, startY - 10);

        for (int i = 0; i < 4; i++) {
            int x = nextPiece.x(i);
            int y = nextPiece.y(i);
            drawSquare(g, startX + x * squareWidth()+32, startY + y * squareHeight() + 32, nextPiece.getShape());
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
        drawNextPiece(g);
    }

    private void start() {
    	curPiece = new Shape();
        curPiece.setRandomShape();
        nextPiece = new Shape();
        nextPiece.setRandomShape();
        timer = new Timer(dropDelay, new GameCycle());
        timer.start();
        newPiece();
    }

    private void pause() {
        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            statusBar.setText("Paused");
        } else {
            timer.start();
            statusBar.setText("Score: " + curScore);
        }
        repaint();
    }

    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            doGameCycle();
        }
    }

    private void doGameCycle() {
        update();
        repaint();
    }

    private void update() {
        if (isPaused) {
            return;
        }
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    private void oneLineDown() {
        if (!tryMove(curPiece, curX, curY - 1)) {
            isFallingFinished = true; // Set flag to true when piece cannot move down anymore
            pieceDropped(); // Call pieceDropped() after the piece has finished falling
        }
    }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (curPiece.getShape() == Tetrominoes.NoShape) {
                return;
            }
            int keycode = e.getKeyCode();
            if (keycode == 'p' || keycode == 'P') {
                pause();
                return;
            }
            if (isPaused) {
                return;
            }
            switch (keycode) {
                case KeyEvent.VK_LEFT:
                    tryMove(curPiece, curX - 1, curY);
                    break;
                case KeyEvent.VK_RIGHT:
                    tryMove(curPiece, curX + 1, curY);
                    break;
                case KeyEvent.VK_DOWN:
                    dropDelay = ACCELERATED_DROP_DELAY;
                    timer.setDelay(dropDelay);
                    break;
                case KeyEvent.VK_Z:
                    tryMove(curPiece.rotateLeft(), curX, curY);
                    break;
                case KeyEvent.VK_X:
                    tryMove(curPiece.rotateRight(), curX, curY);
                    break;
                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;
                case KeyEvent.VK_D:
                    oneLineDown();
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int keycode = e.getKeyCode();
            if (keycode == KeyEvent.VK_DOWN) {
                dropDelay = calculateDropDelay(level);
                timer.setDelay(dropDelay);
            }
        }
    }

    private void dropDown() {
        int newY = curY;
        while (newY > 0) {
            if (!tryMove(curPiece, curX, newY - 1)) {
                break;
            }
            newY--;
        }
        pieceDropped();
    }
    private void returnToMainMenu() {
        Timer timer = new Timer(4000, new ActionListener() {  // 4000 ms = 4 seconds
            @Override
            public void actionPerformed(ActionEvent e) {
                mainmenu.setVisible(true);
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(Tetris.this);
                if (topFrame != null) {
                    topFrame.dispose();  // Close the game window
                }
            }
        });
        timer.setRepeats(false);  // Ensure the timer only runs once
        timer.start();
    }
    private void animateLineClear(List<Integer> fullLines) {
        Timer animationTimer = new Timer(50, new ActionListener() {
            int animationStep = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int line : fullLines) {
                    for (int j = BOARD_WIDTH / 2 - animationStep; j <= BOARD_WIDTH / 2 + animationStep; j++) {
                        if (j >= 0 && j < BOARD_WIDTH) {
                            board[line * BOARD_WIDTH + j] = Tetrominoes.NoShape;
                        }
                    }
                }
                repaint();

                animationStep++;
                if (animationStep > BOARD_WIDTH / 2) {
                    ((Timer) e.getSource()).stop();
                    finalizeLineClear(fullLines);
                }
            }
        });
        animationTimer.start();
    }

    private void finalizeLineClear(List<Integer> fullLines) {
        for (int line : fullLines) {
            for (int k = line; k < BOARD_HEIGHT - 1; k++) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
                }
            }
            for (int j = 0; j < BOARD_WIDTH; j++) {
                board[(BOARD_HEIGHT - 1) * BOARD_WIDTH + j] = Tetrominoes.NoShape;
            }
        }

        curScore += calculateScore(fullLines.size());
        System.out.println("lignes:" + fullLines.size());
        System.out.println("niveau:" + level);
        statusBar.setText("Score: " + curScore);
        linesCleared += fullLines.size();
        if (linesCleared >= level * 10) {
            increaseLevel();
        }
        isAnimating = false;
        timer.start();
        repaint();
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

}
