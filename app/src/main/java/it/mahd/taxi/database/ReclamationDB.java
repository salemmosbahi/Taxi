package it.mahd.taxi.database;

/**
 * Created by salem on 3/24/16.
 */
public class ReclamationDB {
    private String id;
    private String subject;
    private String date;
    private Boolean status;

    public ReclamationDB(String id, String subject, String date, Boolean status) {
        this.id = id;
        this.subject = subject;
        this.date = date;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getDate() {
        return date;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
