package Client;

import Server.Task;
import Client.TaskList;
import javax.swing.*;
import com.toedter.calendar.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.beans.*;
import java.beans.PropertyChangeListener;
import java.time.*;
import java.time.format.DateTimeFormatter;
import javax.swing.border.TitledBorder;
/**
 * 
 * @author Wojciech Mucha
 */
/**
 * 
 * Class CalendarMainWindow handles displaying of user GUI
 */
public class CalendarMainWindow extends JFrame implements ActionListener {

    private JPanel topPanel, calendarPanel, eventInpectorPanel;
    private JLabel welcomingMessage, clockLabel;
    private JButton clearButton;
    private JCalendar calendar;
    private JTextArea eventArea;
    private Clock clock;
    private Client client;
    private String currentUser;
    private JMenuItem addTask, manageTasks, author;
    private TaskForm taskForm;

    public CalendarMainWindow(Client client, String login) {
        //reference to client class and current logged user
        this.client = client;
        this.currentUser = login;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000, 600);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        this.setTitle("Time Wise");
        this.setLocationRelativeTo(null);
        ImageIcon icon = new ImageIcon("logo.png");
        Image image = icon.getImage();
        this.setIconImage(image);

        //program menu
        JMenuBar mainBar = new JMenuBar();

        //events menu let user add events
        JMenu eventMenu = new JMenu("Events");
        addTask = new JMenuItem("Add task");
        addTask.addActionListener(this);
        manageTasks = new JMenuItem("Manage tasks");
        manageTasks.addActionListener(this);
        eventMenu.add(addTask);
        eventMenu.add(manageTasks);
        mainBar.add(eventMenu);

        //author menu displays author of app
        JMenu aboutMenu = new JMenu("About");
        author = new JMenuItem("Author");
        author.addActionListener(this);
        aboutMenu.add(author);
        mainBar.add(aboutMenu);

        //setting up threee panels for widow look one top and two one on left, second on right
        topPanel = new JPanel();
        topPanel.setLayout(null);
        topPanel.setPreferredSize(new Dimension(1000, 50));

        welcomingMessage = new JLabel("Hi " + login);
        welcomingMessage.setFont(new Font("Tahoma", Font.BOLD, 25));
        welcomingMessage.setBounds(20, 0, 400, 50);

        clockLabel = new JLabel();
        clockLabel.setBounds(850, 0, 120, 50);
        clockLabel.setFont(new Font("Tahoma", Font.BOLD, 25));
        clock = new Clock();
        clock.start();
        topPanel.add(welcomingMessage);
        topPanel.add(clockLabel);

        calendarPanel = new JPanel();
        calendarPanel.setPreferredSize(new Dimension(600, 550));

        calendar = new JCalendar();
        calendar.setBorder(BorderFactory.createLineBorder(Color.gray));
        calendar.setPreferredSize(new Dimension(542, 430));
        calendarPanel.add(calendar);
        
        //panel responsible for displaying event inspector
        eventInpectorPanel = new JPanel();
        eventInpectorPanel.setPreferredSize(new Dimension(400, 550));
        eventArea = new JTextArea(10, 10);
        eventArea.setFont(new Font("Tahoma", Font.PLAIN, 15));
        eventArea.setLineWrap(true);
        eventArea.setWrapStyleWord(true);
        JScrollPane eventAreaScroll = new JScrollPane(eventArea);
        eventAreaScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Event Inspector", TitledBorder.LEFT, TitledBorder.TOP, new Font("Tahoma", Font.PLAIN, 20)));
        eventAreaScroll.setPreferredSize(new Dimension(360, 400));
        eventInpectorPanel.add(eventAreaScroll);
        clearButton = new JButton("Clear");
        clearButton.addActionListener(this);
        eventInpectorPanel.add(clearButton);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(calendarPanel, BorderLayout.CENTER);
        this.add(eventInpectorPanel, BorderLayout.EAST);

        //setting up JMenuBar
        setJMenuBar(mainBar);
        //adding window adapter when closing user logs out
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("TimeWise is closing");
                client.sendDataToServer("END");
                dispose();
            }
        });
        //adding property listener to listen for click on calendar,
        //when user click specific date task for that date will appear on event inspector
        calendar.getDayChooser().addPropertyChangeListener("day", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //getting choosen date from calendar
                Date selectedDate = calendar.getDate();
                LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                // fetching tasks for date
                ArrayList<Task> tasksForSelectedDate = client.getTasksForDate(localDate);
                // displaying tasks in text area
                displayTasksInTextArea(tasksForSelectedDate);
            }
        });
       
        this.setVisible(true);
    }
    /**
     * Method displayTasksInTextArea is responsible for displaying tasks for specified date in event inspector
     * @param tasks array list responsible for holding task for specified date
     */
    private void displayTasksInTextArea(ArrayList<Task> tasks) {
        // cleaning the ext area before displaying task from date
        eventArea.setText("");
        // displaying tasks in text area
        for (Task task : tasks) {
            eventArea.append(task.displayTask() + "\n");
        }
    }
    //getters setters
    public Client getClient() {
        return client;
    }

    public String getCurrentUser() {
        return currentUser;
    }
    public void setManageTaskEnabled(){
        manageTasks.setEnabled(true);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == clearButton) {
            eventArea.setText(null);
        }
        if (source == addTask) {
            taskForm = new TaskForm(null, false, null, -1, this);
            taskForm.setVisible(true);
        }
        if (source == manageTasks) {
            TaskList taskList = new TaskList(this);
            manageTasks.setEnabled(false);
        }
        if (source == author) {
            JOptionPane.showMessageDialog(null, "Wojciech Mucha");
        }
    }

    /**
     * Inner Clock class responsible for displaying clock in user gui
     */
    class Clock extends Thread {

        public void run() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            while (true) {
                try {
                    LocalTime currentTime = LocalTime.now();
                    String timeString = currentTime.format(formatter);
                    clockLabel.setText(timeString);
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    System.out.println("Something interrupted clock thread");
                }
            }
        }
    }
}
