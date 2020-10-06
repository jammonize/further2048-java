package gui;

import game.Game;
import game.Tile;
import ai.Direction;
import ai.Expectimax;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("serial")
public class GUI extends JPanel implements Runnable {

    private final int DELAY = 1000; //milliseconds

    private final Color BG_COLOR = new Color(0xbbada0);
    private final String FONT = "Arial";
    private final int TILE_SIZE = 101;
    private final int TILE_MARGIN = 16;

    public int lessThan2048 = 0;
    public int exactly2048 = 0;
    public int exactly4096 = 0;
    public int moreThan8192 = 0;
    public int cycles = 0;
    public int largest = 0;

    private LinkedList<Tile[]> viewQueue = new LinkedList<Tile[]>();

    public final Game game2048 = new Game();

    public GUI() {
        setFocusable(true);
        playGame();
        new Thread(this).start(); //Remove to allow player-controlled game
    }

    public static void main(String[]args) {

        JFrame game = new JFrame();
        game.setTitle("2048");
        game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        game.setSize(500, 600);

        game.add(new GUI());

        game.setLocationRelativeTo(null);
        game.setVisible(true);
    }

    public void run() {
        Expectimax algorithm = new Expectimax();

        game2048.resetGame();
        
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                repaint();
            }
        }, DELAY);


        while(true) {
            viewQueue.add(game2048.getTiles());
            try {
                Thread.sleep(0); //Adjust value to slow down A.I.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            game2048.move(algorithm.nextMove(game2048.getTiles()));
            repaint();

            if(game2048.lost) {
                for (Tile tile : game2048.getTiles()) {
                    if(tile.value > largest)
                        largest = tile.value;
                }
                if(largest < 2048)
                    lessThan2048++;
                else if (largest == 2048)
                	exactly2048++;
                else if(largest == 4096)
                    exactly4096++;
                else if(largest >= 8192) {
                	moreThan8192++;
                    System.out.println("\nLargest tile: "+largest+"\nScore: "+game2048.getScore());
                    break;
                }
                game2048.resetGame();
                cycles++;
                }
            }
        }

    private void playGame() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    game2048.resetGame();
                }
                if (!game2048.lost) {
                    Direction dir = null;
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_A:
                            dir = Direction.LEFT;
                            break;
                        case KeyEvent.VK_D:
                            dir = Direction.RIGHT;
                            break;
                        case KeyEvent.VK_W:
                            dir = Direction.UP;
                            break;
                        case KeyEvent.VK_S:
                            dir = Direction.DOWN;
                            break;
                        default:
                            return;
                    }
                    game2048.move(dir);
                }
                if (!game2048.canMove()) {
                    game2048.lost = true;
                }

                addToQueue(game2048.getTiles());
                repaint();
            }
        });
        game2048.resetGame();
        addToQueue(game2048.getTiles());
    }

    public void addToQueue(Tile[] tiles) {
        viewQueue.add(tiles);
    }

    private int offsetCoors(int arg) {
        return arg * (TILE_MARGIN + TILE_SIZE) + TILE_MARGIN;
    }

    //GUI
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(BG_COLOR);
        g.fillRect(0, 0, this.getSize().width, this.getSize().height);
        Tile[] tiles = game2048.getTiles();//viewQueue.poll();
        if(tiles==null)
            return;
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                drawTile(g, tiles[x + y * 4], x, y);
            }
        }
    }

    private void drawTile(Graphics g2, Tile tile, int x, int y) {
        Graphics2D g = ((Graphics2D) g2);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        int value = tile.value;
        int xOffset = offsetCoors(x);
        int yOffset = offsetCoors(y);
        g.setColor(tile.getBackground());
        g.fillRoundRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE, 14, 14);
        g.setColor(tile.getForeground());
        final int size = value < 100 ? 36 : value < 1000 ? 32 : 24;
        final Font font = new Font(FONT, Font.BOLD, size);
        g.setFont(font);

        String s = String.valueOf(value);
        final FontMetrics fm = getFontMetrics(font);

        final int w = fm.stringWidth(s);
        final int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2];

        if (value != 0)
            g.drawString(s, xOffset + (TILE_SIZE - w) / 2, yOffset + TILE_SIZE - (TILE_SIZE - h) / 2 - 2);

            if (game2048.lost) {
                g.setFont(new Font(FONT, Font.PLAIN, 38));
                g.drawString("GAME OVER", 130, 255);

            }
        g.setFont(new Font(FONT, Font.PLAIN, 18));
        g.drawString("Score: " + game2048.getScore(), 350, 495);
        g.drawString("2048: "+exactly2048, 25, 495);
        g.drawString("4096: "+exactly4096, 25, 520);
        g.drawString("8192: "+moreThan8192, 25, 545);
        g.drawString("Cycles: " +cycles, 180, 495);
        g.drawString("Failures: " +lessThan2048, 180, 520);
        g.drawString("Highest Tile: " +largest, 180, 545);
    }
}