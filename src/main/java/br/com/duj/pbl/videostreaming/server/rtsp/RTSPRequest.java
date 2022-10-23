package br.com.duj.pbl.videostreaming.server.rtsp;

import lombok.Data;

@Data
public class RTSPRequest {
    private String fileName;
    private int requestType;
    private int rtpPort;
}
