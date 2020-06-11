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

package me.lucko.luckperms.common.commands.user;

import me.lucko.luckperms.common.actionlog.LoggedAction;
import me.lucko.luckperms.common.command.CommandResult;
import me.lucko.luckperms.common.command.abstraction.ChildCommand;
import me.lucko.luckperms.common.command.abstraction.CommandException;
import me.lucko.luckperms.common.command.access.ArgumentPermissions;
import me.lucko.luckperms.common.command.access.CommandPermission;
import me.lucko.luckperms.common.command.tabcomplete.TabCompleter;
import me.lucko.luckperms.common.command.tabcomplete.TabCompletions;
import me.lucko.luckperms.common.command.utils.ArgumentParser;
import me.lucko.luckperms.common.command.utils.MessageUtils;
import me.lucko.luckperms.common.command.utils.StorageAssistant;
import me.lucko.luckperms.common.locale.LocaleManager;
import me.lucko.luckperms.common.locale.command.CommandSpec;
import me.lucko.luckperms.common.locale.message.Message;
import me.lucko.luckperms.common.model.Track;
import me.lucko.luckperms.common.model.User;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;
import me.lucko.luckperms.common.sender.Sender;
import me.lucko.luckperms.common.storage.misc.DataConstraints;
import me.lucko.luckperms.common.util.Predicates;

import net.luckperms.api.context.MutableContextSet;
import net.luckperms.api.track.PromotionResult;

import java.util.List;
import java.util.function.Predicate;

public class UserPromote extends ChildCommand<User> {
    public UserPromote(LocaleManager locale) {
        super(CommandSpec.USER_PROMOTE.localize(locale), "promote", CommandPermission.USER_PROMOTE, Predicates.is(0));
    }

    @Override
    public CommandResult execute(LuckPermsPlugin plugin, Sender sender, User user, List<String> args, String label) throws CommandException {
        if (ArgumentPermissions.checkModifyPerms(plugin, sender, getPermission().get(), user)) {
            Message.COMMAND_NO_PERMISSION.send(sender);
            return CommandResult.NO_PERMISSION;
        }

        boolean addToFirst = !args.remove("--dont-add-to-first");

        final String trackName = args.get(0).toLowerCase();
        if (!DataConstraints.TRACK_NAME_TEST.test(trackName)) {
            Message.TRACK_INVALID_ENTRY.send(sender, trackName);
            return CommandResult.INVALID_ARGS;
        }

        Track track = StorageAssistant.loadTrack(trackName, sender, plugin);
        if (track == null) {
            return CommandResult.LOADING_ERROR;
        }

        if (track.getSize() <= 1) {
            Message.TRACK_EMPTY.send(sender, track.getName());
            return CommandResult.STATE_ERROR;
        }

        boolean dontShowTrackProgress = args.remove("-s");
        MutableContextSet context = ArgumentParser.parseContext(1, args, plugin);

        if (ArgumentPermissions.checkContext(plugin, sender, getPermission().get(), context)) {
            Message.COMMAND_NO_PERMISSION.send(sender);
            return CommandResult.NO_PERMISSION;
        }

        Predicate<String> nextGroupPermissionChecker = s ->
                !ArgumentPermissions.checkArguments(plugin, sender, getPermission().get(), track.getName(), s) &&
                !ArgumentPermissions.checkGroup(plugin, sender, s, context);

        PromotionResult result = track.promote(user, context, nextGroupPermissionChecker, sender, addToFirst);
        switch (result.getStatus()) {
            case MALFORMED_TRACK:
                Message.USER_PROMOTE_ERROR_MALFORMED.send(sender, result.getGroupTo().get());
                return CommandResult.LOADING_ERROR;
            case UNDEFINED_FAILURE:
                Message.COMMAND_NO_PERMISSION.send(sender);
                return CommandResult.NO_PERMISSION;
            case AMBIGUOUS_CALL:
                Message.TRACK_AMBIGUOUS_CALL.send(sender, user.getFormattedDisplayName());
                return CommandResult.FAILURE;
            case END_OF_TRACK:
                Message.USER_PROMOTE_ERROR_ENDOFTRACK.send(sender, track.getName(), user.getFormattedDisplayName());
                return CommandResult.STATE_ERROR;

            case ADDED_TO_FIRST_GROUP: {
                if (!addToFirst && !result.getGroupTo().isPresent()) {
                    Message.USER_PROMOTE_NOT_ON_TRACK.send(sender, track.getName(), user.getFormattedDisplayName());
                    return CommandResult.STATE_ERROR;
                }

                Message.USER_TRACK_ADDED_TO_FIRST.send(sender, user.getFormattedDisplayName(), result.getGroupTo().get(), MessageUtils.contextSetToString(plugin.getLocaleManager(), context));

                LoggedAction.build().source(sender).target(user)
                        .description("promote", track.getName(), context)
                        .build().submit(plugin, sender);

                StorageAssistant.save(user, sender, plugin);
                return CommandResult.SUCCESS;
            }

            case SUCCESS: {
                String groupFrom = result.getGroupFrom().get();
                String groupTo = result.getGroupTo().get();

                Message.USER_PROMOTE_SUCCESS.send(sender, user.getFormattedDisplayName(), track.getName(), groupFrom, groupTo, MessageUtils.contextSetToString(plugin.getLocaleManager(), context));
                if (!dontShowTrackProgress) {
                    Message.BLANK.send(sender, MessageUtils.listToArrowSep(track.getGroups(), groupFrom, groupTo, false));
                }

                LoggedAction.build().source(sender).target(user)
                        .description("promote", track.getName(), context)
                        .build().submit(plugin, sender);

                StorageAssistant.save(user, sender, plugin);
                return CommandResult.SUCCESS;
            }

            default:
                throw new AssertionError("Unknown status: " + result.getStatus());
        }
    }

    @Override
    public List<String> tabComplete(LuckPermsPlugin plugin, Sender sender, List<String> args) {
        return TabCompleter.create()
                .at(0, TabCompletions.tracks(plugin))
                .from(1, TabCompletions.contexts(plugin))
                .complete(args);
    }
}
