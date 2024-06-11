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
 * RegisterWindow - class that handles register window gui and send to server register data
 */
public class RegisterWindow extends JDialog implements ActionListener {

    private JButton signUpButton, loginButton;
    private JPasswordField passwordField;
    private JTextField loginField, emailField;
    private JPanel brandPanel, registrationPanel;
    private JLabel appLogoLabel, appNameLabel, authorLabel, mottoLabel, loginLabel, emailLabel, passwordLabel, signUpLabel, existingAccountLabel;
    private Client client;
    private LoginWindow loginWindow;
    public RegisterWindow(Client client, LoginWindow loginWindow) {
        this.client = client;
        this.loginWindow = loginWindow;
        //prepearing logo 
        ImageIcon icon = new ImageIcon("logo.png");
        Image image = icon.getImage();

        this.setIconImage(image);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setTitle("Welcome to TimeWise Planner");
        this.setSize(1000, 600);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        //setting components manually 
        this.setLayout(null);

        brandPanel = new JPanel();
        brandPanel.setLayout(null);
        brandPanel.setBounds(0, 0, 400, 561);
        brandPanel.setBackground(new Color(131, 58, 180));
        brandPanel.setBorder(BorderFactory.createLineBorder((new Color(131, 58, 180)), 4));

        registrationPanel = new JPanel();
        registrationPanel.setBounds(400, 0, 584, 561);
        registrationPanel.setLayout(null);

        mottoLabel = new JLabel("Plan.Study.Improve.");
        mottoLabel.setFont(new Font("Tahoma", Font.BOLD, 33));
        mottoLabel.setBounds(30, 30, 400, 100);

        appLogoLabel = new JLabel();
        appLogoLabel.setIcon(icon);
        appLogoLabel.setBounds(70, 130, 250, 250);

        appNameLabel = new JLabel("Welcome to TimeWise!");
        appNameLabel.setFont(new Font("Tahoma", Font.BOLD, 33));
        appNameLabel.setBounds(10, 380, 400, 100);

        authorLabel = new JLabel("Java Project - Wojciech Mucha 2024");
        authorLabel.setBounds(90, 450, 400, 100);

        signUpLabel = new JLabel("SIGN UP");
        signUpLabel.setFont(new Font("Tahoma", Font.BOLD, 50));
        signUpLabel.setBounds(180, 47, 600, 60);

        loginLabel = new JLabel("Login");
        loginLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
        loginLabel.setBounds(80, 140, 50, 30);

        loginField = new JTextField();
        loginField.setFont(new Font("Tahoma", Font.PLAIN, 20));
        loginField.setBounds(80, 170, 430, 40);

        emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
        emailLabel.setBounds(80, 235, 50, 30);

        emailField = new JTextField();
        emailField.setFont(new Font("Tahoma", Font.PLAIN, 20));
        emailField.setBounds(80, 265, 430, 40);

        passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
        passwordLabel.setBounds(80, 335, 90, 40);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Tahoma", Font.PLAIN, 20));
        passwordField.setBounds(80, 370, 430, 40);

        signUpButton = new JButton("Sign Up");
        signUpButton.setFocusable(false);
        signUpButton.setBorder(null);
        signUpButton.setBackground(new Color(221, 42, 123));
        signUpButton.setForeground(Color.white);
        signUpButton.setBounds(80, 435, 80, 35);
        signUpButton.addActionListener(this);

        existingAccountLabel = new JLabel("I have an account");
        existingAccountLabel.setBounds(80, 480, 140, 35);
        existingAccountLabel.setFont(new Font("Tahoma", Font.BOLD, 15));

        loginButton = new JButton("Login");
        loginButton.setForeground(new Color(221, 42, 123));
        loginButton.setFocusable(false);
        loginButton.setBorder(BorderFactory.createEtchedBorder(new Color(221, 42, 123), new Color(221, 42, 123)));
        loginButton.setBackground(Color.white);
        loginButton.setBounds(220, 480, 80, 35);
        loginButton.addActionListener(this);

        //adding components to brandPanel (left panel)
        brandPanel.add(mottoLabel);
        brandPanel.add(appLogoLabel);
        brandPanel.add(appNameLabel);
        brandPanel.add(authorLabel);
        //adding components to registerPanel (right panel)
        registrationPanel.add(signUpLabel);
        registrationPanel.add(loginLabel);
        registrationPanel.add(emailLabel);
        registrationPanel.add(passwordLabel);
        registrationPanel.add(loginField);
        registrationPanel.add(emailField);
        registrationPanel.add(passwordField);
        registrationPanel.add(signUpButton);
        registrationPanel.add(existingAccountLabel);
        registrationPanel.add(loginButton);

        //adding panel to JDialog
        this.add(brandPanel);
        this.add(registrationPanel);
        this.setVisible(true);

        //setting up window listener 
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Register window is closing");
                client.sendDataToServer("END");
                loginWindow.dispose();
                dispose();
            }
        });
    }
    /**
     * getUserInput method that get's user input from register window textFields and sends data to server using client object
     */
    public void getUserInput() {
        String login = loginField.getText();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText();
        if (login.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter data to all fields");
        } else {
            client.sendDataToServer("register");
            client.sendDataToServer(login);
            client.sendDataToServer(password);
            client.sendDataToServer(email);
            loginField.setText(null);
            passwordField.setText(null);
            emailField.setText(null);
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        //handling buttons logic
        if (source == loginButton) {
            this.dispose();
            loginWindow.setVisible(true);
        }
        if (source == signUpButton) {
            getUserInput();
        }
    }
}
