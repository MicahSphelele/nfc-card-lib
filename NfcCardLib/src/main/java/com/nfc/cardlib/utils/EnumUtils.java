package com.nfc.cardlib.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nfc.cardlib.model.enums.IKeyEnum;


public final class EnumUtils  {
    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumUtils.class);

    /**
     * Get the value of and enum from his key
     *
     * @param pKey
     *            key to find
     * @param pClass
     *            Enum class
     * @return Enum instance of the specified key or null otherwise
     */
    @SuppressWarnings("unchecked")
    public static <T extends IKeyEnum> T getValue(final int pKey, final Class<T> pClass) {
        if(pClass.getEnumConstants()!=null){//TODO("Clear if doesn't works")
            for (IKeyEnum val : pClass.getEnumConstants()) {
                if (val.getKey() == pKey) {
                    return (T) val;
                }
            }
            LOGGER.error("Unknow value:" + pKey + " for Enum:" + pClass.getName());
        }

        return null;
    }

    /**
     * Private constructor
     */
    private EnumUtils() {
    }
}
