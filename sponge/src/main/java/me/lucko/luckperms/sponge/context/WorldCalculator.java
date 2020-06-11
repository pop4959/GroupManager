/*
 * This file is part of GroupManager, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.sponge.context;

import me.lucko.luckperms.common.config.ConfigKeys;
import me.lucko.luckperms.common.context.contextset.ImmutableContextSetImpl;
import me.lucko.luckperms.sponge.LPSpongePlugin;

import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.DefaultContextKeys;
import net.luckperms.api.context.ImmutableContextSet;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WorldCalculator implements ContextCalculator<Subject> {
    private final LPSpongePlugin plugin;

    public WorldCalculator(LPSpongePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void calculate(@NonNull Subject subject, @NonNull ContextConsumer consumer) {
        CommandSource source = subject.getCommandSource().orElse(null);
        if (source == null || !(source instanceof Player)) {
            return;
        }

        Player p = ((Player) source);

        Set<String> seen = new HashSet<>();
        String world = p.getWorld().getName().toLowerCase();
        while (seen.add(world)) {
            consumer.accept(DefaultContextKeys.WORLD_KEY, world);
            world = this.plugin.getConfiguration().get(ConfigKeys.WORLD_REWRITES).getOrDefault(world, world).toLowerCase();
        }
    }

    @Override
    public ContextSet estimatePotentialContexts() {
        Game game = this.plugin.getBootstrap().getGame();
        if (!game.isServerAvailable()) {
            return ImmutableContextSetImpl.EMPTY;
        }

        Collection<World> worlds = game.getServer().getWorlds();
        ImmutableContextSet.Builder builder = new ImmutableContextSetImpl.BuilderImpl();
        for (World world : worlds) {
            builder.add(DefaultContextKeys.WORLD_KEY, world.getName().toLowerCase());
        }
        return builder.build();
    }

    @Listener(order = Order.LAST)
    public void onWorldChange(MoveEntityEvent.Teleport e) {
        Entity targetEntity = e.getTargetEntity();
        if (!(targetEntity instanceof Player)) {
            return;
        }

        if (e.getFromTransform().getExtent().equals(e.getToTransform().getExtent())) {
            return;
        }

        Player player = (Player) targetEntity;
        this.plugin.getContextManager().signalContextUpdate(player);
    }
}
