package uk.gov.hmcts.bar.api.componenttests;

import org.junit.Test;
import uk.gov.hmcts.bar.api.data.model.ChequePaymentInstruction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.bar.api.data.model.ChequePaymentInstruction.chequePaymentInstructionWith;


public class ChequeInstructionCrudComponentTest extends ComponentTestBase {


    @Test
    public void whenChequeInstructionDetails_thenCreateChequePaymentInstruction() throws Exception {
        ChequePaymentInstruction.ChequePaymentInstructionBuilder  proposedChequePaymentInstruction =chequePaymentInstructionWith()
            .payerName("Mr Payer Payer")
            .amount(500)
            .currency("GBP")
            .chequeNumber("000000")
            .sortCode("000000")
            .accountNumber("00000000");

        restActions
            .post("/cheques", proposedChequePaymentInstruction.build())
            .andExpect(status().isCreated())
            .andExpect(body().as(ChequePaymentInstruction.class, chequeItemDto -> {
                assertThat(chequeItemDto).isEqualToComparingOnlyGivenFields(
                    chequePaymentInstructionWith()
                        .payerName("Mr Payer Payer")
                        .amount(500)
                        .currency("GBP")
                        .chequeNumber("000000")
                        .sortCode("000000")
                        .accountNumber("00000000"));
            }));
    }


    @Test
    public void whenChequeInstructionWithInvalidSortCode_thenReturn400() throws Exception {
        ChequePaymentInstruction.ChequePaymentInstructionBuilder  proposedChequePaymentInstruction =chequePaymentInstructionWith()
            .payerName("Mr Payer Payer")
            .amount(500)
            .currency("GBP")
            .chequeNumber("000000")
            .sortCode("xxxxxx")
            .accountNumber("00000000");

        restActions
            .post("/cheques", proposedChequePaymentInstruction.build())
            .andExpect(status().isBadRequest())
            ;
    }
    @Test
    public void whenChequeInstructionWithInvalidAccountNumber_thenReturn400() throws Exception {
        ChequePaymentInstruction.ChequePaymentInstructionBuilder  proposedChequePaymentInstruction =chequePaymentInstructionWith()
            .payerName("Mr Payer Payer")
            .amount(500)
            .currency("GBP")
            .chequeNumber("000000")
            .sortCode("000000")
            .accountNumber("xxxxxxxx");

        restActions
            .post("/cheques", proposedChequePaymentInstruction.build())
            .andExpect(status().isBadRequest())
        ;
    }

    @Test
    public void whenChequeInstructionWithInvalidInstrumentNumber_thenReturn400() throws Exception {
        ChequePaymentInstruction.ChequePaymentInstructionBuilder  proposedChequePaymentInstruction =chequePaymentInstructionWith()
            .payerName("Mr Payer Payer")
            .amount(500)
            .currency("GBP")
            .chequeNumber("xxxxxx")
            .sortCode("000000")
            .accountNumber("00000000");

        restActions
            .post("/cheques", proposedChequePaymentInstruction.build())
            .andExpect(status().isBadRequest())
        ;
    }

    @Test
    public void whenChequeInstructionWithInvalidCurrency_thenReturn400() throws Exception {
        ChequePaymentInstruction.ChequePaymentInstructionBuilder  proposedChequePaymentInstruction =chequePaymentInstructionWith()
            .payerName("Mr Payer Payer")
            .amount(500)
            .currency("xxx")
            .chequeNumber("000000")
            .sortCode("000000")
            .accountNumber("00000000");

        restActions
            .post("/cheques", proposedChequePaymentInstruction.build())
            .andExpect(status().isBadRequest())
        ;
    }





}


