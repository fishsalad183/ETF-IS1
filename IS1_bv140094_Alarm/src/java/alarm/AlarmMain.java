package alarm;

import entities.Alarm;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AlarmMain {

    @Resource(lookup = "jms/__defaultConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(lookup = "CommandTopic")
    private static Topic topic;

    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("IS1_bv140094_AlarmPU");
    private static final EntityManager EM;

    static {
        EM = EMF.createEntityManager();
        EM.setFlushMode(FlushModeType.COMMIT);
    }

    private static final List<Timer> TIMERS = new ArrayList<Timer>();

    private static class TimerPlayer extends TimerTask {    // TODO: Implement alarm cancellation from UserDevice.

        private final String name;

        public TimerPlayer(final String name) {
            this.name = name;
        }

        @Override
        public void run() {
            try {
                Clip clip = AudioSystem.getClip();
                InputStream is = AlarmMain.class.getResourceAsStream(name);
                BufferedInputStream bis = new BufferedInputStream(is);
                AudioInputStream ais = AudioSystem.getAudioInputStream(bis);
                clip.open(ais);
                clip.start();
            } catch (LineUnavailableException | UnsupportedAudioFileException | IOException ex) {
                Logger.getLogger(AlarmMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void scheduleTimer(Alarm a) {
        Timer timer = new Timer(true);
        TIMERS.add(timer);
        if (a.getRepeatIntervalSeconds() > 0) {
            timer.scheduleAtFixedRate(new TimerPlayer(a.getSound()), a.getDue(), a.getRepeatIntervalSeconds() * 1000);
        } else {
            timer.schedule(new TimerPlayer(a.getSound()), a.getDue());
        }
    }

    private static void initializeAlarms() {
//        TypedQuery<Alarm> query = EM.createNamedQuery("Alarm.findAllUpcoming", Alarm.class);
        TypedQuery<Alarm> query = EM.createNamedQuery("Alarm.findAllLaterThan", Alarm.class);
        query.setParameter("date", new Date(), TemporalType.TIMESTAMP);
        List<Alarm> results = query.getResultList();
        for (Alarm a : results) {
            scheduleTimer(a);
        }
    }

    private static void reactivateTimers() {
        for (Timer timer : TIMERS) {
            timer.cancel();
        }
        TIMERS.clear();
        initializeAlarms();
    }

    public static void main(String[] args) {
        JMSContext context = connectionFactory.createContext();
        JMSConsumer consumer = context.createConsumer(topic, "for = 'alarm'", true);

        initializeAlarms();

        while (true) {
            Message message = consumer.receive();
            if (message instanceof TextMessage) {
                try {
                    TextMessage textMessage = (TextMessage) message;
                    switch (textMessage.getStringProperty("command")) {
                        case "set":
                            Date due = new Date(textMessage.getLongProperty("due"));
                            int repeatInterval = textMessage.getIntProperty("repeatInterval");
                            String sound = textMessage.getStringProperty("sound");
                            EM.getTransaction().begin();
                            Alarm alarm = new Alarm(due, repeatInterval, sound);
                            EM.persist(alarm);
                            EM.getTransaction().commit();
                            scheduleTimer(alarm);
                            break;
                        case "reactivate":
                            reactivateTimers();
                            break;
                        default:
                            break;
                    }
                } catch (JMSException ex) {
                    Logger.getLogger(AlarmMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
