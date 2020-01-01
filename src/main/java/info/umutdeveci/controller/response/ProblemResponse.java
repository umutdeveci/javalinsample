package info.umutdeveci.controller.response;

import lombok.Builder;
import lombok.Data;

/**
 * Will be sent back as a response in case of any exceptions are thrown
 */
@Data
@Builder
public class ProblemResponse {

    private int status;
    private String description;
    private String message;
}
