package Server;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Wojciech Mucha
 */
/**
 * 
 * Task - class representing each task user can create 
 */
//implements serializable will be sent thourght socket
public class Task implements Serializable {

    private String taskTitle;
    private String taskDescription;
    private LocalDate taskDate;
    private LocalTime taskTime;
    private boolean isTaskDone;
    private TaskCategory taskCategory;
    //constructor of Task Class
    public Task(String taskTitle, String taskDescription, LocalDate taskDate, LocalTime taskTime, boolean isTaskDone, TaskCategory taskCategory) {
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.taskDate = taskDate;
        this.taskTime = taskTime;
        this.isTaskDone = isTaskDone;
        this.taskCategory = taskCategory;
    }
    //getters and setters
    public String getTaskTitle() {
        return taskTitle;
    }

    public LocalDate getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(LocalDate taskDate) {
        this.taskDate = taskDate;
    }

    public LocalTime getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(LocalTime taskTime) {
        this.taskTime = taskTime;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public boolean isIsTaskDone() {
        return isTaskDone;
    }

    public void setIsTaskDone(boolean isTaskDone) {
        this.isTaskDone = isTaskDone;
    }

    public TaskCategory getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(TaskCategory taskCategory) {
        this.taskCategory = taskCategory;
    }
    /**
     * 
     * @return returns String depending on boolean isTaskDone in more attractive manner
     */
    public String taskStatus(){
        String status = isTaskDone ? "Completed" : "Pending";
        return status;
    }
    //method for displaying tasks in event inspector
    public String displayTask() {
        return "Title: " + taskTitle + "\nDescription: " + taskDescription + "\nDate: " + taskDate + "\nTime: " + taskTime + "\nStatus: " + taskStatus() + "\nCategory: " + taskCategory + "\n";
    }
    //toString is useful with Jlist view of a task list
    @Override
    public String toString() {
        return taskTitle + " " + taskDate + " " + taskTime + " " + taskStatus() + " " + taskCategory;
    }

}
