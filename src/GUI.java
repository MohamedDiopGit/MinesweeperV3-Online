
import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
// import java.util.Scanner;  // For terminal entries mode, uncomment it if you want to use terminal entries. 

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * {@code GUI} : Graphic User Interface class, extends {@code JPanel}.
 * The Main component that runs the Graphic interface,
 * and manages the front and back-end processes to call entities and specific
 * functions
 * in order to display the information.
 * It allows to display the grid, the menubar on the Main frame and the pop-ups
 * to give and collect data correctly.
 */
public class GUI extends JPanel {

    /**
     * Field to be process for the grid and display in the GUI.
     */
    private Field field;

    /**
     * imported Main from the "Main.java".
     */
    private Main main;

    /**
     * Timer for the session which update every second the {@code timeSession}.
     */
    private Timer timer;

    /**
     * Seconds elapsed since the beginning.
     */
    private int seconds = 0;


    /**
     * score of the current game session.
     */
    private JLabel score = new JLabel();
    /**
     * Score of the current game session to be copy on the {@code JLabel} instance.
     */
    private int scoreTemp = 0;

    /**
     * Time session (elapsed) information to display
     */
    private JLabel timeSession = new JLabel();

    /**
     * restart button's text
     */
    private JButton restart = new JButton("Restart");

    /**
     * Pane in the center of the screen that displays the grid
     */
    private JPanel panelCenter = new JPanel();
    private JPanel panelNorth = new JPanel();
    /**
     * Current game level of the session
     * 
     * @see Levels
     */
    private Levels levelGame;

    /**
     * Game mode's text
     */
    private JLabel levelGameModeInfo = new JLabel();

    private DataOutputStream outStream;
    private List<JButton> buttons = new ArrayList<JButton>();
    /**
     * Constructor for the GUI, which starts the game.
     * 
     * @param {@code Main} : the Main component that contains a {@code Field}.
     * @see #startNewGame()
     */
    public GUI(Main main) {
        this.main = main;
        this.field = main.getField();
        startNewGame();
    }
    /**
     * Global starter method which starts and initializes the game
     * by launching {@code displayGUI()}, {@code setTimeLimit()},
     * {@code Field.initField()} methods and catch the game level in the GUI.
     * 
     * @see #setTimeLimit()
     * @see #displayGUI()
     */
    public void startNewGame() {
        field.initField();

        // Deprecated method of level initialization
        this.levelGame = field.getLevel();
        this.displayGUI();
    }


    /**
     * Main GUI initialization's method for the Main frame, it displays the menu,
     * the time elapsed
     * the current score, the restart button, and display the field at the beginning
     * (with hidden boxes)
     * on the frame.
     * 
     * @see #displayMenu()
     * @see #displayScore()
     * @see #timeElapsed()
     * @see #restartButton()
     * @see #reInitField()
     * @see #displayStartEmptyField()
     */
    public void displayGUI() {
        setLayout(new BorderLayout());
        this.timeElapsed();
        this.displayScore();
        this.restartButton();
        this.reInitField();
    }

    /**
     * Displays the menu bar for choosing between multiple difficulties
     * and display their informations.
     * It adds the {@code ActionListener} on the difficulty options,
     * in order to {@code startNewGame()} with parameters depending on level game
     * mode.
     * 
     * @see #startNewGame()
     */

    public void selectorLevelGame(Levels level) {
        field = new Field(level);
        levelGameModeInfo.setText(String.valueOf(level));
        startNewGame();
    }


