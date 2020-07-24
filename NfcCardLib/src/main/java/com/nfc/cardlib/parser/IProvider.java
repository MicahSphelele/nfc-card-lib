package com.nfc.cardlib.parser;

import com.nfc.cardlib.exceptions.CommunicationException;

/**
 * Interface for provider for transmit command to card
 */
public interface IProvider {
    /**
     * Method used to transmit and receive card response
     *
     * @param command
     *            command to send to card
     * @return byte array returned by card
     */
    byte[] transceive(byte[] command) throws CommunicationException;

}
