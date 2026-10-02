package me.millo.mcGit.utility;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ByteArray {

    private int head = 0;
    private int readHead = 0;
    private byte[] array;

    public ByteArray() {
        array = new byte[0];
    }

    private void add(boolean bit) {
        if (head % 8 == 0) expand();

        if (bit) array[head / 8] |= (byte) (1 << (7 - (head % 8)));
        head++;
    }

    public void add(byte b) {
        add(b, 8);
    }

    public void add(byte b, int length) {
        for (int i = 0; i < length; i++) {
            add((b & (1 << (7 - i))) != 0);
        }
    }

    public void add(byte[] bytes, int length) {
        while (length > 0) {
            int toAdd = Math.min(length, 8);
            add(bytes[0], toAdd);
            bytes = shift(bytes, toAdd);
            length -= toAdd;
        }
    }

    public void add(int value, int length) {
        for (int i = 0; i < length; i++) {
            add((value & (1 << (length - 1 - i))) != 0);
        }
    }

    public void add(long value, int length) {
        for (int i = 0; i < length; i++) {
            add((value & (1L << (length - 1 - i))) != 0);
        }
    }

    private int[] append(int[] arr, int value) {
        int[] newArr = new int[arr.length + 1];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        newArr[arr.length] = value;
        return newArr;
    }

    public void append(ByteArray other) {
        for (int i = 0; i < other.head; i++) {
            add((other.array[i / 8] & (1 << (7 - (i % 8)))) != 0);
        }
    }

    public int readInt() {
        return (int) read(Integer.SIZE);
    }

    public long read(int length) {
        if (length < 1 || length > 64)
            throw new IllegalArgumentException("Length must be between 1 and 64");

        long value = 0;
        for (int i = 0; i < length; i++) {
            value <<= 1;
            if (((array[readHead / 8] & 0xFF) & (1 << (7 - (readHead % 8)))) != 0) value |= 1;

            readHead++;
        }

        return value;
    }

    public long readSigned(int length) {
        long value = read(length);

        if (length < 64 && (value & (1L << (length - 1))) != 0) {
            value |= (~0L << (length - 1) << 1);
        }

        return value;
    }

    private byte[] shift(byte[] bytes, int bits) {
        byte[] newBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            int shifted = (bytes[i] & 0xFF) << bits;
            newBytes[i] |= (byte) (shifted & 0xFF);
            if (i + 1 < bytes.length) {
                newBytes[i + 1] |= (byte) ((shifted >> 8) & 0xFF);
            }
        }
        return newBytes;
    }

    private void expand() {
        byte[] newArray = new byte[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        array = newArray;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < head; i++) {
            sb.append((array[i / 8] & (1 << (7 - (i % 8)))) != 0 ? "1" : "0");
        }
        return sb.toString();
    }

    public int length() {
        return head;
    }

    public String compress() {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut);
            gzipOut.write(array);
            gzipOut.close();
            byte[] compressedBytes = byteOut.toByteArray();
            return Base64.getEncoder().encodeToString(compressedBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static ByteArray decompress(String string) {
        try {
            byte[] compressedBytes = Base64.getDecoder().decode(string);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(compressedBytes);
            GZIPInputStream gzipIn = new GZIPInputStream(byteIn);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;

            while ((length = gzipIn.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }

            gzipIn.close();

            ByteArray result = new ByteArray();
            result.array = output.toByteArray();
            result.head = result.array.length * 8;

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
