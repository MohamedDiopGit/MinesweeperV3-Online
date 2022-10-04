
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

public class Case extends JPanel implements MouseListener {
    private Field field;
    private int DIM = 60;
    private int xBox, yBox, xOnStart, yOnStart;
    private String text;
    private boolean enter = false;
    private boolean exit = false;
    private boolean clicked = false;
    private boolean released = false;
    private boolean pressed = false;
    private boolean addFlag = false;
    private int dimParam;
    

    

    private ImageIcon bomb = new ImageIcon("img/bomb.png");
    private ImageIcon flag = new ImageIcon("img/flag.png");
    
    Case(int xOnStart, int yOnStart, int xBox, int yBox, String text, int dimParam){
        this.xBox = xBox;
        this.yBox = yBox;
        this.text = text;
        this.xOnStart = xOnStart;
        this.yOnStart = yOnStart;
        this.dimParam = dimParam;
        setPreferredSize(new Dimension(DIM,DIM));
        addMouseListener(this);
    }
    



    public void paintComponent(Graphics g){
        super.paintComponent(g);
        setBorder(BorderFactory.createLineBorder(Color.black));

        if(addFlag){
            super.paintComponent(g);
            g.drawImage(flag.getImage(),0,0, getWidth(), getHeight(), this);
        }

        else{
            if(exit && !clicked) {
                super.paintComponent(g); // appel méthode mère (efface le dessin précedent)

                


                double deltaX = Math.abs(xOnStart - xBox);
                double deltaY = Math.abs(yOnStart - yBox);
                double result = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                Random alea = new Random();
                if ((result <= alea.nextDouble((int) dimParam / 1.5)) && !text.equals("x") ) { // Undisover boxes that are in a calculated
                    switch (Integer.valueOf(text)) { // Set the Color of the number depending on
                        // its value
                        case 0:
                            g.setColor(Color.GRAY);
                            break;
                        case 1:
                            g.setColor(Color.BLUE);
                            break;
                        case 2:
                            g.setColor(Color.GREEN);
                            break;
                        case 3:
                            g.setColor(Color.RED);
                            break;
                        case 4:
                            g.setColor(Color.ORANGE);
                            break;
                        case 5:
                            g.setColor(Color.MAGENTA);
                            break;
                        case 6:
                            g.setColor(Color.CYAN);
                            break;
                    }
                    g.drawString(text, getWidth()/2, getHeight()/2);
                }
                else{
                    g.setColor(Color.lightGray);
                    g.fillRect(0,0, getWidth(), getHeight()); // dessin du texte à la position i,j
                }
                enter = false;
            }
            else if(released && !clicked) {
                super.paintComponent(g); // appel méthode mère (efface le dessin précedent)
                g.setColor(Color.lightGray);
                g.fillRect(0,0, getWidth(), getHeight()); // dessin du texte à la position i,j
            }
            
            else if(clicked && !addFlag){
                super.paintComponent(g);
                if(text.equals("x")){
                    g.drawImage(bomb.getImage(),0,0, getWidth(), getHeight(), this);
                }
                else{
                    switch (Integer.valueOf(text)) { // Set the Color of the number depending on
                            // its value
                        case 0:
                            g.setColor(Color.GRAY);
                            break;
                        case 1:
                            g.setColor(Color.BLUE);
                            break;
                        case 2:
                            g.setColor(Color.GREEN);
                            break;
                        case 3:
                            g.setColor(Color.RED);
                            break;
                        case 4:
                            g.setColor(Color.ORANGE);
                            break;
                        case 5:
                            g.setColor(Color.MAGENTA);
                            break;
                        case 6:
                            g.setColor(Color.CYAN);
                            break;
                    }
                    g.drawString(text, getWidth()/2, getHeight()/2);
                }
            }
            

            else if(enter){
                super.paintComponent(g); // appel méthode mère (efface le dessin précedent)
                g.setColor(Color.darkGray);
                g.fillRect(0, 0, getWidth(), getHeight()); // dessin du texte à la position i,j
            }
            else if(pressed){
                super.paintComponent(g); // appel méthode mère (efface le dessin précedent)
                g.setColor(Color.white);
                g.fillRect(0, 0, getWidth(), getHeight()); // dessin du texte à la position i,j
            }
            else{
                super.paintComponent(g); // appel méthode mère (efface le dessin précedent)
                g.setColor(Color.lightGray);
                g.fillRect(0, 0, getWidth(), getHeight()); // dessin du texte à la position i,j
            }
        }

    }



    @Override
    public void mouseClicked(MouseEvent e) {
        clicked = true;
        repaint();
        // clicked = false;
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        enter = true;
        repaint();
        // enter = false;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        exit = true;
        repaint();
        // exit = false;
        
    }
    @Override
    public void mousePressed(MouseEvent e) {
        pressed = true;
        if(isLeftMouseButton(e)) {  // left mouse button
            clicked = true;
            repaint();
            // clicked = false;
        }
        else if (isRightMouseButton(e)){// right mouse button
            if(addFlag){
                addFlag = false;
            }
            else{
                addFlag = true;
                repaint();
            }
        }
        // pressed = false;
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // released = true;
        // released = false;
    }

}






