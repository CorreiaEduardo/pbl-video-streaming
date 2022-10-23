package br.com.duj.pbl.videostreaming.server.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Slf4j
public class ImageTranslator {
    private float compressionQuality;

    private BufferedImage bufferedImage;
    private ByteArrayOutputStream baOutputStream;
    private ImageOutputStream imageOutputStream;

    private ImageWriter writer;
    private ImageWriteParam writeParam;

    public ImageTranslator(float compressionQuality, String formatName) throws IOException {
        this.compressionQuality = compressionQuality;

        this.baOutputStream = new ByteArrayOutputStream();
        this.imageOutputStream = ImageIO.createImageOutputStream(baOutputStream);

        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
        this.writer = writers.next();
        this.writer.setOutput(imageOutputStream);

        writeParam = writer.getDefaultWriteParam();
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionQuality(compressionQuality);
    }

    public byte[] compress(byte[] imageBytes) throws IOException {
        baOutputStream.reset();
        bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        writer.write(null, new IIOImage(bufferedImage, null, null), writeParam);

        return baOutputStream.toByteArray();
    }

    public void setCompressionQuality(float compressionQuality) {
        this.compressionQuality = compressionQuality;
        writeParam.setCompressionQuality(compressionQuality);
    }
}