    /**
     * This function takes the first "clicked" box {@code JButton}
     * to initialize the grid {@code GridLayout} from {@code JPanel} with the field
     * and uncover some boxes around it. It also adds {@code MouseAdapter} or
     * {@code ActionListener} events depending
     * on the value of the box (mine, hidden box, non-mine) and the user behaviour
     * (right or left click).
     * 
     * @param xOnStart : the x position of the first "clicked" on the hidden boxes'
     *                 field
     * @param yOnStart : the y position of the same box
     */
    public void initializationField() { // Initialization of boxes with different values for a
                                                                  // certain area / allow to place flags on mines

        remove(panelCenter); // initialization of the panel
        panelCenter = new JPanel();
        add(panelCenter, BorderLayout.CENTER);

        int dimParam = this.field.getDim(); // Get the dimensions of the field
        panelCenter.setLayout(new GridLayout(dimParam, dimParam));
        buttons.clear();

        // Loop on the entire field elements
        for (int x = 0; x < dimParam; x++) {
            for (int y = 0; y < dimParam; y++) { // For loop on the matrix to display all objects
                final int xBox = x;
                final int yBox = y;

                // Add a box on the grid
                // minefield's boxes
                // Case boxCase = new Case(xOnStart, yOnStart, xBox, yBox,this.field.getElementFromXY(x, y, true), dimParam );
                // panelCenter.add(boxCase);

                JButton box = new JButton(this.field.getElementFromXY(x, y, false)); // Clickeable button on each
                box.setBackground(Color.WHITE);
                box.setPreferredSize(new Dimension(40, 40));
                buttons.add(box);
                panelCenter.add(buttons.get(buttons.size() - 1));

                box.setText(""); // Hide it with a white background and not text

                box.addMouseListener(new MouseAdapter() { // OnClick event : Place a flag or trigger the "Game over
                                                          // event"
                    @Override
                    public void mouseClicked(MouseEvent event) {
                        String boxType = field.getElementFromXY(xBox, yBox, false);
                        String typeClicked = "";
                        if (isRightMouseButton(event)) // Set the box with a red flag
                        {
                            typeClicked = "rightClick";
                            if (boxType.equals("x") && box.getText() != "F") {
                                scoreTemp++;
                                score.setText(String.valueOf(scoreTemp));
                                if (scoreTemp == field.getNumberOfMines()) {
                                    JOptionPane.showMessageDialog(main, "You won ! : what a player !",
                                            "Game win", JOptionPane.WARNING_MESSAGE);
                                    resetMineSweeperOnServer();
                                    reInitField();
                                }
                            }
                            box.setText("F");
                        }

                        else if (isLeftMouseButton(event) && box.getText() != "F") {
                            typeClicked = "leftClick";
                            clickBoxOnServer(xBox, yBox, typeClicked);
                            if(boxType.equals("x")){
                                box.setText("X");
                                // Code To popup an Game Over message :
                                JOptionPane.showMessageDialog(main, "You clicked on a mine : Game Over LOOSER >-<",
                                        "GAME OVER", JOptionPane.WARNING_MESSAGE);
                                resetMineSweeperOnServer(); 
                                reInitField();
                            }

                            else if(boxType.equals("0")){
                                box.setText(field.getElementFromXY(xBox, yBox, true)); 
                                box.setBackground(Color.GRAY);
                                switch ( Integer.valueOf(box.getText()) ) {
                                    case 0:
                                        box.setBackground(Color.GRAY);
                                        break;
                                    case 1:
                                        box.setForeground(Color.BLUE);
                                        break;
                                    case 2:
                                        box.setForeground(Color.GREEN);
                                        break;
                                    case 3:
                                        box.setForeground(Color.RED);
                                        break;
                                    case 4:
                                        box.setForeground(Color.ORANGE);
                                        break;
                                    case 5:
                                        box.setForeground(Color.MAGENTA);
                                        break;
                                    case 6:
                                        box.setForeground(Color.CYAN);
                                        break;

                                }
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * Update the selected box on the server with the type of click.
     * @param x
     * @param y
     * @param typeClick
     */
    public void clickBoxOnServer(int x, int y, String typeClick){
        try {
            outStream.writeUTF("-1:" + typeClick);
            outStream.writeInt(x);
            outStream.writeInt(y);
        } catch (IOException e) {
            System.out.println("error writing message : clickBoxOnServer");
        }
    }

    public void setOutputStream(DataOutputStream out){
        this.outStream = out;
    }
    /**
     * Activates the restart button by adding an {@code ActionListener} event
     * on the restart button. It will call the {@code reInitField()} method.
     * 
     * @see #reInitField()
     */
    public void restartButton() { // Restart a game
        restart.setBackground(Color.WHITE);
        add(restart, BorderLayout.SOUTH);
        restart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reInitField();
            }
        });

    }

    /**
     * Generates a new field, and restarts the timer, and the score of the current
     * game.
     * It also calls the {@code displayStartEmptyField()} method to clear the
     * field/grid.
     * 
     * @see #displayStartEmptyField()
     */
    public void reInitField() {
        seconds = 0;
        scoreTemp = 0;
        score.setText(String.valueOf(scoreTemp));
        timeSession.setText(String.valueOf(seconds));
        timer.start();

        field.initField();
        this.initializationField();
    }
    public void resetMinesweeperParameters() {
        seconds = 0;
        scoreTemp = 0;
        score.setText(String.valueOf(scoreTemp));
        timeSession.setText(String.valueOf(seconds));
        timer.start();
    }
    /**
     * Processes the time elapsed since the beginning of the start of a game
     * session.
     * It also checks if the time session has outdated the time limit, if so,
     * it will reinitialize the game after showing a popup (Game over) to the user.
     * 
     * @see #reInitField()
     */
    public void timeElapsed() { //
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seconds++;
                timeSession.setText(String.valueOf(seconds));
            }
        });
    }
    
    /**
     * Displays the current score of the player on the Main frame.
     */
    public void displayScore() { //
        remove(panelNorth);
        panelNorth.removeAll();
        add(panelNorth, BorderLayout.NORTH);
        panelNorth.setLayout(new FlowLayout());
        panelNorth.add(new JLabel("Score: "));
        panelNorth.add(score);
        panelNorth.add(new JLabel(" | Time Elapsed(s): "));
        panelNorth.add(timeSession);

    }

    /**
     * Saves the game level in a local file "LevelRegistred.dat"
     */
    public void saveGameLevel() {
        new LevelsFileWriter(this.levelGame);
    }
    public void setFieldXY(int x, int y, boolean value) {
        field.setFieldGrid(x, y, value);
    }

    public Field getField() {
        return this.field;
    }
    public void updateBoxOnClient(int xReceived, int yReceived, String typeClicked) {
        if(typeClicked.equals("leftClick")){
            // Update the box
            buttons.get( yReceived + xReceived*field.getDim() ).setText(field.getElementFromXY(xReceived, yReceived, true));
            buttons.get( yReceived + xReceived*field.getDim() ).setBackground(Color.GRAY);

            if(!buttons.get( yReceived + xReceived*field.getDim() ).getText().equals("x")){
                switch ( Integer.valueOf(buttons.get( yReceived + xReceived*field.getDim() ).getText()) ) {
                    case 0:
                        buttons.get( yReceived + xReceived*field.getDim() ).setBackground(Color.GRAY);
                        break;
                    case 1:
                        buttons.get( yReceived + xReceived*field.getDim() ).setForeground(Color.BLUE);
                        break;
                    case 2:
                        buttons.get( yReceived + xReceived*field.getDim() ).setForeground(Color.GREEN);
                        break;
                    case 3:
                        buttons.get( yReceived + xReceived*field.getDim() ).setForeground(Color.RED);
                        break;
                    case 4:
                        buttons.get( yReceived + xReceived*field.getDim() ).setForeground(Color.ORANGE);
                        break;
                    case 5:
                        buttons.get( yReceived + xReceived*field.getDim() ).setForeground(Color.MAGENTA);
                        break;
                    case 6:
                        buttons.get( yReceived + xReceived*field.getDim() ).setForeground(Color.CYAN);
                        break;
    
                }
            }
        }
        else if(typeClicked.equals("rightClick")){
            // Update the box
            if(buttons.get( yReceived + xReceived*field.getDim() ).equals("x") ){
                scoreTemp++;
                score.setText(String.valueOf(score));
            }
            buttons.get( yReceived + xReceived*field.getDim() ).setText("F");
        }
    }

    public void resetMineSweeperOnServer(){
        try {
            outStream.writeUTF("-1:resetMineSweeper");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}