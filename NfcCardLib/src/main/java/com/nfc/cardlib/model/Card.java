package com.nfc.cardlib.model;

import com.nfc.cardlib.enums.EmvCardScheme;

import java.util.Arrays;
/**
 * Bean used to describe data in EMV card
 *
 */
public class Card extends AbstractData {

    /**
     * Generated serial UID
     */
    private static final long serialVersionUID = 736740432469989941L;

    /**
     * Card number
     */
    private byte[] cardNumber;

    /**
     * Card services
     */
    private Service service;

    public void setService(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }

    /**
     * Card type
     */
    private EmvCardScheme type;

    /**
     * Indicate if the nfc is locked on the card
     */
    private boolean nfcLocked;


    /**
     * Method used to get the field cardNumber
     *
     * @return the cardNumber
     */
    public byte [] getCardNumber() {
        return cardNumber;
    }

    /**
     * Setter for the field cardNumber
     *
     * @param cardNumber
     *            the cardNumber to set
     */
    public void setCardNumber(final byte[] cardNumber) {
        this.cardNumber = cardNumber;
    }


    /**
     * Method used to get the field type
     *
     * @return the type
     */
    public EmvCardScheme getType() {
        return type;
    }

    /**
     * Setter for the field type
     *
     * @param type
     *            the type to set
     */
    public void setType(final EmvCardScheme type) {
        this.type = type;
    }

    @Override
    public boolean equals(final Object arg0) {
        return arg0 instanceof Card && cardNumber != null && Arrays.equals(cardNumber, ((Card) arg0).getCardNumber());
    }


    /**
     * Method used to get the field nfcLocked
     *
     * @return the nfcLocked
     */
    public boolean isNfcLocked() {
        return nfcLocked;
    }

    /**
     * Setter for the field nfcLocked
     *
     * @param nfcLocked
     *            the nfcLocked to set
     */
    public void setNfcLocked(final boolean nfcLocked) {
        this.nfcLocked = nfcLocked;
    }
}
