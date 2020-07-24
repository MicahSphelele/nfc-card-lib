package com.nfc.cardlib.parser;

import android.os.Build;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import fr.devnied.bitlib.BytesUtils;
import com.nfc.cardlib.enums.CommandEnum;
import com.nfc.cardlib.enums.EmvCardScheme;
import com.nfc.cardlib.enums.SwEnum;
import com.nfc.cardlib.exceptions.CommunicationException;
import com.nfc.cardlib.iso7816emv.EmvTags;
import com.nfc.cardlib.iso7816emv.EmvTerminal;
import com.nfc.cardlib.iso7816emv.TLV;
import com.nfc.cardlib.iso7816emv.TagAndLength;
import com.nfc.cardlib.model.Afl;
import com.nfc.cardlib.model.Card;
import com.nfc.cardlib.utils.CommandApdu;
import com.nfc.cardlib.utils.ResponseUtils;
import com.nfc.cardlib.utils.TlvUtil;
import com.nfc.cardlib.utils.TrackUtils;

/**
 * Emv Parser.<br/>
 * Class used to read and parse EMV card
 */
public class EmvParser {

    /**
     * Class Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EmvParser.class);

    /**
     * PPSE directory "2PAY.SYS.DDF01"
     */
    private static final byte[] PPSE = "2PAY.SYS.DDF01".getBytes();

    /**
     * PSE directory "1PAY.SYS.DDF01"
     */
    private static final byte[] PSE = "1PAY.SYS.DDF01".getBytes();

    /**
     * Provider
     */
    private IProvider provider;

    /**
     * use contact less mode
     */
    private boolean contactLess;

    /**
     * Card data
     */
    private Card card;

    /**
     * Constructor
     *
     * @param pProvider
     *            provider to launch command
     * @param pContactLess
     *            boolean to indicate if the EMV card is contact less or not
     */
    public EmvParser(final IProvider pProvider, final boolean pContactLess) {
        provider = pProvider;
        contactLess = pContactLess;
        card = new Card();
    }

    /**
     * Method used to read public data from EMV card
     *
     * @return data read from card or null if any provider match the card type
     *@throws CommunicationException if possible
     */
    public Card readEmvCard(Class cls) throws CommunicationException {

        Log.d("@SDK","readEmvCard() " + cls.getSimpleName());
        // use PSE first
        if (!readWithPSE()) {
            Log.d("@SDK","!readWithPSE()="+!readWithPSE());
            // Find with AID
            readWithAID();
        }
        return card;
    }

