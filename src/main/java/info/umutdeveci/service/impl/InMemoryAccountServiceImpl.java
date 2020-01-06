package info.umutdeveci.service.impl;

import static info.umutdeveci.service.util.AccountServiceUtil.checkAmountNotNegative;
import static info.umutdeveci.service.util.AccountServiceUtil.convertToAccount;

import info.umutdeveci.exception.Problem;
import info.umutdeveci.model.Account;
import info.umutdeveci.service.AccountService;
import info.umutdeveci.service.entity.AccountEntity;
import info.umutdeveci.service.model.TransferResult;
import info.umutdeveci.service.util.AccountServiceUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;

/**
 * This service provides the logic for Account operations. I chose a simple HashMap for the data storage with an
 * external lock for locking. Since the map is not supposed to be accessed without acquiring the lock, no need to use a
 * ConcurrentHashMap in this case. So, in practice, this simulates a database which uses table level locking, instead of
 * row locking or bucket locking (also known as page locking). It does create a bottle neck but it is the simplest
 * solution available. I really do not want to embed a database, like H2, and start writing sql queries and deal with
 * jdbc for this simple project.
 */
@Slf4j
public class InMemoryAccountServiceImpl implements AccountService {

    private Map<String, AccountEntity> repository;
    private Lock lock = new ReentrantLock(true);

    public InMemoryAccountServiceImpl(@NonNull final List<AccountEntity> accountEntities) {
        this.repository = new HashMap<>(accountEntities.size());
        accountEntities.forEach(accountEntity -> repository.put(accountEntity.getAccountNumber(), accountEntity));
    }

    @Override
    public List<Account> getAll() {
        return doWithLock(() -> repository.values().stream()
            .map(AccountServiceUtil::convertToAccount)
            .collect(Collectors.toList()));
    }

    @Override
    public Account get(@NonNull final String accountNumber) {
        return doWithLock(() -> {
            final AccountEntity entity = getInternal(accountNumber);
            return convertToAccount(entity);
        });
    }

    @Override
    public Account withdraw(@NonNull final String accountNumber, @NonNull final BigDecimal amount) {
        return doWithLock(() -> {
            final AccountEntity entity = getInternal(accountNumber);

            withdrawInternal(entity, amount);

            return convertToAccount(entity);
        });
    }

    @Override
    public Account deposit(@NonNull final String accountNumber, @NonNull final BigDecimal amount) {
        return doWithLock(() -> {
            final AccountEntity entity = getInternal(accountNumber);

            depositInternal(entity, amount);

            return convertToAccount(entity);
        });
    }

    @Override
    public TransferResult transfer(@NonNull final String fromAccountNumber, @NonNull final String toAccountNumber,
        @NonNull final BigDecimal amount) {
        return doWithLock(() -> {
            if (fromAccountNumber.equalsIgnoreCase(toAccountNumber)) {
                throw new Problem(HttpStatus.BAD_REQUEST_400, "Can not transfer between same accounts");
            }

            final AccountEntity fromEntity = getInternal(fromAccountNumber);
            final AccountEntity toEntity = getInternal(toAccountNumber);

            withdrawInternal(fromEntity, amount);
            depositInternal(toEntity, amount);

            return TransferResult.builder()
                .fromAccount(convertToAccount(fromEntity))
                .toAccount(convertToAccount(toEntity))
                .build();
        });
    }

    private AccountEntity getInternal(@NonNull final String accountNumber) {
        final AccountEntity entity = repository.get(accountNumber);
        if (entity == null) {
            throw new Problem(HttpStatus.BAD_REQUEST_400, String.format("Account %s does not exist.", accountNumber));
        }

        return entity;
    }

    private void withdrawInternal(@NonNull final AccountEntity entity, @NonNull final BigDecimal amount) {
        checkAmountNotNegative(amount);

        final BigDecimal newBalance = entity.getBalance().subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new Problem(HttpStatus.BAD_REQUEST_400, "Account does not have enough balance.");
        }

        entity.setBalance(newBalance);
    }

    private void depositInternal(@NonNull final AccountEntity entity, @NonNull final BigDecimal amount) {
        checkAmountNotNegative(amount);
        final BigDecimal newBalance = entity.getBalance().add(amount);
        entity.setBalance(newBalance);
    }


    private <T> T doWithLock(Supplier<T> supplier) {
        try {
            lock.lock();
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

}
