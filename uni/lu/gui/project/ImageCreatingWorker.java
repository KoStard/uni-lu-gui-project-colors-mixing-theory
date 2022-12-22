package uni.lu.gui.project;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingWorker;

public class ImageCreatingWorker extends SwingWorker<Void, Void> implements ControlChangeListener {
    private final int fps;
    private AtomicBoolean running = new AtomicBoolean(true);
    private AtomicBoolean anythingChanged = new AtomicBoolean(true);
    private ControlPanel controlPanel;
    private ContentPanel contentPanel;
    private Optional<AtomicImage> image;

    public ImageCreatingWorker(int fps, final ControlPanel controlPanel, final ContentPanel contentPanel) {
        this.fps = fps;
        this.controlPanel = controlPanel;
        this.contentPanel = contentPanel;
        image = Optional.empty();
    }

    @Override
    protected Void doInBackground() throws Exception {
        while (running.get()) {
            if (anythingChanged.getAndSet(false)) {
                Image image = createImage();
                if (this.image.isPresent()) {
                    this.image.get().setImage(image);
                } else {
                    this.image = Optional.of(new AtomicImage(image));
                }
                publish();
            }
            Thread.sleep(1000 / fps);
        }
        return null;
    }

    private Image createImage() {
        // It should be safe to access the Swing components from the worker thread in read-only mode.
        int angle = controlPanel.getAngle();
        int distance = controlPanel.getDistance();
        int intensity = controlPanel.getIntensity();
        int intensityDropEffect = controlPanel.getIntensityDropEffect();
        int radius = controlPanel.getRadius();
        // When the window is too small, for the content panel, the height can be
        // negative.
        int width = Math.max(contentPanel.getWidth(), 1);
        int height = Math.max(contentPanel.getHeight(), 1);
        PixelChangeMode pixelChangeMode = controlPanel.getPixelChangeMode();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(getBackgroundColor(pixelChangeMode));
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());

        int centralCircleX = width / 2;
        int centralCircleY = height / 2;

        int leftCircleX = centralCircleX - (int) (distance * Math.cos(Math.toRadians(angle)));
        int leftCircleY = centralCircleY - (int) (distance * Math.sin(Math.toRadians(angle)));

        int rightCircleX = centralCircleX + (int) (distance * Math.cos(Math.toRadians(angle)));
        int rightCircleY = centralCircleY - (int) (distance * Math.sin(Math.toRadians(angle)));

        drawCircle(image, centralCircleX, centralCircleY, radius, Color.RED, intensity, intensityDropEffect,
                pixelChangeMode);
        drawCircle(image, leftCircleX, leftCircleY, radius, Color.GREEN, intensity, intensityDropEffect,
                pixelChangeMode);
        drawCircle(image, rightCircleX, rightCircleY, radius, Color.BLUE, intensity, intensityDropEffect,
                pixelChangeMode);

        return image;
    }

    private Color getBackgroundColor(PixelChangeMode pixelChangeMode) {
        switch (pixelChangeMode) {
            case Additive:
                return Color.BLACK;
            case Subtractive:
                return Color.WHITE;
            default:
                throw new IllegalArgumentException("Unknown pixel change mode: " + pixelChangeMode);
        }
    }

    private void drawCircle(BufferedImage image, int centerX, int centerY, int radius, Color color, int intensity,
            int intensityDropEffect, PixelChangeMode pixelChangeMode) {
        int left = centerX - radius;
        int top = centerY - radius;
        int right = centerX + radius;
        int bottom = centerY + radius;
        int leftOffset = 0;
        int topOffset = 0;

        if (left < 0) {
            leftOffset = -left;
            left = 0;
        }
        if (top < 0) {
            topOffset = -top;
            top = 0;
        }
        if (right > image.getWidth()) {
            right = image.getWidth();
        }
        if (bottom > image.getHeight()) {
            bottom = image.getHeight();
        }

        int width = right - left;
        int height = bottom - top;

        int[] pixels = new int[width * height];
        image.getRGB(left, top, width, height, pixels, 0, width);
        for (int i = 0; i < pixels.length; i++) {
            int x = i % width + leftOffset;
            int y = i / width + topOffset;
            int dx = x - radius;
            int dy = y - radius;
            if (dx * dx + dy * dy <= radius * radius) {
                int rgb = pixels[i];

                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = (rgb) & 0xFF;

                // decrease intensity based on distance
                int distance = (int) Math.sqrt(dx * dx + dy * dy);
                double possibleIntensityDrop = ((double) radius - distance) / radius;

                int localIntensity = (int) (intensity * (1 - (1 - possibleIntensityDrop) * intensityDropEffect / 100.0));

                int newRed = computeNewColorDimension(red, color.getRed(), localIntensity, pixelChangeMode);
                int newGreen = computeNewColorDimension(green, color.getGreen(), localIntensity, pixelChangeMode);
                int newBlue = computeNewColorDimension(blue, color.getBlue(), localIntensity, pixelChangeMode);

                pixels[i] = (newRed << 16) | (newGreen << 8) | newBlue;
            }
        }

        image.setRGB(left, top, width, height, pixels, 0, width);
    }

    private int computeNewColorDimension(int colorDimension, int newColorDimension, int intensity,
            PixelChangeMode pixelChangeMode) {
        switch (pixelChangeMode) {
            case Additive:
                return Math.min(colorDimension + newColorDimension * intensity / 100, 255);
            case Subtractive:
                return Math.max(colorDimension - newColorDimension * intensity / 100, 0);
            default:
                throw new IllegalArgumentException("Unknown pixel change mode: " + pixelChangeMode);
        }
    }

    @Override
    protected void process(List<Void> chunks) {
        if (image.isPresent()) {
            contentPanel.setImage(image.get().getImage());
            contentPanel.repaint();
        }
    }

    public void stop() {
        running.set(false);
    }

    @Override
    protected void done() {
        try {
            get();
        } catch (Exception e) {
            System.out.println("The worker has failed");
            e.printStackTrace();
        }
    }

    @Override
    public void controlChanged() {
        anythingChanged.set(true);
    }
}