package info.umutdeveci.service.model;

import info.umutdeveci.model.Account;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferResult {

    private Account fromAccount;
    private Account toAccount;
}
