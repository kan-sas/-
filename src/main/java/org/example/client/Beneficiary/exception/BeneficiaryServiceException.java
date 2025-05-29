package org.example.client.Beneficiary.exception;

public class BeneficiaryServiceException extends Exception {
    public BeneficiaryServiceException(String message) {
        super(message);
    }

    public BeneficiaryServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
