import javax.swing.*;
import java.net.*;
import java.util.Random;
import java.io.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code Client} : Client program that makes a connection with a {@code Server}
 * via socket, referenced by ip address and port.
 * 
 * @see ServerStatic
 */
public class Client extends JFrame implements Runnable  {
    /**
     * Chat box.
     */
    private ChatClient chatClient;
    /**
     * Message to send.
     */
    private String message = "";
    /**
     * Input Stream for collecting data from server.
     */
    private DataInputStream in;
    /**
     * Thread which reads the incoming data of the server and displays it on the
     * chat box.
     */
    private Thread chatReader = new Thread(this);

    /**
     * Default constructor for the client session.
     */
    /**
     * minesweeper GUI for the game : Minesweeper.
     */
    private Main minesweeper;
    private JLabel levelGameModeInfo = new JLabel();
    private String pseudo;
    /**
     * Label for connected clients in the subMenu
     */
    private JMenu connectedClients = new JMenu("Connected clients");
    private static List<String> pseudoClients = new ArrayList<String>();
    Client() {
        setClientParameters();
        // Random r = new Random();
        // // runClient("localhost", 10000,"Client-"+alea);  // Dev usage
        // int alea = r.nextInt((100 - 0) + 1) + 0;


        setTitle("Client: "+pseudo);
        setLayout(new FlowLayout());

        // Chat GUI display (Menu)
        JMenuItem totalConnectedClient = new JMenuItem("Total connected clients");
        JMenu infoMenu = new JMenu("Server infos");

        infoMenu.add(totalConnectedClient);    
        infoMenu.add(connectedClients);

        totalConnectedClient.addActionListener(e-> showTotalConnectedClients() );

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(infoMenu);
        setJMenuBar(menuBar);

        // GUI : Minesweeper interface

        // minesweeper = new Main();
        // add(minesweeper.getGUI());

        JMenuItem menu = new JMenu("Mode");
        JMenuItem easyMode = new JMenuItem("EASY");
        JMenuItem mediumMode = new JMenuItem("MEDIUM");
        JMenuItem hardMode = new JMenuItem("HARD");
        JMenuItem customMode = new JMenuItem("CUSTOM");
        JButton quit = new JButton("Quit");
        JButton saveGame = new JButton("Save");

        

        quit.setBackground(Color.RED);
        quit.setForeground(Color.WHITE);
        saveGame.setBackground(Color.ORANGE);
        saveGame.setForeground(Color.WHITE);

        menu.add(easyMode);
        menu.add(mediumMode);
        menu.add(hardMode);
        menu.add(customMode);
        menuBar.add(quit);
        menuBar.add(saveGame);
        menuBar.add(menu);

        // Add menu options
        saveGame.addActionListener(evt -> saveGameLevel());
        quit.addActionListener(evt -> System.exit(0));

        // Add different mode in the menu
        easyMode.addActionListener(evt -> selectorLevelGame(Levels.EASY));
        mediumMode.addActionListener(evt -> selectorLevelGame(Levels.MEDIUM));
        hardMode.addActionListener(evt -> selectorLevelGame(Levels.HARD));
        customMode.addActionListener(evt -> selectorLevelGame(Levels.CUSTOM));


        menuBar.add(levelGameModeInfo);
        

        // Frame settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Shuts down the server when exit
        pack();
        // setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Shuts down the server when exit
    }

    // GUI METHODS

    private void saveGameLevel() {
        minesweeper.getGUI().saveGameLevel();
    }
    private void selectorLevelGame(Levels level) {
        minesweeper.getGUI().selectorLevelGame(level);
        levelGameModeInfo.setText(String.valueOf(level));
        pack();
    }
    private void showTotalConnectedClients() {
        chatClient.addTextToChat("Info: " + pseudoClients.size() + " client(s) connected.");
    }

    // NETWORK 

