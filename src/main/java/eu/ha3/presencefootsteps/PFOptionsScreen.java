package eu.ha3.presencefootsteps;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.gson.FormattingStyle;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.JsonOps;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;

import eu.ha3.presencefootsteps.config.EntitySelector;
import eu.ha3.presencefootsteps.sound.acoustics.Acoustic;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsFile;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.util.BlockReport;
import eu.ha3.presencefootsteps.util.ResourceUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

class PFOptionsScreen {
    public static final Text TITLE = Text.translatable("menu.pf.title");

    Screen build(Screen parentScreen) {
        PFConfig config = PresenceFootsteps.getInstance().getConfig();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("%s (%s)", TITLE, PresenceFootsteps.getInstance().getOptionsKeyBinding().getBoundKeyLocalizedText()))
                .category(ConfigCategory.createBuilder()
                        .name(TITLE)
                        .tooltip(Text.literal("Main options of Presence Footsteps."))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("General"))
                                .description(OptionDescription.of(Text.literal("Options related to the general functionality of Presence Footsteps.")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("key.presencefootsteps.toggle"))
                                        .description(OptionDescription.of(Text.translatable("menu.pf.disable_mod")))
                                        .binding(false, config::getDisabled, config::setDisabled)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(state ->
                                                        Text.translatable(
                                                                "key.presencefootsteps.toggle." + (state ? "disabled": "enabled")
                                                        ).formatted(state ? Formatting.RED : Formatting.GREEN))
                                                .coloured(false))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("menu.pf.multiplayer"))
                                        .binding(true, config::getMultiplayer, config::setMultiplayer)
                                        .controller(TickBoxControllerBuilder::create)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(state -> Text.translatable("menu.pf.multiplayer." + state))
                                                .coloured(true))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("menu.pf.group.volume"))
                                .description(OptionDescription.of(Text.literal("Options related to loudness of sounds that are handled by Presence Footsteps.")))
                                .option(createVolumeOption("volume", 70, 0, 100, config::getGlobalVolume, config::setGlobalVolume))
                                .option(createVolumeOption("volume.player", 100, 0, 100, config::getClientPlayerVolume, config::setClientPlayerVolume))
                                .option(createVolumeOption("volume.other_players", 100, 0, 100, config::getOtherPlayerVolume, config::setOtherPlayerVolume))
                                .option(createVolumeOption("volume.hostile_entities", 100, 0, 100, config::getHostileEntitiesVolume, config::setHostileEntitiesVolume))
                                .option(createVolumeOption("volume.passive_entities", 100, 0, 100, config::getPassiveEntitiesVolume, config::setPassiveEntitiesVolume))
                                .option(createVolumeOption("volume.wet", 50, 0, 100, config::getWetSoundsVolume, config::setWetSoundsVolume))
                                .option(createVolumeOption("volume.foliage", 100, 0, 100, config::getFoliageSoundsVolume, config::setFoliageSoundsVolume))
                                .option(createVolumeOption("volume.running", 0, -100, 100, config::getRunningVolumeIncrease, config::setRunningVolumeIncrease))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("menu.pf.group.footsteps"))
                                .description(OptionDescription.of(Text.literal("Options related to loudness of sounds that are handled by Presence Footsteps.")))
                                .option(Option.<Locomotion>createBuilder()
                                        .name(Text.translatable("menu.pf.stance"))
                                        .description(v -> OptionDescription.of(v.getOptionTooltip()))
                                        .binding(new Binding<>() {
                                            @Override
                                            public void setValue(Locomotion value) {
                                                config.setLocomotion(value);
                                            }

                                            @Override
                                            public Locomotion getValue() {
                                                return config.getLocomotion();
                                            }

                                            @Override
                                            public Locomotion defaultValue() {
                                                return Locomotion.NONE;
                                            }
                                        })
                                        .controller(opt -> EnumControllerBuilder.create(opt)
                                                .enumClass(Locomotion.class)
                                                .formatValue(Locomotion::getOptionName)
                                        )
                                        .build())
                                .option(Option.<EntitySelector>createBuilder()
                                        .name(Text.translatable("menu.pf.footsteps.targets"))
                                        .binding(new Binding<>() {
                                            @Override
                                            public void setValue(EntitySelector value) {
                                                config.setEntitySelector(value);
                                            }

                                            @Override
                                            public EntitySelector getValue() {
                                                return config.getEntitySelector();
                                            }

                                            @Override
                                            public EntitySelector defaultValue() {
                                                return EntitySelector.ALL;
                                            }
                                        })
                                        .controller(opt -> EnumControllerBuilder.create(opt).
                                                enumClass(EntitySelector.class)
                                                .formatValue(EntitySelector::getOptionName)
                                        )
                                        .build())
                                .option(createOnOffOption("footwear", true, config::getFootwear, config::setGlobal))
                                .option(createOnOffOption("exclusive_mode", false, config::getExclusive, config::setExclusive))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("menu.pf.group.debugging"))
                                .option(ButtonOption.createBuilder()
                                        .name(Text.translatable("menu.pf.report.concise"))
                                        .description(OptionDescription.of(Text.translatable("menu.pf.report.concise.tooltip")))
                                        .available(MinecraftClient.getInstance().world != null)
                                        .action((screen, opt) -> {
                                            opt.setAvailable(false);
                                            BlockReport.execute(PresenceFootsteps.getInstance().getEngine().getIsolator(), "report_concise", false)
                                                    .thenRun(() -> opt.setAvailable(true));
                                        })
                                        .build())
                                .option(ButtonOption.createBuilder()
                                        .name(Text.translatable("menu.pf.report.full"))
                                        .description(OptionDescription.of(Text.translatable("menu.pf.report.full.tooltip")))
                                        .available(MinecraftClient.getInstance().world != null)
                                        .action((screen, opt) -> {
                                            opt.setAvailable(false);
                                            BlockReport.execute(PresenceFootsteps.getInstance().getEngine().getIsolator(), "report_full", false)
                                                    .thenRun(() -> opt.setAvailable(true));
                                        })
                                        .build())
                                .option(ButtonOption.createBuilder()
                                        .name(Text.translatable("menu.pf.report.acoustics"))
                                        .description(OptionDescription.of(Text.translatable("menu.pf.report.acoustics.tooltip")))
                                        .action((screen, opt) -> {
                                            opt.setAvailable(false);
                                            BlockReport.execute(loc -> {
                                                ResourceUtils.forEach(AcousticsFile.FILE_LOCATION, MinecraftClient.getInstance().getResourceManager(), reader -> {
                                                    Map<String, Acoustic> acoustics = new HashMap<>();
                                                    @SuppressWarnings("deprecation")
                                                    AcousticsFile file = AcousticsFile.read(reader, acoustics::put, true);
                                                    if (file != null) {
                                                        for (var acoustic : acoustics.entrySet()) {
                                                            Acoustic.CODEC.encodeStart(JsonOps.INSTANCE, acoustic.getValue()).resultOrPartial(error -> {
                                                                PresenceFootsteps.logger.error("Error whilst exporting acoustic: {}", error);
                                                            }).ifPresent(json -> {
                                                                try (var writer = new JsonWriter(Files.newBufferedWriter(loc.resolve(acoustic.getKey().toLowerCase(Locale.ROOT) + ".json")))) {
                                                                    writer.setFormattingStyle(FormattingStyle.PRETTY);
                                                                    Streams.write(json, writer);
                                                                } catch (IOException e) {
                                                                    PresenceFootsteps.logger.error("Error whilst exporting acoustics", e);
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }, "acoustics", "").thenRun(() -> opt.setAvailable(true));
                                        })
                                        .build())
                                .build())
                        .build())
                .save(() -> {
                    PresenceFootsteps.getInstance().saveAndReloadConfig();
                })
                .build()
                .generateScreen(parentScreen);
    }

    private Option<Integer> createVolumeOption(String key, Integer def, Integer min, Integer max, Supplier<Integer> getter, Consumer<Integer> setter) {
        return Option.<Integer>createBuilder()
                .name(Text.translatable("menu.pf." + key))
                .description(OptionDescription.of(Text.translatable("menu.pf." + key + ".tooltip")))
                .binding(def, getter, setter)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(min, max)
                        .step(1)
                )
                .build();
    }

    public Option<Boolean> createOnOffOption(String key, Boolean def, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Text.translatable("menu.pf." + key))
                .binding(def, getter, setter)
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .onOffFormatter()
                        .coloured(true))
                .build();
    }
}