package br.com.duj.pbl.videostreaming.server.rtsp;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetAddress;

@Data
@AllArgsConstructor
public class RTSPConnectionData {
    private String filename;
    private int rtpPort;
    private InetAddress clientAddress;
}
