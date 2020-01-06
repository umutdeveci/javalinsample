package info.umutdeveci.controller.response;

import info.umutdeveci.model.Account;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferResponse {

    private Account fromAccount;
    private Account toAccount;
}
