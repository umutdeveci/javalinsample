package info.umutdeveci.controller.request;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DepositRequest {

    private BigDecimal amount;
}
