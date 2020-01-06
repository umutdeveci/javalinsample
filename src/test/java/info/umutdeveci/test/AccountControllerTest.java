package info.umutdeveci.test;

import static info.umutdeveci.util.Utils.generateRandomAccounts;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import info.umutdeveci.controller.AccountController;
import info.umutdeveci.controller.request.DepositRequest;
import info.umutdeveci.controller.request.TransferRequest;
import info.umutdeveci.controller.request.WithdrawRequest;
import info.umutdeveci.controller.response.TransferResponse;
import info.umutdeveci.exception.Problem;
import info.umutdeveci.model.Account;
import info.umutdeveci.service.AccountService;
import info.umutdeveci.service.model.TransferResult;
import info.umutdeveci.service.util.AccountServiceUtil;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJackson;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountControllerTest {

    private AccountController controller;
    private AccountService accountService;
    private Context ctx;
    private ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    @BeforeEach
    void setup() {
        this.accountService = mock(AccountService.class);
        this.ctx = mock(Context.class);
        JavalinJackson.configure(mapper);
        this.controller = new AccountController(accountService);
    }

    @Test
    void testListAccounts() {
        final List<Account> accounts = generateRandomAccounts(5).stream().map(AccountServiceUtil::convertToAccount)
            .collect(Collectors.toList());

        when(accountService.getAll()).thenReturn(accounts);

        controller.listAccounts(ctx);
        verify(accountService).getAll();
        verify(ctx).json(eq(accounts));
    }

    @Test
    void testGetAccountDetailSuccess() {
        final Account account = Account.builder().accountNumber("test").balance(BigDecimal.TEN).build();
        when(accountService.get(eq("test"))).thenReturn(account);
        doReturn("test").when(ctx).pathParam(anyString());

        controller.getAccountDetail(ctx);
        verify(ctx).json(eq(account));
    }

    @Test
    void testAccountNotFound() {
        when(accountService.get(anyString())).thenThrow(new Problem(HttpStatus.NOT_FOUND_404, "Account not found"));
        doReturn("test").when(ctx).pathParam(anyString());

        assertThrows(Problem.class, () -> controller.getAccountDetail(ctx));
    }

    @Test
    void testWithdrawSuccess() throws Exception {
        final Account account = Account.builder().accountNumber("test").balance(BigDecimal.TEN).build();
        when(accountService.withdraw(eq("test"), eq(BigDecimal.TEN))).thenReturn(account);

        doReturn("test").when(ctx).pathParam(anyString());

        final WithdrawRequest request = new WithdrawRequest(BigDecimal.TEN);
        when(ctx.body()).thenReturn(mapper.writeValueAsString(request)); // Ugly hack
        when(ctx.bodyValidator(eq(WithdrawRequest.class))).thenCallRealMethod(); // followed by another ugly hack

        controller.withdraw(ctx);

        verify(ctx).pathParam(eq("account_number"));
        verify(accountService).withdraw(eq("test"), eq(BigDecimal.TEN));
        verify(ctx).json(eq(account));
    }

    @Test
    void testWithdrawAmountNegative() throws Exception {
        doReturn("test").when(ctx).pathParam(anyString());

        final WithdrawRequest request = new WithdrawRequest(new BigDecimal(-2.0));
        when(ctx.body()).thenReturn(mapper.writeValueAsString(request)); // Ugly hack
        when(ctx.bodyValidator(eq(WithdrawRequest.class))).thenCallRealMethod(); // followed by another ugly hack

        assertThrows(BadRequestResponse.class, () -> controller.withdraw(ctx));

        verify(ctx).pathParam(eq("account_number"));
    }

    @Test
    void testWithdrawServiceException() throws Exception {
        doReturn("test").when(ctx).pathParam(anyString());

        final WithdrawRequest request = new WithdrawRequest(BigDecimal.TEN);
        when(ctx.body()).thenReturn(mapper.writeValueAsString(request)); // Ugly hack
        when(ctx.bodyValidator(eq(WithdrawRequest.class))).thenCallRealMethod(); // followed by another ugly hack

        when(accountService.withdraw(eq("test"), eq(BigDecimal.TEN))).thenThrow(new RuntimeException("generic"));

        assertThrows(RuntimeException.class, () -> controller.withdraw(ctx));

        verify(ctx).pathParam(eq("account_number"));
    }

    @Test
    void testDepositSuccess() throws Exception {
        final Account account = Account.builder().accountNumber("test").balance(BigDecimal.TEN).build();
        when(accountService.deposit(eq("test"), eq(BigDecimal.TEN))).thenReturn(account);
        doReturn("test").when(ctx).pathParam(anyString());

        final DepositRequest request = new DepositRequest(BigDecimal.TEN);
        when(ctx.body()).thenReturn(mapper.writeValueAsString(request)); // Ugly hack
        when(ctx.bodyValidator(eq(DepositRequest.class))).thenCallRealMethod(); // followed by another ugly hack

        controller.deposit(ctx);

        verify(ctx).pathParam(eq("account_number"));
        verify(accountService).deposit(eq("test"), eq(BigDecimal.TEN));
        verify(ctx).json(eq(account));
    }

    @Test
    void testDepositAmountNegative() throws Exception {
        doReturn("test").when(ctx).pathParam(anyString());

        final DepositRequest request = new DepositRequest(new BigDecimal(-2.0));
        when(ctx.body()).thenReturn(mapper.writeValueAsString(request)); // Ugly hack
        when(ctx.bodyValidator(eq(DepositRequest.class))).thenCallRealMethod(); // followed by another ugly hack

        assertThrows(BadRequestResponse.class, () -> controller.deposit(ctx));

        verify(ctx).pathParam(eq("account_number"));
    }

    @Test
    void testDepositServiceException() throws Exception {
        doReturn("test").when(ctx).pathParam(anyString());

        final DepositRequest request = new DepositRequest(BigDecimal.TEN);
        when(ctx.body()).thenReturn(mapper.writeValueAsString(request)); // Ugly hack
        when(ctx.bodyValidator(eq(DepositRequest.class))).thenCallRealMethod(); // followed by another ugly hack

        when(accountService.deposit(eq("test"), eq(BigDecimal.TEN))).thenThrow(new RuntimeException("generic"));

        assertThrows(RuntimeException.class, () -> controller.deposit(ctx));

        verify(ctx).pathParam(eq("account_number"));
    }

    @Test
    void testTransferSuccess() throws Exception {
        final Account fromAccount = Account.builder().accountNumber("test1").balance(BigDecimal.TEN).build();
        final Account toAccount = Account.builder().accountNumber("test2").balance(BigDecimal.TEN).build();

        final TransferRequest request = TransferRequest.builder()
            .fromAccountNumber(fromAccount.getAccountNumber())
            .toAccountNumber(toAccount.getAccountNumber())
            .amount(BigDecimal.TEN)
            .build();

        when(ctx.body()).thenReturn(mapper.writeValueAsString(request)); // Ugly hack
        when(ctx.bodyValidator(eq(TransferRequest.class))).thenCallRealMethod(); // followed by another ugly hack

        final TransferResult transferResult = TransferResult.builder()
            .fromAccount(fromAccount)
            .toAccount(toAccount)
            .build();

        when(accountService
            .transfer(eq(fromAccount.getAccountNumber()), eq(toAccount.getAccountNumber()), eq(request.getAmount())))
            .thenReturn(transferResult);

        controller.transfer(ctx);

        final TransferResponse expected = TransferResponse.builder()
            .fromAccount(fromAccount)
            .toAccount(toAccount)
            .build();

        verify(accountService)
            .transfer(eq(fromAccount.getAccountNumber()), eq(toAccount.getAccountNumber()), eq(request.getAmount()));
        verify(ctx).json(eq(expected));
    }

    @Test
    void testFromAccountMissing() throws Exception {
        final Account toAccount = Account.builder().accountNumber("test2").balance(BigDecimal.TEN).build();

        final TransferRequest request = TransferRequest.builder()
            .toAccountNumber(toAccount.getAccountNumber())
            .amount(BigDecimal.TEN)
            .build();

        when(ctx.body()).thenReturn(mapper.writeValueAsString(request)); // Ugly hack
        when(ctx.bodyValidator(eq(TransferRequest.class))).thenCallRealMethod(); // followed by another ugly hack

        assertThrows(BadRequestResponse.class, () -> controller.transfer(ctx));
    }

    @Test
    void testToAccountMissing() throws Exception {
        final Account fromAccount = Account.builder().accountNumber("test").balance(BigDecimal.TEN).build();

        final TransferRequest request = TransferRequest.builder()
            .fromAccountNumber(fromAccount.getAccountNumber())
            .amount(BigDecimal.TEN)
            .build();

        when(ctx.body()).thenReturn(mapper.writeValueAsString(request)); // Ugly hack
        when(ctx.bodyValidator(eq(TransferRequest.class))).thenCallRealMethod(); // followed by another ugly hack

        assertThrows(BadRequestResponse.class, () -> controller.transfer(ctx));
    }

    @Test
    void testAmountMissing() throws Exception {
        final Account fromAccount = Account.builder().accountNumber("test1").balance(BigDecimal.TEN).build();
        final Account toAccount = Account.builder().accountNumber("test2").balance(BigDecimal.TEN).build();

        final TransferRequest request = TransferRequest.builder()
            .fromAccountNumber(fromAccount.getAccountNumber())
            .toAccountNumber(toAccount.getAccountNumber())
            .build();

        when(ctx.body()).thenReturn(mapper.writeValueAsString(request)); // Ugly hack
        when(ctx.bodyValidator(eq(TransferRequest.class))).thenCallRealMethod(); // followed by another ugly hack

        assertThrows(BadRequestResponse.class, () -> controller.transfer(ctx));
    }


    @Test
    void testAmountNegative() throws Exception {
        final Account fromAccount = Account.builder().accountNumber("test1").balance(BigDecimal.TEN).build();
        final Account toAccount = Account.builder().accountNumber("test2").balance(BigDecimal.TEN).build();

        final TransferRequest request = TransferRequest.builder()
            .fromAccountNumber(fromAccount.getAccountNumber())
            .toAccountNumber(toAccount.getAccountNumber())
            .amount(new BigDecimal(-2.0))
            .build();

        when(ctx.body()).thenReturn(mapper.writeValueAsString(request)); // Ugly hack
        when(ctx.bodyValidator(eq(TransferRequest.class))).thenCallRealMethod(); // followed by another ugly hack

        assertThrows(BadRequestResponse.class, () -> controller.transfer(ctx));
    }

    @Test
    void testAmountZero() throws Exception {
        final Account fromAccount = Account.builder().accountNumber("test1").balance(BigDecimal.TEN).build();
        final Account toAccount = Account.builder().accountNumber("test2").balance(BigDecimal.TEN).build();

        final TransferRequest request = TransferRequest.builder()
            .fromAccountNumber(fromAccount.getAccountNumber())
            .toAccountNumber(toAccount.getAccountNumber())
            .amount(BigDecimal.ZERO)
            .build();

        when(ctx.body()).thenReturn(mapper.writeValueAsString(request)); // Ugly hack
        when(ctx.bodyValidator(eq(TransferRequest.class))).thenCallRealMethod(); // followed by another ugly hack

        assertThrows(BadRequestResponse.class, () -> controller.transfer(ctx));
    }

    @Test
    void testServiceException() throws Exception {
        final Account fromAccount = Account.builder().accountNumber("test1").balance(BigDecimal.TEN).build();
        final Account toAccount = Account.builder().accountNumber("test2").balance(BigDecimal.TEN).build();

        final TransferRequest request = TransferRequest.builder()
            .fromAccountNumber(fromAccount.getAccountNumber())
            .toAccountNumber(toAccount.getAccountNumber())
            .amount(BigDecimal.TEN)
            .build();

        when(ctx.body()).thenReturn(mapper.writeValueAsString(request)); // Ugly hack
        when(ctx.bodyValidator(eq(TransferRequest.class))).thenCallRealMethod(); // followed by another ugly hack

        final TransferResult transferResult = TransferResult.builder()
            .fromAccount(fromAccount)
            .toAccount(toAccount)
            .build();

        when(accountService
            .transfer(eq(fromAccount.getAccountNumber()), eq(toAccount.getAccountNumber()), eq(request.getAmount())))
            .thenThrow(new RuntimeException("generic"));

        assertThrows(RuntimeException.class, () ->  controller.transfer(ctx));
    }
}
