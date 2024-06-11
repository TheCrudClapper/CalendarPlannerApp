package Server;
import java.util.ArrayList;

/**
 *
 * @author Wojciech Mucha
 */

/**
 * 
 * User Class represents a user, each user has it's own task list
 */
public class User {
    private String userLogin;
    private String userPassword;
    private String userEmail;
    private ArrayList<Task> taskList; 
    //constructor
    public User(String userLogin, String userPassword, String userEmail) {
        this.userLogin = userLogin;
        this.userPassword = userPassword;
        this.userEmail = userEmail;
        //when user is created we instiantiate his own server list of tasks
        taskList = new ArrayList<>();
    }
    //getters and setters
    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public ArrayList<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }
    public void addTaskToUser(Task taskToAdd){
        taskList.add(taskToAdd);
    }
    public void removeTaskFromUser(Task taskToRemove){
        taskList.remove(taskToRemove);
    }

}
