package com.nfc.cardlib.parser.apdu.annotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import com.nfc.cardlib.iso7816emv.ITag;


public final class AnnotationUtils {

    /**
     * AnnotationUtils singleton
     */
    private static final AnnotationUtils INSTANCE = new AnnotationUtils();

    /**
     * Method to get the unique instance of the class
     *
     * @return AnnotationUtils instance
     */
    public static AnnotationUtils getInstance() {
        return INSTANCE;
    }

    /**
     * Map which contain
     */
    private final Map<String, Map<ITag, AnnotationData>> map;
    private final Map<String, Set<AnnotationData>> mapSet;

    /**
     * Private default constructor
     */
    private AnnotationUtils() {
        map = new HashMap<>();
        mapSet = new HashMap<>();
        extractAnnotation();
    }

    /**
     * Method to extract all annotation information and store them in the map
     */
    private void extractAnnotation() {

    }

    /**
     * Getter map set
     *
     * @return the map
     */
    public Map<String, Set<AnnotationData>> getMapSet() {
        return mapSet;
    }

    /**
     * Getter map
     *
     * @return the map
     */
    public Map<String, Map<ITag, AnnotationData>> getMap() {
        return map;
    }
}
