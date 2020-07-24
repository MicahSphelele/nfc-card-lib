package com.sphe.nfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.nfc.cardlib.model.Card;
import com.nfc.cardlib.parser.EmvParser;
import com.nfc.cardlib.response.TransactionListener;
import com.nfc.cardlib.utils.Constants;
import com.nfc.cardlib.utils.IkNfcUtils;
import com.nfc.cardlib.utils.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TlvCardProcessor extends AsyncTask<Void,Void,Object> {

    private Provider mProvider = new Provider();
    private boolean mException;
    private Card mCard;
    private NfcListener mListener;
    private TransactionListener mTransactionListener;
    private Tag mTag;
    private IkNfcUtils mIkNfcUtils;
    private float amount;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private TlvCardProcessor(Builder builder){
        mTag = builder.tag;
        mIkNfcUtils = builder.ikNfcUtils;
        amount = builder.amount;


        if (mTag != null) {
            mListener = builder.listener;
            mTransactionListener = builder.transactionListener;
            try {

                if (mTag.toString().equals(new String(Constants.NFC_TAG_A, StandardCharsets.UTF_8))
                        || mTag.toString().equals(new String(Constants.NFC_TAG_B, StandardCharsets.UTF_8)) || mTag.toString().equals(new String(Constants.NFC_TAG_CLASSIC)) ) {
                    execute();
                } else {
                    if (!builder.fromStart) {
                        mListener.onUnknownEmvCard();
                    }
                    clearAll();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onStartNfc();
    }

    @Override
    protected Object doInBackground(Void... voids) {
        Object result = null;

        try {
            doInBackground();
        } catch (Exception e) {
            result = e;
        }

        return result;

    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (!mException) {
            if (mCard != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (StringUtils.isNotBlank(new String(mCard.getCardNumber(), StandardCharsets.UTF_8))) {
                        Log.d("@NFC_SDK","FOUND : " + mCard.getType());
                        switch (mCard.getType()){
                            case VISA:
                            case NAB_VISA:
                                //TODO()
                                break;
                            case MASTER_CARD:
                                Log.d("@NFC_SDK","FOUND : " + mCard.getType());
                                //new MasterCardSdk(mIkNfcUtils.getActivity(),mIkNfcUtils,mTransactionListener).startTransaction(mTag,amount);
                                break;

                        }

                    } else if (mCard.isNfcLocked()) {
                        mListener.onCardWithLockedNfc();
                    }
                }
            } else {
                mListener.onUnknownEmvCard();
            }
        } else {
            mListener.onDoNotMoveCardSoFast();
        }
        mListener.onFinishNfcReadCard();
        clearAll();
    }

    private void doInBackground(){
        IsoDep mIsoDep = IsoDep.get(mTag);
        if (mIsoDep == null) {
            mListener.onDoNotMoveCardSoFast();
            return;
        }
        mException = false;

        try {
            // Open tag connection
            mIsoDep.connect();

            mProvider.setISODep(mIsoDep);

            EmvParser parser = new EmvParser(mProvider, true);
            mCard = parser.readEmvCard(TlvCardProcessor.class);
        } catch (IOException e) {
            mException = true;
        } finally {
            //Close tag connection
            IOUtils.closeQuietly(mIsoDep);
        }
    }

    private void clearAll() {
        mTransactionListener = null;
        mListener = null;
        mProvider = null;
        mCard = null;
        mTag = null;
    }

    public static class Builder {
        //private Activity activity;
        private TransactionListener transactionListener;
        private NfcListener listener;
        private Tag tag;
        private IkNfcUtils ikNfcUtils;
        private boolean fromStart;
        private float amount;


        public Builder(TransactionListener transactionListener, NfcListener listener, Intent intent, IkNfcUtils ikNfcUtils , boolean fromStart, float amount) {
            //this.activity=activity;
            this.transactionListener= transactionListener;
            this.listener = listener;
            this.tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            this.ikNfcUtils = ikNfcUtils;
            this.fromStart = fromStart;
            this.amount = amount;
        }

        @SuppressWarnings("UnusedReturnValue")
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public TlvCardProcessor build() {
            return new TlvCardProcessor(this);
        }
    }

    public interface NfcListener{
        void onStartNfc();

        void onDoNotMoveCardSoFast();

        void onUnknownEmvCard();

        void onCardWithLockedNfc();

        void onFinishNfcReadCard();
    }
}
