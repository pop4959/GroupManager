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

package me.lucko.luckperms.common.commands.generic.parent;

import me.lucko.luckperms.common.command.CommandResult;
import me.lucko.luckperms.common.command.abstraction.GenericChildCommand;
import me.lucko.luckperms.common.command.access.ArgumentPermissions;
import me.lucko.luckperms.common.command.access.CommandPermission;
import me.lucko.luckperms.common.command.utils.ArgumentParser;
import me.lucko.luckperms.common.command.utils.MessageUtils;
import me.lucko.luckperms.common.command.utils.SortMode;
import me.lucko.luckperms.common.command.utils.SortType;
import me.lucko.luckperms.common.locale.LocaleManager;
import me.lucko.luckperms.common.locale.command.CommandSpec;
import me.lucko.luckperms.common.locale.message.Message;
import me.lucko.luckperms.common.model.HolderType;
import me.lucko.luckperms.common.model.PermissionHolder;
import me.lucko.luckperms.common.node.comparator.NodeWithContextComparator;
import me.lucko.luckperms.common.node.factory.NodeCommandFactory;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;
import me.lucko.luckperms.common.query.QueryOptionsImpl;
import me.lucko.luckperms.common.sender.Sender;
import me.lucko.luckperms.common.util.DurationFormatter;
import me.lucko.luckperms.common.util.Iterators;
import me.lucko.luckperms.common.util.Predicates;
import me.lucko.luckperms.common.util.TextUtils;

import net.kyori.text.ComponentBuilder;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.luckperms.api.node.types.InheritanceNode;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ParentInfo extends GenericChildCommand {
    public ParentInfo(LocaleManager locale) {
        super(CommandSpec.PARENT_INFO.localize(locale), "info", CommandPermission.USER_PARENT_INFO, CommandPermission.GROUP_PARENT_INFO, Predicates.notInRange(0, 2));
    }

    @Override
    public CommandResult execute(LuckPermsPlugin plugin, Sender sender, PermissionHolder holder, List<String> args, String label, CommandPermission permission) {
        if (ArgumentPermissions.checkViewPerms(plugin, sender, permission, holder)) {
            Message.COMMAND_NO_PERMISSION.send(sender);
            return CommandResult.NO_PERMISSION;
        }

        int page = ArgumentParser.parseIntOrElse(0, args, 1);
        SortMode sortMode = SortMode.determine(args);

        // get the holders nodes
        List<InheritanceNode> nodes = new LinkedList<>();
        holder.normalData().copyInheritanceNodesTo(nodes, QueryOptionsImpl.DEFAULT_NON_CONTEXTUAL);

        // remove irrelevant types (these are displayed in the other info commands)
        nodes.removeIf(node -> !node.getValue());

        // handle empty
        if (nodes.isEmpty()) {
            Message.PARENT_INFO_NO_DATA.send(sender, holder.getFormattedDisplayName());
            return CommandResult.SUCCESS;
        }

        // sort the list alphabetically instead
        if (sortMode.getType() == SortType.ALPHABETICALLY) {
            nodes.sort(ALPHABETICAL_NODE_COMPARATOR);
        }

        // reverse the order if necessary
        if (!sortMode.isAscending()) {
            Collections.reverse(nodes);
        }

        int pageIndex = page - 1;
        List<List<InheritanceNode>> pages = Iterators.divideIterable(nodes, 19);

        if (pageIndex < 0 || pageIndex >= pages.size()) {
            page = 1;
            pageIndex = 0;
        }

        List<InheritanceNode> content = pages.get(pageIndex);

        // send header
        Message.PARENT_INFO.send(sender, holder.getFormattedDisplayName(), page, pages.size(), nodes.size());

        // send content
        for (InheritanceNode node : content) {
            String s = "&3> &a" + node.getGroupName() + MessageUtils.getAppendableNodeContextString(plugin.getLocaleManager(), node);
            if (node.hasExpiry()) {
                s += "\n&2  expires in " + DurationFormatter.LONG.format(node.getExpiryDuration());
            }

            TextComponent message = TextUtils.fromLegacy(s, TextUtils.AMPERSAND_CHAR).toBuilder().applyDeep(makeFancy(holder, label, node)).build();
            sender.sendMessage(message);
        }

        return CommandResult.SUCCESS;
    }

    private static final Comparator<InheritanceNode> ALPHABETICAL_NODE_COMPARATOR = (o1, o2) -> {
        int i = o1.getGroupName().compareTo(o2.getGroupName());
        if (i != 0) {
            return i;
        }

        // fallback to priority
        return NodeWithContextComparator.reverse().compare(o1, o2);
    };

    private static Consumer<ComponentBuilder<? ,?>> makeFancy(PermissionHolder holder, String label, InheritanceNode node) {
        HoverEvent hoverEvent = HoverEvent.showText(TextUtils.fromLegacy(TextUtils.joinNewline(
                "&3> &f" + node.getGroupName(),
                " ",
                "&7Click to remove this parent from " + holder.getPlainDisplayName()
        ), TextUtils.AMPERSAND_CHAR));

        String id = holder.getType() == HolderType.GROUP ? holder.getObjectName() : holder.getPlainDisplayName();
        boolean explicitGlobalContext = !holder.getPlugin().getConfiguration().getContextsFile().getDefaultContexts().isEmpty();
        String command = "/" + label + " " + NodeCommandFactory.undoCommand(node, id, holder.getType(), explicitGlobalContext);
        ClickEvent clickEvent = ClickEvent.suggestCommand(command);

        return component -> {
            component.hoverEvent(hoverEvent);
            component.clickEvent(clickEvent);
        };
    }
}
