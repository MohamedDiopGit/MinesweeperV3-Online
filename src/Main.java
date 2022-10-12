


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
        field = new Field(Levels.EASY);
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

    public void setField(Levels level) {
        this.field = new Field(level);
    }

    public GUI getGUI() {
        return gui;
    }
}