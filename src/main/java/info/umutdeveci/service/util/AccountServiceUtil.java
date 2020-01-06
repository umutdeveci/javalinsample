package info.umutdeveci.service.util;

import info.umutdeveci.exception.Problem;
import info.umutdeveci.model.Account;
import info.umutdeveci.service.entity.AccountEntity;
import java.math.BigDecimal;
import org.eclipse.jetty.http.HttpStatus;

public class AccountServiceUtil {

    private AccountServiceUtil() {

    }

    public static Account convertToAccount(final AccountEntity entity) {
        return Account.builder()
            .accountNumber(entity.getAccountNumber())
            .balance(entity.getBalance()) // BigDecimal is immutable
            .build();
    }

    public static void checkAmountGreaterThanZero(final BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Problem(HttpStatus.BAD_REQUEST_400, "Amount can not be less than or equal to zero.");
        }
    }
}
