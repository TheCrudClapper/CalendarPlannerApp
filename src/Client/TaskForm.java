package Client;

import Server.Task;
import Server.TaskCategory;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Wojciech Mucha
 */
/**
 * 
 * TaskForm class is used for adding and editing tasks, it handles input of data, validation and sending data to server
 */
public class TaskForm extends JDialog implements ActionListener {

        private JPanel mainPanel;
        private JTextField titleField, timeField;
        private JTextArea descriptionArea;
        private JDateChooser dateChooser;
        private JComboBox<TaskCategory> categoryComboBox;
        private CalendarMainWindow mainWindow;
        private JButton saveButton, cancelButton;
        private JRadioButton taskCompletedButton, taskPendingButton;
        private boolean isEditing, isTaskDone;
        private Client client;
        private String currentUser;
        private TaskList taskList;
        //variable to store currently editing task index
        int taskIndex;
        //variable to store currently editing task
        private Task taskToEdit;
        
        public TaskForm(Task taskToEdit, boolean isEditing, TaskList taskList, int taskIndex, CalendarMainWindow mainWindow) {
            this.mainWindow = mainWindow;
            this.client = mainWindow.getClient();
            this.currentUser = mainWindow.getCurrentUser();
            this.taskToEdit = taskToEdit;
            this.isEditing = isEditing;
            this.taskList = taskList;
            this.taskIndex = taskIndex;
            this.setLocationRelativeTo(mainWindow);
            this.setTitle(isEditing ? "Editing Task" : "Adding Task");
            this.setSize(400, 600);
            this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            //main panel for all components
            mainPanel = new JPanel();
            mainPanel.setLayout(new GridLayout(7, 2, 20, 10));

            mainPanel.add(new JLabel("Title:"));
            titleField = new JTextField();
            mainPanel.add(titleField);

            mainPanel.add(new JLabel("Description"));
            descriptionArea = new JTextArea();
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            mainPanel.add(descriptionScrollPane);

            mainPanel.add(new JLabel("Date: (yyyy-MM-dd)"));
            dateChooser = new JDateChooser();
            //user cannot set date older than today for task
            dateChooser.setMinSelectableDate(new Date());
            //setting how date will be formatted 
            dateChooser.setDateFormatString("yyyy-MM-dd");
            mainPanel.add(dateChooser);

            mainPanel.add(new JLabel("Time: (HH:mm)"));
            timeField = new JTextField();
            mainPanel.add(timeField);
            
            JPanel taskStatusButtonsPanel = new JPanel();
            taskStatusButtonsPanel.setLayout(new GridLayout(2,1));
            
            taskCompletedButton = new JRadioButton("Completed");
            taskCompletedButton.addActionListener(this);
            taskStatusButtonsPanel.add(taskCompletedButton);
            taskPendingButton = new JRadioButton("Pending");
            taskPendingButton.addActionListener(this);
            taskStatusButtonsPanel.add(taskPendingButton);
            
            mainPanel.add(new JLabel("Task Status:"));
            mainPanel.add(taskStatusButtonsPanel);
            
            ButtonGroup taskStatusButtonGroup = new ButtonGroup();
            taskStatusButtonGroup.add(taskCompletedButton);
            taskStatusButtonGroup.add(taskPendingButton);

            mainPanel.add(new JLabel("Category:"));
            categoryComboBox = new JComboBox<>(TaskCategory.values());
            mainPanel.add(categoryComboBox);

            saveButton = new JButton("Save");
            saveButton.addActionListener(this);
            mainPanel.add(saveButton);

            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(this);
            mainPanel.add(cancelButton);
            
            this.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    if(isEditing)
                    //when we exit the window of task edition using X we set buttons back to enabled
                    taskList.setButtonsEnabled();
                }
            });
            populateTaskForm();
            this.add(mainPanel);
        }
        /**
         * Method populateTaskForm is being used to populate form fields with currently editing task's data
         */
        public void populateTaskForm(){
            if(isEditing){
                titleField.setText(taskToEdit.getTaskTitle());
                descriptionArea.setText(taskToEdit.getTaskDescription());
                LocalDate taskLocalDate = taskToEdit.getTaskDate();
                String localDate = taskLocalDate+"";
                try {
                    Date taskDate = new SimpleDateFormat("yyyy-MM-dd").parse(localDate);
                    dateChooser.setDate(taskDate);
                } catch (ParseException ex) {
                    System.out.println("Error parsing date");
                }
                timeField.setText(taskToEdit.getTaskTime()+"");
                if(taskToEdit.isIsTaskDone()){
                    taskCompletedButton.setSelected(true);
                }else{
                    taskPendingButton.setSelected(isEditing);
                }
                categoryComboBox.setSelectedItem(taskToEdit.getTaskCategory());
            }
        }
        //getter
        public boolean isIsEditing() {
            return isEditing;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == saveButton) {
                if (isIsEditing()) {
                    if (validateUserInputs()) {
                        //gathering input from date chooser component
                        Date date = dateChooser.getDate();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String formattedDate = dateFormat.format(date);

                        LocalDate taskDate = LocalDate.parse(formattedDate);
                        LocalTime taskTime = LocalTime.parse(timeField.getText());
                        TaskCategory taskCategory = TaskCategory.valueOf(categoryComboBox.getSelectedItem().toString());
                       
                        //updating task data for local task list, after setting new data we send "taskToEdit" to method in TaskList that updates data in JList and local task list
                        taskToEdit.setTaskTitle(titleField.getText());
                        taskToEdit.setTaskDescription(descriptionArea.getText());
                        taskToEdit.setTaskDate(taskDate);
                        taskToEdit.setTaskTime(taskTime);
                        taskToEdit.setIsTaskDone(isTaskDone);
                        taskToEdit.setTaskCategory(taskCategory);
                        
                        //also sending data to change task data in server
                        client.sendDataToServer("edit_task");
                        client.sendDataToServer(currentUser);
                        client.sendDataToServer(taskIndex + "");
                        client.sendDataToServer(titleField.getText());
                        client.sendDataToServer(descriptionArea.getText());
                        client.sendDataToServer(taskDate + "");
                        client.sendDataToServer(taskTime + "");
                        client.sendDataToServer(isTaskDone + "");
                        client.sendDataToServer(taskCategory + "");

                        //updating tasks in list
                        taskList.setButtonsEnabled();
                        taskList.updateTask(taskIndex, taskToEdit);
                        this.dispose();
                    }

                } else {
                    // if flag is editing is false it means that we are adding new task
                    //this portion of code is responsible for adding task both to server list and for local list of tasks for specific user
                    if (validateUserInputs()) {
                        //here i get date from JDateChooser and change it to better format
                        Date date = dateChooser.getDate();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String formattedDate = dateFormat.format(date);
                        
                        LocalDate taskDate = LocalDate.parse(formattedDate);
                        LocalTime taskTime = LocalTime.parse(timeField.getText());
                        TaskCategory taskCategory = TaskCategory.valueOf(categoryComboBox.getSelectedItem().toString());
                        //formatting or casting more data from fileds has no point here because we sent them as strings anyway
                        client.sendDataToServer("add_task");
                        //sending sending task to currently logged user
                        client.sendDataToServer(currentUser);
                        client.sendDataToServer(titleField.getText());
                        client.sendDataToServer(descriptionArea.getText());
                        client.sendDataToServer(taskDate+"");
                        client.sendDataToServer(taskTime+"");
                        client.sendDataToServer(isTaskDone+"");
                        client.sendDataToServer(taskCategory+"");
                        //but for local data i need to cast them here
                        

                        Task taskToAddLocally = new Task(titleField.getText(), descriptionArea.getText(), taskDate, taskTime, isTaskDone, taskCategory);
                        client.addTaskLocally(taskToAddLocally);
                        //after sending data we got rid of window
                        this.dispose();
                    }
                }
            }
            if (source == cancelButton) {
                //cancel button disposes window
                if(isEditing){
                   taskList.setButtonsEnabled();
                }
                this.dispose();
            }
            if (source == taskCompletedButton){
                isTaskDone = true;
            }
            if (source == taskPendingButton){
                isTaskDone = false;
            }
        }

        /**
         * Method validateUserInputs() is being used to validate user inputs in task form
         */
        private boolean validateUserInputs() {
            String taskTitle = titleField.getText().trim();
            String taskDescription = descriptionArea.getText().trim();
            Date taskDate = dateChooser.getDate();
            String taskTime = timeField.getText().trim();
            TaskCategory taskCategory = (TaskCategory) categoryComboBox.getSelectedItem();
            //using if statements for each field to determine if there are any problems
            //this method works with two auxiliary methods (isValidTimeFormat && showError)to judge user's input and let user know about eventual mistakes
            if (taskTitle.isEmpty()) {
                showError("Title cannot be empty !");
                return false;
            }
            if (taskDescription.isEmpty()) {
                showError("Task description cannot be empty !");
                return false;
            }
            if (taskDate == null) {
                showError("Date cannot be empty !");
                return false;
            }
            if (taskTime.isEmpty()) {
                showError("Time cannot be empty");
                return false;
            }
            if (taskCategory == null) {
                showError("Task category must be selected");
                return false;
            }
            if (!taskCompletedButton.isSelected() && !taskPendingButton.isSelected()){
                showError("Task status cannot be left unselected !");
                return false;
            }
            if (!isValidTimeFormat(taskTime)) {
                showError("Invalid time format. Please use HH:mm format !");
                return false;
            }
            //if everything is alright return true, so we can send tasks data to server
            return true;
        }//method to help determine whether inserted time format is right
        private boolean isValidTimeFormat(String taskTime) {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            try {
                LocalTime.parse(taskTime, timeFormatter);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        }

        /**
         * method used for showing errors if occured
         * @param message message that will be displayed
         */
        private void showError(String message) {
            //showing error message when specific error occured
            JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
