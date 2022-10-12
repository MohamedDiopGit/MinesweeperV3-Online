import javax.swing.*;
import java.net.*;
import java.util.Random;
import java.io.*;
import java.awt.*;

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
    
    private JMenuItem connectedClient;
    private JMenu connectedClients = new JMenu();

    Client() {
        // Realease version mode
        setClientParameters();

        // Debugging mode
        // Random r = new Random();
        // int alea = r.nextInt((100 - 0) + 1) + 0;
        // runClient("localhost", 10000,"Client-"+alea);  // Dev usage
        // setTitle("Client: "+alea);
        
        setLayout(new FlowLayout());

        // Chat GUI display (Menu)
        JMenu infoServer = new JMenu("Server infos");
        JMenuItem modeSolo = new JMenuItem("Mode solo");
        connectedClients = new JMenu("Connected clients");

        infoServer.add(modeSolo);    
        infoServer.add(connectedClients);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JLabel levelMode = new JLabel(" Mode ");
        JButton quit = new JButton("Quit");
        

        quit.setBackground(Color.RED);
        quit.setForeground(Color.WHITE);
        quit.addActionListener(evt -> System.exit(0));

        menuBar.add(quit);
        menuBar.add(infoServer);
        menuBar.add(levelMode);
        menuBar.add(levelGameModeInfo);
        

        // Frame settings
        setResizable(false);
        // setVisible(true);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Shuts down the server when exit
    }

    // GUI METHODS

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
                    messageReceived = in.readUTF();
                    levelGameModeInfo.setText(messageReceived);
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
                else if(messageReceived.equals("-1:connectedClients")){
                    int totalConnected = in.readInt();
                    connectedClients.removeAll();
                    for(int i = 0; i < totalConnected; i++) {
                        messageReceived = in.readUTF();
                        connectedClient = new JMenuItem(messageReceived);
                        connectedClients.add(connectedClient);
                    }
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