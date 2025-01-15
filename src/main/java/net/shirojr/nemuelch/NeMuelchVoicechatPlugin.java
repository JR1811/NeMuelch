package net.shirojr.nemuelch;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;

public class NeMuelchVoicechatPlugin implements VoicechatPlugin {
    public static VoicechatApi voicechatApi;

    @Override
    public String getPluginId() {
        return NeMuelch.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        VoicechatPlugin.super.initialize(api);
        voicechatApi = api;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        VoicechatPlugin.super.registerEvents(registration);
        //TODO: registration.registerEvent(MicrophonePacketEvent.class, IllusionVoiceChat::onMicrophonePacket);
    }
}
