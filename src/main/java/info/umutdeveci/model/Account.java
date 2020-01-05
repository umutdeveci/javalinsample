package info.umutdeveci.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Represents an account. Instances of this class are expected to be used as data transfer objects.
 */
@Data
@Builder
@AllArgsConstructor
public class Account {

    private String accountNumber;
    private BigDecimal balance;
}
