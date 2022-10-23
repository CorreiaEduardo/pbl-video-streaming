package br.com.duj.pbl.videostreaming;

public interface Constants {
    String CRLF = "\r\n";

    interface MEDIA {
        int MJPEG_TYPE = 26;    //RTP payload type for MJPEG video
        int FRAME_PERIOD = 100; //Frame period of the video to stream, in ms
        int VIDEO_LENGTH = 500; //Length of the video in frames
        int TRANSMISSION_BUFFER = 20000;
    }

    interface RTSP {
        int INIT = 0;
        int READY = 1;
        int PLAYING = 2;
        int SETUP = 3;
        int PLAY = 4;
        int PAUSE = 5;
        int TEARDOWN = 6;
        int DESCRIBE = 7;
    }

    interface RTCP {
        int RTCP_RCV_PORT = 19001; // Port where the client will receive the RTP packets
        int RTCP_PERIOD = 400;     // How often to check for control events
        int BUFFER_SIZE = 512;

        int HEADER_SIZE = 8;
        int BODY_SIZE = 24;

        int VERSION = 2;
        int PADDING = 0;
        int RC = 1;
        int PAYLOAD_TYPE = 201;
        int LENGTH = 32;
    }

    interface RTP {
        int HEADER_SIZE = 12;

        int VERSION = 2;
        int PADDING = 0;
        int EXTENSION = 0;
        int CC = 0;
        int MARKER = 0;
        int SSRC = 1337;
    }
}
