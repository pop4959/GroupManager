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

package me.lucko.luckperms.common.commands.generic.other;

import com.google.gson.JsonObject;

import me.lucko.luckperms.common.command.CommandResult;
import me.lucko.luckperms.common.command.abstraction.ChildCommand;
import me.lucko.luckperms.common.command.access.ArgumentPermissions;
import me.lucko.luckperms.common.command.access.CommandPermission;
import me.lucko.luckperms.common.commands.misc.EditorCommand;
import me.lucko.luckperms.common.context.contextset.ImmutableContextSetImpl;
import me.lucko.luckperms.common.locale.LocaleManager;
import me.lucko.luckperms.common.locale.command.CommandSpec;
import me.lucko.luckperms.common.locale.message.Message;
import me.lucko.luckperms.common.model.Group;
import me.lucko.luckperms.common.model.HolderType;
import me.lucko.luckperms.common.model.PermissionHolder;
import me.lucko.luckperms.common.model.User;
import me.lucko.luckperms.common.node.matcher.ConstraintNodeMatcher;
import me.lucko.luckperms.common.node.matcher.StandardNodeMatchers;
import me.lucko.luckperms.common.node.types.Inheritance;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;
import me.lucko.luckperms.common.sender.Sender;
import me.lucko.luckperms.common.storage.misc.NodeEntry;
import me.lucko.luckperms.common.util.Predicates;
import me.lucko.luckperms.common.verbose.event.MetaCheckEvent;
import me.lucko.luckperms.common.web.WebEditor;

import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HolderEditor<T extends PermissionHolder> extends ChildCommand<T> {
    public HolderEditor(LocaleManager locale, HolderType type) {
        super(CommandSpec.HOLDER_EDITOR.localize(locale), "editor", type == HolderType.USER ? CommandPermission.USER_EDITOR : CommandPermission.GROUP_EDITOR, Predicates.alwaysFalse());
    }

    @Override
    public CommandResult execute(LuckPermsPlugin plugin, Sender sender, T holder, List<String> args, String label) {
        if (ArgumentPermissions.checkViewPerms(plugin, sender, getPermission().get(), holder) || ArgumentPermissions.checkGroup(plugin, sender, holder, ImmutableContextSetImpl.EMPTY)) {
            Message.COMMAND_NO_PERMISSION.send(sender);
            return CommandResult.NO_PERMISSION;
        }

        List<PermissionHolder> holders = new ArrayList<>();

        // also include users who are a member of the group
        if (holder instanceof Group) {
            Group group = (Group) holder;
            ConstraintNodeMatcher<Node> matcher = StandardNodeMatchers.key(Inheritance.key(group.getName()));

            Map<UUID, User> users = new LinkedHashMap<>(plugin.getUserManager().getAll());

            // only include online players who are in the group
            users.values().removeIf(user -> user.normalData().immutable().values().stream().noneMatch(matcher));

            // fill up with other matching users
            if (users.size() < EditorCommand.MAX_USERS) {
                plugin.getStorage().searchUserNodes(matcher).join().stream()
                        .map(NodeEntry::getHolder)
                        .distinct()
                        .filter(uuid -> !users.containsKey(uuid))
                        .sorted()
                        .limit(EditorCommand.MAX_USERS - users.size())
                        .forEach(uuid -> {
                            User user = plugin.getStorage().loadUser(uuid, null).join();
                            if (user != null) {
                                users.put(uuid, user);
                            }
                            plugin.getUserManager().getHouseKeeper().cleanup(uuid);
                        });
            }

            users.values().stream()
                    .sorted(Comparator
                            .<User>comparingInt(u -> u.getCachedData().getMetaData(QueryOptions.nonContextual()).getWeight(MetaCheckEvent.Origin.INTERNAL)).reversed()
                            .thenComparing(User::getFormattedDisplayName, String.CASE_INSENSITIVE_ORDER)
                    )
                    .forEach(holders::add);

            // remove holders which the sender doesn't have perms to view
            holders.removeIf(h -> ArgumentPermissions.checkViewPerms(plugin, sender, getPermission().get(), h) || ArgumentPermissions.checkGroup(plugin, sender, h, ImmutableContextSetImpl.EMPTY));
        }

        // include the original holder too
        holders.add(holder);

        Message.EDITOR_START.send(sender);

        JsonObject payload = WebEditor.formPayload(holders, Collections.emptyList(), sender, label, plugin);
        return WebEditor.post(payload, sender, plugin);
    }

}
