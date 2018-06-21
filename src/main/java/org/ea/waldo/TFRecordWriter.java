package org.ea.waldo;

import org.apache.commons.codec.digest.PureJavaCrc32C;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TFRecordWriter {
    private static final int MASK_DELTA = 0xa282ead8;
    private final DataOutput output;

    public TFRecordWriter(DataOutput output) {
        this.output = output;
    }

    public static int maskedCrc32c(byte[] data) {
        return maskedCrc32c(data, 0, data.length);
    }

    public static int maskedCrc32c(byte[] data, int offset, int length) {
        PureJavaCrc32C crc32c = new PureJavaCrc32C();
        crc32c.update(data, offset, length);
        int crc = (int)crc32c.getValue();
        return ((crc >>> 15) | (crc << 17)) + MASK_DELTA;
    }

    public void write(byte[] record, int offset, int length) throws IOException {
        /**
         * TFRecord format:
         * uint64 length
         * uint32 masked_crc32_of_length
         * byte   data[length]
         * uint32 masked_crc32_of_data
         */
        byte[] len = toInt64LE(length);
        output.write(len);
        output.write(toInt32LE(maskedCrc32c(len)));
        output.write(record, offset, length);
        output.write(toInt32LE(maskedCrc32c(record, offset, length)));
    }

    public void write(byte[] record) throws IOException {
        write(record, 0, record.length);
    }

    private byte[] toInt64LE(long data) {
        byte[] buff = new byte[8];
        ByteBuffer bb = ByteBuffer.wrap(buff);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putLong(data);
        return buff;
    }

    private byte[] toInt32LE(int data) {
        byte[] buff = new byte[4];
        ByteBuffer bb = ByteBuffer.wrap(buff);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(data);
        return buff;
    }
}
