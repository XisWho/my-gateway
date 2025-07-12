package com.xw.constants;

public class BufferTypeConstant {

    public final static String FLUSHER = "FLUSHER";

    public final static String MPMC = "MPMC";

    public static boolean isFlusher(String bufferType) {
        return FLUSHER.equals(bufferType);
    }

    public static boolean isMpmc(String bufferType) {
        return MPMC.equals(bufferType);
    }

}
