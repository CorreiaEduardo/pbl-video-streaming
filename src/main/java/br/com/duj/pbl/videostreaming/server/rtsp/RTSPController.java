package br.com.duj.pbl.videostreaming.server.rtsp;

import br.com.duj.pbl.videostreaming.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.function.Consumer;

import static br.com.duj.pbl.videostreaming.Constants.CRLF;
import static br.com.duj.pbl.videostreaming.Constants.RTSP.*;

@Slf4j
public class RTSPController {
    private Socket socket;
    private int state;

    private String rtspSessionId = UUID.randomUUID().toString(); //ID of the RTSP session
    private int rtspSeqNumber = 0;                           //Sequence number of RTSP messages within the session
    private int currentDestPort;
    private String currentVideoFilename;

    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private Map<String, Integer> controls = Map.of(
            "SETUP", SETUP,
            "PLAY", PLAY,
            "PAUSE", PAUSE,
            "TEARDOWN", TEARDOWN,
            "DESCRIBE", DESCRIBE
    );

    public RTSPConnectionData connect(int port) throws IOException {
        ServerSocket listenSocket = new ServerSocket(port);
        socket = listenSocket.accept();
        listenSocket.close();

        state = Constants.RTSP.INIT;
        currentDestPort = port;

        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        while (true) { // Wait for setup
            final RTSPRequest request = parseRequest();

            if (request.getRequestType() == SETUP) {
                state = Constants.RTSP.READY;
                sendDefaultResponse();

                currentVideoFilename = request.getFileName();
                return new RTSPConnectionData(currentVideoFilename, request.getRtpPort(), socket.getInetAddress());
            }

        }
    }

    public void listen(Consumer<Integer> action) throws IOException {
        final RTSPRequest request = parseRequest();
        final int requestType = request.getRequestType();

        if (requestType == PLAY && state == READY) {
            sendDefaultResponse();
            state = PLAYING;
            action.accept(requestType);
        } else if (requestType == PAUSE && state == PLAYING) {
            sendDefaultResponse();
            state = READY;
            action.accept(requestType);
        } else if (requestType == TEARDOWN) {
            sendDefaultResponse();
            action.accept(requestType);
        } else if (requestType == DESCRIBE) {
            sendDescribeResponse();
        }
    }

    private RTSPRequest parseRequest() throws IOException {
        RTSPRequest parsedRequest = new RTSPRequest();

        // EXAMPLE: SETUP movie.mjpeg RTSP/1.0
        String firstRequestLine = bufferedReader.readLine();
        log.info("[RSTP] Request received :: {}", firstRequestLine);

        StringTokenizer tokens = new StringTokenizer(firstRequestLine);
        String command = tokens.nextToken();

        final Integer requestType = controls.get(command);
        parsedRequest.setRequestType(requestType);

        if (requestType == SETUP) {
            String fileName = tokens.nextToken();
            parsedRequest.setFileName(fileName);
        }

        // EXAMPLE: CSeq: 1
        String secondRequestLine = bufferedReader.readLine();
        tokens = new StringTokenizer(secondRequestLine);
        tokens.nextToken(); // skip CSeq: prefix

        rtspSeqNumber = Integer.parseInt(tokens.nextToken());

        // EXAMPLE: Transport: RTP/UDP; client_port= 25000
        String lastLine = bufferedReader.readLine();
        tokens = new StringTokenizer(lastLine);

        if (requestType == SETUP) {
            tokens.nextToken(); //skip transport prefix
            tokens.nextToken(); //skip transport prefix
            tokens.nextToken(); //skip client_port prefix

            final int port = Integer.parseInt(tokens.nextToken());
            parsedRequest.setRtpPort(port);
        } else if (requestType != DESCRIBE) {
            tokens.nextToken(); //skip
            rtspSessionId = tokens.nextToken();
        }

        return parsedRequest;
    }

    private void sendDefaultResponse() throws IOException {
        bufferedWriter.write("RTSP/1.0 200 OK" + CRLF);
        bufferedWriter.write("CSeq: " + rtspSeqNumber + CRLF);
        bufferedWriter.write("Session: " + rtspSessionId + CRLF);

        bufferedWriter.flush();
    }

    private void sendDescribeResponse() throws IOException {
        bufferedWriter.write("RTSP/1.0 200 OK" + CRLF);
        bufferedWriter.write("CSeq: " + rtspSeqNumber + CRLF);
        bufferedWriter.write(mountDescribeResponse());

        bufferedWriter.flush();
    }

    private String mountDescribeResponse() {
        StringWriter bodyWriter = new StringWriter();
        StringWriter responseWriter = new StringWriter();

        bodyWriter.write("v=0" + CRLF);
        bodyWriter.write("m=video " + currentDestPort + " RTP/AVP " + Constants.MEDIA.MJPEG_TYPE + CRLF);
        bodyWriter.write("a=control:streamid=" + rtspSessionId + CRLF);
        bodyWriter.write("a=mimetype:string;\"video/MJPEG\"" + CRLF);
        String body = bodyWriter.toString();

        responseWriter.write("Content-Base: " + currentVideoFilename + CRLF);
        responseWriter.write("Content-Type: " + "application/sdp" + CRLF);
        responseWriter.write("Content-Length: " + body.length() + CRLF);
        responseWriter.write(body);

        return responseWriter.toString();
    }

    public void closeConnection() throws IOException {
        this.socket.close();
    }
}
