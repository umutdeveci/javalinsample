package info.umutdeveci.controller.request;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {

    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
}
