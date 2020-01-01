package info.umutdeveci.exception;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.http.HttpStatus;

/**
 * A simple exception class that can be thrown which will directly translate into http error codes. Please do not send
 * non error codes :(
 */
@Getter
@Setter
public class Problem extends RuntimeException {

    private int httpCode;

    public Problem(final String message) {
        super(message);
        this.httpCode = HttpStatus.INTERNAL_SERVER_ERROR_500;
    }

    public Problem(final int httpCode, final String message) {
        super(message);
        this.httpCode = httpCode;
    }

    public Problem(final int httpCode, final Exception originalException) {
        super(originalException);
        this.httpCode = httpCode;
    }

    public Problem(final int httpCode, final String message, final Exception originalException) {
        super(message, originalException);
        this.httpCode = httpCode;
    }
}
