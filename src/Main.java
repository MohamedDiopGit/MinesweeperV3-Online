


import javax.swing.JPanel;

/**
 * {@code Main} application : Minesweeper program.
 */
public class Main extends JPanel {
    /**
     * Main GUI for the game : Minesweeper.
     */
    private final GUI gui;
    /**
     * Field to start with in the game.
     */
    private Field field;

    public Main() {
        loadGameLevel(); // Load the game level
        this.field.initField(); // initialisation of the field

        gui = new GUI(this);
        add(gui);

    }

    /**
     * Runs the minesweeper program.
     * 
     * @param args : optional arguments to pass to the program.
     */
    public static void main(String[] args) {
        new Main();
    }

    /**
     * Returns the current field that {@code Main} is running.
     * 
     * @return {@code Field}
     */
    public Field getField() { // Getter of the field
        return this.field;
    }

    /**
     * Loads the saved level's configuration from "LevelRegistred.dat"
     * 
     * @see LevelsFileReader
     */
    public void loadGameLevel() {

        try {
            LevelsFileReader fileReader = new LevelsFileReader();

            // Waiting for the reader thread to finish loading the level mmode
            fileReader.geThread().join();

            // Configure the field with the level mode.
            field = new Field(fileReader.getLevelFromFile());

        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Failed to load last save... EASY_Mode selected.");
        }
    }
}