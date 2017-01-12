package com.jetan.www.reminders;

/**
 * Created by anningyu on 2017-01-12.
 */

public class Reminder {
    private int id;
    private String content;
    private int important;

    public Reminder(int id, String content, int important) {
        this.id = id;
        this.content = content;
        this.important = important;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getImportant() {
        return important;
    }

    public void setImportant(int important) {
        this.important = important;
    }
}
