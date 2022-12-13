package com.cxdev.voicenotes;

public class Note {
    private Integer id;
    private String title;
    private String note;
    private String timestamp;

    // make the note object
    public Note(Integer id, String title, String note, String timestamp) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.timestamp = timestamp;
    }

    public Integer getID() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
