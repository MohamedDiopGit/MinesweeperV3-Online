
import javax.swing.*;
import java.net.*;
import java.util.Random;
import java.io.*;

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
    private ChatGUI chatGUI;
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
    Client() {
        // setClientParameters();
        Random r = new Random();
        int alea = r.nextInt((100 - 0) + 1) + 0;
        runClient("localhost", 10000,"Client-"+alea);  // Dev usage


        setTitle("Client-"+alea + ": " + "Chat box");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Shuts down the server when exit
    }

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
            String pseudo = pseudoField.getText();
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
            chatGUI = new ChatGUI(pseudo);
            chatGUI.setOutputStream(out);
            
            // Read message from the server
            chatReader.start();

            add(chatGUI);
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
                messageReceived = in.readUTF();
                chatGUI.addTextToChat(messageReceived);
            } catch (IOException e) { // Server off
                chatGUI.addTextToChat("Server offline... disconnected.");
                message = "end";
            }
        }
        chatReader = null;
    }

}