    /**
     * Method used to select payment environment PSE or PPSE
     *
     * @return response byte array
     * @throws CommunicationException if possible
     */
    private byte[] selectPaymentEnvironment() throws CommunicationException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Select " + (contactLess ? "PPSE" : "PSE") + " Application");
        }
        // Select the PPSE or PSE directory
        return provider.transceive(new CommandApdu(CommandEnum.SELECT, contactLess ? PPSE : PSE, 0).toBytes());
    }

    /**
     * Method used to parse FCI Proprietary Template
     *
     * @param pData
     *            data to parse
     * @return pData
     * @throws CommunicationException if possible
     */
    private byte[] parseFCIProprietaryTemplate(final byte[] pData) throws CommunicationException {
        // Get SFI
        byte[] data = TlvUtil.getValue(pData, EmvTags.SFI);

        // Check SFI
        if (data != null) {
            int sfi = BytesUtils.byteArrayToInt(data);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("SFI found:" + sfi);
            }
            data = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, sfi, sfi << 3 | 4, 0).toBytes());
            // If LE is not correct
            if (ResponseUtils.isEquals(data, SwEnum.SW_6C)) {
                data = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, sfi, sfi << 3 | 4, data[data.length - 1]).toBytes());
            }
            return data;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("(FCI) Issuer Discretionary Data is already present");
        }
        return pData;
    }

    /**
     * Method used to extract application label
     *
     * @return decoded application label or null
     */
    private String extractApplicationLabel(final byte[] pData) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Extract Application label");
        }
        String label = null;
        byte[] labelByte = TlvUtil.getValue(pData, EmvTags.APPLICATION_LABEL);
        if (labelByte != null) {
            label = new String(labelByte);
        }
        return label;
    }

    /**
     * Read EMV card with Payment System Environment or Proximity Payment System
     * Environment
     * @return true is succeed false otherwise
     * @throws CommunicationException if possible
     */
    private boolean readWithPSE() throws CommunicationException {
        boolean ret = false;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to read card with Payment System Environment");
        }
        // Select the PPSE or PSE directory
        byte[] data = selectPaymentEnvironment();
        if (ResponseUtils.isSucceed(data)) {
            // Parse FCI Template
            data = parseFCIProprietaryTemplate(data);
            // Extract application label
            if (ResponseUtils.isSucceed(data)) {
                // Get Aids
                List<byte[]> aids = getAids(data);
                for (byte[] aid : aids) {
                    ret = extractPublicData(aid, extractApplicationLabel(data));
                    if (ret) {
                        break;
                    }
                }
                if (!ret) {
                    card.setNfcLocked(true);
                }
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((contactLess ? "PPSE" : "PSE") + " not found -> Use kown AID");
        }

        return ret;
    }

    /**
     * Method used to get the aid list, if the Kernel Identifier is defined, <br/>
     * this value need to be appended to the ADF Name in the data field of <br/>
     * the SELECT command.
     *
     * @param pData
     *            FCI proprietary template data
     * @return the Aid to select
     */
    private List<byte[]> getAids(final byte[] pData) {
        List<byte[]> ret = new ArrayList<>();
        List<TLV> listTlv = TlvUtil.getlistTLV(pData, EmvTags.AID_CARD, EmvTags.KERNEL_IDENTIFIER);
        for (TLV tlv : listTlv) {
            if (tlv.getTag() == EmvTags.KERNEL_IDENTIFIER && ret.size() != 0) {
                ret.add(ArrayUtils.addAll(ret.get(ret.size() - 1), tlv.getValueBytes()));
            } else {
                ret.add(tlv.getValueBytes());
            }
        }
        return ret;
    }

    /**
     * Read EMV card with AID
     * @throws CommunicationException if possible
     */
    private void readWithAID() throws CommunicationException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to read card with AID");
        }
        // Test each card from know EMV AID
        for (EmvCardScheme type : EmvCardScheme.values()) {
            for (byte[] aid : type.getAidByte()) {
                if (extractPublicData(aid, type.getName())) {
                    return;
                }
            }
        }
    }

    /**
     * Select application with AID or RID
     *
     * @param pAid
     *            byte array containing AID or RID
     * @return response byte array
     * @throws CommunicationException if possible
     */
    private byte[] selectAID(final byte[] pAid) throws CommunicationException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Select AID: " + BytesUtils.bytesToString(pAid));
        }
        return provider.transceive(new CommandApdu(CommandEnum.SELECT, pAid, 0).toBytes());
    }

    /**
     * Read public card data from parameter AID
     *
     * @param pAid
     *            card AID in bytes
     * @param pApplicationLabel
     *            application scheme (Application label)
     * @return true if succeed false otherwise
     */
    private boolean extractPublicData(final byte[] pAid, final String pApplicationLabel) throws CommunicationException {
        boolean ret = false;
        // Select AID
        byte[] data = selectAID(pAid);
        // check response
        if (ResponseUtils.isSucceed(data)) {
            // Parse select response
            ret = parse(data, provider);
            if (ret) {
                // Get AID
                String aid = BytesUtils.bytesToStringNoSpace(TlvUtil.getValue(data, EmvTags.DEDICATED_FILE_NAME));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Application label:" + pApplicationLabel + " with Aid:" + aid);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    card.setType(findCardScheme(aid, new String(card.getCardNumber(), StandardCharsets.UTF_8)));
                }
            }
        }
        return ret;
    }

    /**
     * Method used to find the real card scheme
     *
     * @param pAid
     *            card complete AID
     * @param pCardNumber
     *            card number
     * @return card scheme
     */
    private EmvCardScheme findCardScheme(final String pAid, final String pCardNumber) {
        EmvCardScheme type = EmvCardScheme.getCardTypeByAid(pAid);

        // Get real type for french card
        if (type == EmvCardScheme.CB) {
            type = EmvCardScheme.getCardTypeByCardNumber(pCardNumber);
            if (type != null) {
                LOGGER.debug("Real type:" + type.getName());
            }
        }
        return type;
    }

    /**
     * Method used to extract commons card data
     *
     * @param pGpo
     *            global processing options response
     */
    private boolean extractCommonsCardData(final byte[] pGpo) throws CommunicationException {
        boolean ret = false;
        // Extract data from Message Template 1
        byte [] data = TlvUtil.getValue(pGpo, EmvTags.RESPONSE_MESSAGE_TEMPLATE_1);
        if (data != null) {
            data = ArrayUtils.subarray(data, 2, data.length);
        } else { // Extract AFL data from Message template 2
            ret = TrackUtils.extractTrack2Data(card, pGpo);
            if (!ret) {
                data = TlvUtil.getValue(pGpo, EmvTags.APPLICATION_FILE_LOCATOR);
            }
        }

        if (data != null) {
            // Extract Afl
            List<Afl> listAfl = extractAfl(data);
            // for each AFL
            for (Afl afl : listAfl) {
                // check all records
                for (int index = afl.getFirstRecord(); index <= afl.getLastRecord(); index++) {
                    byte[] info = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, index, afl.getSfi() << 3 | 4, 0).toBytes());
                    if (ResponseUtils.isEquals(info, SwEnum.SW_6C)) {
                        info = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, index, afl.getSfi() << 3 | 4,
                                info[info.length - 1]).toBytes());
                    }

                    // Extract card data
                    if (ResponseUtils.isSucceed(info)) {
                        if (TrackUtils.extractTrack2Data(card, info)) {
                            return true;
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Method used to parse EMV card
     */
    private boolean parse(final byte[] pSelectResponse, final IProvider pProvider) throws CommunicationException {
        boolean ret = false;
        // Get TLV log entry
        //byte[] logEntry = getLogEntry(pSelectResponse);
        // Get PDOL
        byte[] pdol = TlvUtil.getValue(pSelectResponse, EmvTags.PDOL);
        // Send GPO Command
        byte[] gpo = getGetProcessingOptions(pdol, pProvider);

        // Check empty PDOL
        if (!ResponseUtils.isSucceed(gpo)) {
            gpo = getGetProcessingOptions(null, pProvider);
            // Check response
            if (!ResponseUtils.isSucceed(gpo)) {
                return false;
            }
        }

        // Extract commons card data (number, expire date, ...)
        if (extractCommonsCardData(gpo)) {


            ret = true;
        }

        return ret;
    }

    /**
     * Method used to create GPO command and execute it
     *
     * @param pPdol
     *            PDOL data
     * @param pProvider
     *            provider
     * @return return data
     */
    private byte[] getGetProcessingOptions(final byte[] pPdol, final IProvider pProvider) throws CommunicationException {
        // List Tag and length from PDOL
        List<TagAndLength> list = TlvUtil.parseTagAndLength(pPdol);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(EmvTags.COMMAND_TEMPLATE.getTagBytes()); // COMMAND
            // TEMPLATE
            out.write(TlvUtil.getLength(list)); // ADD total length
            if (!list.isEmpty()) {
                for (TagAndLength tl : list) {
                    out.write(EmvTerminal.constructValue(tl));
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("Construct GPO Command:" + ioe.getMessage(), ioe);
        }
        return pProvider.transceive(new CommandApdu(CommandEnum.GPO, out.toByteArray(), 0).toBytes());
    }


    /**
     * Extract list of application file locator from Afl response
     *
     * @param pAfl
     *            AFL data
     * @return list of AFL
     */
    private List<Afl> extractAfl(final byte[] pAfl) {
        List<Afl> list = new ArrayList<>();
        ByteArrayInputStream bai = new ByteArrayInputStream(pAfl);
        while (bai.available() >= 4) {
            Afl afl = new Afl();
            afl.setSfi(bai.read() >> 3);
            afl.setFirstRecord(bai.read());
            afl.setLastRecord(bai.read());
            afl.setOfflineAuthentication(bai.read() == 1);
            list.add(afl);
        }
        return list;
    }



    /**
     * Method used to get the field card
     *
     * @return the card
     */
    public Card getCard() {
        return card;
    }
}

