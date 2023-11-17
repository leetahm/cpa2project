package com.example.project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TaskManagerController {

    private String username;
    @FXML
    private Button logout;
    @FXML
    private Button addTask;
    @FXML
    private Button finishTask;
//    @FXML
//    private TableView inProgressTasks;
//    @FXML
//    private TableView finishedTasks;
    @FXML
    private Button editTasks;
    @FXML
    private ListView<Task> inProgressTasksView;
    @FXML
    private ListView<Task> completedTasksView;
    @FXML
    private ListView<Task> priorityTasksView;
    @FXML
    private ListView<Map.Entry<String, Task>> allTasksView;

    @FXML
    private TextField taskNameField;
    @FXML
    private TextField dueDateField;
    @FXML
    private TextField priorityField;
    @FXML
    private TextField assignedPersonField;

    private Queue<Task> inProgressTasks = new LinkedList<>();
    private Stack<Task> completedTasks = new Stack<>();
    private PriorityQueue<Task> priorityTasks = new PriorityQueue<>(Comparator.comparingInt(Task::getPriority));
    private Map<String, Task> allTasks = new HashMap<>();

    private ObservableList<Task> inProgressTasksObservable = FXCollections.observableArrayList();
    private ObservableList<Task> completedTasksObservable = FXCollections.observableArrayList();
    private ObservableList<Task> priorityTasksObservable = FXCollections.observableArrayList();
    private ObservableList<Map.Entry<String, Task>> allTasksObservable = FXCollections.observableArrayList();

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    public void initialize() {
        inProgressTasksView.setItems(inProgressTasksObservable);
        completedTasksView.setItems(completedTasksObservable);
        priorityTasksView.setItems(priorityTasksObservable);
        allTasksView.setItems(allTasksObservable);
    }


    @FXML
    private void handleLogout()
    {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login_screen.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logout.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO add methods for task operations such as: add, completed tasks, prioritize tasks


    @FXML
    private void handleAddTaskButtonAction() {
        try {
            String id = UUID.randomUUID().toString();
            String name = taskNameField.getText();
            String dueDate = dueDateField.getText(); // might have to parse date
            int priority = Integer.parseInt(priorityField.getText());
            String assignedPerson = assignedPersonField.getText();

            Task newTask = new Task(id, name, priority, assignedPerson);
            addTask(newTask, true);

            // will clear the text fields after adding the task
            taskNameField.clear();
            dueDateField.clear();
            priorityField.clear();
            assignedPersonField.clear();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFinishTaskButtonAction() {
        Task selectedTask = inProgressTasksView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            completeTask(selectedTask);
        } else {

        }
    }

    @FXML
    private void handleEditTasksButtonAction() {
        Task selectedTask = allTasksView.getSelectionModel().getSelectedItem().getValue();
        if (selectedTask != null) {
            // TODO
            taskNameField.setText(selectedTask.getDescription());
        } else {

        }
    }

    public void addTask(Task task, boolean writeToDisk) {
        if (task.getPriority() > 0) {
            priorityTasks.add(task);
            priorityTasksObservable.add(task);
        } else {
            inProgressTasks.add(task);
            inProgressTasksObservable.add(task);
        }
        allTasks.put(task.getId(), task);
        Map.Entry<String, Task> entry = new AbstractMap.SimpleEntry<>(task.getId(), task);
        allTasksObservable.add(entry);

        // will write to the txt file
        if (writeToDisk) {
            // Write to the txt file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.username + ".txt", true))) {
                String taskData = convertTaskToString(task);
                writer.newLine();  // Start a new line for the task
                writer.write(taskData);
            } catch (IOException e) {
                e.printStackTrace();  // Log or handle the error appropriately
            }
        }
    }

    // will convert task to string for the txt file
    private String convertTaskToString(Task task) {
        return task.getId() + "," + task.getDescription() + "," + task.getPriority() + "," + task.getAssignedPerson();
    }

    // method to complete a task
    public void completeTask(Task task) {
        inProgressTasks.remove(task);
        inProgressTasksObservable.remove(task);

        completedTasks.push(task);
        completedTasksObservable.add(0, task); // add at the top of the stack

        if (task.getPriority() > 0) {
            priorityTasks.remove(task);
            priorityTasksObservable.remove(task);
        }
    }

    // method to assign priority to a task
    public void assignPriorityToTask(String taskId, int priority) {
        Task task = allTasks.get(taskId);
        if (task != null) {
            task.setPriority(priority);

            inProgressTasks.remove(task);
            inProgressTasksObservable.remove(task);

            priorityTasks.add(task);
            priorityTasksObservable.add(task);
        }
    }

    // Method to remove a task
    public void removeTask(String taskId) {
        Task task = allTasks.remove(taskId);
        if (task != null) {
            allTasksObservable.remove(taskId);

            // remove from other data structures and their ObservableLists
            inProgressTasks.remove(task);
            inProgressTasksObservable.remove(task);

            completedTasks.remove(task);
            completedTasksObservable.remove(task);

            priorityTasks.remove(task);
            priorityTasksObservable.remove(task);
        }
    }

    public void loadTasksForUser(String username) {
        File file = new File(username + ".txt");
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                if (scanner.hasNextLine()) {
                    scanner.nextLine();  // will skip the first line sinc e it's the password
                }
                while (scanner.hasNextLine()) {
                    String taskData = scanner.nextLine();
                    Task task = convertStringToTask(taskData);
                    addTask(task, false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Task convertStringToTask(String taskData) {
        String[] parts = taskData.split(",");

        return new Task(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3]);
    }

}
