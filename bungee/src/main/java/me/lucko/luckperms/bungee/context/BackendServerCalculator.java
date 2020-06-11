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

package me.lucko.luckperms.bungee.context;

import me.lucko.luckperms.bungee.LPBungeePlugin;
import me.lucko.luckperms.common.config.ConfigKeys;
import me.lucko.luckperms.common.context.contextset.ImmutableContextSetImpl;

import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.DefaultContextKeys;
import net.luckperms.api.context.ImmutableContextSet;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BackendServerCalculator implements ContextCalculator<ProxiedPlayer>, Listener {

    private static String getServer(ProxiedPlayer player) {
        return player.getServer() == null ? null : (player.getServer().getInfo() == null ? null : player.getServer().getInfo().getName().toLowerCase());
    }

    private final LPBungeePlugin plugin;

    public BackendServerCalculator(LPBungeePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void calculate(@NonNull ProxiedPlayer subject, @NonNull ContextConsumer consumer) {
        Set<String> seen = new HashSet<>();
        String server = getServer(subject);
        while (server != null && seen.add(server)) {
            consumer.accept(DefaultContextKeys.WORLD_KEY, server);
            server = this.plugin.getConfiguration().get(ConfigKeys.WORLD_REWRITES).getOrDefault(server, server).toLowerCase();
        }
    }

    @Override
    public ContextSet estimatePotentialContexts() {
        Collection<ServerInfo> servers = this.plugin.getBootstrap().getProxy().getServers().values();
        ImmutableContextSet.Builder builder = new ImmutableContextSetImpl.BuilderImpl();
        for (ServerInfo server : servers) {
            builder.add(DefaultContextKeys.WORLD_KEY, server.getName().toLowerCase());
        }
        return builder.build();
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent e) {
        this.plugin.getContextManager().signalContextUpdate(e.getPlayer());
    }
}
