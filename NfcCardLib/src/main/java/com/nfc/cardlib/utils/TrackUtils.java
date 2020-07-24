package com.nfc.cardlib.utils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.devnied.bitlib.BytesUtils;

import com.nfc.cardlib.iso7816emv.EmvTags;
import com.nfc.cardlib.model.Card;
import com.nfc.cardlib.model.Service;


public final class TrackUtils {

    /**
     * Track 2 pattern
     */
    private static final Pattern TRACK2_PATTERN = Pattern.compile("([0-9]{1,19})D([0-9]{4})([0-9]{3})?(.*)");
    /**
     * Extract track 2 data
     *
     * @param pEmvCard
     *            Object card representation
     * @param pData
     *            data to parse
     * @return true if the extraction succeed false otherwise
     */
    public static boolean extractTrack2Data(final Card pEmvCard, final byte[] pData) {
        boolean ret = false;
        byte[] track2 = TlvUtil.getValue(pData, EmvTags.TRACK_2_EQV_DATA, EmvTags.TRACK2_DATA);

        if (track2 != null) {
            String data = BytesUtils.bytesToStringNoSpace(track2);
            Matcher m = TRACK2_PATTERN.matcher(data);
            // Check pattern
            if (m.find()) {
                // read card number

                pEmvCard.setCardNumber(Objects.requireNonNull(m.group(1)).getBytes());

                // Read expire date
                //String month = m.group(2).substring(2,4);
                //String year = m.group(2).substring(0,2);
                //pEmvCard.setExpireDate(month+"/"+year);
                // Read service
                pEmvCard.setService(new Service(m.group(3)));
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Private constructor
     */
    private TrackUtils() {
    }
}