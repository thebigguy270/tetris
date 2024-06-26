package tetrispkg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JFrame {
    private JButton startButton;
    private JButton instructionsButton;
    private JButton exitButton;

    public MainMenu() {
        setTitle("Tetris - Menu Principal");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1));

        startButton = new JButton("Commencer une nouvelle partie");
        instructionsButton = new JButton("Voir les instructions");
        exitButton = new JButton("Quitter le jeu");

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Démarrer le jeu
                dispose(); // Ferme le menu principal
                createAndShowGUI(MainMenu.this);
            }
        });

        instructionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Afficher les instructions
                JOptionPane.showMessageDialog(MainMenu.this, "Instructions du jeu Tetris:\n- Utilisez les touches fléchées pour déplacer les pièces\n- Utilisez la touche entrée pour accélérer la descente\n- Utilisez la touche espace pour faire tomber la pièce directement\n- Appuyez sur P pour mettre le jeu en pause", "Instructions", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Quitter le jeu
                System.exit(0);
            }
        });

        add(startButton);
        add(instructionsButton);
        add(exitButton);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainMenu mainMenu = new MainMenu();
                mainMenu.setVisible(true);
            }
        });
    }
    
    private static void createAndShowGUI(MainMenu mainmenu) {
        JFrame frame = new JFrame("Tetris");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Tetris(mainmenu));
        frame.pack();
        frame.setVisible(true);
    }
}
