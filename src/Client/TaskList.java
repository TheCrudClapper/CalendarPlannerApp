/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import Client.CalendarMainWindow;
import Client.TaskForm;
import Server.Task;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Wojciech Mucha
 */
/**
 * TaskList class is responsible for displaying all local user task in jlist
 * component it also provides deleteTask and updateTask method that handles
 * editing and removing task both for default list model and local task list
 */
public class TaskList extends JDialog implements ListSelectionListener, ActionListener {

    //buttons responsible for editing and removing data from JList and task local array list
    private JButton editButton, deleteButton;
    //list model that will hold data for JList
    private DefaultListModel<Task> listModel = new DefaultListModel<>();
    private JList list;
    //field that holds actually selected task index
    private int selectedIndex = -1;
    private CalendarMainWindow mainWindow;
    private Client client;
    private String currentUser;

    public TaskList(CalendarMainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.client = mainWindow.getClient();
        this.currentUser = mainWindow.getCurrentUser();
        this.setTitle("Tasks List");
        this.setSize(400, 400);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(mainWindow);
        //adding data model to j list
        list = new JList(listModel);
        list.addListSelectionListener(this);
        populateListWithData();

        JScrollPane listScrollPane = new JScrollPane(list);
        listScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(Color.gray, Color.black), currentUser + " tasks list"));
        editButton = new JButton("Edit");
        editButton.addActionListener(this);

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(this);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);

        this.add(listScrollPane, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.SOUTH);
        
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                //after closing task list we once again enable that option
                mainWindow.setManageTaskEnabled();
            }
        });
        
        this.setVisible(true);
    }

    /**
     *
     * @param index is index of task we want to change in user's local task list
     * and listModel
     * @param taskToUpdate is task that will overwrite old task
     */
    public void updateTask(int index, Task taskToUpdate) {
        if (index >= 0 && index < client.getLocalTaskList().size()) {
            client.getLocalTaskList().set(index, taskToUpdate);
            listModel.set(index, taskToUpdate);
        } else {
            System.err.println("Invalid index: " + index);
        }
    }

    /**
     *
     * @param index is index of a task we want to remove from user's local task
     * list and listModel; This method also sends protocol message to server for
     * task removal
     */
    public void deleteTask(int index) {
        if (index >= 0 && index < client.getLocalTaskList().size()) {
            client.getLocalTaskList().remove(index);
            //client.removeTask(index);
            listModel.remove(index);
            //removing task on server too
            client.sendDataToServer("remove_task");
            client.sendDataToServer(currentUser);
            client.sendDataToServer(index + "");
        } else {
            System.err.println("Invalid index: " + index);
        }
    }

    /**
     * Method that populates list's model with user's local task list data
     */
    public void populateListWithData() {
        for (Task item : client.getLocalTaskList()) {
            //adding each item from local user list to list model
            listModel.addElement(item);
        }
    }

    /**
     * Method that updates actually selected list item index, when user
     * interacts with list
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        selectedIndex = list.getSelectedIndex();
    }
    /**
     * Method that sets buttons enabled
     */
    public void setButtonsEnabled(){
        deleteButton.setEnabled(true);
        editButton.setEnabled(true);
    }
   
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == editButton && selectedIndex != -1) {
            Task selectedTask = client.getLocalTaskList().get(selectedIndex);
            TaskForm taskEditingForm = new TaskForm(selectedTask, true, this, selectedIndex, mainWindow);
            taskEditingForm.setVisible(true);
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
        if (source == deleteButton && selectedIndex != -1) {
            int confirmation = JOptionPane.showConfirmDialog(this, "Do you really want to delete this task?", "Delete Task", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                deleteTask(selectedIndex);
            }
        }
    }
}
