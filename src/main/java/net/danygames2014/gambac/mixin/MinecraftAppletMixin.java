package net.danygames2014.gambac.mixin;

import net.danygames2014.gambac.BrnoMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftApplet;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;

@SuppressWarnings("removal")
@Mixin(MinecraftApplet.class)
public class MinecraftAppletMixin extends Applet {

    @Shadow private Minecraft minecraft;

    /**
     * @author Proudly overwritten by DanyGames2014
     * @reason because i don't give a shit
     */
    @Overwrite(remap = false)
    public void init(){
        boolean var1 = false;
        if (this.getParameter("fullscreen") != null) {
            var1 = this.getParameter("fullscreen").equalsIgnoreCase("true");
        }

        this.minecraft = new BrnoMinecraft(this.getWidth(), this.getHeight(), var1);
        this.minecraft.hostAddress = this.getDocumentBase().getHost();
        if (this.getDocumentBase().getPort() > 0) {
            StringBuilder var10000 = new StringBuilder();
            Minecraft var10002 = this.minecraft;
            var10002.hostAddress = var10000.append(var10002.hostAddress).append(":").append(this.getDocumentBase().getPort()).toString();
        }

        if (this.getParameter("username") != null && this.getParameter("sessionid") != null) {
            this.minecraft.session = new Session(this.getParameter("username"), this.getParameter("sessionid"));
            System.out.println("Setting user: " + this.minecraft.session.username);
            if (this.getParameter("mppass") != null) {
                this.minecraft.session.mpPass = this.getParameter("mppass");
            }
        } else {
            this.minecraft.session = new Session("Payer" + System.currentTimeMillis() % 10000, "");
        }

        if (this.getParameter("server") != null && this.getParameter("port") != null) {
            this.minecraft.setStartupServer(this.getParameter("server"), Integer.parseInt(this.getParameter("port")));
        }

        SwingUtilities.invokeLater(() -> {
            hideThemAll(this.getParent().getParent().getParent());
            hideThemAll(this.getParent().getParent());
            hideThemAll(this.getParent());
            hideThemAll(this);
            this.startThread();
        });
    }

    @Unique
    private void hideThemAll(Container container) {
        try {
            if (container instanceof Frame) {
                ((Frame) container).dispose();
            }
            for (Component component : container.getComponents()) {
                component.setVisible(false);
            }
        } catch (NullPointerException ignore) {
        }
    }

    /**
     * @author DanyGames2014
     * @reason because i don't give a shit
     */
    @Overwrite
    public void startThread() { // startMainThread
        this.minecraft.run();
    }

    @Inject(method = "destroy", at = @At(value = "HEAD"), remap = false, cancellable = true)
    public void destroy(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "stopThread", at= @At(value = "HEAD"), cancellable = true)
    public void stopThread(CallbackInfo ci) { // onRemoveNotify / shutdown
        ci.cancel();
    }

    @Inject(method = "clearMemory", at = @At(value = "HEAD"), cancellable = true)
    public void clearMemory(CallbackInfo ci) { // clearApplet
        ci.cancel();
    }
}
