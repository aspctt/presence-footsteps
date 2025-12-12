package eu.ha3.presencefootsteps;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import eu.ha3.presencefootsteps.config.EntitySelector;
import eu.ha3.presencefootsteps.config.JsonFile;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;

public class PFConfig extends JsonFile {

    private boolean disabled = false;

    private float volume = 0.7F;
    private float runningVolumeIncrease = 0.0F;

    private float clientPlayerVolume = 1.0F;
    private float otherPlayerVolume = 1.0F;
    private float hostileEntitiesVolume = 1.0F;
    private float passiveEntitiesVolume = 1.0F;

    private float wetSoundsVolume = 0.5F;
    private float foliageSoundsVolume = 1.0F;

    private int maxSteppingEntities = 50;

    private boolean multiplayer = true;
    private boolean global = true;
    private boolean footwear = true;
    private boolean visualiser = false;
    private boolean exclusive = false;

    private Locomotion stance = Locomotion.NONE;

    private EntitySelector targetEntities = EntitySelector.ALL;

    private Set<Identifier> ignoredEntityTypes = Set.of(
                Identifier.ofVanilla("ghast"),
                Identifier.ofVanilla("happy_ghast"),
                Identifier.ofVanilla("phantom")
            );

    public PFConfig(Path file) {
        super(file);
    }

    public boolean isDisabled() {
        return disabled || getGlobalVolume() < 0.03F;
    }

    public boolean getEnabled() {
        return !isDisabled();
    }

    public boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(boolean value) {
        disabled = value;
    }

    public int getGlobalVolume() {
        return (int) (MathHelper.clamp(volume, 0.0F, 1.0F) * 100.0F);
    }

    public void setGlobalVolume(int value) {
        volume = MathHelper.clamp(value * 0.01F, 0.0F, 1.0F);
    }

    public int getRunningVolumeIncrease() {
        return (int) (MathHelper.clamp(runningVolumeIncrease, -1.0F, 1.0F) * 100.0F);
    }

    public void setRunningVolumeIncrease(int value) {
        runningVolumeIncrease = MathHelper.clamp(value * 0.01F, -1.0F, 1.0F);
    }

    public int getClientPlayerVolume() {
        return (int) (MathHelper.clamp(clientPlayerVolume, 0.0F, 1.0F) * 100.0F);
    }

    public void setClientPlayerVolume(int value) {
        clientPlayerVolume = MathHelper.clamp(value * 0.01F, 0.0F, 1.0F);
    }

    public int getOtherPlayerVolume() {
        return (int) (MathHelper.clamp(otherPlayerVolume, 0.0F, 1.0F) * 100.0F);
    }

    public void setOtherPlayerVolume(int value) {
        otherPlayerVolume = MathHelper.clamp(value * 0.01F, 0.0F, 1.0F);
    }

    public int getHostileEntitiesVolume() {
        return (int) (MathHelper.clamp(hostileEntitiesVolume, 0.0F, 1.0F) * 100.0F);
    }

    public void setHostileEntitiesVolume(int value) {
        hostileEntitiesVolume = MathHelper.clamp(value * 0.01F, 0.0F, 1.0F);
    }

    public int getPassiveEntitiesVolume() {
        return (int) (MathHelper.clamp(passiveEntitiesVolume, 0.0F, 1.0F) * 100.0F);
    }

    public void setPassiveEntitiesVolume(int value) {
        passiveEntitiesVolume = MathHelper.clamp(value * 0.01F, 0.0F, 1.0F);
    }

    public int getWetSoundsVolume() {
        return (int) (MathHelper.clamp(wetSoundsVolume, 0.0F, 1.0F) * 100.0F);
    }

    public void setWetSoundsVolume(int value) {
        wetSoundsVolume = MathHelper.clamp(value * 0.01F, 0.0F, 1.0F);
    }

    public int getFoliageSoundsVolume() {
        return (int) (MathHelper.clamp(foliageSoundsVolume, 0.0F, 1.0F) * 100.0F);
    }

    public void setFoliageSoundsVolume(int value) {
        foliageSoundsVolume = MathHelper.clamp(value * 0.01F, 0.0F, 1.0F);
    }

    public int getMaxSteppingEntities() {
        return maxSteppingEntities;
    }

    public void setMaxSteppingEntities(int value) {
        maxSteppingEntities = value;
    }

    public boolean getMultiplayer() {
        return multiplayer;
    }

    public void setMultiplayer(boolean value) {
        multiplayer = value;
    }

    public boolean getGlobal() {
        return global;
    }

    public void setGlobal(boolean value) {
        global = value;
    }

    public boolean getFootwear() {
        return footwear;
    }

    public void setFootwear(boolean value) {
        footwear = value;
    }

    public boolean getVisualiser() {
        return visualiser;
    }

    public void setVisualiser(boolean value) {
        visualiser = value;
    }

    public boolean getExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean value) {
        exclusive = value;
    }

    public Locomotion getLocomotion() {
        return stance == null ? Locomotion.NONE : stance;
    }

    public void setLocomotion(Locomotion value) {
        stance = value == null ? Locomotion.NONE : value;
    }

    public EntitySelector getEntitySelector() {
        return targetEntities == null ? EntitySelector.ALL : targetEntities;
    }

    public void setEntitySelector(EntitySelector value) {
        targetEntities = value == null ? EntitySelector.ALL : value;
    }

    public boolean isIgnoredForFootsteps(EntityType<?> type) {
        return this.ignoredEntityTypes.contains(Registries.ENTITY_TYPE.getId(type));
    }

    public void populateCrashReport(CrashReportSection section) {
        section.add("Disabled", getDisabled());
        section.add("Global Volume", volume);
        section.add("User's Selected Stance", getLocomotion());
        section.add("Target Selector", getEntitySelector());
        section.add("Enabled Global", global);
        section.add("Enabled Multiplayer", multiplayer);
    }
}
