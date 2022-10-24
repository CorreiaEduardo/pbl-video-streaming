package br.com.duj.pbl.videostreaming.server;

import br.com.duj.pbl.videostreaming.Constants;
import br.com.duj.pbl.videostreaming.server.rtcp.RTCPController;
import br.com.duj.pbl.videostreaming.server.rtp.RTPController;
import br.com.duj.pbl.videostreaming.server.rtsp.RTSPConnectionData;
import br.com.duj.pbl.videostreaming.server.rtsp.RTSPController;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import static br.com.duj.pbl.videostreaming.Constants.RTSP.*;

@Slf4j
public class ServerApplication extends JFrame implements ActionListener {
    private final String RTP_TIMER_COMMAND = "RTP_TIMER_COMMAND";
    private final String RTCP_TIMER_COMMAND = "RTCP_TIMER_COMMAND";

    private final int RTCP_INTERVAL = Constants.RTCP.RTCP_PERIOD;

    private Timer rtpTimer;
    private Timer rtcpTimer;
    private int transmissionDelay;

    private RTPController rtpController;
    private RTCPController rtcpController;
    private RTSPController rtspController;

    private JLabel jLabel;

    public ServerApplication() throws IOException {
        super("RTSP Server");

        transmissionDelay = Constants.MEDIA.FRAME_PERIOD;
        rtspController = new RTSPController();
        rtpController = new RTPController();
        rtcpController = new RTCPController();

        rtpTimer = new Timer(transmissionDelay, this);
        rtpTimer.setActionCommand(RTP_TIMER_COMMAND);
        rtpTimer.setInitialDelay(0);
        rtpTimer.setCoalesce(true);

        rtcpTimer = new Timer(RTCP_INTERVAL, this);
        rtcpTimer.setInitialDelay(0);
        rtcpTimer.setCoalesce(true);

        configureGUI();
    }

    public static void main(String[] args) {
        final int rtspPort = Integer.parseInt(args[0]);
        try {
            ServerApplication server = new ServerApplication();

            // Configuring GUI
            server.pack();
            server.setVisible(true);
            server.setSize(new Dimension(400, 200));

            final RTSPConnectionData rtspConnectionData = server.rtspController.connect(rtspPort);
            server.rtpController.configureConnection(rtspConnectionData.getRtpPort(),
                    rtspConnectionData.getFilename(), rtspConnectionData.getClientAddress());

            while (true) {
                server.rtspController.listen(command -> {
                    try {
                        switch (command) {
                            case PLAY:
                                server.rtpTimer.start();
                                server.rtcpTimer.start();
                                break;
                            case PAUSE:
                                server.rtpTimer.stop();
                                server.rtcpTimer.stop();
                                break;
                            case TEARDOWN:
                                server.rtpTimer.stop();
                                server.rtcpTimer.stop();
                                server.rtspController.closeConnection();
                                server.rtpController.closeConnection();
                                System.exit(0);
                                break;
                        }
                    } catch (Exception ex) {
                        log.error("Exception caught", ex);
                    }
                });
            }
        } catch (IOException ex) {
            log.error("Unable to start the server", ex);
        }
    }

    private void configureGUI() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                rtpTimer.stop();
                rtcpTimer.stop();
                System.exit(0);
            }
        });

        jLabel = new JLabel("Send frame #        ", JLabel.CENTER);
        getContentPane().add(jLabel, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            final String actionCommand = e.getActionCommand();

            if (RTP_TIMER_COMMAND.equals(actionCommand)) {
                rtpController.handlePacket(
                        frameNumber -> jLabel.setText("Send frame #" + frameNumber),
                        () -> {
                            rtpTimer.stop();
                            rtcpTimer.stop();
                        });
            } else if (RTCP_TIMER_COMMAND.equals(actionCommand)) {
                rtcpController.handlePacket();
            }

        } catch (IOException ex) {
            log.error("Exception caught", ex);
        }
    }
}
