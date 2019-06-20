package entities;

import java.io.Serializable;
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
@Table(name = "alarm")
@NamedQueries({
    //    @NamedQuery(name = "Alarm.findAllUpcoming", query = "SELECT a FROM Alarm a WHERE a.due > CURRENT_TIMESTAMP"),   // Seems not to work!
    @NamedQuery(name = "Alarm.findAllLaterThan", query = "SELECT a FROM Alarm a WHERE a.due > :date")
})
public class Alarm implements Serializable {

//    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "Due")
    private Date due;

    @Column(name = "RepeatIntervalSeconds")
    private int repeatIntervalSeconds;

    @Column(name = "Sound")
    private String sound;

    public Alarm() {
    }

    public Alarm(Date due, int repeatIntervalSeconds, String sound) {
        this.due = due;
        this.repeatIntervalSeconds = repeatIntervalSeconds;
        this.sound = sound;
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

    public int getRepeatIntervalSeconds() {
        return repeatIntervalSeconds;
    }

    public void setRepeatIntervalSeconds(int repeatIntervalSeconds) {
        this.repeatIntervalSeconds = repeatIntervalSeconds;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
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
        if (!(object instanceof Alarm)) {
            return false;
        }
        Alarm other = (Alarm) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Alarm[ id=" + id + " ]";
    }

}
