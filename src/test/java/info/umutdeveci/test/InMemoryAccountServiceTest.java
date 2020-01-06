package info.umutdeveci.test;

import static info.umutdeveci.util.Utils.generateRandomAccounts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.umutdeveci.exception.Problem;
import info.umutdeveci.model.Account;
import info.umutdeveci.service.entity.AccountEntity;
import info.umutdeveci.service.impl.InMemoryAccountService;
import info.umutdeveci.service.model.TransferResult;
import info.umutdeveci.service.util.AccountServiceUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryAccountServiceTest {

    private InMemoryAccountService service;
    private List<AccountEntity> accountEntities;

    @BeforeEach
    void setup() {
        accountEntities = generateRandomAccounts(50);
        service = new InMemoryAccountService(accountEntities);
    }

    @Test
    void accountsShouldBeEqual() {
        final List<Account> accounts = service.getAll();
        assertEquals(accountEntities.size(), accounts.size());

        assertTrue(accountEntities.stream().map(AccountServiceUtil::convertToAccount).allMatch(accounts::contains));
    }

    @Test
    void containsAccount() {
        final int randomIndex = ThreadLocalRandom.current().nextInt(0, accountEntities.size());
        final AccountEntity entity = accountEntities.get(randomIndex);
        final Account account = service.get(entity.getAccountNumber());

        assertNotNull(account);

        assertEquals(entity.getAccountNumber(), account.getAccountNumber());
        assertEquals(entity.getBalance(), account.getBalance());
    }

    @Test
    void accountDoesNotExist() {
        assertThrows(Problem.class, () -> service.get("does not exist"));
    }

    @Test
    void withdrawSuccess() {
        final int randomIndex = ThreadLocalRandom.current().nextInt(0, accountEntities.size());
        final AccountEntity entity = accountEntities.get(randomIndex);
        final BigDecimal oldBalance = entity.getBalance();

        final double randomAmount = ThreadLocalRandom.current().nextDouble(0, oldBalance.doubleValue());
        final BigDecimal withdrawAmount = new BigDecimal(randomAmount).setScale(2, RoundingMode.DOWN);

        final Account newStatus = service.withdraw(entity.getAccountNumber(), withdrawAmount);

        assertEquals(oldBalance.subtract(withdrawAmount), newStatus.getBalance());
    }

    @Test
    void withdrawNotEnoughBalance() {
        final int randomIndex = ThreadLocalRandom.current().nextInt(0, accountEntities.size());
        final AccountEntity entity = accountEntities.get(randomIndex);
        final BigDecimal oldBalance = entity.getBalance();

        final double randomAmount = ThreadLocalRandom.current()
            .nextDouble(oldBalance.doubleValue() + 1d, oldBalance.doubleValue() + 1000d);
        final BigDecimal withdrawAmount = new BigDecimal(randomAmount).setScale(2, RoundingMode.DOWN);

        assertThrows(Problem.class, () -> service.withdraw(entity.getAccountNumber(), withdrawAmount));
    }

    @Test
    void withdrawNegativeAmount() {
        final int randomIndex = ThreadLocalRandom.current().nextInt(0, accountEntities.size());
        final AccountEntity entity = accountEntities.get(randomIndex);

        final BigDecimal withdrawAmount = new BigDecimal(-5).setScale(2, RoundingMode.DOWN);

        assertThrows(Problem.class, () -> service.withdraw(entity.getAccountNumber(), withdrawAmount));
    }

    @Test
    void withdrawAccountDoesNotExist() {
        assertThrows(Problem.class, () -> service.withdraw("does not exists", BigDecimal.ZERO));
    }

    @Test
    void depositSuccess() {
        final int randomIndex = ThreadLocalRandom.current().nextInt(0, accountEntities.size());
        final AccountEntity entity = accountEntities.get(randomIndex);
        final BigDecimal oldBalance = entity.getBalance();

        final double randomAmount = ThreadLocalRandom.current().nextDouble(0, oldBalance.doubleValue());
        final BigDecimal depositAmount = new BigDecimal(randomAmount).setScale(2, RoundingMode.DOWN);

        final Account newStatus = service.deposit(entity.getAccountNumber(), depositAmount);

        assertEquals(oldBalance.add(depositAmount), newStatus.getBalance());
    }

    @Test
    void depositNegativeAmount() {
        final int randomIndex = ThreadLocalRandom.current().nextInt(0, accountEntities.size());
        final AccountEntity entity = accountEntities.get(randomIndex);

        final BigDecimal depositAmount = new BigDecimal(-5).setScale(2, RoundingMode.DOWN);

        assertThrows(Problem.class, () -> service.deposit(entity.getAccountNumber(), depositAmount));
    }

    @Test
    void depositAccountDoesNotExist() {
        assertThrows(Problem.class, () -> service.deposit("does not exists", BigDecimal.ZERO));
    }

    @Test
    void transferSuccess() {
        final int midPoint = accountEntities.size() / 2;

        final int fromRandomIndex = ThreadLocalRandom.current().nextInt(0, midPoint);
        final AccountEntity fromEntity = accountEntities.get(fromRandomIndex);
        final BigDecimal fromEntityBalance = fromEntity.getBalance();

        final int toRandomIndex = ThreadLocalRandom.current().nextInt(midPoint, accountEntities.size());
        final AccountEntity toEntity = accountEntities.get(toRandomIndex);
        final BigDecimal toEntityBalance = toEntity.getBalance();

        final double randomAmount = ThreadLocalRandom.current().nextDouble(0, fromEntityBalance.doubleValue());
        final BigDecimal transferAmount = new BigDecimal(randomAmount).setScale(2, RoundingMode.DOWN);

        final TransferResult transferResult = service
            .transfer(fromEntity.getAccountNumber(), toEntity.getAccountNumber(), transferAmount);

        final Account fromAccountStatus = transferResult.getFromAccount();
        final Account toAccountStatus = transferResult.getToAccount();

        assertEquals(fromEntity.getAccountNumber(), fromAccountStatus.getAccountNumber());
        assertEquals(fromEntityBalance.subtract(transferAmount), fromAccountStatus.getBalance());

        assertEquals(toEntity.getAccountNumber(), toAccountStatus.getAccountNumber());
        assertEquals(toEntityBalance.add(transferAmount), toAccountStatus.getBalance());
    }

    @Test
    void transferNotEnoughBalance() {
        final int midPoint = accountEntities.size() / 2;

        final int fromRandomIndex = ThreadLocalRandom.current().nextInt(0, midPoint);
        final AccountEntity fromEntity = accountEntities.get(fromRandomIndex);
        final BigDecimal fromEntityBalance = fromEntity.getBalance();

        final int toRandomIndex = ThreadLocalRandom.current().nextInt(midPoint, accountEntities.size());
        final AccountEntity toEntity = accountEntities.get(toRandomIndex);

        final double randomAmount = ThreadLocalRandom.current()
            .nextDouble(fromEntityBalance.doubleValue() + 1, fromEntityBalance.doubleValue() + 5000);
        final BigDecimal transferAmount = new BigDecimal(randomAmount).setScale(2, RoundingMode.DOWN);

        assertThrows(Problem.class,
            () -> service.transfer(fromEntity.getAccountNumber(), toEntity.getAccountNumber(), transferAmount));
    }

    @Test
    void transferNegativeAmount() {
        final int midPoint = accountEntities.size() / 2;

        final int fromRandomIndex = ThreadLocalRandom.current().nextInt(0, midPoint);
        final AccountEntity fromEntity = accountEntities.get(fromRandomIndex);

        final int toRandomIndex = ThreadLocalRandom.current().nextInt(midPoint, accountEntities.size());
        final AccountEntity toEntity = accountEntities.get(toRandomIndex);

        final BigDecimal transferAmount = new BigDecimal(-5).setScale(2, RoundingMode.DOWN);

        assertThrows(Problem.class,
            () -> service.transfer(fromEntity.getAccountNumber(), toEntity.getAccountNumber(), transferAmount));
    }

    @Test
    void transferFromAccountDoesNotExist() {
        final int toRandomIndex = ThreadLocalRandom.current().nextInt(0, accountEntities.size());
        final AccountEntity toEntity = accountEntities.get(toRandomIndex);

        assertThrows(Problem.class,
            () -> service.transfer("does not exists", toEntity.getAccountNumber(), BigDecimal.TEN));
    }

    @Test
    void transferToAccountDoesNotExist() {
        final int toRandomIndex = ThreadLocalRandom.current().nextInt(0, accountEntities.size());
        final AccountEntity fromEntity = accountEntities.get(toRandomIndex);

        assertThrows(Problem.class,
            () -> service.transfer(fromEntity.getAccountNumber(), "does not exists", BigDecimal.TEN));
    }

    @Test
    void transferZeroAmount() {
        final int midPoint = accountEntities.size() / 2;

        final int fromRandomIndex = ThreadLocalRandom.current().nextInt(0, midPoint);
        final AccountEntity fromEntity = accountEntities.get(fromRandomIndex);

        final int toRandomIndex = ThreadLocalRandom.current().nextInt(midPoint, accountEntities.size());
        final AccountEntity toEntity = accountEntities.get(toRandomIndex);

        assertThrows(Problem.class,
            () -> service.transfer(fromEntity.getAccountNumber(), toEntity.getAccountNumber(), BigDecimal.ZERO));
    }
}
