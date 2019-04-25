package com.chunyan.bluetoothtest2.utils;

import android.util.Log;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class Utils {
    public static byte[] hexStringToByteArray(String s) {
        if (s == null) return null;
        if (s.trim().length() == 1) {
            s = "0" + s;
        }
        if (s.trim().length() == 3) {
            s = "0" + s;
        }
        String dataString = s.replace(" ", "");
        int len = dataString.length();
        byte[] data = new byte[len / 2];

        if (len == 1) {
            data = s.getBytes();
        } else {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(dataString.charAt(i), 16) << 4) + Character.digit(dataString.charAt(i + 1), 16));
            }
        }
        return data;
    }

    public static int getLengthFromToken(byte[] bytes) {
        if (bytes.length >= 3) {
            return (bytes[1] << 8) | (bytes[2] & 0xff);
        }
        return 0;
    }



    //数据分包处理
    public static Queue<byte[]> splitPacketFor20Byte(byte[] data) {
        Queue<byte[]> dataInfoQueue = new LinkedList<>();
        if (data != null) {
            int index = 0;
            do {
                byte[] surplusData = new byte[data.length - index];
                byte[] currentData;
                System.arraycopy(data, index, surplusData, 0, data.length - index);
                if (surplusData.length <= 20) {
                    currentData = new byte[surplusData.length];
                    System.arraycopy(surplusData, 0, currentData, 0, surplusData.length);
                    index += surplusData.length;
                } else {
                    currentData = new byte[20];
                    System.arraycopy(data, index, currentData, 0, 20);
                    index += 20;
                }
                Log.e("mcy_分包数据", "" + Arrays.toString(currentData));
                dataInfoQueue.offer(currentData);
            } while (index < data.length);
        }
        return dataInfoQueue;
    }

}
