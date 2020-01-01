package info.umutdeveci.plugin;

import info.umutdeveci.controller.response.ProblemResponse;
import info.umutdeveci.exception.Problem;
import io.javalin.Javalin;
import io.javalin.core.plugin.Plugin;
import java.util.Optional;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.jetbrains.annotations.NotNull;

/**
 * Generic exception handler for the Javalin application
 */
public class ExceptionHandlerPlugin implements Plugin {

    @Override
    public void apply(@NotNull final Javalin app) {
        app.exception(Problem.class, (exception, ctx) -> {
            final ProblemResponse problemResponse = createProblemResponse(exception);
            ctx.status(problemResponse.getStatus());
            ctx.json(problemResponse);
        }).exception(Exception.class, (exception, ctx) -> {
            final ProblemResponse problemResponse = createInternalServerErrorResponse(exception);
            ctx.status(problemResponse.getStatus());
            ctx.json(problemResponse);
        });
    }

    private ProblemResponse createProblemResponse(final Problem problem) {
        final HttpStatus.Code httpCode = Optional.ofNullable(HttpStatus.getCode(problem.getHttpCode()))
            .orElse(HttpStatus.Code.INTERNAL_SERVER_ERROR);

        return ProblemResponse.builder()
            .status(httpCode.getCode())
            .description(httpCode.getMessage())
            .message(problem.getMessage())
            .build();
    }

    private ProblemResponse createInternalServerErrorResponse(final Exception e) {
        return ProblemResponse.builder()
            .status(Code.INTERNAL_SERVER_ERROR.getCode())
            .description(Code.INTERNAL_SERVER_ERROR.getMessage())
            .message(e.toString()).build();
    }
}
