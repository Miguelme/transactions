package transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import transaction.dto.Statistics;
import transaction.dto.Transaction;

import java.io.IOException;
import java.time.Instant;

import static org.junit.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class TransactionControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    private static final String TRANSACTION_URL = "/transaction";
    private static final String STATISTICS_URL = "/statistics";
    private static final Double FIRST_AMOUNT = 100D;
    private static final Double SECOND_AMOUNT = 200D;
    private static final Double THIRD_AMOUNT = 300D;
    private static final Transaction FIRST_TRANSACTION = Transaction.builder().amount(FIRST_AMOUNT).build();
    private static final Transaction SECOND_TRANSACTION = Transaction.builder().amount(SECOND_AMOUNT).build();
    private static final Transaction THIRD_TRANSACTION = Transaction.builder().amount(THIRD_AMOUNT).build();
    private static final Statistics THREE_TRANSACTION_STATISTICS = Statistics.builder()
            .avg(200D).sum(600D).max(THIRD_AMOUNT).min(FIRST_AMOUNT).count(3L).build();

    private static final Statistics INITIAL_STATISTICS = Statistics.builder()
            .avg(0D).sum(0D).max(Double.MIN_VALUE).min(Double.MAX_VALUE).count(0L).build();

    private static final Long MINUTE_IN_MILLIS = 60 * 1000L;
    private static final HttpStatus FAILED_STATUS_CODE = HttpStatus.NO_CONTENT;
    private static final HttpStatus SUCCESS_STATUS_CODE = HttpStatus.OK;

    @Autowired
    private ObjectMapper objectMapper;


    private ResponseEntity<String> postTransaction(Transaction transaction) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return testRestTemplate.exchange(TRANSACTION_URL, HttpMethod.POST, new HttpEntity<Object>(transaction, headers), String.class);
    }

    private ResponseEntity<String> getStatistics() {
        return testRestTemplate.exchange(STATISTICS_URL, HttpMethod.GET, null, String.class);
    }

    private void validatePostTransaction(Transaction transaction, HttpStatus status) {
        ResponseEntity<String> response = postTransaction(transaction);
        assertEquals("Expected " + status, status, response.getStatusCode());
    }

    @Test
    public void testOldTransaction() {
        Long nowInMillis = Instant.now().toEpochMilli();
        Long oldTimeInMillis = nowInMillis - MINUTE_IN_MILLIS;
        FIRST_TRANSACTION.setTimestamp(oldTimeInMillis);
        validatePostTransaction(FIRST_TRANSACTION, FAILED_STATUS_CODE);
    }

    @Test
    public void testStatistics() throws InterruptedException, IOException {
        Statistics initialResponse = objectMapper.readValue(getStatistics().getBody(), Statistics.class);
        assertEquals("Expected statistic to be all inclusive", INITIAL_STATISTICS, initialResponse);

        Long nowInMillis = Instant.now().toEpochMilli();
        FIRST_TRANSACTION.setTimestamp(nowInMillis);
        SECOND_TRANSACTION.setTimestamp(nowInMillis);
        THIRD_TRANSACTION.setTimestamp(nowInMillis);
        validatePostTransaction(FIRST_TRANSACTION, SUCCESS_STATUS_CODE);
        validatePostTransaction(SECOND_TRANSACTION, SUCCESS_STATUS_CODE);
        validatePostTransaction(THIRD_TRANSACTION, SUCCESS_STATUS_CODE);

        Thread.sleep(10000);
        Statistics allTransactionsResponse = objectMapper.readValue(getStatistics().getBody(), Statistics.class);
        assertEquals("Expected statistic to be all inclusive", THREE_TRANSACTION_STATISTICS, allTransactionsResponse);

        Thread.sleep(MINUTE_IN_MILLIS);
        assertEquals("Expected statistic to be all inclusive", INITIAL_STATISTICS, initialResponse);

    }


}
