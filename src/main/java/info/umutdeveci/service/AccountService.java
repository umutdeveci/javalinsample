package info.umutdeveci.service;

import info.umutdeveci.model.Account;
import info.umutdeveci.service.model.TransferResult;
import java.math.BigDecimal;
import java.util.List;
import lombok.NonNull;

public interface AccountService {

    List<Account> getAll();

    Account get(@NonNull final String accountNumber);

    Account withdraw(@NonNull final String accountNumber, @NonNull final BigDecimal amount);

    Account deposit(@NonNull final String accountNumber, @NonNull final BigDecimal amount);

    TransferResult transfer(@NonNull final String fromAccount, @NonNull final String toAccount,
        @NonNull final BigDecimal amount);
}