//     @Override
//     public void actionPerformed(ActionEvent e) {
//         if (text == "x") { // If there is a mine
//             text = ""; // Hide it with a white background and not text

//             this.addMouseListener(new MouseAdapter() { // OnClicked event : Place a flag or trigger the "Game over
//                 // event"
//                 @Override
//                 public void mouseClickeded(MouseEvent event) {

//                     if (isRightMouseButton(event)) // Set the box with a red flag
//                     {
//                         if (field.getElementFromXY(xBox, yBox, false) == "x" && text != "🚩") { // Check
//                             // if
//                             // there
//                             // is a
//                             // mine
//                             // and
//                             // not
//                             // flagged
//                             // before
//                             // scoreTemp++;
//                             // score.setText(String.valueOf(scoreTemp));
//                             // if (scoreTemp == field.getNumberOfMines()) {
//                             //     JOptionPane.showMessageDialog(null, "You won ! : what a player 💯",
//                             //             "Game win", JOptionPane.WARNING_MESSAGE);
//                             //     // reInitField();
//                             // }
//                         }
//                         text = "🚩";
//                     } else if (isLeftMouseButton(event) && text != "🚩") { // Check if Left clicked and
//                         // not a mine discovered :
//                         // GAME OVER
//                         // Code To popup an Game Over message :
//                         JOptionPane.showMessageDialog(null, "You clickeded on a mine : Game Over LOOSER 🤣",
//                                 "GAME OVER", JOptionPane.WARNING_MESSAGE);
//                         // reInitField();
//                     }
//                 }
//             });
//         }

//         else if (text == "0") { // Operations on non-mined boxes
//             double deltaX = Math.abs(xOnStart - xBox);
//             double deltaY = Math.abs(yOnStart - yBox);
//             double result = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
//             Random alea = new Random();
//             int dimParam = field.getDim();
//             if (result <= alea.nextDouble((int) dimParam / 1.5)) { // Undisover boxes that are in a calculated
//                 // area with euclidian distance as radius

//                 this.setBackground(Color.GRAY); // Color the unblocked boxes with gray color
//                 text = field.getElementFromXY(xBox, yBox, true); // Update the text in the box with the
//                 // computed value

//                 switch (Integer.valueOf(text)) { // Set the Color of the number depending on its value
//                     case 0:
//                         this.setBackground(Color.GRAY);
//                         break;
//                     case 1:
//                         this.setForeground(Color.BLUE);
//                         break;
//                     case 2:
//                         this.setForeground(Color.GREEN);
//                         break;
//                     case 3:
//                         this.setForeground(Color.RED);
//                         break;
//                     case 4:
//                         this.setForeground(Color.ORANGE);
//                         break;
//                     case 5:
//                         this.setForeground(Color.MAGENTA);
//                         break;
//                     case 6:
//                         this.setForeground(Color.CYAN);
//                         break;

//                 }
//             } else { // Boxes outside the calculated area
//                 text = field.getElementFromXY(xBox, yBox, true); // Change the value with the
//                 // computed one
//                 this.setBackground(Color.GRAY);
//                 switch (Integer.valueOf(text)) { // Set the Color of the number depending on
//                     // its value
//                     case 0:
//                         this.setBackground(Color.GRAY);
//                         break;
//                     case 1:
//                         this.setForeground(Color.BLUE);
//                         break;
//                     case 2:
//                         this.setForeground(Color.GREEN);
//                         break;
//                     case 3:
//                         this.setForeground(Color.RED);
//                         break;
//                     case 4:
//                         this.setForeground(Color.ORANGE);
//                         break;
//                     case 5:
//                         this.setForeground(Color.MAGENTA);
//                         break;
//                     case 6:
//                         this.setForeground(Color.CYAN);
//                         break;

//                 }

//                 this.addMouseListener(new MouseAdapter() { // OnClicked event : Place a flag or trigger the "Game
//                     // over event"
//                     @Override
//                     public void mouseClickeded(MouseEvent event) {

//                         if (isRightMouseButton(event)) // Set the box with a red flag
//                         {
//                             if (field.getElementFromXY(xBox, yBox, false) == "x" && text != "🚩") { // Chech
//                                 // if
//                                 // there
//                                 // is
//                                 // a
//                                 // mine
//                                 // and
//                                 // not
//                                 // flagged
//                                 // before
//                                 // scoreTemp++;
//                                 // score.setText(String.valueOf(scoreTemp));
//                             }
//                             text = "🚩";
//                         }
//                     }
//                 });
//             }
//         }

//     }

// }
