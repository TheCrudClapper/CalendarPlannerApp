package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author Wojciech Mucha
 */
/**
 * 
 * LoginWindow - class that handles login window gui and sends to server login data
 */
public class LoginWindow extends JDialog implements ActionListener {

    private JTextField loginField;
    private JPasswordField passwordField;
    private JLabel windowTitleLabel, loginLabel, passwordLabel, accountExistLabel;
    private JButton loginButton, signUpButton;
    private Client clientWriter;

    /**
     * Constructor prepeares window to be displayed
     * @param client objects allows to communicate with server
     */
    public LoginWindow(Client client) {
        this.clientWriter = client;
        ImageIcon icon = new ImageIcon("logo.png");
        Image image = icon.getImage();

        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setTitle("TimeWise");
        this.setLayout(null);
        this.setSize(500, 600);
        this.setLocationRelativeTo(null);
        this.setIconImage(image);
        this.setResizable(false);
        //Setting up components and adding them to window

        windowTitleLabel = new JLabel("Login");
        windowTitleLabel.setFont(new Font("Tahoma", Font.BOLD, 50));
        windowTitleLabel.setForeground(new Color(221, 42, 123));
        windowTitleLabel.setBounds(170, 60, 140, 60);

        loginLabel = new JLabel("Login");
        loginLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
        loginLabel.setBounds(30, 180, 50, 30);

        loginField = new JTextField();
        loginField.setFont(new Font("Tahoma", Font.PLAIN, 20));
        loginField.setBounds(30, 210, 430, 40);

        passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
        passwordLabel.setBounds(30, 280, 90, 30);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Tahoma", Font.PLAIN, 20));
        passwordField.setBounds(30, 310, 430, 40);

        loginButton = new JButton("Log in");
        loginButton.setForeground(Color.white);
        loginButton.setBackground(new Color(221, 42, 123));
        loginButton.setFocusable(false);
        loginButton.setBorder(null);
        loginButton.setBounds(30, 380, 80, 35);
        loginButton.addActionListener(this);

        accountExistLabel = new JLabel("I'm new here !");
        accountExistLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
        accountExistLabel.setBounds(30, 440, 110, 40);

        signUpButton = new JButton("Sign Up");
        signUpButton.setForeground(new Color(221, 42, 123));
        signUpButton.setFocusable(false);
        signUpButton.setBorder(BorderFactory.createEtchedBorder(new Color(221, 42, 123), new Color(221, 42, 123)));
        signUpButton.setBackground(Color.white);
        signUpButton.setBounds(145, 443, 80, 35);
        signUpButton.addActionListener(this);

        //adding components to main frame
        this.add(windowTitleLabel);
        this.add(loginLabel);
        this.add(loginField);
        this.add(passwordLabel);
        this.add(passwordField);
        this.add(loginButton);
        this.add(accountExistLabel);
        this.add(signUpButton);
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Login window is closing");
                client.sendDataToServer("END");
                dispose();
            }
        });
    }
    /**
     * Method that get's user input from login window textFields and sends data to server using client object
     */
    public void getUserInput() {
        String login = loginField.getText();
        String password = new String(passwordField.getPassword());
        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter data to all fields");
        } else {
            clientWriter.sendDataToServer("login");
            clientWriter.sendDataToServer(login);
            clientWriter.sendDataToServer(password);
            loginField.setText(null);
            passwordField.setText(null);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == signUpButton) {
            //logic for sending to server credintal for registration
            this.setVisible(false);
            new RegisterWindow(clientWriter, this);
        }
        if (source == loginButton) {
            getUserInput();
        }

    }
}
