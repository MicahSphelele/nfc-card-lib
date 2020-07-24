package com.nfc.cardlib.utils;


import android.util.Log;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Sphelele Micah Ngubane on 2019/11/12
 */

public final class AtrUtils {

    private static String TAG = "@IkhokhaNfcSDK";
    /**
     * MultiMap containing ATR
     */
    private static final MultiMap<String, String> MAP = new MultiValueMap<String, String>();
    static {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            is = AtrUtils.class.getResourceAsStream("/smartcard_list.txt");
            if(is!=null)
            isr = new InputStreamReader(is, CharEncoding.UTF_8);
            if (isr!=null)
            br = new BufferedReader(isr);

            int lineNumber = 0;
            String line;
            String currentATR = null;
            while ((line = br.readLine()) != null) {
                ++lineNumber;
                if (line.startsWith("#") || line.trim().length() == 0) { // comment ^#/ empty line ^$/
                    continue;
                } else if (line.startsWith("\t") && currentATR != null) {
                    MAP.put(currentATR, line.replace("\t", "").trim());
                } else if (line.startsWith("3")) { // ATR hex
                    currentATR = StringUtils.deleteWhitespace(line.toUpperCase());
                } else {
                    Log.d(TAG, "Encountered unexpected line in atr list: currentATR=" + currentATR + " Line(" + lineNumber
                            + ") = " + line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(isr);
            IOUtils.closeQuietly(is);
        }
    }
}
