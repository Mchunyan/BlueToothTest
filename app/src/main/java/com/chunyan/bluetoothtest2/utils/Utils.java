package com.chunyan.bluetoothtest2.utils;

public class Utils {
    public static byte[] hexStringToByteArray(String s) {
        if(s == null) return null;
        if(s.trim().length() == 1) {
            s = "0"+s;
        }
        if(s.trim().length() == 3) {
            s = "0"+s;
        }
        String dataString = s.replace(" ","");
        int len = dataString.length();
        byte[] data = new byte[len/2];

        if(len == 1){
            data = s.getBytes();
        } else {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(dataString.charAt(i), 16) << 4) + Character.digit(dataString.charAt(i + 1), 16));
            }
        }
        return data;
    }
    public static int getLengthFromToken(byte[] bytes){
        if (bytes.length >= 3) {
            return (bytes[1]<<8)|(bytes[2] &0xff);
        }
        return 0;
    }
}
