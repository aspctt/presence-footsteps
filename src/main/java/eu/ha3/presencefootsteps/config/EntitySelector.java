package eu.ha3.presencefootsteps.config;

import java.util.Locale;
import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum EntitySelector implements Predicate<Entity> {
    ALL {
        @Override
        public boolean test(Entity e) {
            return true;
        }
    },
    PLAYERS_AND_HOSTILES {
        @Override
        public boolean test(Entity e) {
            return e instanceof PlayerEntity || e instanceof Monster;
        }
    },
    PLAYERS_AND_PASSIVES {
        @Override
        public boolean test(Entity e) {
            return e instanceof PlayerEntity || !(e instanceof Monster);
        }
    },
    PLAYERS_ONLY {
        @Override
        public boolean test(Entity e) {
            return e instanceof PlayerEntity;
        }
    };

    public static final EntitySelector[] VALUES = values();

    private final String translationKey = "menu.pf.global." + name().toLowerCase(Locale.ROOT);

    public Text getOptionName() {
        return Text.translatable(translationKey).formatted(name().equals("ALL") ? Formatting.GREEN : Formatting.RESET);
    }
}
