package com.littlebayreal.easysocketlib.util;

import com.littlebayreal.easysocketlib.SLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 一些常用的字节处理方法
 */
public class BitOperator {

    final int WORD = 2;

    //将长整形转为byte数组
    public static byte[] numToByteArray(long num, int size) {
        byte[] result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[i] = (byte) ((num >> (size - i - 1) * 8) & 0xFF);
        }
        return result;
    }

    public static byte[] numToByteArray_LH(long num, int size) {
        byte[] result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[i] = (byte) ((num >> i * 8) & 0xFF);
        }
        return result;
    }

    /**
     * 把一个整形该为byte
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static byte integerTo1Byte(int value) {
        return (byte) (value & 0xFF);
    }

    /**
     * 把一个整形该为1位的byte数组
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static byte[] integerTo1Bytes(int value) {
        byte[] result = new byte[1];
        result[0] = (byte) (value & 0xFF);
        return result;
    }


    /**
     * 把byte[]转化位整形,通常为指令用
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static int byteToInteger(byte[] value) {
        int result;
        if (value.length == 1) {
            result = oneByteToInteger(value[0]);
        } else if (value.length == 2) {
            result = twoBytesToInteger(value);
        } else if (value.length == 3) {
            result = threeBytesToInteger(value);
        } else if (value.length == 4) {
            result = fourBytesToInteger(value);
        } else {
            result = fourBytesToInteger(value);
        }
        return result;
    }

    public static int byteToInteger_LH(byte[] value) {
        int result;
        if (value.length == 1) {
            result = oneByteToInteger(value[0]);
        } else if (value.length == 2) {
            result = twoBytesToInteger_LH(value);
        } else if (value.length == 3) {
            result = threeBytesToInteger_LH(value);
        } else if (value.length == 4) {
            result = fourBytesToInteger_LH(value);
        } else {
            result = fourBytesToInteger_LH(value);
        }
        return result;
    }

    /**
     * 把一个byte转化位整形,通常为指令用
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static int oneByteToInteger(byte value) {
        return (int) value & 0xFF;
    }

    /**
     * 把一个2位的数组转化位整形
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static int twoBytesToInteger(byte[] value) {
        // if (value.length < 2) {
        // throw new Exception("Byte array too short!");
        // }
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        return ((temp0 << 8) + temp1);
    }

    public static int twoBytesToInteger_LH(byte[] value) {
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        return ((temp1 << 8) + temp0);
    }

    /**
     * 把一个3位的数组转化位整形
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static int threeBytesToInteger(byte[] value) {
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;
        return ((temp0 << 16) + (temp1 << 8) + temp2);
    }

    public static int threeBytesToInteger_LH(byte[] value) {
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;
        return ((temp2 << 16) + (temp1 << 8) + temp0);
    }

    /**
     * 把一个4位的数组转化位整形,通常为指令用
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static int fourBytesToInteger(byte[] value) {
        // if (value.length < number_4) {
        // throw new Exception("Byte array too short!");
        // }
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;
        int temp3 = value[3] & 0xFF;
        return ((temp0 << 24) + (temp1 << 16) + (temp2 << 8) + temp3);
    }

    public static int fourBytesToInteger_LH(byte[] value) {
        // if (value.length < number_4) {
        // throw new Exception("Byte array too short!");
        // }
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;
        int temp3 = value[3] & 0xFF;
        return ((temp3 << 24) + (temp2 << 16) + (temp1 << 8) + temp0);
    }

    /**
     * 把一个4位的数组转化位整形
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static long fourBytesToLong(byte[] value) throws Exception {
        // if (value.length < number_4) {
        // throw new Exception("Byte array too short!");
        // }
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;
        int temp3 = value[3] & 0xFF;
        return (((long) temp0 << 24) + (temp1 << 16) + (temp2 << 8) + temp3);
    }

    /**
     * 把一个数组转化长整形
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static long bytes2Long(byte[] value) {
        long result = 0;
        int len = value.length;
        int temp;
        for (int i = 0; i < len; i++) {
            temp = (len - 1 - i) * 8;
            if (temp == 0) {
                result += (value[i] & 0x0ff);
            } else {
                result += (value[i] & 0x0ff) << temp;
            }
        }
        return result;
    }

    /**
     * 把一个长整形改为byte数组
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static byte[] longToBytes(long value) {
        return longToBytes(value, 8);
    }

    /**
     * 把一个长整形改为byte数组
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static byte[] longToBytes(long value, int len) {
        byte[] result = new byte[len];
        int temp;
        for (int i = 0; i < len; i++) {
            temp = (len - 1 - i) * 8;
            if (temp == 0) {
                result[i] += (value & 0x0ff);
            } else {
                result[i] += (value >>> temp) & 0x0ff;
            }
        }
        return result;
    }


    /**
     * 把IP拆分位int数组
     *
     * @param ip
     * @return
     * @throws Exception
     */
    public static int[] getIntIPValue(String ip) throws Exception {
        String[] sip = ip.split("[.]");
        // if (sip.length != number_4) {
        // throw new Exception("error IPAddress");
        // }
        int[] intIP = {Integer.parseInt(sip[0]), Integer.parseInt(sip[1]), Integer.parseInt(sip[2]),
                Integer.parseInt(sip[3])};
        return intIP;
    }

    /**
     * 把byte类型IP地址转化位字符串
     *
     * @param address
     * @return
     * @throws Exception
     */
    public static String getStringIPValue(byte[] address) throws Exception {
        int first = oneByteToInteger(address[0]);
        int second = oneByteToInteger(address[1]);
        int third = oneByteToInteger(address[2]);
        int fourth = oneByteToInteger(address[3]);

        return first + "." + second + "." + third + "." + fourth;
    }

    /**
     * 合并字节数组
     *
     * @param first
     * @param rest
     * @return
     */
    public static byte[] concatAll(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            if (array != null) {
                totalLength += array.length;
            }
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        return result;
    }

    /**
     * 合并字节数组
     *
     * @param rest
     * @return
     */
    public static byte[] concatAll(List<byte[]> rest) {
        int totalLength = 0;
        for (byte[] array : rest) {
            if (array != null) {
                totalLength += array.length;
            }
        }
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] array : rest) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        return result;
    }

    public static float byte2Float(byte[] bs) {
        return Float.intBitsToFloat(
                (((bs[3] & 0xFF) << 24) + ((bs[2] & 0xFF) << 16) + ((bs[1] & 0xFF) << 8) + (bs[0] & 0xFF)));
    }

    public static float byteBE2Float(byte[] bytes) {
        int l;
        l = bytes[0];
        l &= 0xff;
        l |= ((long) bytes[1] << 8);
        l &= 0xffff;
        l |= ((long) bytes[2] << 16);
        l &= 0xffffff;
        l |= ((long) bytes[3] << 24);
        return Float.intBitsToFloat(l);
    }

    public static int getCheckSum4JT808(byte[] bs, int start, int end) {
        if (start < 0 || end > bs.length)
            throw new ArrayIndexOutOfBoundsException("getCheckSum4JT808 error : index out of bounds(start=" + start
                    + ",end=" + end + ",bytes length=" + bs.length + ")");
        int cs = 0;
        for (int i = start; i < end; i++) {
            cs ^= bs[i];
        }
        return cs;
    }


    public static int getBitRange(int number, int start, int end) {
        if (start < 0)
            throw new IndexOutOfBoundsException("min index is 0,but start = " + start);
        if (end >= Integer.SIZE)
            throw new IndexOutOfBoundsException("max index is " + (Integer.SIZE - 1) + ",but end = " + end);

        return (number << Integer.SIZE - (end + 1)) >>> Integer.SIZE - (end - start + 1);
    }

    public static int getBitAt(int number, int index) {
        if (index < 0)
            throw new IndexOutOfBoundsException("min index is 0,but " + index);
        if (index >= Integer.SIZE)
            throw new IndexOutOfBoundsException("max index is " + (Integer.SIZE - 1) + ",but " + index);

        return ((1 << index) & number) >> index;
    }

    public static int getBitAtS(int number, int index) {
        String s = Integer.toBinaryString(number);
        return Integer.parseInt(s.charAt(index) + "");
    }


    public static byte[] splitBytes(byte[] bytes, int start, int end) {
    	SLog.i("start:"+ start + "end:"+ end);
        byte[] b = new byte[0];
        try {
            b = new byte[end - start + 1];

            for (int i = start; i <= end; i++) {
                b[i - start] = bytes[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return b;
    }

    /*public byte[]getHeaderBytes(byte[]all)
    {
        return splitBytes(all,1,16);
    }*/
    //查找byte数组中的多余尾部  通常以0标识
    public static int eliminate(byte[] e, byte sign) {
        for (int i = e.length - 1; i >= 0; i--) {
            if (e[i] != sign) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 去掉指定byte数组中首部的0
     *
     * @param originByte 首部带有0的数组
     * @return 首部不带0的数组
     */
    public static byte[] removeStartZero(byte[] originByte) {
        int offset = 0;
        int length = 0;
        for (int i = 0; i < originByte.length; i++) {
            if (originByte[i] == 0) {
                offset = i + 1;
                length = originByte.length - offset;
            }
        }
        return splitBytes(originByte, offset, offset + length - 1);
    }

    /**
     * 断字符串中第一位字符是否是ASCII字符（ 0–127）,ASCII字符占一个字节
     *
     * @param c
     * @return
     */
    public static boolean isAscii(int c) {
        return (c & 0x80) == 0x80 ? false : true;
    }
	/**
	 * 替换某一位置的byte数组
	 */
	public static byte[] replaceBytes(byte[] src,int position,byte[] des) throws IOException{
		// 输出：34 5e 5d 5d 7d 73 5e 7d 7d 5e 5d 7d 
		if (position < 0 || position > src.length)
			throw new ArrayIndexOutOfBoundsException("replaceBytes error : index out of bounds(position=" + position
				 + ",bytes length=" + src.length + ")");
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			for (int i = 0; i < position; i++) {
				baos.write(src[i]);
			}
			for (int i = 0; i < des.length; i++) {
				baos.write(des[i]);
			}
			for (int i = position + des.length; i < src.length; i++) {
				baos.write(src[i]);
			}
			return baos.toByteArray();
		} catch (Exception e) {
			throw e;
		} finally {
			if (baos != null) {
				baos.close();
				baos = null;
			}
		}
	}
}
