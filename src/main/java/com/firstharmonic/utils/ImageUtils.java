package com.firstharmonic.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * based on http://radio.javaranch.com/davo/2007/07/20/1184942077153.html
 * 
 * @author the.james.burton
 * 
 */
public class ImageUtils {

    private static final int _size = 10;
    private static final String _font = "Verdana";
    private static final int _buffer = 3;
    private static final int _bufferWidth = 500;
    private static final int _bufferHeight = 50;
    private static final String _imageType = "png";

    public static void rotate(String text, File file) throws IOException {

        // Create a rotated font
        AffineTransform at = new AffineTransform();
        at.quadrantRotate(3);
        // rotate(Math.toRadians(90));
        final Font basicFont = new Font(_font, Font.PLAIN, _size);
        final Font font = new Font(_font, Font.PLAIN, _size).deriveFont(at);

        // Create an image BUFFER_HEIGHT x BUFFER_WIDTH, before cropping
        BufferedImage bufferedImage = new BufferedImage(_bufferHeight, _bufferWidth, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        g2d.setBackground(new Color(255, 255, 255));
        g2d.clearRect(0, 0, _bufferHeight, _bufferWidth);

        // create metrics from the basic font, as the rotated one behaves
        // strangely
        g2d.setFont(basicFont);
        final FontMetrics fm = g2d.getFontMetrics();
        g2d.setFont(font);

        g2d.setColor(new Color(0, 0, 0));
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Rectangle2D rect = fm.getStringBounds(text, g2d);
        int drop = fm.getDescent();
        int width = Math.min(_bufferWidth, (int) rect.getWidth() + 2 * _buffer);
        int height = Math.min(_bufferHeight, (int) rect.getHeight() + 2 * _buffer);

        // g2d.drawString(text, drop + _buffer, _buffer);
        g2d.drawString(text, _bufferHeight - drop, _bufferWidth);
        BufferedImage croppedImage = bufferedImage.getSubimage(_bufferHeight - height, _bufferWidth - width, height, width);

        // Free graphic resources
        g2d.dispose();

        // Save the image
        ImageIO.write(croppedImage, _imageType, file);

    }

}
