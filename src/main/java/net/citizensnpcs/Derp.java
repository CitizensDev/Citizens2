package net.citizensnpcs;

import java.util.Random;

import net.citizensnpcs.api.ai.AI;
import net.citizensnpcs.api.ai.AbstractGoal;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Character;
import net.citizensnpcs.api.trait.SaveId;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.Messaging;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;

@SaveId("derp")
public class Derp extends Character {
    @Override
    public void load(DataKey key) throws NPCLoadException {
        Messaging.log("load? derples!");
    }

    @Override
    public void save(DataKey key) {
        Messaging.log("save? derpin' herp!");
    }

    @Override
    public void onSet(final NPC npc) {
        AI ai = npc.getAI();
        final Random rand = new Random();
        ai.addGoal(0, new AbstractGoal() {
            @Override
            public void start() {
                npc.chat("derp time!");
            }

            @Override
            public void update() {
                Location loc = npc.getBukkitEntity().getLocation();
                loc.setPitch(new Random().nextFloat() * 360);
                loc.setYaw(new Random().nextFloat() * 360);
                npc.getBukkitEntity().teleport(loc);
                npc.move(rand.nextInt(3), rand.nextInt(3), rand.nextInt(3));
            }

            @Override
            public boolean isCompatibleWith(Goal other) {
                return false;
            }

            @Override
            public boolean continueExecuting() {
                return rand.nextInt(100) < 80;
            }
        });
        ai.addGoal(1, new AbstractGoal() {

            @Override
            public void update() {
                int length = rand.nextInt(10);
                StringBuilder builder = new StringBuilder();
                int values = ChatColor.values().length;
                for (int i = 0; i <= length; ++i) {
                    builder.append(ChatColor.values()[rand.nextInt(values)].toString() + getRandomChar());
                }
                npc.chat(builder.toString());
            }

            @Override
            public boolean continueExecuting() {
                return rand.nextInt(100) < 60;
            }

            private char getRandomChar() {
                int r = rand.nextInt(1000);
                while (!java.lang.Character.isDefined(r)) {
                    r = rand.nextInt(1000);
                }
                return (char) r;
            }
        });
        ai.addGoal(2, new AbstractGoal() {

            @Override
            public boolean continueExecuting() {
                return rand.nextInt(100) < 40;
            }

            @Override
            public void start() {
                npc.chat("Firing away!");
            }

            @Override
            public void update() {
                Location loc = npc.getBukkitEntity().getLocation();
                loc.setPitch(new Random().nextFloat() * 360);
                loc.setYaw(new Random().nextFloat() * 360);
                npc.getBukkitEntity().teleport(loc);
                npc.getBukkitEntity().launchProjectile(Arrow.class);
            }
        });
    }
}
