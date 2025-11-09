/**
 * Author : Abhirami R Iyer
 */
package request;

/**
 * Defines an interface for making types of requests to AI.
 * @param  <T> to specify the type of the input got from getInput()
 */
public interface AiRequestable<T> {

    /**
     * Returns the prompt for the particular AI request.
     *
     * @return the context as a String
     */
    String getContext();

    /**
     * Returns the input data for the request.
     *
     * @return the input as a String
     */
    T getInput();

    /**
     * Returns the type of request.
     *
     * @return "REG" if regularization,
     * "DESC" if interpretation
     * "INS" if insights generation
     */
    String getReqType();
}
