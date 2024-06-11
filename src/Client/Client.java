package Client;

import Server.Task;
import Server.TaskCategory;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Wojciech Mucha
 */

/**
 * Class Client is a class-bridge between client and server connection, it
 * handles sending commands to server.
 */
public class Client {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private LoginWindow loginWindow;
    private String currentUser;
    private CalendarMainWindow calendarMainWindow;
    /**
     * @param localTaskList is a list where tasks are stored locally, each user
     * has it's own localTaskList
     */
    private ArrayList<Task> localTaskList;

    public Client() {
        int serverPort = 12345;
        String serverAddress = "127.0.0.1"; // Address of the server

        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            localTaskList = new ArrayList<>();
            //starting the read thread to handle server responses
            new ClientReadThread(in, this).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to connect to the server.");
            System.exit(0);
        }

        //initializing and showing login window
        loginWindow = new LoginWindow(this);
        loginWindow.setVisible(true);
    }

    /**
     *
     * @param data is a string message that will be sent to server
     */
    public void sendDataToServer(String data) {
        out.println(data);
    }

    /**
     * Method logging user in and displays main window of app
     */
    public void displayMainWindow() {
        try {
            currentUser = in.readLine();
            if (currentUser != null) {
                loginWindow.setVisible(false);
            }
            calendarMainWindow = new CalendarMainWindow(this, currentUser);
            System.out.println("User " + currentUser + " logged in");
            requestClientTaskList(currentUser);
        } catch (IOException ex) {
            System.out.println("Something went wrong wiht reading user's login");
        }

    }

    /**
     * @param task represents task that will be added
     */
    public void addTaskLocally(Task task) {
        localTaskList.add(task);
    }

    /**
     *
     * @param currentUser user currently logged in, for him we are fetching
     * arraylist
     */
    public void requestClientTaskList(String currentUser) {
        sendDataToServer("request_user_tasklist");
        sendDataToServer(currentUser);
    }

    public ArrayList<Task> getLocalTaskList() {
        return localTaskList;
    }

    public void setLocalTaskList(ArrayList<Task> listToSet) {
        this.localTaskList = listToSet;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public Socket getSocket() {
        return socket;
    }

    /**
     * @param date localdate for which tasks are required
     * @return a list of tasks for the specified date
     */
    public ArrayList<Task> getTasksForDate(LocalDate date) {
        ArrayList<Task> tasksForDate = new ArrayList<>();
        // i iterate thourght clients local list
        for (Task task : localTaskList) {
            //and when we find right date we are adding tasks to array
            if (task.getTaskDate().equals(date)) {
                tasksForDate.add(task);
            }
        }
        return tasksForDate;
    }

    //main method
    public static void main(String[] args) {
        new Client();
    }
}

/**
 * Class ClientReadThread is responsible for recieving messaging from server
 */
class ClientReadThread extends Thread {

    private BufferedReader in;
    private Client client;
    /**
     * @param inObj object used for deserialization of tasks send from server
     */
    private ObjectInputStream inObj;

    public ClientReadThread(BufferedReader in, Client client) {
        this.in = in;
        this.client = client;
        try {
            this.inObj = new ObjectInputStream(client.getSocket().getInputStream());
        } catch (IOException ex) {
            System.out.println("Something went wrong with initializing object input stream");
        }
    }
    /**
     * Method populateLocalTaskList() is responsible for recieving serializated object and deserialize then,
     * adds task to local user's task list
     */
    public void populateLocalTaskList() {
        try {
            Task task;
            ArrayList<Task> tasks = new ArrayList<>();
            //when we recieve null it means that sending tasks is done
//            while ((task = (Task) inObj.readObject()) != null) {
//                tasks.add(task);
//            }
            while(true){
                task = (Task) inObj.readObject();
                if(task == null){
                    break;
                }
                tasks.add(task);
            }
            client.setLocalTaskList(tasks);
        } catch (IOException ex) {
            System.out.println("An error occurred while reading tasks from the server.");
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to retrieve tasks from the server.");
        } catch (ClassNotFoundException ex) {
            System.out.println("The Task class was not found.");
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Data format error: Task class not found. Import task class !");
        }
    }

    @Override
    /**
     * Responsible for recieving messages from server and reacting to them
     */
    public void run() {
        try {
            /**
             * @param messageFromServer is a protocol message from server it
             * tells client what have happend on the server
             */
            String messageFromServer;
            while (true) {
                messageFromServer = in.readLine();
                if (messageFromServer == null) {
                    break;
                }
                switch (messageFromServer) {
                    case "login_success":
                        client.displayMainWindow();
                        break;
                    case "sending_task_list":
                        populateLocalTaskList();
                        System.out.println("Task list recived");
                        break;
                    case "login_failed":
                        System.out.println("Login failed. Please try again.");
                        JOptionPane.showMessageDialog(null, "Login failed. Please try again.");
                        break;
                    case "user_account_exist":
                        System.out.println("This username already exist, try again");
                        JOptionPane.showMessageDialog(null, "This username already exist, try again");
                        break;
                    case "register_success":
                        System.out.println("Succesfully added user to database");
                        JOptionPane.showMessageDialog(null, "User added to database, now log in");
                        break;
                    case "task_added_succesfully":
                        System.out.println("Succesfully added new task !");
                        JOptionPane.showMessageDialog(null, "Succesfully added new task");
                        break;
                    case "task_edited_successfully":
                        System.out.println("Succesfully edited task !");
                        JOptionPane.showMessageDialog(null, "Succesfully edited task");
                        break;
                    case "task_removed_successfully":
                        System.out.println("Succesfully removed task !");
                        JOptionPane.showMessageDialog(null, "Succesfully removed task");
                        break;
                    case "user_tasklist_empty":
                        System.out.println("User's server task list is empty, nothing to download :-(");
                        break;
                    default:
                        System.out.println("Unknown command from server");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
