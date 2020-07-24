package com.nfc.cardlib.exceptions;


public class TlvException extends RuntimeException {

    /**
     * Constructor using field
     *
     * @param cause
     *            cause
     */
    public TlvException(final String cause) {
        super(cause);
    }
}
