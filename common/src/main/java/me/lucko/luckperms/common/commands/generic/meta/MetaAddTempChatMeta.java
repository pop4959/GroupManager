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

package me.lucko.luckperms.common.commands.generic.meta;

import me.lucko.luckperms.common.actionlog.LoggedAction;
import me.lucko.luckperms.common.command.CommandResult;
import me.lucko.luckperms.common.command.abstraction.CommandException;
import me.lucko.luckperms.common.command.abstraction.GenericChildCommand;
import me.lucko.luckperms.common.command.access.ArgumentPermissions;
import me.lucko.luckperms.common.command.access.CommandPermission;
import me.lucko.luckperms.common.command.tabcomplete.TabCompleter;
import me.lucko.luckperms.common.command.tabcomplete.TabCompletions;
import me.lucko.luckperms.common.command.utils.ArgumentParser;
import me.lucko.luckperms.common.command.utils.MessageUtils;
import me.lucko.luckperms.common.command.utils.StorageAssistant;
import me.lucko.luckperms.common.config.ConfigKeys;
import me.lucko.luckperms.common.locale.LocaleManager;
import me.lucko.luckperms.common.locale.command.CommandSpec;
import me.lucko.luckperms.common.locale.command.LocalizedCommandSpec;
import me.lucko.luckperms.common.locale.message.Message;
import me.lucko.luckperms.common.model.PermissionHolder;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;
import me.lucko.luckperms.common.sender.Sender;
import me.lucko.luckperms.common.util.DurationFormatter;
import me.lucko.luckperms.common.util.Predicates;
import me.lucko.luckperms.common.util.TextUtils;

import net.kyori.text.TextComponent;
import net.kyori.text.event.HoverEvent;
import net.luckperms.api.context.MutableContextSet;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.data.DataType;
import net.luckperms.api.model.data.TemporaryNodeMergeStrategy;
import net.luckperms.api.node.ChatMetaType;

import java.time.Duration;
import java.util.List;

public class MetaAddTempChatMeta extends GenericChildCommand {

    public static MetaAddTempChatMeta forPrefix(LocaleManager locale) {
        return new MetaAddTempChatMeta(
                ChatMetaType.PREFIX,
                CommandSpec.META_ADDTEMP_PREFIX.localize(locale),
                "addtempprefix",
                CommandPermission.USER_META_ADD_TEMP_PREFIX,
                CommandPermission.GROUP_META_ADD_TEMP_PREFIX
        );
    }

    public static MetaAddTempChatMeta forSuffix(LocaleManager locale) {
        return new MetaAddTempChatMeta(
                ChatMetaType.SUFFIX,
                CommandSpec.META_ADDTEMP_SUFFIX.localize(locale),
                "addtempsuffix",
                CommandPermission.USER_META_ADD_TEMP_SUFFIX,
                CommandPermission.GROUP_META_ADD_TEMP_SUFFIX
        );
    }

    private final ChatMetaType type;

    private MetaAddTempChatMeta(ChatMetaType type, LocalizedCommandSpec spec, String name, CommandPermission userPermission, CommandPermission groupPermission) {
        super(spec, name, userPermission, groupPermission, Predicates.inRange(0, 2));
        this.type = type;
    }

    @Override
    public CommandResult execute(LuckPermsPlugin plugin, Sender sender, PermissionHolder holder, List<String> args, String label, CommandPermission permission) throws CommandException {
        if (ArgumentPermissions.checkModifyPerms(plugin, sender, permission, holder)) {
            Message.COMMAND_NO_PERMISSION.send(sender);
            return CommandResult.NO_PERMISSION;
        }

        int priority = ArgumentParser.parsePriority(0, args);
        String meta = ArgumentParser.parseString(1, args);
        Duration duration = ArgumentParser.parseDuration(2, args);
        TemporaryNodeMergeStrategy modifier = ArgumentParser.parseTemporaryModifier(3, args).orElseGet(() -> plugin.getConfiguration().get(ConfigKeys.TEMPORARY_ADD_BEHAVIOUR));
        MutableContextSet context = ArgumentParser.parseContext(3, args, plugin);

        if (ArgumentPermissions.checkContext(plugin, sender, permission, context) ||
                ArgumentPermissions.checkGroup(plugin, sender, holder, context)) {
            Message.COMMAND_NO_PERMISSION.send(sender);
            return CommandResult.NO_PERMISSION;
        }

        DataMutateResult.WithMergedNode result = holder.setNode(DataType.NORMAL, this.type.builder(meta, priority).expiry(duration).withContext(context).build(), modifier);

        if (result.getResult().wasSuccessful()) {
            duration = result.getMergedNode().getExpiryDuration();

            TextComponent.Builder builder = Message.ADD_TEMP_CHATMETA_SUCCESS.asComponent(plugin.getLocaleManager(), holder.getFormattedDisplayName(), this.type.name().toLowerCase(), meta, priority, DurationFormatter.LONG.format(duration), MessageUtils.contextSetToString(plugin.getLocaleManager(), context)).toBuilder();
            HoverEvent event = HoverEvent.showText(TextUtils.fromLegacy(
                    "§3Raw " + this.type.name().toLowerCase() + ": §r" + meta,
                    '§'
            ));
            builder.applyDeep(c -> c.hoverEvent(event));
            sender.sendMessage(builder.build());

            LoggedAction.build().source(sender).target(holder)
                    .description("meta" , "addtemp" + this.type.name().toLowerCase(), priority, meta, duration, context)
                    .build().submit(plugin, sender);

            StorageAssistant.save(holder, sender, plugin);
            return CommandResult.SUCCESS;
        } else {
            Message.ALREADY_HAS_TEMP_CHAT_META.send(sender, holder.getFormattedDisplayName(), this.type.name().toLowerCase(), meta, priority, MessageUtils.contextSetToString(plugin.getLocaleManager(), context));
            return CommandResult.STATE_ERROR;
        }
    }

    @Override
    public List<String> tabComplete(LuckPermsPlugin plugin, Sender sender, List<String> args) {
        return TabCompleter.create()
                .from(3, TabCompletions.contexts(plugin))
                .complete(args);
    }
}
