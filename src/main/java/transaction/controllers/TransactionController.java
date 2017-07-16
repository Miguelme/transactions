package transaction.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import transaction.constants.TransactionConstants;
import transaction.dto.Statistics;
import transaction.dto.Transaction;
import transaction.services.TransactionService;

@RestController
public class TransactionController {


    @Autowired
    private TransactionService transactionService;

    @RequestMapping(method = RequestMethod.POST, value = "transaction")
    public ResponseEntity<?> postTransaction(@Validated @RequestBody Transaction transaction) {
        if (transactionService.isOlderThanSeconds(transaction.getTimestamp(), TransactionConstants.SECONDS_THRESHOLD)) {
            return ResponseEntity.noContent().build();
        } else {
            transactionService.updateValue(transaction);
            return ResponseEntity.ok().build();
        }
    }


    @RequestMapping(method = RequestMethod.GET, value = "statistics")
    public Statistics getStatistics() {
        return transactionService.getStatistics();
    }
}