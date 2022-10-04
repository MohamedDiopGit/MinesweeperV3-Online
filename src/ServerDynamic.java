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
 * {@code Server} : Main class that creates a server for communication between
 * multiple clients with dynamic multithreading.
 */
public class ServerDynamic extends JFrame implements Runnable {
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
     * Main Server program.
     */

     
    /**
     * Main GUI for the game : Minesweeper.
     */
    private GUI minesweeperGUI;
    private Main main;
    /**
     * Field to start with in the game.
     */
    private Field field;

    public static void main(String args[]) {
        System.out.println("Running server...");
        new ServerDynamic();
    }
    /**
     * Constructor for the server.
     */
    ServerDynamic() {

        setTitle("Server");
        setLayout(new FlowLayout());

        // Chat GUI display
        chatGUI = new ChatGUI(); // Default chat GUI : server side
        add(chatGUI);
        JMenuItem totalConnectedClient = new JMenuItem("Total connected clients");
        JMenu infoMenu = new JMenu("Show infos");

        infoMenu.add(totalConnectedClient);    
        infoMenu.add(connectedClients);

        totalConnectedClient.addActionListener(e-> showTotalConnectedClients() );

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(infoMenu);
        setJMenuBar(menuBar);

        // GUI : Minesweeper interface
        main = new Main();
        minesweeperGUI = new GUI(main);
        add(minesweeperGUI);

        // Frame settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Shuts down the server when exit
        pack();
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

    // NETWORK METHODS

    private void showTotalConnectedClients() {
        chatGUI.addTextToChat("Info: " + clients.size() + " client(s) connected.");
    }



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
            
            // Read data from client
            String message = "";
            while (!message.equals("end")) {
                try {
                    message = entree.readUTF();
                    chatGUI.addTextToChat(getUtcDateTime() + " :[" + pseudoClient + "]: " + message);

                    sendToAll(pseudoClient, message); // Broadcast to the others connected clients

                    if (message.equals("server-off")) {
                        message = "end";
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