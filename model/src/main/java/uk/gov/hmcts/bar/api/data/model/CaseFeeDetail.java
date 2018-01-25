package uk.gov.hmcts.bar.api.data.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CaseFeeDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CASE_FEE_ID")
    @JsonProperty(access= JsonProperty.Access.READ_ONLY)
	private int caseFeeId;
	
	private Integer caseReferenceId;
	
	private String feeCode;
	
	private Integer amount;
	
	private String feeDescription;
	
	private String feeVersion;
	
	private Integer remissionAmount;
	
	private String remissionBenefiter;
	
	private String remissionAuthorisation;
	
	private Integer refundAmount;
	
	@JsonCreator
    @Builder(builderMethodName = "caseFeeDetailWith")
	public CaseFeeDetail(@JsonProperty("case_reference_id") Integer caseReferenceId,
			@JsonProperty("fee_code") String feeCode,
            @JsonProperty("amount") Integer amount,
            @JsonProperty("fee_description") String feeDescription,
            @JsonProperty("fee_version") String feeVersion,
            @JsonProperty("remission_amount") Integer remissionAmount,
            @JsonProperty("remission_benefiter") String remissionBenefiter,
            @JsonProperty("remission_authorisation") String remissionAuthorisation,
            @JsonProperty("refund_amount") Integer refundAmount) {

		this.caseReferenceId = caseReferenceId;
		this.amount = amount;
		this.feeCode = feeCode;
		this.feeDescription = feeDescription;
		this.feeVersion =  feeVersion;
		this.remissionAmount = remissionAmount;
		this.remissionBenefiter = remissionBenefiter;
		this.remissionAuthorisation = remissionAuthorisation;
		this.refundAmount = refundAmount;
	}
}