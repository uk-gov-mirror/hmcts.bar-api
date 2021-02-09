package uk.gov.hmcts.bar.api.data.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public interface PaymentInstructionStats {

    String getName();
    Integer getCount();
    String getStatus();
    Long getTotalAmount();
    String getPaymentType();
    String getBgc();
    String getAction();
    String getUserId();
}
