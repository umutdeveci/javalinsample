package info.umutdeveci.util;

import info.umutdeveci.service.entity.AccountEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Utils {

    private Utils() {
    }

    public static List<AccountEntity> generateRandomAccounts(final int size) {
        return ThreadLocalRandom.current().doubles(size, 0, 7500)
            .mapToObj(random -> new AccountEntity(UUID.randomUUID().toString(),
                new BigDecimal(random).setScale(2, RoundingMode.DOWN)))
            .collect(Collectors.toList());
    }

}