    /**
     * Sets the client parameters for the client session.
     */
    public void setClientParameters() {
        JTextField addressField = new JTextField();
        JTextField portField = new JTextField();
        JTextField pseudoField = new JTextField();
        Object[] message = {
                "Ip Address:", addressField,
                "Port:", portField,
                "Pseudo:", pseudoField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Set connection to the server",
                JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) { // Check if something is entered
            String address = addressField.getText();
            int port = Integer.valueOf(portField.getText());
            pseudo = pseudoField.getText();
            runClient(address, port, pseudo);
        } else {
            JOptionPane.showMessageDialog(null, "Nothing selected. Press OK to exit.",
                    "ERROR", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Runs the Client main program to connect with a server and send message.
     * 
     * @param args : useless paramter.
     */
    public static void main(String[] args) {
        System.out.println("Running client...");
        new Client();
    }

    /**
     * Establishes a connection with the server and allows to send messages
     * 
     * @param address
     * @param port
     * @param pseudo
     */
    private void runClient(String address, int port, String pseudo) {
        int idClient;
        System.out.print("Trying to connect to " + address + " port:" + port + "...");
        try{
            Socket sock = new Socket(address, port);
            System.out.println("done.");

            // Initialize the streams
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
            in = new DataInputStream(sock.getInputStream());
      
            // Get information from the server
            out.writeUTF(pseudo);
            idClient = in.readInt(); // id of client : reception
            System.out.println("Client id: " + idClient);      
            
            // Initialize the message box
            chatClient = new ChatClient(pseudo);
            chatClient.setOutputStream(out);

            // Initialize the minesweeper
            minesweeper = new Main();
            
            minesweeper.getGUI().setOutputStream(out);
            pack();
            levelGameModeInfo.setText(String.valueOf(minesweeper.getField().getLevel()));
                
            // Read message from the server
            
            chatReader.start();

            add(chatClient);
            add(minesweeper.getGUI());
            pack();
            setVisible(true);

        } catch (IOException e ) {

            JOptionPane.showMessageDialog(null, address + ":" + port + " unreachable. Retry later.",
                    "ERROR", JOptionPane.WARNING_MESSAGE);
            setClientParameters();
        }
    }

    /**
     * Thread's method to read the incoming data and put in on the chat box GUI.
     */
    @Override
    public void run() {

        String messageReceived;
        while (!message.equals("end")) {
            try {
                int xReceived;
                int yReceived;
                messageReceived = in.readUTF();
                if(messageReceived.equals("-1:initField")){
                    int dimParam;
                    boolean valueBool;
                    dimParam = Integer.valueOf(in.readUTF());

                    int numMinesToPlace;
                    numMinesToPlace = Integer.valueOf(in.readUTF());
                    minesweeper.getGUI().getField().setFieldFromClient(numMinesToPlace, dimParam);

                    for(int x=0; x<dimParam; x++) {
                        for(int y=0; y<dimParam; y++) {
                            messageReceived = in.readUTF();
                            if(messageReceived.equals("x")){
                                valueBool = true;
                            }
                            else{
                                valueBool = false;
                            }
                            minesweeper.getGUI().setFieldXY(x, y, valueBool);
                            
                        }
                    }
                    minesweeper.getGUI().resetMinesweeperParameters();
                    minesweeper.getGUI().initializationField();
                    pack();
                }

                else if(messageReceived.equals("-1:rightClick")){
                    xReceived = in.readInt();
                    yReceived = in.readInt();
                    minesweeper.getGUI().updateBoxOnClient(xReceived, yReceived, "rightClick");

                }
                else if(messageReceived.equals("-1:leftClick")){
                    xReceived = in.readInt();
                    yReceived = in.readInt();
                    minesweeper.getGUI().updateBoxOnClient(xReceived, yReceived, "leftClick");
                }
                else{
                    chatClient.addTextToChat(messageReceived);
                }
            } catch (IOException e) { // Server off
                chatClient.addTextToChat("Server offline... disconnected.");
                message = "end";
            }
        }
        chatReader = null;
    }


}