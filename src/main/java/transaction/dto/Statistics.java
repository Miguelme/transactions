package transaction.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Statistics {
    private Double sum;
    private Double avg;
    private Double max;
    private Double min;
    private Long count;
}
