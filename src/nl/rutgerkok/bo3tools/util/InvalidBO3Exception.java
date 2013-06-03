package nl.rutgerkok.bo3tools.util;

/**
 * Thrown when a user tries to create a BO3 that is too big, or is spread across
 * multiple worlds.
 * 
 */
@SuppressWarnings("serial")
// No need to serialize
public class InvalidBO3Exception extends Exception {
    /**
     * Constructs a new InvalidBO3Exception.
     * 
     * @param reason
     *            Reason why the BO3 is invalid.
     */
    public InvalidBO3Exception(String reason) {
        super(reason);
    }
}
