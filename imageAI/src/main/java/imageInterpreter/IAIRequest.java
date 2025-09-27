package imageinterpreter;

/**
 * Defines an interface for making types of requests to AI.
 */
public interface IAIRequest {
    /**
     * Returns the prompt for the particular AI request.
     *
     * @return the context as a String
     */
    String getContext();
}
