package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.shirojr.nemuelch.util.LoggerUtil;

public class ClientTickHandler {
    private int tick;
    private boolean shouldTick = false;
    private Runnable executor;

    public void startTicking(float seconds, Runnable executor) {
        int ticks = (int) (seconds * 20);
        this.startTicking(ticks, executor);
    }

    public void startTicking(int ticks, Runnable executor) {
        this.shouldTick = true;
        this.tick = ticks;
        this.executor = executor;
        LoggerUtil.devLogger("Client ticker object is now ticking down");
    }

    public void stopAndResetTicking() {
        this.shouldTick = false;
        this.tick = -1;
        this.executor = () -> LoggerUtil.devLogger("Executor is empty");

        LoggerUtil.devLogger("Client ticker object stopped ticking down");
    }

    public void registerCountdown() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!shouldTick || client == null) return;
            this.tick--;
            if (tick <= 0) {
                this.shouldTick = false;
                this.executor.run();
            }
        });
    }
}
