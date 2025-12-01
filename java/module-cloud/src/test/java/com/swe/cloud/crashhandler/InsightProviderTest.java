package crashhandler;

import com.google.genai.Client;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsightProviderTest {

    @Test
    void testInsightProvider() {
        final InsightProvider insightProvider = new InsightProvider();

        String response = insightProvider.getInsights("null data, Do not generate anything");
    }

    @Test
    void testConnectionFailure() {
        final InsightProvider insightProvider = new InsightProvider(){
          @Override
          protected Client createClient() {
              throw new RuntimeException("Simulated connection failure...");
          }
        };

        String response = insightProvider.getInsights("null data, Do not generate anything");
    }

}