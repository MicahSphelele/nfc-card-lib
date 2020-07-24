package com.nfc.cardlib.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;

/**
 * Created by Sphelele Micah Ngubane on 2019/11/12
 */
public class IkNfcUtils {


    private final NfcAdapter mNfcAdapter;
    private final PendingIntent mPendingIntent;
    private final Activity mActivity;
    private static final IntentFilter[] INTENT_FILTER = new IntentFilter[] {
            new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};
    private static final int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A |
            NfcAdapter.FLAG_READER_NFC_B |
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK |
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS;

    private static final String[][] TECH_LIST = new String[][] { {
            NfcA.class.getName(), IsoDep.class.getName() } };

    public IkNfcUtils(final Activity pActivity) {
        mActivity = pActivity;
        mNfcAdapter = NfcAdapter.getDefaultAdapter(mActivity);
        mPendingIntent = PendingIntent.getActivity(mActivity, 0,
                new Intent(mActivity, mActivity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    public boolean isNFCEnabled() {
        return mNfcAdapter!= null && mNfcAdapter.isEnabled();
    }

    public void disableDispatch() {
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(mActivity);
        }
    }

    public void enableDispatch() {
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(mActivity, mPendingIntent, INTENT_FILTER, TECH_LIST);
        }
    }

    public Activity getActivity() {
        return this.mActivity;
    }
}
