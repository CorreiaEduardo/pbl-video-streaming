package br.com.duj.pbl.videostreaming.server.rtp;

import br.com.duj.pbl.videostreaming.Constants;
import br.com.duj.pbl.videostreaming.server.util.VideoStream;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.function.Consumer;

@Slf4j
public class RTPController {
    private final byte[] buffer;     //buffer used to store the images to send to the client
    private int rtpPort;
    private InetAddress destinationAddr;
    private DatagramSocket socket;
    private DatagramPacket packedFrame;
    private int frameNumber = 0; //image nb of the image currently transmitted
    private VideoStream video;  //VideoStream object used to access video frames

    public RTPController() throws SocketException {
        buffer = new byte[Constants.MEDIA.TRANSMISSION_BUFFER];
        socket = new DatagramSocket();
    }

    public void configureConnection(int rtpPort, String filename, InetAddress destinationAddr) throws IOException {
        this.rtpPort = rtpPort;
        this.video = new VideoStream(filename);
        this.destinationAddr = destinationAddr;
    }

    public void handlePacket(Consumer<Integer> onSuccess, Runnable onEnd) {
        byte[] frame;

        if (frameNumber < Constants.MEDIA.VIDEO_LENGTH) {
            frameNumber++;

            try {
                int imageLength = video.next(buffer);

                RTPPacket packet = new RTPPacket(Constants.MEDIA.MJPEG_TYPE,
                        frameNumber, frameNumber * Constants.MEDIA.FRAME_PERIOD, buffer, imageLength);

                int packetLength = packet.getLength();

                byte[] packetBits = new byte[packetLength];
                packet.getPacket(packetBits);

                packedFrame = new DatagramPacket(packetBits, packetLength, destinationAddr, rtpPort);
                socket.send(packedFrame);

                log.info("[RTP] Sent frame #{}, with size = {} ({})", frameNumber, imageLength, buffer.length);

                onSuccess.accept(frameNumber);
            } catch (Exception ex) {
                log.error("Exception caught", ex);

            }
        } else {
            onEnd.run();
        }
    }

    public void closeConnection() {
        this.socket.close();
    }
}
