package info.umutdeveci.service.entity;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Represents an account record in the database. Expected to be used by service only.
 */
@Data
@Builder
@AllArgsConstructor
public class AccountEntity {

    private String accountNumber;
    private BigDecimal balance;
}
