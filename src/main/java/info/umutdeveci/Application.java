package info.umutdeveci;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

    public static void main(String[] args) {
        final Javalin app = Javalin
            .create(config -> config.registerPlugin(new OpenApiPlugin(createOpenApiOptions())))
            .start(8080);
        app.get("/", ctx -> ctx.result("Hello World"));
    }

    private static OpenApiOptions createOpenApiOptions() {
        Info applicationInfo = new Info()
            .title("Account API")
            .version("1.0")
            .description(
                "A simple API that provides account information, withdrawal/deposit operations and transfers between"
                    + "  accounts. For simplicity sake, all accounts considered to have same currency.");
        return new OpenApiOptions(applicationInfo)
            .path("/swagger-docs");
    }

}
