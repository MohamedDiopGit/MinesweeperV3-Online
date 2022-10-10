import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.awt.*;
import javax.swing.*;
import java.io.*; // Streams
import java.net.*;

import static java.lang.Thread.currentThread;

/**
 * {@code Server} : minesweeper class that creates a server for communication between
 * multiple clients with dynamic multithreading.
 */
public class Server extends JFrame implements Runnable {
    /**
     * Server socket center.
     */
    private ServerSocket gestSock;
    /**
     * Array of the threads.
     */
    private static List<Thread> clients = new ArrayList<Thread>();
    private static List<String> pseudoClients = new ArrayList<String>();
    /* 
     * Array of the out streams for broadcasting messages.
     */
    private static List<DataOutputStream> outs = new ArrayList<DataOutputStream>();
    /**
     * Chat box built in a GUI.
     * see {@code ChatGUI}
     */
    private ChatGUI chatGUI;

    /**
     * Data formatter to send the data with messages
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-uuuu HH:mm:ss z");

    /**
     * Label for connected clients in the subMenu
     */
    private JMenu connectedClients = new JMenu("Connected clients");
    
    /**
     * minesweeper Server program.
     */

     
    /**
     * minesweeper GUI for the game : Minesweeper.
     */
    private Main minesweeper;
    private JLabel levelGameModeInfo = new JLabel();

