package br.com.duj.pbl.videostreaming.server.rtp;

import br.com.duj.pbl.videostreaming.Constants;

import java.util.Arrays;

public class RTPPacket {
    private static int HEADER_SIZE = Constants.RTP.HEADER_SIZE;

    private int version;
    private int padding;
    private int ext;
    private int cc;
    private int marker;
    private int payloadType;
    private int sequenceNumber;
    private int timestamp;
    private int ssrc;

    private byte[] header;

    private byte[] payload;
    private int payloadSize;

    public RTPPacket(int payloadType, int frameNumber, int time, byte[] data, int dataLength) {
        this.version = Constants.RTP.VERSION;
        this.padding = Constants.RTP.PADDING;
        this.ext = Constants.RTP.EXTENSION;
        this.cc = Constants.RTP.CC;
        this.marker = Constants.RTP.MARKER;
        this.ssrc = Constants.RTP.SSRC;

        this.sequenceNumber = frameNumber;
        this.timestamp = time;
        this.payloadType = payloadType;

        this.header = new byte[HEADER_SIZE];

        header[0] = (byte) (version << 6 | padding << 5 | ext << 4 | cc);
        header[1] = (byte) (marker << 7 | payloadType & 0x000000FF);
        header[2] = (byte) (sequenceNumber >> 8);
        header[3] = (byte) (sequenceNumber & 0xFF);
        header[4] = (byte) (timestamp >> 24);
        header[5] = (byte) (timestamp >> 16);
        header[6] = (byte) (timestamp >> 8);
        header[7] = (byte) (timestamp & 0xFF);
        header[8] = (byte) (ssrc >> 24);
        header[9] = (byte) (ssrc >> 16);
        header[10] = (byte) (ssrc >> 8);
        header[11] = (byte) (ssrc & 0xFF);

        payloadSize = dataLength;
        payload = new byte[dataLength];
        payload = Arrays.copyOf(data, payloadSize);
    }

    public int getPacket(byte[] packet) {
        if (HEADER_SIZE >= 0) System.arraycopy(header, 0, packet, 0, HEADER_SIZE);
        if (payloadSize >= 0) System.arraycopy(payload, 0, packet, HEADER_SIZE, payloadSize);

        return (payloadSize + HEADER_SIZE);
    }

    public int getLength() {
        return (payloadSize + HEADER_SIZE);
    }
}
