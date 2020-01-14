package com.sziti.easysocketlib.util;

public class BCD8421Operater {

    public static byte[] numToByteArray(long num, int size) {
        byte[] result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[i] = (byte) ((num >> (size - i - 1) * 8) & 0xFF);
        }
        return result;
    }

    /**
     * BCD字节数组===>String
     *
     * @param bytes
     * @return 十进制字符串
     */
    public static String bcd2String(byte[] bytes) {
        StringBuilder temp = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            // 高四位
            temp.append((bytes[i] & 0xf0) >>> 4);
            // 低四位
            temp.append(bytes[i] & 0x0f);
        }
//		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();
        return temp.toString();
    }

    /**
     * 字符串==>BCD字节数组
     *
     * @param str
     * @return BCD字节数组
     */
    public static byte[] string2Bcd(String str) {
        // 奇数,前补零
        if ((str.length() & 0x1) == 1) {
            str = "0" + str;
        }

        byte ret[] = new byte[str.length() / 2];
        byte bs[] = str.getBytes();
        for (int i = 0; i < ret.length; i++) {

            byte high = ascII2Bcd(bs[2 * i]);
            byte low = ascII2Bcd(bs[2 * i + 1]);

            // TODO 只遮罩BCD低四位?
            ret[i] = (byte) ((high << 4) | low);
        }
        return ret;
    }

    private static byte ascII2Bcd(byte asc) {
        if ((asc >= '0') && (asc <= '9'))
            return (byte) (asc - '0');
        else if ((asc >= 'A') && (asc <= 'F'))
            return (byte) (asc - 'A' + 10);
        else if ((asc >= 'a') && (asc <= 'f'))
            return (byte) (asc - 'a' + 10);
        else
            return (byte) (asc - 48);
    }
}
