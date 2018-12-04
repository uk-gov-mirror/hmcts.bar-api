package uk.gov.hmcts.bar.api.converters;

import org.junit.Assert;
import org.junit.Test;

public class PaymentInstructionsCsvConverterTest {

    @Test
    public void testRead() {
        PaymentInstructionsCsvConverter converter = new PaymentInstructionsCsvConverter();
        Assert.assertEquals(0, converter.read(null, null, null).size());
    }

    @Test
    public void testReadInternal() {
        PaymentInstructionsCsvConverter converter = new PaymentInstructionsCsvConverter();
        Assert.assertEquals(0, converter.readInternal(null, null).size());
    }
}
