package net.shirojr.nemuelch.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RegistryParticleListScreen extends Screen {
    private final ParticleEmitterBlockScreen parentScreen;
    private RegistryParticleEntryListWidget particleEntryList;

    protected RegistryParticleListScreen(Text title, ParticleEmitterBlockScreen parentScreen) {
        super(title);
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        this.particleEntryList = new RegistryParticleEntryListWidget(this.client);
        this.addSelectableChild(this.particleEntryList);
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 75, this.height - 38, 150, 20, ScreenTexts.DONE, button -> {
            RegistryParticleEntryListWidget.ParticleEntry particleEntry = this.particleEntryList.getSelectedOrNull();
            if (particleEntry != null) {
                this.parentScreen.setSelectedParticleType(particleEntry.particleType);
            }
            if (this.client != null) {
                this.client.setScreen(this.parentScreen);
            }
        }));

        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.particleEntryList.render(matrices, mouseX, mouseY, delta);
        RegistryParticleListScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    class RegistryParticleEntryListWidget extends AlwaysSelectedEntryListWidget<RegistryParticleEntryListWidget.ParticleEntry> {
        public RegistryParticleEntryListWidget(MinecraftClient minecraftClient) {
            super(minecraftClient, RegistryParticleListScreen.this.width,
                    RegistryParticleListScreen.this.height, 32,
                    RegistryParticleListScreen.this.height - 65 + 4, 18);

            for (ParticleType<?> entry : Registry.PARTICLE_TYPE) {
                ParticleEntry particleEntry = new ParticleEntry(entry);
                this.addEntry(particleEntry);

            }
        }

        public class ParticleEntry extends AlwaysSelectedEntryListWidget.Entry<ParticleEntry> {
            final ParticleType<?> particleType;

            public ParticleEntry(ParticleType<?> particleType) {
                this.particleType = particleType;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button != 0) return false;
                RegistryParticleEntryListWidget parentWidget = RegistryParticleEntryListWidget.this;
                RegistryParticleListScreen parentScreen = RegistryParticleListScreen.this;
                ParticleEntry selectedEntry = parentWidget.getSelectedOrNull();
                if (parentScreen.client != null && selectedEntry != null && selectedEntry.particleType.equals(this.particleType)) {
                    parentScreen.client.setScreen(parentScreen.parentScreen);
                    parentScreen.parentScreen.setSelectedParticleType(selectedEntry.particleType);
                    parentScreen.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
                parentWidget.setSelected(this);
                return true;
            }

            @Override
            public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                Identifier id = Registry.PARTICLE_TYPE.getId(this.particleType);
                if (id != null) {
                    RegistryParticleListScreen parentScreen = RegistryParticleListScreen.this;
                    parentScreen.textRenderer.drawWithShadow(matrices, id.toString(),
                            (float) parentScreen.width / 2 - (float) parentScreen.textRenderer.getWidth(id.toString()) / 2,
                            y + 1, 0xFFFFFF, true);

                }
            }

            @Override
            public Text getNarration() {
                Identifier id = Registry.PARTICLE_TYPE.getId(this.particleType);
                if (id == null) return null;
                return new TranslatableText(id.getPath());
            }
        }
    }
}
