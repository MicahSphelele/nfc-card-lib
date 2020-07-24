package com.nfc.cardlib.exceptions;


import java.io.IOException;

public class CommunicationException extends IOException {

    /**
     * Default constructor
     *
     * @param message
     *            Exception message
     */
    public CommunicationException(final String message) {
        super(message);
    }

}
