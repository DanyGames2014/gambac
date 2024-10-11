package net.danygames2014.gambac;

import net.minecraft.client.CrashReportPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ScreenScaler;
import net.minecraft.util.crash.CrashReport;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BrnoMinecraft extends Minecraft {

    private final Frame frame;
    private int previousWidth, previousHeight;

    public BrnoMinecraft(int width, int height, boolean fullscreen) {
        super(null, null, null, width, height, fullscreen);
        this.previousWidth = width;
        this.previousHeight = height;
        this.frame = new Frame("Minecraft");
    }

    @Override
    public void handleCrash(CrashReport throwable) { // displayUnexpectedThrowable(UnexpectedThrowable)
        this.frame.removeAll();
        this.frame.add(new CrashReportPanel(throwable), "Center");
        this.frame.validate();
        this.frame.setSize(this.displayWidth, this.displayHeight);
        this.frame.setLocationRelativeTo(null);
        this.frame.setAutoRequestFocus(true);
        this.frame.addWindowListener(new WindowAdapter() {
                                         public void windowClosing(WindowEvent we) {
                                             frame.dispose();
                                             System.exit(1);
                                         }
                                     }
        );
        this.frame.setVisible(true);
        Display.destroy();
    }

    @Override
    public void init() {
        Display.setResizable(true);
        super.init();
        Display.setTitle("Minecraft Beta 1.7.3");

        ByteBuffer[] icons = new ByteBuffer[2];
        try {
            icons[0] = loadIcon("/assets/gambac/icons/16.png");
            icons[1] = loadIcon("/assets/gambac/icons/32.png");
        } catch (Exception ignored) {
        }

        if (icons[0] != null && icons[1] != null) {
            Display.setIcon(icons);
        }

        try {
            Display.makeCurrent();
            Display.update();
        } catch (LWJGLException e) {
            System.err.println("Error while making the Display current");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }


    @Override
    public void tick() {
        if (GL11.glGetString(GL11.GL_RENDERER).contains("Apple M")) {
            GL11.glEnable(GL30.GL_FRAMEBUFFER_SRGB);
        }

        if (Display.getWidth() != this.displayWidth || Display.getHeight() != this.displayHeight) {
            this.resize(Display.getWidth(), Display.getHeight());
        }

        super.tick();
    }

    private static ByteBuffer loadIcon(String path) {
        try {
            InputStream stream = BrnoMinecraft.class.getResourceAsStream(path);
            if (stream == null) {
                throw new RuntimeException("Icon resource not found: " + path);
            }
            BufferedImage image = ImageIO.read(stream);

            int[] pixels = new int[image.getWidth() * image.getHeight()];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

            ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = pixels[y * image.getWidth() + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                    buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
                    buffer.put((byte) (pixel & 0xFF));         // Blue
                    buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
                }
            }

            buffer.flip();
            return buffer;
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public void toggleFullscreen() {
        final Object fullscreen_b;
        boolean fullscreen;

        try {
            fullscreen_b = this.getClass().getDeclaredField("fullscreen").get(this);
            fullscreen = (boolean) fullscreen_b;
        } catch (NoSuchFieldException | IllegalAccessException ignore) {
            fullscreen = Display.isFullscreen();
        }

        try {
            fullscreen = !fullscreen;
            if (fullscreen) {
                this.previousWidth = Display.getWidth();
                this.previousHeight = Display.getHeight();

                Display.setDisplayMode(Display.getDesktopDisplayMode());
                this.displayWidth = Display.getDisplayMode().getWidth();
                this.displayHeight = Display.getDisplayMode().getHeight();
            } else {
                this.displayWidth = this.previousWidth;
                this.displayHeight = this.previousHeight;
                Display.setDisplayMode(new DisplayMode(this.displayWidth, this.displayHeight));
            }
            if (this.displayWidth <= 0) {
                this.displayWidth = 1;
            }
            if (this.displayHeight <= 0) {
                this.displayHeight = 1;
            }

            if (this.currentScreen != null) {
                this.resize(this.displayWidth, this.displayHeight);
            }

            Display.setFullscreen(fullscreen);
            Display.update();
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

        try {
            this.getClass().getDeclaredField("fullscreen").set(this, fullscreen);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {

        }
    }

    private void resize(int var1, int var2) { // resize
        if (var1 <= 0) {
            var1 = 1;
        }
        if (var2 <= 0) {
            var2 = 1;
        }
        this.displayWidth = var1;
        this.displayHeight = var2;
        if (this.currentScreen != null) {
            ScreenScaler var3 = new ScreenScaler(this.options, var1, var2);
            int var4 = var3.getScaledWidth();
            int var5 = var3.getScaledHeight();
            this.currentScreen.init(this, var4, var5);
        }
    }
}