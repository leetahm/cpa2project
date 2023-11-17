package com.example.project;

public class Task {
    private String id;
    private String description;
    private int priority;
    private String assignedPerson; // Person assigned to the task

    public Task(String id, String description, int priority, String assignedPerson) {
        this.id = id;
        this.description = description;
        this.priority = priority;
        this.assignedPerson = assignedPerson;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getAssignedPerson() {
        return assignedPerson;
    }

    public void setAssignedPerson(String assignedPerson) {
        this.assignedPerson = assignedPerson;
    }
}
