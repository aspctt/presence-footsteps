package eu.ha3.presencefootsteps;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.screen.DebugOptionsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import eu.ha3.presencefootsteps.sound.SoundEngine;
import eu.ha3.presencefootsteps.util.Edge;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class PresenceFootsteps implements ClientModInitializer {
    public static final Logger logger = LogManager.getLogger("PFSolver");

    private static final String MODID = "presencefootsteps";
    private static final KeyBinding.Category KEY_BINDING_CATEGORY = KeyBinding.Category.create(id("category"));

    public static final Text MOD_NAME = Text.translatable("mod.presencefootsteps.name");

    public static Identifier id(String name) {
        return Identifier.of(MODID, name);
    }

    private static PresenceFootsteps instance;

    public static PresenceFootsteps getInstance() {
        return instance;
    }

    private final Path pfFolder = FabricLoader.getInstance().getConfigDir().resolve("presencefootsteps");
    private final PFConfig config = new PFConfig(pfFolder.resolve("userconfig.json"));
    private final SoundEngine engine = new SoundEngine(config);
    private final PFDebugHud debugHud = new PFDebugHud(engine);
    private boolean prevEnabled = config.getEnabled();

    private final KeyBinding optionsKeyBinding = new KeyBinding("key.presencefootsteps.settings", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_F10, KEY_BINDING_CATEGORY);
    private final KeyBinding toggleKeyBinding = new KeyBinding("key.presencefootsteps.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, KEY_BINDING_CATEGORY);
    private final KeyBinding debugToggleKeyBinding = new KeyBinding("key.presencefootsteps.debug_toggle", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_Z, KEY_BINDING_CATEGORY);
    private final Edge toggler = new Edge(z -> {
        if (z) {
            config.setDisabled(!config.getDisabled());
            saveAndReloadConfig();
        }
    });
    private final Edge debugToggle = new Edge(z -> {
        if (z) {
            MinecraftClient.getInstance().debugHudEntryList.toggleVisibility(PFDebugHud.ID);
        }
    });

    public PresenceFootsteps() {
        instance = this;
    }

    public PFDebugHud getDebugHud() {
        return debugHud;
    }

    public SoundEngine getEngine() {
        return engine;
    }

    public PFConfig getConfig() {
        return config;
    }

    public KeyBinding getOptionsKeyBinding() {
        return optionsKeyBinding;
    }


    @Override
    public void onInitializeClient() {
        config.load();

        KeyBindingHelper.registerKeyBinding(optionsKeyBinding);
        KeyBindingHelper.registerKeyBinding(toggleKeyBinding);
        KeyBindingHelper.registerKeyBinding(debugToggleKeyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(SoundEngine.ID, engine);
        DebugHudEntries.register(PFDebugHud.ID, debugHud);
    }

    private void onTick(MinecraftClient client) {
        debugToggle.accept(MinecraftClient.getInstance().debugHudEntryList.isF3Enabled() && debugToggleKeyBinding.isPressed());

        Optional.ofNullable(client.player).filter(e -> !e.isRemoved()).ifPresent(cameraEntity -> {
            if (client.currentScreen == null) {
                if (optionsKeyBinding.isPressed()) {
                    client.setScreen(new PFOptionsScreen().build(client.currentScreen));
                }
                toggler.accept(toggleKeyBinding.isPressed());
            }

            engine.onFrame(client, cameraEntity);
        });
    }

    void onEnabledStateChange(boolean enabled) {
        showSystemToast(
                MOD_NAME,
                Text.translatable("key.presencefootsteps.toggle." + (enabled ? "enabled" : "disabled")).formatted(enabled ? Formatting.GREEN : Formatting.GRAY)
        );
    }

    public void showSystemToast(Text title, Text body) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.getToastManager().add(SystemToast.create(client, SystemToast.Type.PACK_LOAD_FAILURE, title, body));
    }

    public void saveAndReloadConfig() {
        config.save();
        boolean enabled = config.getEnabled();
        if (prevEnabled != enabled) {
            onEnabledStateChange(enabled);
            prevEnabled = enabled;
        }
        engine.reload();
    }
}
