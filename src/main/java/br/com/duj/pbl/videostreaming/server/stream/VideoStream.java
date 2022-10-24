package br.com.duj.pbl.videostreaming.server.stream;

import lombok.Getter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Getter
public class VideoStream {
    private FileInputStream videoFile;
    private int currentFrame;

    public VideoStream(String fileName) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        Path temp = Files.createTempFile("resource-", ".Mjpeg");
        Files.copy(classLoader.getResourceAsStream(fileName), temp, StandardCopyOption.REPLACE_EXISTING);
        this.videoFile = new FileInputStream(temp.toFile());
        currentFrame = 0;
    }

    public int next(byte[] frame) throws IOException {
        byte[] currentFrameLength = new byte[5];
        videoFile.read(currentFrameLength, 0, 5);

        int length = Integer.parseInt(new String(currentFrameLength));
        return (videoFile.read(frame, 0, length));
    }
}
