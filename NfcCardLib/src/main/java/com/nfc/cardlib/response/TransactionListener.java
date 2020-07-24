package com.nfc.cardlib.response;

/**
 * Interface used to handle transaction events and response
 * Interface may be optimized.
 */
public interface TransactionListener {

    /**
     * Method start progress if required of the transaction
     *
     */
    void onTransactionStart();
    /**
     * Method used to handle transaction response
     *
     * @param status of the transaction
     */
    void onTransactionResponse(TransactionStatus status);
    /**
     * Method used to handle terminal response
     *
     * @param error of the terminal should there be a problem with the terminal. Can also be used to handle api error response
     */
    void onTransactionError(String error);

    //TODO("We may need to implement a different listener for pin on glass. Any sensitive data will be in a byte array")

}
