package br.com.duj.pbl.videostreaming.server.rtcp;

import br.com.duj.pbl.videostreaming.Constants;
import lombok.Getter;

import java.nio.ByteBuffer;

@Getter
public class RtcpPacket {
    final static int HEADER_SIZE = Constants.RTCP.HEADER_SIZE;
    final static int BODY_SIZE = Constants.RTCP.BODY_SIZE;

    private int version;            // Version number 2
    private int padding;            // Padding of packet
    private int receptionReport;    // Reception report count = 1 for one receiver
    private int payloadType;        // 201 for Receiver Report
    private int length;                // 1 source is always 32 bytes: 8 header, 24 body
    private int ssrc;                // Ssrc of sender
    private float fractionLost;        // The fraction of RTP data packets from sender lost since the previous RR packet was sent
    private int totalLoss;            // The total number of RTP data packets from sender that have been lost since the beginning of reception.
    private int highSeqNb;            // Highest sequence number received
    private int jitter;                // Not used
    private int lsr;                // Not used
    private int dlsr;                // Not used

    private byte[] header;            //Bitstream of header
    private byte[] body;            //Bitstream of the body

    public RtcpPacket(float fractionLost, int totalLoss, int highSeqNb) {
        this.fractionLost = fractionLost;
        this.totalLoss = totalLoss;
        this.highSeqNb = highSeqNb;

        version = Constants.RTCP.VERSION;
        padding = Constants.RTCP.PADDING;
        receptionReport = Constants.RTCP.RC;
        payloadType = Constants.RTCP.PAYLOAD_TYPE;
        length = Constants.RTCP.LENGTH;

        header = new byte[HEADER_SIZE];
        body = new byte[BODY_SIZE];

        header[0] = (byte) (version << 6 | padding << 5 | receptionReport);
        header[1] = (byte) (payloadType & 0xFF);
        header[2] = (byte) (length >> 8);
        header[3] = (byte) (length & 0xFF);
        header[4] = (byte) (ssrc >> 24);
        header[5] = (byte) (ssrc >> 16);
        header[6] = (byte) (ssrc >> 8);
        header[7] = (byte) (ssrc & 0xFF);

        ByteBuffer byteBuffer = ByteBuffer.wrap(body);
        byteBuffer.putFloat(fractionLost);
        byteBuffer.putInt(totalLoss);
        byteBuffer.putInt(highSeqNb);
    }

    public RtcpPacket(byte[] content) {
        header = new byte[HEADER_SIZE];
        body = new byte[BODY_SIZE];

        System.arraycopy(content, 0, header, 0, HEADER_SIZE);
        System.arraycopy(content, HEADER_SIZE, body, 0, BODY_SIZE);

        version = (header[0] & 0xFF) >> 6;
        payloadType = header[1] & 0xFF;
        length = (header[3] & 0xFF) + ((header[2] & 0xFF) << 8);
        ssrc = (header[7] & 0xFF) + ((header[6] & 0xFF) << 8) + ((header[5] & 0xFF) << 16) + ((header[4] & 0xFF) << 24);

        ByteBuffer bb = ByteBuffer.wrap(body);
        fractionLost = bb.getFloat();
        totalLoss = bb.getInt();
        highSeqNb = bb.getInt();
    }

    public int getPacket(byte[] packet) {
        System.arraycopy(header, 0, packet, 0, HEADER_SIZE);
        System.arraycopy(body, 0, packet, HEADER_SIZE, BODY_SIZE);

        return (BODY_SIZE + HEADER_SIZE);
    }

    public int getPacketLength() {
        return (BODY_SIZE + HEADER_SIZE);
    }

    public String toString() {
        return "[RTCP] Version: " + version + ", Fraction Lost: " + fractionLost
                + ", Total Lost: " + totalLoss + ", Highest Seq Num: " + highSeqNb;
    }
}
