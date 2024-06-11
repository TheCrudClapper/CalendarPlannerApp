package Server;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 *
 * @author Wojciech Mucha
 *
 */
/**
 *
 * ClientHandler is a class that handles logic on server side, handles login
 * verification, registration, addition of tasks, edition, deletion, working
 * with files, storing container with users, serving a large amount of users at
 * the same time.
 */
public class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    //streeam for writing objects
    private ObjectOutputStream objOut;
    //private static List<User> users = Collections.synchronizedList(new ArrayList<>());
    private static ArrayList<User> users = new ArrayList<>();

    public ClientHandler(Socket socket) {
        System.out.println("Constructor of ClientHandler thread");
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            objOut = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.out.println("Something went wrong with prepearing stream for operations");
        }
    }

    /**
     * Method saveUsersToFile is responsible for saving all users that are in
     * server to file
     */
    public static void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt"))) {
            //synchronizing saving to file, only one thread at time can save server data to file to prevent file corruption
            synchronized (users) {
                for (User user : users) {
                    writer.write(user.getUserLogin() + "," + user.getUserPassword() + "," + user.getUserEmail() + "\n");
                    for (Task task : user.getTaskList()) {
                        writer.write(user.getUserLogin() + ","
                                + task.getTaskTitle() + ","
                                + task.getTaskDescription() + ","
                                + task.getTaskDate() + ","
                                + task.getTaskTime() + ","
                                + task.isIsTaskDone() + ","
                                + task.getTaskCategory() + "\n");
                    }

                }
            }
            System.out.println("Saved client's data to file users.txt");
        } catch (IOException e) {
            System.err.println("Error saving users to file: " + e.getMessage());
        }
    }

    /**
     * Method loadUsersFromFile() is responsible for loading all users and their
     * tasks to server list, it also check if file "users.txt" is created, if
     * not it creates one.
     */
    //method that loads users from file
    //if file doesn't exist we create it anyway
    public static void loadUsersFromFile() {
        File userFile = new File("users.txt");
        if (userFile.exists() && userFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                User currentUser = null;
                while ((line = reader.readLine()) != null) {
                    String[] userData = line.split(",");
                    if (currentUser == null || !userData[0].equals(currentUser.getUserLogin())) {
                        //Creating new user if we encounter new login
                        currentUser = new User(userData[0], userData[1], userData[2]);
                        users.add(currentUser);
                    } else if (userData.length >= 6) {
                        LocalDate taskDate = LocalDate.parse(userData[3]);
                        LocalTime taskTime = LocalTime.parse(userData[4]);
                        boolean isTaskDone = Boolean.parseBoolean(userData[5]);
                        TaskCategory taskCategory = TaskCategory.valueOf(userData[6]);
                        //After parsing we are adding task to current user
                        currentUser.addTaskToUser(new Task(userData[1], userData[2], taskDate, taskTime, isTaskDone, taskCategory));
                    }
                }
                System.out.println("All client's data succesfully loaded from file !");
            } catch (IOException e) {
                System.err.println("Error loading users from file, restart server and file users.txt will be created automatically");
            }

        } else {
            try {
                userFile.createNewFile();
                System.out.println("Created new users.txt file");
            } catch (IOException ex) {
                System.out.println("Cannot create file users.txt");
            }
        }

    }

    /**
     * Method loginVerification() is responsible for recieving data from client
     * related to logging in, checks if user login and password are correct if
     * yes, sends login to client's app to allow him use of the application
     */
    private void loginVerification() {
        try {
            String login = in.readLine();
            String password = in.readLine();
            boolean userFound = false;
            synchronized (users) {
                for (User item : users) {
                    if (login.equals(item.getUserLogin()) && password.equals(item.getUserPassword())) {
                        System.out.println("User " + login + " logged in");
                        out.println("login_success");
                        out.println(item.getUserLogin());
                        userFound = true;
                        break;
                    }
                }
            }
            if (!userFound) {
                out.println("login_failed");
                System.out.println("User " + login + " is not in database");
            }
        } catch (IOException ex) {
            System.out.println("Something went wrong with login verification");
        }
    }

    /**
     * Method register() responsible for recieving user data related to
     * registration process, checks wheter user account is already in database,
     * if not adds user.
     */
    private void register() {
        try {
            String login = in.readLine();
            String password = in.readLine();
            String email = in.readLine();
            boolean userExists = false;
            synchronized (users) {
                for (User item : users) {
                    if (login.equals(item.getUserLogin())) {
                        System.out.println("User is already in database");
                        out.println("user_account_exist");
                        userExists = true;
                        break;
                    }
                }
                if (!userExists) {
                    User newUser = new User(login, password, email);
                    users.add(newUser);
                    out.println("register_success");
                    System.out.println("User " + login + " was added to database");
                }
            }

        } catch (IOException ex) {
            System.out.println("Something went wrong with registration");
        }
    }

    /**
     * Method sendUserTaskList(String currentUser) is responsible for sending
     * user's task located on server to localTask list located in Client class.
     * First we are seraching for right User, then we send tasks one by one.
     */
    private void sendUserTaskList(String currentUser) {
        try {
            User user = null;
            for (User item : users) {
                if (item.getUserLogin().equals(currentUser)) {
                    user = item;
                    break;
                }
            }
            if (user.getTaskList().isEmpty()) {
                out.println("user_tasklist_empty");
            } else {
                out.println("sending_task_list");
                for (Task task : user.getTaskList()) {
                    objOut.writeObject(task);
                }
                objOut.writeObject(null);
                objOut.flush();
            }
        } catch (IOException e) {
            System.out.println("Something went wrong with request of user task list");
        }
    }

    /**
     * Method addTask() responsible for adding recieving user task data, parsing
     * data and adding task to user's server tasks list
     */
    private void addTask() {
        try {
            String currentUser = in.readLine();
            String taskTitle = in.readLine();
            String taskDesciption = in.readLine();
            String taskDate = in.readLine();
            String taskTime = in.readLine();
            String isTaskDoneString = in.readLine();
            String taskCategoryString = in.readLine();

            LocalDate localDate = LocalDate.parse(taskDate);
            LocalTime localTime = LocalTime.parse(taskTime);

            boolean isTaskDone = Boolean.parseBoolean(isTaskDoneString);

            TaskCategory taskCategory = TaskCategory.valueOf(taskCategoryString);
            synchronized (users) {
                for (User item : users) {
                    if (currentUser.equals(item.getUserLogin())) {
                        item.addTaskToUser(new Task(taskTitle, taskDesciption, localDate, localTime, isTaskDone, taskCategory));
                        System.out.println("Succesfully added task to user " + currentUser);
                        out.println("task_added_succesfully");
                        return;
                    }
                }
                System.out.println("User not found (add method)");
            }
        } catch (IOException ex) {
            System.out.println("Something went wrong with adding task");
        }
    }

    /**
     * Method editTask() is responsible for editing user task data, parsing data
     * and updating selected user task with new data
     */
    private void editTask() {
        try {
            String currentUser = in.readLine();
            String taskIndex = in.readLine();
            String taskTitle = in.readLine();
            String taskDesciption = in.readLine();
            String taskDate = in.readLine();
            String taskTime = in.readLine();
            String isTaskDoneString = in.readLine();
            String taskCategoryString = in.readLine();

            int taskToEditIndex = Integer.parseInt(taskIndex);
            LocalDate localDate = LocalDate.parse(taskDate);
            LocalTime localTime = LocalTime.parse(taskTime);

            boolean isTaskDone = Boolean.parseBoolean(isTaskDoneString);

            TaskCategory taskCategory = TaskCategory.valueOf(taskCategoryString);
            synchronized (users) {
                for (User item : users) {
                    if (currentUser.equals(item.getUserLogin())) {
                        //reference to user's localtasklist
                        List<Task> userTasks = item.getTaskList();
                        //if index to edit is valid (if fits in local user's task list)
                        if (taskToEditIndex >= 0 && taskToEditIndex < userTasks.size()) {
                            Task taskToEdit = userTasks.get(taskToEditIndex);
                            taskToEdit.setTaskTitle(taskTitle);
                            taskToEdit.setTaskDescription(taskDesciption);
                            taskToEdit.setTaskDate(localDate);
                            taskToEdit.setTaskTime(localTime);
                            taskToEdit.setIsTaskDone(isTaskDone);
                            taskToEdit.setTaskCategory(taskCategory);
                            System.out.println("Task for " + currentUser + " updated successfully");
                            out.println("task_edited_successfully");
                        } else {
                            System.out.println("Invalid task index");
                        }
                        return;
                    }
                }
                System.out.println("User not found (edit method)");
            }

        } catch (IOException ex) {
            System.out.println("Something went wrong with editing task");
        }
    }

    /**
     * Method removeTask() is responsible for removing user's task via index
     */
    private void removeTask() {
        try {
            String currentUser = in.readLine();
            String taskIndexString = in.readLine();
            int taskToRemoveIndex = Integer.parseInt(taskIndexString);
            synchronized (users) {
                for (User user : users) {
                    if (user.getUserLogin().equals(currentUser)) {
                        //reference to user tasklist
                        List<Task> userTasks = user.getTaskList();
                        if (taskToRemoveIndex >= 0 && taskToRemoveIndex < userTasks.size()) {
                            userTasks.remove(taskToRemoveIndex);
                            System.out.println("Task removed successfully for user: " + currentUser);
                            out.println("task_removed_successfully");
                        } else {
                            System.out.println("Invalid task index for user: " + currentUser);
                        }
                        return;
                    }
                }
                System.out.println("User not found: " + currentUser);
            }

        } catch (IOException ex) {
            System.out.println("Error handling task removal: " + ex.getMessage());
        }
    }

    public void run() {
        try {
            /**
             * @param messageFromClient is a protocol message from client it
             * tells server what client whants to do
             */
            String messageFromClient;
            while (true) {
                messageFromClient = in.readLine();
                //if messageFromClient recieved from client is null or end we exit the loop and end client handler work
                if (messageFromClient == null || messageFromClient.equals("END")) {
                    break;
                }
                switch (messageFromClient) {
                    case "login":
                        loginVerification();
                        break;
                    case "register":
                        register();
                        break;
                    case "add_task":
                        addTask();
                        break;
                    case "remove_task":
                        removeTask();
                        break;
                    case "edit_task":
                        editTask();
                        break;
                    case "request_user_tasklist":
                        String currentUser = in.readLine();
                        sendUserTaskList(currentUser);
                        break;
                    default:
                        System.out.println("Unknown command from client");
                        break;
                }
            }
            System.out.println("Thread finished its work, client " + socket.getInetAddress() + " disconnected");
            //saving all users to file each time client closes app
            ClientHandler.saveUsersToFile();
            in.close();
            out.close();
            socket.close();
            objOut.close();
        } catch (IOException e) {
            System.out.println("Error at communication loop");
        }
    }
}
