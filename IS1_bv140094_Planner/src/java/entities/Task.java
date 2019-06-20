package entities;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "task")
@NamedQueries({
    @NamedQuery(name = "Task.findAll", query = "SELECT t FROM Task t"),
    @NamedQuery(name = "Task.findById", query = "SELECT t FROM Task t WHERE t.id = :id"),
})
public class Task implements Serializable {

//    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "Due")
    private Date due;

    @Column(name = "Text")
    private String text;

    @Column(name = "StartingLocation")
    private String startingLocation;

    @Column(name = "Destination")
    private String destination;

    @Column(name = "Alarm")
    private boolean alarm;

    public Task() {
    }

    public Task(Date due, String text, String startingLocation, String destination, boolean alarm) {
        this.due = due;
        this.text = text;
        this.startingLocation = startingLocation;
        this.destination = destination;
        this.alarm = alarm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDue() {
        return due;
    }

    public void setDue(Date due) {
        this.due = due;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(String startingLocation) {
        this.startingLocation = startingLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public boolean isAlarm() {
        return alarm;
    }

    public void setAlarm(boolean alarm) {
        this.alarm = alarm;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Task)) {
            return false;
        }
        Task other = (Task) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    @Override
    public String toString() {
        return "id" + id + " | [due] " + dateFormat.format(due) + " | " + text + " | [start] " + startingLocation + " | [destination] " + destination + " | " + (alarm ? "[has alarm]" : "[no alarm]");
    }

}
