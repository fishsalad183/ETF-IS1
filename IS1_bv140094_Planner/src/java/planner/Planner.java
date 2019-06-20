package planner;

import entities.Task;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class Planner {

    @Resource(lookup = "jms/__defaultConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(lookup = "CommandTopic")
    private static Topic topic;
    @Resource(lookup = "PlannerQueue")
    private static Queue queue;

    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("IS1_bv140094_PlannerPU");
    private static final EntityManager EM;

    static {
        EM = EMF.createEntityManager();
        EM.setFlushMode(FlushModeType.COMMIT);
    }

    private static List<String> fetchTasksAsStrings() {
        TypedQuery<Task> query = EM.createNamedQuery("Task.findAll", Task.class);
        List<Task> results = query.getResultList();
        return results.stream().map(task -> task.toString()).collect(Collectors.toList());
    }

    private static void setAlarm(Task task) {
        // TODO: Implement.
    }

    private static void createNewTask(TextMessage textMessage) throws JMSException {
        Date due = new Date(textMessage.getLongProperty("due"));
        String text = textMessage.getStringProperty("text");
        String startingLocation = textMessage.getStringProperty("startingLocation");
        String destination = textMessage.getStringProperty("destination");
        boolean alarm = textMessage.getBooleanProperty("alarm");
        EM.getTransaction().begin();
        Task task = new Task(due, text, startingLocation, destination, alarm);
        EM.persist(task);
        EM.getTransaction().commit();
        setAlarm(task);
    }

    private static void changeTask(TextMessage textMessage) throws JMSException {
        TypedQuery<Task> query = EM.createNamedQuery("Task.findById", Task.class);
        query.setParameter("id", textMessage.getLongProperty("id"));
        Task task = query.getSingleResult();
        EM.getTransaction().begin();
        task.setDue(new Date(textMessage.getLongProperty("due")));
        task.setText(textMessage.getStringProperty("text"));
        task.setStartingLocation(textMessage.getStringProperty("startingLocation"));
        task.setDestination(textMessage.getStringProperty("destination"));
        task.setAlarm(textMessage.getBooleanProperty("alarm"));
        EM.getTransaction().commit();
    }

    public static void main(String[] args) {
        JMSContext context = connectionFactory.createContext();
        JMSConsumer consumer = context.createConsumer(topic, "for = 'planner'", true);
        JMSProducer producer = context.createProducer();

        while (true) {
            Message message = consumer.receive();
            if (message instanceof TextMessage) {
                try {
                    TextMessage textMessage = (TextMessage) message;
                    switch (textMessage.getStringProperty("command")) {
                        case "list":
                            ObjectMessage objectMessage = context.createObjectMessage();
                            objectMessage.setObject((Serializable) fetchTasksAsStrings());  // UNSAFE CAST ?!?!?!
                            objectMessage.setStringProperty("for", "userDevice");
                            objectMessage.setStringProperty("reply", "list");
                            producer.send(queue, objectMessage);
                            break;
                        case "new":
                            createNewTask(textMessage);
                            break;
                        case "change":
                            changeTask(textMessage);
                            break;
                        case "delete":
                            EM.getTransaction().begin();
                            Query query = EM.createQuery("DELETE FROM Task t WHERE t.id = :id");
                            query.setParameter("id", textMessage.getLongProperty("id")).executeUpdate();
                            EM.getTransaction().commit();
                            break;
                        default:
                            break;
                    }
                } catch (JMSException ex) {
                    Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
