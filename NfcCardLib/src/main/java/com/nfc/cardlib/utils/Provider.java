package com.nfc.cardlib.utils;

import android.nfc.tech.IsoDep;
import android.util.Log;

import java.io.IOException;

import com.nfc.cardlib.exceptions.CommunicationException;

import com.nfc.cardlib.parser.IProvider;


public class Provider implements IProvider {

    private StringBuffer log = new StringBuffer();

    private IsoDep mTagCom;

    public void setISODep(final IsoDep mTagCom) {
        this.mTagCom = mTagCom;
    }


    @Override
    public byte[] transceive(byte[] command) throws CommunicationException {
        byte[] response;
        try {
            // send command to emv card
            response = mTagCom.transceive(command);
            Log.d("@Provider",BytesUtils.bytesToString(response).replace(" ",""));
        } catch (IOException e) {
            throw new CommunicationException(e.getMessage());
        }

        //log.append(BytesUtils.bytesToString(response).replace(" ",""));

       /* try {
            //SwEnum val = SwEnum.getSW(response);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        return response;
    }
}
