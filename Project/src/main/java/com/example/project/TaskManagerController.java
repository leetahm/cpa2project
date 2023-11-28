package com.example.project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.util.Comparator;
import javafx.collections.FXCollections;

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

    @FXML
    private TableView<Task> inProgressTasksTable;
    @FXML
    private TableColumn<Task, String> inProgressIdColumn;
    @FXML
    private TableColumn<Task, String> inProgressDescriptionColumn;
    @FXML
    private TableColumn<Task, Integer> inProgressPriorityColumn;
    @FXML
    private TableColumn<Task, String> inProgressAssignedPersonColumn;
    @FXML
    private TableColumn<Task, String> inProgressDueDateColumn;
    @FXML
    private TableView<Task> completedTasksTable;
    @FXML
    private TableColumn<Task, String> completedIdColumn;
    @FXML
    private TableColumn<Task, String> completedDescriptionColumn;
    @FXML
    private TableColumn<Task, Integer> completedPriorityColumn;
    @FXML
    private TableColumn<Task, String> completedDueDateColumn;
    @FXML
    private TableColumn<Task, String> completedAssignedPersonColumn;

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    public void initialize() {

        // TODO
        // DEBUGGER REMEMBER TO REMOVE IN REAL RUN
        inProgressTasksObservable.add(new Task("1", "Test Task", 1, "Test Person", "tomorrow"));

        inProgressDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        inProgressIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        inProgressDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        inProgressPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        inProgressAssignedPersonColumn.setCellValueFactory(new PropertyValueFactory<>("assignedPerson"));

        inProgressTasksTable.setItems(inProgressTasksObservable);

        completedIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        completedDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        completedPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        completedDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        completedAssignedPersonColumn.setCellValueFactory(new PropertyValueFactory<>("assignedPerson"));

        completedTasksTable.setItems(completedTasksObservable);

    }

    // will handle the logout button
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

    // handles the add task button
    @FXML
    private void handleAddTaskButtonAction() {
        try {
            String id = UUID.randomUUID().toString();
            String name = taskNameField.getText();
            String dueDate = dueDateField.getText(); // might have to parse date
            int priority = Integer.parseInt(priorityField.getText());
            String assignedPerson = assignedPersonField.getText();

            Task newTask = new Task(id, name, priority, assignedPerson, dueDate);
            addTask(newTask, true);  // Add the task

            taskNameField.clear();
            dueDateField.clear();
            priorityField.clear();
            assignedPersonField.clear();
        } catch (NumberFormatException e) {
            e.printStackTrace(); // Consider more user-friendly error handling
        }
    }


    @FXML
    private void handleFinishTaskButtonAction() {
        Task selectedTask = inProgressTasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            completeTask(selectedTask);
        } else {

        }
    }

    @FXML
    private void handleEditTaskButtonAction() {
        Task selectedTask = inProgressTasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            try {
                String newDescription = taskNameField.getText(); // Assuming taskNameField is used for editing description
                int newPriority = Integer.parseInt(priorityField.getText());
                String newAssignedPerson = assignedPersonField.getText();

                // Update the task properties
                selectedTask.descriptionProperty().set(newDescription);
                selectedTask.priorityProperty().set(newPriority);
                selectedTask.assignedPersonProperty().set(newAssignedPerson);

                // Clear the input fields after editing
                taskNameField.clear();
                priorityField.clear();
                assignedPersonField.clear();
            } catch (NumberFormatException e) {
                e.printStackTrace(); // Consider more user-friendly error handling
            }
        } else {
            // Handle case where no task is selected
        }
    }


    public void addTask(Task task, boolean writeToDisk) {
        inProgressTasksObservable.add(task); // Add all tasks to this list
        allTasks.put(task.getId(), task);

        if (task.getPriority() > 0) {
            priorityTasks.add(task);
            priorityTasksObservable.add(task);
        } else {
            inProgressTasks.add(task);
        }

        Map.Entry<String, Task> entry = new AbstractMap.SimpleEntry<>(task.getId(), task);
        allTasksObservable.add(entry);

        if (writeToDisk) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.username + ".txt", true))) {
                String taskData = convertTaskToString(task);
                writer.newLine();
                writer.write(taskData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // will convert task to string for the txt file
    private String convertTaskToString(Task task) {
        return task.getId() + "," + task.getDescription() + "," + task.getPriority() + "," + task.getAssignedPerson() + "," + task.getDueDate();
    }

    // method to complete a task
    public void completeTask(Task task) {
        // will remove the task from the in progress tableview
        inProgressTasks.remove(task);
        inProgressTasksObservable.remove(task);

        // will add the task to the completed tableview
        completedTasks.push(task);
        completedTasksObservable.add(0, task);

        task.setCompleted(true);

        // remove from prio queue
        if (task.getPriority() > 0) {
            priorityTasks.remove(task);
            priorityTasksObservable.remove(task);
        }
        saveAllTasksToFile();
    }

    // will save changes to file
    private void saveAllTasksToFile() {
        File file = new File(this.username + ".txt");
        String firstLine = ""; // to store the first line/password

        // read and store the first line
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                if (scanner.hasNextLine()) {
                    firstLine = scanner.nextLine(); // Read the password
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // rewrites the file with the updated tableview
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(firstLine);
            writer.newLine();

            for (Task task : allTasks.values()) {
                String taskData = convertTaskToString(task);
                if (completedTasks.contains(task)) {
                    taskData += "~"; // will append '~' for completed tasks
                }
                writer.write(taskData);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                // Skip the first line (password)
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }

                while (scanner.hasNextLine()) {
                    String taskData = scanner.nextLine();
                    Task task = convertStringToTask(taskData);

                    // Check if the task is completed and add to the appropriate list
                    if (task.isCompleted()) {
                        completedTasks.push(task);
                        completedTasksObservable.add(task);
                    } else {
                        inProgressTasks.add(task);
                        inProgressTasksObservable.add(task);
                    }

                    // Add to priority queue if needed
                    if (task.getPriority() > 0) {
                        priorityTasks.add(task);
                        priorityTasksObservable.add(task);
                    }

                    // Add to all tasks map
                    allTasks.put(task.getId(), task);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Task convertStringToTask(String taskData) {
        boolean isCompleted = taskData.endsWith("~");
        if (isCompleted) {
            taskData = taskData.substring(0, taskData.length() - 1); // Remove the '~'
        }

        String[] parts = taskData.split(",");
        Task task = new Task(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], parts[4]);
        task.setCompleted(isCompleted);
        return task;
    }

    @FXML
    private void handleSortButtonAction() {
        sortTasksByPriority(inProgressTasksObservable);
        sortTasksByPriority(completedTasksObservable);
    }

    private void sortTasksByPriority(ObservableList<Task> tasks) {
        FXCollections.sort(tasks, Comparator.comparingInt(Task::getPriority));
    }

}
