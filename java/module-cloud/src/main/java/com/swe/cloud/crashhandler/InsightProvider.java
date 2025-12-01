/********************************************************************************
 * Filename    = InsightProvider.java
 * Author      = Sooryanarayanan Ganesh
 * Project     = Comm-Uni-Cator
 * Description = Connects to a Gemini model and generates AI response for crashes.
 *******************************************************************************/

package crashhandler;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;


/**
 * Object which is the Azure-OpenAI client.
 * Used to provide insights from exceptions.
 */
class InsightProvider {

    /** OpenAI client. */
    private Client client;

    /** Connection flag to check AI connection. */
    private Boolean connectionFlag;

    /** Deployment model. */
    private final String deploymentModel = "gemini-2.5-flash";

    /** Constructor for Insight Provider. */
    InsightProvider() {

        try {
            this.client = createClient();
            connectionFlag = true;
        } catch (Exception e) {
            connectionFlag = false;
        }
    }

    protected Client createClient() {
        return Client.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build();
    }

    /** Function to get the AI insights on crashes and exceptions.
     * @param crashData CrashData from cosmosDB.
     * @return AI Response.
     */
    public String getInsights(final String crashData) {

        GenerateContentResponse response = null;

        try {
            if (!connectionFlag) {
                throw new RuntimeException("Connection establishment failed");
            }
            response = client.models.generateContent(
                    deploymentModel,
                    "Analyze this crash/exception:" + crashData,
                    null
            );
        } catch (Exception e) {
            return "No response, NOJOY:" + e.getMessage();
        }

        return response.text();
    }
}
