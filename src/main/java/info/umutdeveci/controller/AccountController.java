package info.umutdeveci.controller;

import info.umutdeveci.controller.request.DepositRequest;
import info.umutdeveci.controller.request.TransferRequest;
import info.umutdeveci.controller.request.WithdrawRequest;
import info.umutdeveci.controller.response.ProblemResponse;
import info.umutdeveci.controller.response.TransferResponse;
import info.umutdeveci.model.Account;
import info.umutdeveci.service.AccountService;
import info.umutdeveci.service.model.TransferResult;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.ContentType;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiParam;
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import java.math.BigDecimal;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class AccountController {

    private final AccountService accountService;

    public AccountController(final AccountService accountService) {
        this.accountService = accountService;
    }

    @OpenApi(
        path = "/account",
        method = HttpMethod.GET,
        summary = "Returns a list of available accounts with their balances.",
        operationId = "listAccounts",
        responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(from = Account.class, type = ContentType.JSON, isArray = true)),
            @OpenApiResponse(status = "default", content = @OpenApiContent(from = ProblemResponse.class, type = ContentType.JSON))
        }
    )
    public void listAccounts(final Context ctx) {
        ctx.json(accountService.getAll());
    }

    @OpenApi(
        path = "/account/:account_number",
        method = HttpMethod.GET,
        summary = "Returns account details with specified account number",
        operationId = "getAccountDetail",
        pathParams = {@OpenApiParam(required = true, name = "account_number")},
        responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(from = Account.class, type = ContentType.JSON)),
            @OpenApiResponse(status = "default", content = @OpenApiContent(from = ProblemResponse.class, type = ContentType.JSON))
        }
    )
    public void getAccountDetail(final Context ctx) {
        final String accountNumber = ctx.pathParam("account_number");
        ctx.json(accountService.get(accountNumber));
    }

    @OpenApi(
        path = "/account/:account_number/withdraw",
        method = HttpMethod.POST,
        summary = "Withdraws money from an account and returns latest state of the account",
        operationId = "withdraw",
        pathParams = {@OpenApiParam(required = true, name = "account_number")},
        requestBody = @OpenApiRequestBody(required = true, content = @OpenApiContent(from = WithdrawRequest.class, type = ContentType.JSON)),
        responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(from = Account.class, type = ContentType.JSON)),
            @OpenApiResponse(status = "default", content = @OpenApiContent(from = ProblemResponse.class, type = ContentType.JSON))
        }
    )
    public void withdraw(final Context ctx) {
        final String accountNumber = ctx.pathParam("account_number");

        final WithdrawRequest request = ctx.bodyValidator(WithdrawRequest.class)
            .check(withdrawRequest -> {
                final BigDecimal amount = withdrawRequest.getAmount();
                return isAmountValid(amount);
            }).get();

        ctx.json(accountService.withdraw(accountNumber, request.getAmount()));
    }

    @OpenApi(
        path = "/account/:account_number/deposit",
        method = HttpMethod.POST,
        summary = "Deposits money to an account and returns latest state of the account",
        operationId = "deposit",
        pathParams = {@OpenApiParam(required = true, name = "account_number")},
        requestBody = @OpenApiRequestBody(required = true, content = @OpenApiContent(from = DepositRequest.class, type = ContentType.JSON)),
        responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(from = Account.class, type = ContentType.JSON)),
            @OpenApiResponse(status = "default", content = @OpenApiContent(from = ProblemResponse.class, type = ContentType.JSON))
        }
    )
    public void deposit(final Context ctx) {
        final String accountNumber = ctx.pathParam("account_number");

        final DepositRequest request = ctx.bodyValidator(DepositRequest.class)
            .check(depositRequest -> {
                final BigDecimal amount = depositRequest.getAmount();
                return isAmountValid(amount);
            }).get();

        ctx.json(accountService.deposit(accountNumber, request.getAmount()));
    }

    @OpenApi(
        path = "/transfer",
        method = HttpMethod.POST,
        summary = "Transfers money between two accounts",
        operationId = "transfer",
        requestBody = @OpenApiRequestBody(required = true, content = @OpenApiContent(from = TransferRequest.class, type = ContentType.JSON)),
        responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(from = TransferResult.class, type = ContentType.JSON)),
            @OpenApiResponse(status = "default", content = @OpenApiContent(from = ProblemResponse.class, type = ContentType.JSON))
        }
    )
    public void transfer(final Context ctx) {
        final TransferRequest request = ctx.bodyValidator(TransferRequest.class)
            .check(this::validateTransferRequest)
            .get();

        final TransferResult transferResult = accountService
            .transfer(request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());

        final TransferResponse response = TransferResponse.builder()
            .fromAccount(transferResult.getFromAccount())
            .toAccount(transferResult.getToAccount())
            .build();

        ctx.json(response);
    }

    private boolean validateTransferRequest(@NonNull final TransferRequest transferRequest) {
        return StringUtils.isNotEmpty(transferRequest.getFromAccountNumber()) &&
            StringUtils.isNotEmpty(transferRequest.getToAccountNumber()) &&
            isAmountValid(transferRequest.getAmount());
    }

    private boolean isAmountValid(final BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
