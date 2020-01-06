package info.umutdeveci;

import static info.umutdeveci.util.Utils.generateRandomAccounts;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

import info.umutdeveci.controller.AccountController;
import info.umutdeveci.plugin.ExceptionHandlerPlugin;
import info.umutdeveci.service.AccountService;
import info.umutdeveci.service.entity.AccountEntity;
import info.umutdeveci.service.impl.InMemoryAccountServiceImpl;
import info.umutdeveci.util.Utils;
import io.javalin.Javalin;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.info.Info;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

    public static void main(String[] args) {
        final AccountService accountService = initializeAccountService();
        final AccountController accountController = new AccountController(accountService);

        final Javalin app = Javalin
            .create(config -> {
                config.registerPlugin(new OpenApiPlugin(createOpenApiOptions()));
                config.registerPlugin(new ExceptionHandlerPlugin());
                config.defaultContentType = "application/json";
            }).routes(() -> {
                path("account", () -> {
                    get(accountController::listAccounts);
                    path(":account_number", () -> {
                        get(accountController::getAccountDetail);
                        post("withdraw", accountController::withdraw);
                        post("deposit", accountController::deposit);
                    });
                });
                post("transfer", accountController::transfer);
            });

        app.start(8080);
    }

    private static OpenApiOptions createOpenApiOptions() {
        final Info applicationInfo = new Info()
            .title("Account API")
            .version("1.0")
            .description(
                "A simple API that provides account information, withdrawal/deposit operations and transfers between"
                    + "  accounts. For simplicity sake, all accounts considered to have same currency.");
        return new OpenApiOptions(applicationInfo)
            .path("/swagger-docs")
            .swagger(new SwaggerOptions("/swagger-ui"));
    }

    private static AccountService initializeAccountService() {
        final List<AccountEntity> randomAccounts = generateRandomAccounts(50);
        return new InMemoryAccountServiceImpl(randomAccounts);
    }
}
