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

package me.lucko.luckperms.velocity.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.lucko.luckperms.velocity.LPVelocityPlugin;

import net.luckperms.api.messenger.IncomingMessageConsumer;
import net.luckperms.api.messenger.Messenger;
import net.luckperms.api.messenger.message.OutgoingMessage;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An implementation of {@link Messenger} using the plugin messaging channels.
 */
public class PluginMessageMessenger implements Messenger {
    private static final ChannelIdentifier CHANNEL = MinecraftChannelIdentifier.create("luckperms", "update");

    private final LPVelocityPlugin plugin;
    private final IncomingMessageConsumer consumer;

    public PluginMessageMessenger(LPVelocityPlugin plugin, IncomingMessageConsumer consumer) {
        this.plugin = plugin;
        this.consumer = consumer;
    }

    public void init() {
        ProxyServer proxy = this.plugin.getBootstrap().getProxy();
        proxy.getChannelRegistrar().register(CHANNEL);
        proxy.getEventManager().register(this.plugin.getBootstrap(), this);
    }

    @Override
    public void close() {
        ProxyServer proxy = this.plugin.getBootstrap().getProxy();
        proxy.getChannelRegistrar().unregister(CHANNEL);
        proxy.getEventManager().unregisterListener(this.plugin.getBootstrap(), this);
    }

    private void dispatchMessage(byte[] message) {
        for (RegisteredServer server : this.plugin.getBootstrap().getProxy().getAllServers()) {
            server.sendPluginMessage(CHANNEL, message);
        }
    }

    @Override
    public void sendOutgoingMessage(@NonNull OutgoingMessage outgoingMessage) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(outgoingMessage.asEncodedString());

        byte[] message = out.toByteArray();
        dispatchMessage(message);
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent e) {
        // compare the underlying text representation of the channel
        // the namespaced representation is used by legacy servers too, so we
        // are able to support both. :)
        if (!e.getIdentifier().getId().equals(CHANNEL.getId())) {
            return;
        }

        e.setResult(ForwardResult.handled());

        if (e.getSource() instanceof Player) {
            return;
        }

        ByteArrayDataInput in = e.dataAsDataStream();
        String msg = in.readUTF();

        if (this.consumer.consumeIncomingMessageAsString(msg)) {
            // Forward to other servers
            this.plugin.getBootstrap().getScheduler().executeAsync(() -> dispatchMessage(e.getData()));
        }
    }
}
