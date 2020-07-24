package com.nfc.cardlib.iso7816emv;


import com.nfc.cardlib.enums.TagTypeEnum;
import com.nfc.cardlib.enums.TagValueTypeEnum;

public interface ITag {
    enum Class {
        UNIVERSAL, APPLICATION, CONTEXT_SPECIFIC, PRIVATE
    }

    boolean isConstructed();

    byte[] getTagBytes();

    String getName();

    String getDescription();

    TagTypeEnum getType();

    TagValueTypeEnum getTagValueType();

    Class getTagClass();

    int getNumTagBytes();
}
