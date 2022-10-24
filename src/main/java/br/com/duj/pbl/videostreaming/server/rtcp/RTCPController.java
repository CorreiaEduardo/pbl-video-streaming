package br.com.duj.pbl.videostreaming.server.rtcp;

import br.com.duj.pbl.videostreaming.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import static br.com.duj.pbl.videostreaming.Constants.RTCP.RTCP_RCV_PORT;

@Slf4j
public class RTCPController {
    private final byte[] rtcpBuffer;
    private final DatagramSocket socket;

    public RTCPController() throws SocketException {
        this.rtcpBuffer = new byte[Constants.RTCP.BUFFER_SIZE];
        this.socket = new DatagramSocket(RTCP_RCV_PORT);
    }

    public void handlePacket() throws IOException {
        DatagramPacket dp = new DatagramPacket(rtcpBuffer, rtcpBuffer.length);

        socket.receive(dp);
        RtcpPacket rtcpPkt = new RtcpPacket(dp.getData());
        log.info("[RTCP] Packet received :: {}", rtcpPkt);
    }
}
