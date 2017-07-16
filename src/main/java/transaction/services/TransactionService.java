package transaction.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import transaction.constants.TransactionConstants;
import transaction.dto.Statistics;
import transaction.dto.Transaction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService implements Runnable{
    private List<Transaction> transactions = new ArrayList<>();
    private Long numberOfElements = 0L;
    private Double sumOfElements = 0D;
    private Double avgOfElements = 0D;
    private Double maxOfElements = Double.MIN_VALUE;
    private Double minOfElements = Double.MAX_VALUE;

    @Autowired
    private TaskScheduler taskScheduler;

    public void updateValue(Transaction transaction){
        transactions.add(transaction);
        taskScheduler.schedule(this, new Date(Instant.now().toEpochMilli()));
        taskScheduler.schedule(this, new Date(transaction.getTimestamp() + ((TransactionConstants.SECONDS_THRESHOLD) * TransactionConstants.SECONDS_IN_MILLISECONDS)));
    }

    public Statistics getStatistics() {
        return Statistics.builder()
                .count(numberOfElements)
                .max(maxOfElements)
                .min(minOfElements)
                .avg(avgOfElements)
                .sum(sumOfElements)
                .build();
    }

    public boolean isOlderThanSeconds(Long timestamp, Long numberOfSeconds){
        Long currentTime = Instant.now().toEpochMilli();
        Long secondsPassed = (currentTime - timestamp) / TransactionConstants.SECONDS_IN_MILLISECONDS;


        return secondsPassed >= numberOfSeconds;
    }

    public void calculateValues() {
        synchronized (this) {
            sumOfElements = 0D;
            avgOfElements = 0D;
            maxOfElements = Double.MIN_VALUE;
            minOfElements = Double.MAX_VALUE;

            List<Transaction> activeTransactions = transactions.stream()
                    .filter(transaction1 -> !isOlderThanSeconds(transaction1.getTimestamp(), TransactionConstants.SECONDS_THRESHOLD))
                    .collect(Collectors.toList());
            activeTransactions.forEach(activeTransaction -> {
                sumOfElements += activeTransaction.getAmount();
                maxOfElements = Math.max(maxOfElements, activeTransaction.getAmount());
                minOfElements = Math.min(minOfElements, activeTransaction.getAmount());
            });

            numberOfElements = Long.valueOf(activeTransactions.size());
            avgOfElements = numberOfElements.equals(0D) ? 0D : sumOfElements / numberOfElements;
            transactions = new ArrayList<>(activeTransactions);
        }
    }

    public void run(){
        calculateValues();

    }
}