    public static void main(String args[]) {
        System.out.println("Running server...");
        new Server();
    }
    /**
     * Constructor for the server.
     */
    Server() {

        setTitle("Server");
        setLayout(new FlowLayout());

        // Chat GUI display
        chatGUI = new ChatGUI(); // Default chat GUI : server side
        add(chatGUI);
        JMenuItem totalConnectedClient = new JMenuItem("Total connected clients");
        JMenu infoMenu = new JMenu("Server infos");

        infoMenu.add(totalConnectedClient);    
        infoMenu.add(connectedClients);

        totalConnectedClient.addActionListener(e-> showTotalConnectedClients() );

        JMenuBar menuBar = new JMenuBar();
        
        setJMenuBar(menuBar);

        // GUI : Minesweeper interface

        minesweeper = new Main();
        add(minesweeper.getGUI());

        JMenuItem menu = new JMenu("Mode");
        JMenuItem easyMode = new JMenuItem("EASY");
        JMenuItem mediumMode = new JMenuItem("MEDIUM");
        JMenuItem hardMode = new JMenuItem("HARD");
        JMenuItem customMode = new JMenuItem("CUSTOM");
        JButton quit = new JButton("Quit");
        JButton saveGame = new JButton("Save");

        levelGameModeInfo.setText(String.valueOf(minesweeper.getField().getLevel()));

        quit.setBackground(Color.RED);
        quit.setForeground(Color.WHITE);
        saveGame.setBackground(Color.ORANGE);
        saveGame.setForeground(Color.WHITE);

        menu.add(easyMode);
        menu.add(mediumMode);
        menu.add(hardMode);
        menu.add(customMode);
        

        // Add menu options
        saveGame.addActionListener(evt -> saveGameLevel());
        quit.addActionListener(evt -> System.exit(0));

        // Add different mode in the menu
        easyMode.addActionListener(evt -> selectorLevelGame(Levels.EASY));
        mediumMode.addActionListener(evt -> selectorLevelGame(Levels.MEDIUM));
        hardMode.addActionListener(evt -> selectorLevelGame(Levels.HARD));
        customMode.addActionListener(evt -> selectorLevelGame(Levels.CUSTOM));

        JButton clientsFieldInit = new JButton("Init Clients Fields");
        clientsFieldInit.addActionListener(evt -> sendToAllField());

        menuBar.add(quit);
        menuBar.add(saveGame);
        menuBar.add(clientsFieldInit);
        menuBar.add(infoMenu);
        menuBar.add(menu);
        menuBar.add(levelGameModeInfo);
        

        // Frame settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Shuts down the server when exit
        pack();
        // setResizable(false);
        setVisible(true);


        // Threads creation
        try {// Socket manager : port 10000
            gestSock = new ServerSocket(10000);
            Thread client = new Thread(this);
            client.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // GUI METHODS

    private void saveGameLevel() {
        minesweeper.getGUI().saveGameLevel();
    }
    private void selectorLevelGame(Levels level) {
        minesweeper.getGUI().selectorLevelGame(level);
        levelGameModeInfo.setText(String.valueOf(level));
        pack();

        sendToAllField();
    }
    private void showTotalConnectedClients() {
        chatGUI.addTextToChat("Info: " + pseudoClients.size() + " client(s) connected.");
    }

    // NETWORK

    @Override
    public void run() {
        int idClient = (int) currentThread().getId();
        try {
            Socket socket = gestSock.accept(); // Waiting for connection
            clients.add(currentThread());

            // Add a thread to wait for another connection
            Thread client = new Thread(this);
            client.start();
            
            

            // Establish a stream connection with client
            DataInputStream entree = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            outs.add(out);

            // Data reading
            String pseudoClient = entree.readUTF();
            JMenuItem pseudoClientItem = new JMenuItem(pseudoClient);
            pseudoClients.add(pseudoClient);
            connectedClients.add(pseudoClientItem);

            // Send data : unique id of the client.
            out.writeInt(idClient);
            
            // Connection notification to all clients connected
            chatGUI.addTextToChat(getUtcDateTime() + " [" + pseudoClient + "]: " + " is connected");
            notifyConnectionToAll(idClient, pseudoClient, true, out);
            
            sendToAllField();
            // Read data from client
            String message = "";
            while (!message.equals("end")) {
                try {
                    message = entree.readUTF();

                    
                    if(message.equals("-1:rightClick")){
                        int xReceived = entree.readInt();
                        int yReceived = entree.readInt();
                        minesweeper.getGUI().updateBoxOnClient(xReceived, yReceived, "rightClick");
                        updateBoxToAll(xReceived, yReceived, message, out);
                    }
                    else if(message.equals("-1:leftClick")){
                        int xReceived = entree.readInt();
                        int yReceived = entree.readInt();
                        minesweeper.getGUI().updateBoxOnClient(xReceived, yReceived, "leftClick");
                        updateBoxToAll(xReceived, yReceived, message, out);
                    }
                    else if(message.equals("-1:resetMineSweeper")){
                        resetAllMineSweeper();
                    }
                    else{

                        chatGUI.addTextToChat(getUtcDateTime() + " :[" + pseudoClient + "]: " + message);
                        sendToAll(pseudoClient, message); // Broadcast to the others connected clients
                    }
                } catch (EOFException | SocketException e) {
                    message = "end";
                }
            }

            // Clean close of the session
            chatGUI.addTextToChat(getUtcDateTime() + " [" + pseudoClient + "]: " + " has disconnected.");
            notifyConnectionToAll(idClient, pseudoClient, false, out);

            out.close();
            outs.remove(out);

            entree.close();
            socket.close();

            pseudoClients.remove(pseudoClient);
            connectedClients.remove(pseudoClientItem);

        } catch (IOException e) {// Quick cleaning
            // throw new RuntimeException();
            System.out.println("Failed to connect on thread: " + idClient + ",please retry.");
        }
        clients.remove(currentThread());
    }

    private void resetServerMineSweeper(){
        minesweeper.getGUI().reInitField();
    }
    private void resetAllMineSweeper() {
        resetServerMineSweeper();
        sendToAllField();
    }

    private void updateBoxToAll(int xReceived, int yReceived, String typeClicked, DataOutputStream outClient) {
        outs.forEach(o -> {
            try {
                if(!o.equals(outClient)) {
                    o.writeUTF(typeClicked);
                    o.writeInt(xReceived);
                    o.writeInt(yReceived);
                }
            } catch (IOException e) {
                System.out.println("error writing message : notifyConnectionToAll");
            }
        });
        chatGUI.addTextToChat("Update box: " + xReceived + " " + yReceived + " " + typeClicked);
    }

    /**
     *  Sends a message to all the connected clients
     * @param pseudoClient
     * @param message
     */
    public void sendToAll(String pseudoClient, String message) {
        String messageComplete = getUtcDateTime() + " [" + pseudoClient + "]: " + message;
        outs.forEach(o -> {
            try {
                o.writeUTF(messageComplete);
            } catch (IOException e) {
                System.out.println("error writing message : sendToAll");
            }
        });
        
    }

    public void sendToAllField() {
        outs.forEach(o -> {
            try {
                o.writeUTF("-1:initField");
                int dimParam = minesweeper.getGUI().getField().getDim();
                o.writeUTF(String.valueOf(dimParam));
                int numMinesToPlace = minesweeper.getGUI().getField().getNumberOfMines();
                o.writeUTF(String.valueOf(numMinesToPlace));

                for(int x=0; x<dimParam; x++) {
                    for(int y=0; y<dimParam; y++) {
                        o.writeUTF(minesweeper.getGUI().getField().getElementFromXY(x,y, false));
                    }
                }
                o.writeUTF("[Server]:Field reinitialization complete.");
                
            } catch (IOException e) {
                System.out.println("error writing message : sendToAll");
            }
        });
        
    }

    /**
     * Notifies the connected client a specific client is connected
     * @param idClientConnected
     * @param pseudoClient
     * @param message
     */
    public void notifyConnectionToAll(int idClientConnected, String pseudoClient, boolean isConnected, DataOutputStream outClient) {
        String messageComplete;
        if(isConnected){
            messageComplete = getUtcDateTime() + " [" + pseudoClient + "]: " + " is connected";
        }
        else{
            messageComplete = getUtcDateTime() + " [" + pseudoClient + "]: " + " has disconnected";
        }
        outs.forEach(o -> {
            try {
                if(!o.equals(outClient)) {
                    o.writeUTF(messageComplete);
                }
            } catch (IOException e) {
                System.out.println("error writing message : notifyConnectionToAll");
            }
        });
    }

    public static String getUtcDateTime() {
        return ZonedDateTime.now(ZoneId.of("Etc/UTC")).format(FORMATTER);
    }


}