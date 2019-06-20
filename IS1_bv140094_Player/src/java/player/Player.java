package player;

import com.google.api.services.youtube.model.SearchResult;
import entities.Playback;
import java.awt.Desktop;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.persistence.TypedQuery;

public class Player {

    @Resource(lookup = "jms/__defaultConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(lookup = "PlayerQueue")
    private static Queue queue;
    @Resource(lookup = "CommandTopic")
    private static Topic topic;
//    @Resource(lookup = "ResponseQueue")
//    private static Queue responseQueue;

    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("IS1_bv140094_PlayerPU");
    private static final EntityManager EM;

    static {
        EM = EMF.createEntityManager();
        EM.setFlushMode(FlushModeType.COMMIT);
    }

    private static final String URL_START = "https://www.youtube.com/watch?v=";

    private static void play(String searchTerm) throws GeneralSecurityException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                List<SearchResult> searchResults = YouTubeSearch.search(searchTerm);
                SearchResult firstResult = searchResults.get(0);
                final String url = URL_START + firstResult.getId().getVideoId();
                Desktop.getDesktop().browse(URI.create(url));
                
                EM.getTransaction().begin();
                Playback playback = new Playback(firstResult.getSnippet().getTitle(), searchTerm);
                EM.persist(playback);
                EM.getTransaction().commit();
            } catch (IOException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private static List<String> fetchHistory() {
        TypedQuery<String> query = EM.createNamedQuery("Playback.findAllTitles", String.class);
        return query.getResultList();
    }

    public static void main(String[] args) {
        JMSContext context = connectionFactory.createContext();
        JMSConsumer consumer = context.createConsumer(topic, "for = 'player'", true);
        JMSProducer producer = context.createProducer();

        while (true) {
            Message message = consumer.receive();
            if (message instanceof TextMessage) {
                try {
                    TextMessage textMessage = (TextMessage) message;
                    switch (textMessage.getStringProperty("command")) {
                        case "search":
                            play(textMessage.getText());
                            break;
                        case "history":
                            ObjectMessage objectMessage = context.createObjectMessage();
                            objectMessage.setObject((Serializable) fetchHistory()); // UNSAFE CAST ?!?!?!
                            objectMessage.setStringProperty("for", "userDevice");
                            objectMessage.setStringProperty("reply", "history");
                            producer.send(queue, objectMessage);
                            break;
                        default:
                            break;
                    }
                } catch (JMSException | GeneralSecurityException ex) {
                    Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
