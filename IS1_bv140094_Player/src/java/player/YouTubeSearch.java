package player;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class YouTubeSearch {

    /**
     * Define a global variable that identifies the name of a file that contains
     * the developer's API key.
     */
    private static final String API_KEY = "AIzaSyBbU_1OcGAF-GOe9Eu9_BqWVfB4L1gbTTM";

    private static final long NUMBER_OF_VIDEOS_RETURNED = 1;

    /**
     * Define a global instance of a YouTube object, which will be used to make
     * YouTube Data API requests.
     */
    private static YouTube youtube;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Initialize a YouTube object to search for videos on YouTube. Then display
     * the name and thumbnail image of each video in the result set.
     *
     * @param term
     * @return
     * @throws java.security.GeneralSecurityException
     * @throws java.io.IOException
     */
    public static List<SearchResult> search(String term) throws GeneralSecurityException, IOException {
        // Read the developer key from the properties file.
//        Properties properties = new Properties();
//        try {
//            InputStream in = YouTubeSearch.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
//            properties.load(in);
// 
//        } catch (IOException e) {
//            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
//                    + " : " + e.getMessage());
//            System.exit(1);
//        }

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // This object is used to make YouTube Data API requests. The last
        // argument is required, but since we don't need anything
        // initialized when the HttpRequest is initialized, we override
        // the interface and provide a no-op function.
        youtube = new YouTube.Builder(httpTransport, JSON_FACTORY, new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName("IS1 bv140094").build();

        // Prompt the user to enter a query term.
        String queryTerm = term;

        // Define the API request for retrieving search results.
        YouTube.Search.List search = youtube.search().list("id,snippet");

        // Set your developer key from the Google Developers Console for
        // non-authenticated requests. See:
        // https://console.developers.google.com/
//            String apiKey = properties.getProperty("youtube.apikey");
//            search.setKey(apiKey);
        search.setKey(API_KEY);
        search.setQ(queryTerm);

        // Restrict the search results to only include videos. See:
        // https://developers.google.com/youtube/v3/docs/search/list#type
        search.setType("video");

        // To increase efficiency, only retrieve the fields that the
        // application uses.
//        search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
        search.setFields("items(id/videoId,snippet/title)");
        search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

        // Call the API and print results.
        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResultList = searchResponse.getItems();

        return searchResultList;
    }
}
