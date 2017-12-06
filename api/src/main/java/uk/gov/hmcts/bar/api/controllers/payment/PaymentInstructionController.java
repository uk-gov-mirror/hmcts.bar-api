package uk.gov.hmcts.bar.api.controllers.payment;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import uk.gov.hmcts.bar.api.controllers.helper.PaymentInstructionControllerHelper;
import uk.gov.hmcts.bar.api.data.exceptions.PaymentInstructionNotFoundException;
import uk.gov.hmcts.bar.api.data.model.AllPayPaymentInstruction;
import uk.gov.hmcts.bar.api.data.model.CashPaymentInstruction;
import uk.gov.hmcts.bar.api.data.model.ChequePaymentInstruction;
import uk.gov.hmcts.bar.api.data.model.PaymentInstruction;
import uk.gov.hmcts.bar.api.data.model.PostalOrderPaymentInstruction;
import uk.gov.hmcts.bar.api.data.service.PaymentInstructionService;

@RestController
@Validated
public class PaymentInstructionController {

	private final PaymentInstructionService paymentInstructionService;

	@Autowired
	public PaymentInstructionController(PaymentInstructionService paymentInstructionService) {
		this.paymentInstructionService = paymentInstructionService;

	}

	@ApiOperation(value = "Get all current payment instructions", notes = "Get all current payment instructions for a given site.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Return all current payment instructions"),
			@ApiResponse(code = 404, message = "Payment instructions not found"),
			@ApiResponse(code = 500, message = "Internal server error") })
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/payment-instructions")
	public List<PaymentInstruction> getPaymentInstructions(
			@RequestParam(name = "status", required = false) String status,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "ddMMyyyy") Date startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "ddMMyyyy") Date endDate) {

		LocalDateTime paramStartDate = null;
		LocalDateTime paramEndDate = null;

		if (startDate != null) {
			paramStartDate = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault()).toLocalDate()
					.atStartOfDay();
		}

		if (endDate != null) {
			paramEndDate = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault()).toLocalDate()
					.atTime(LocalTime.now());
		}

		return PaymentInstructionControllerHelper.updateStatusDisplayValue(
				paymentInstructionService.getAllPaymentInstructions(status, paramStartDate, paramEndDate));
	}

	@ApiOperation(value = "Delete payment instruction", notes = "Delete payment instruction with the given id.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "Payment instruction deleted"),
			@ApiResponse(code = 404, message = "Payment instruction not found"),
			@ApiResponse(code = 500, message = "Internal server error") })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/payment-instructions/{id}")
	public ResponseEntity<?> deletePaymentInstruction(@PathVariable("id") Integer id) {
		try {
			paymentInstructionService.deleteCurrentPaymentInstructionWithDraftStatus(id);
		} catch (PaymentInstructionNotFoundException pinfe) {

		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);

	}

	@ApiOperation(value = "Create cheque payment instruction", notes = "Create cheque payment instruction with the given values.")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Cheque payment instruction created"),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 500, message = "Internal server error") })
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/cheques")
	public PaymentInstruction saveChequeInstruction(
			@Valid @RequestBody ChequePaymentInstruction chequePaymentInstruction) {
		return paymentInstructionService.createPaymentInstruction(chequePaymentInstruction);
	}

	@ApiOperation(value = "Create cash payment instruction", notes = "Create cash payment instruction with the given values.")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Cash payment instruction created"),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 500, message = "Internal server error") })
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/cash")
	public PaymentInstruction saveCashInstruction(@Valid @RequestBody CashPaymentInstruction cashPaymentInstruction) {
		return paymentInstructionService.createPaymentInstruction(cashPaymentInstruction);
	}

	@ApiOperation(value = "Create poatal order payment instruction", notes = "Create postal order payment instruction with the given values.")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Postal order payment instruction created"),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 500, message = "Internal server error") })
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/postal-orders")
	public PaymentInstruction savePostalOrderInstruction(
			@Valid @RequestBody PostalOrderPaymentInstruction postalOrderPaymentInstruction) {
		return paymentInstructionService.createPaymentInstruction(postalOrderPaymentInstruction);
	}

	@ApiOperation(value = "Create allpay payment instruction", notes = "Create allpay payment instruction with the given values.")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "AllPay payment instruction created"),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 500, message = "Internal server error") })
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/allpay")
	public PaymentInstruction saveAllPayInstruction(
			@Valid @RequestBody AllPayPaymentInstruction allPayPaymentInstruction) {
		return paymentInstructionService.createPaymentInstruction(allPayPaymentInstruction);
	}

}
