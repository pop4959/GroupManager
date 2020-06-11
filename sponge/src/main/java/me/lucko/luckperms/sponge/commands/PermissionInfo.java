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

package me.lucko.luckperms.sponge.commands;

import com.google.common.collect.ImmutableMap;

import me.lucko.luckperms.common.command.CommandResult;
import me.lucko.luckperms.common.command.abstraction.ChildCommand;
import me.lucko.luckperms.common.command.access.CommandPermission;
import me.lucko.luckperms.common.command.utils.ArgumentParser;
import me.lucko.luckperms.common.locale.LocaleManager;
import me.lucko.luckperms.common.locale.command.CommandSpec;
import me.lucko.luckperms.common.locale.message.Message;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;
import me.lucko.luckperms.common.sender.Sender;
import me.lucko.luckperms.common.util.Predicates;
import me.lucko.luckperms.sponge.service.model.LPSubjectData;

import net.luckperms.api.context.ImmutableContextSet;

import java.util.List;
import java.util.Map;

public class PermissionInfo extends ChildCommand<LPSubjectData> {
    public PermissionInfo(LocaleManager locale) {
        super(CommandSpec.SPONGE_PERMISSION_INFO.localize(locale), "info", CommandPermission.SPONGE_PERMISSION_INFO, Predicates.alwaysFalse());
    }

    @Override
    public CommandResult execute(LuckPermsPlugin plugin, Sender sender, LPSubjectData subjectData, List<String> args, String label) {
        ImmutableContextSet contextSet = ArgumentParser.parseContextSponge(0, args);
        if (contextSet.isEmpty()) {
            Message.BLANK.send(sender, "&aShowing permissions matching contexts &bANY&a.");
            Map<ImmutableContextSet, ImmutableMap<String, Boolean>> permissions = subjectData.getAllPermissions();
            if (permissions.isEmpty()) {
                Message.BLANK.send(sender, "That subject does not have any permissions defined.");
                return CommandResult.SUCCESS;
            }

            for (Map.Entry<ImmutableContextSet, ImmutableMap<String, Boolean>> e : permissions.entrySet()) {
                Message.BLANK.send(sender, "&3>> &bContext: " + SpongeCommandUtils.contextToString(e.getKey(), plugin.getLocaleManager()) + "\n" + SpongeCommandUtils.nodesToString(e.getValue()));
            }

        } else {
            Map<String, Boolean> permissions = subjectData.getPermissions(contextSet);
            if (permissions.isEmpty()) {
                Message.BLANK.send(sender, "That subject does not have any permissions defined in those contexts.");
                return CommandResult.SUCCESS;
            }

            Message.BLANK.send(sender, "&aShowing permissions matching contexts &b" +
                        SpongeCommandUtils.contextToString(contextSet, plugin.getLocaleManager()) + "&a.\n" + SpongeCommandUtils.nodesToString(permissions));

        }
        return CommandResult.SUCCESS;
    }
}
