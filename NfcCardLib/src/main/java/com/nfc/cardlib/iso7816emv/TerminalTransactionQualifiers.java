package com.nfc.cardlib.iso7816emv;

import java.util.Arrays;

import com.nfc.cardlib.utils.BytesUtils;

public class TerminalTransactionQualifiers {
    private byte[] data = new byte[4];

    public TerminalTransactionQualifiers() {
    }

    public boolean contactlessMagneticStripeSupported() {
        return BytesUtils.matchBitByBitIndex(data[0], 7);
    }

    public boolean contactlessVSDCsupported() {
        return BytesUtils.matchBitByBitIndex(data[0], 6);
    }

    public boolean contactlessEMVmodeSupported() {
        return BytesUtils.matchBitByBitIndex(data[0], 5);
    }

    public boolean contactEMVsupported() {
        return BytesUtils.matchBitByBitIndex(data[0], 4);
    }

    public boolean readerIsOfflineOnly() {
        return BytesUtils.matchBitByBitIndex(data[0], 3);
    }

    public boolean onlinePINsupported() {
        return BytesUtils.matchBitByBitIndex(data[0], 2);
    }

    public boolean signatureSupported() {
        return BytesUtils.matchBitByBitIndex(data[0], 1);
    }

    public boolean onlineCryptogramRequired() {
        return BytesUtils.matchBitByBitIndex(data[1], 7);
    }

    public boolean cvmRequired() {
        return BytesUtils.matchBitByBitIndex(data[1], 6);
    }

    public boolean contactChipOfflinePINsupported() {
        return BytesUtils.matchBitByBitIndex(data[1], 5);
    }

    public boolean issuerUpdateProcessingSupported() {
        return BytesUtils.matchBitByBitIndex(data[2], 7);
    }

    public boolean consumerDeviceCVMsupported() {
        return BytesUtils.matchBitByBitIndex(data[2], 6);
    }

    public void setContactlessMagneticStripeSupported(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 7, value);
    }

    public void setContactlessVSDCsupported(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 6, value);
        if (value) {
            /*
             * A reader that supports contactless VSDC in addition to qVSDC shall not indicate support for qVSDC in the Terminal
             * Transaction Qualifiers (set byte 1 bit 6 to b'0'). The reader shall restore this bit to b'1' prior to deactivation
             */
            setContactlessEMVmodeSupported(false);
        }
    }

    public void setContactlessEMVmodeSupported(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 5, value);
    }

    public void setContactEMVsupported(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 4, value);
    }

    public void setReaderIsOfflineOnly(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 3, value);
    }

    public void setOnlinePINsupported(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 2, value);
    }

    public void setSignatureSupported(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 1, value);
    }

    public void setOnlineCryptogramRequired(final boolean value) {
        data[1] = BytesUtils.setBit(data[1], 7, value);
    }

    public void setCvmRequired(final boolean value) {
        data[1] = BytesUtils.setBit(data[1], 6, value);
    }

    public void setContactChipOfflinePINsupported(final boolean value) {
        data[1] = BytesUtils.setBit(data[1], 5, value);
    }

    public void setIssuerUpdateProcessingSupported(final boolean value) {
        data[2] = BytesUtils.setBit(data[2], 7, value);
    }

    public void setConsumerDeviceCVMsupported(final boolean value) {
        data[2] = BytesUtils.setBit(data[2], 6, value);
    }

    // The rest of the bits in the second byte are RFU (Reserved for Future Use)

    public byte[] getBytes() {
        return Arrays.copyOf(data, data.length);
    }
}
