package com.nfc.cardlib.parser.apdu;

import java.util.List;

import com.nfc.cardlib.iso7816emv.TagAndLength;


/**
 * Interface for File to parse
 */
public interface IFile {
    /**
     * Method to parse byte data
     *
     * @param pData
     *            byte to parse
     * @param pList
     *            Tag and length
     */
    void parse(final byte[] pData, final List<TagAndLength> pList);
}
