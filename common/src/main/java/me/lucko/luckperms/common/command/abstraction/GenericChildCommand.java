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

package me.lucko.luckperms.common.command.abstraction;

import me.lucko.luckperms.common.command.CommandResult;
import me.lucko.luckperms.common.command.access.CommandPermission;
import me.lucko.luckperms.common.locale.command.Argument;
import me.lucko.luckperms.common.locale.command.LocalizedCommandSpec;
import me.lucko.luckperms.common.locale.message.Message;
import me.lucko.luckperms.common.model.HolderType;
import me.lucko.luckperms.common.model.PermissionHolder;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;
import me.lucko.luckperms.common.sender.Sender;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * A sub command which can be be applied to both groups and users.
 * This doesn't extend the other Command or SubCommand classes to avoid generics hell.
 */
public abstract class GenericChildCommand {

    private final LocalizedCommandSpec spec;

    /**
     * The name of the sub command
     */
    private final String name;

    /**
     * The permission needed to use this command
     */
    private final CommandPermission userPermission;
    private final CommandPermission groupPermission;

    /**
     * Predicate to test if the argument length given is invalid
     */
    private final Predicate<? super Integer> argumentCheck;

    public GenericChildCommand(LocalizedCommandSpec spec, String name, CommandPermission userPermission, CommandPermission groupPermission, Predicate<? super Integer> argumentCheck) {
        this.spec = spec;
        this.name = name;
        this.userPermission = userPermission;
        this.groupPermission = groupPermission;
        this.argumentCheck = argumentCheck;
    }

    public abstract CommandResult execute(LuckPermsPlugin plugin, Sender sender, PermissionHolder holder, List<String> args, String label, CommandPermission permission) throws CommandException;

    public List<String> tabComplete(LuckPermsPlugin plugin, Sender sender, List<String> args) {
        return Collections.emptyList();
    }

    public LocalizedCommandSpec getSpec() {
        return this.spec;
    }

    public String getName() {
        return this.name;
    }

    public CommandPermission getUserPermission() {
        return this.userPermission;
    }

    public CommandPermission getGroupPermission() {
        return this.groupPermission;
    }

    public Predicate<? super Integer> getArgumentCheck() {
        return this.argumentCheck;
    }

    public void sendUsage(Sender sender) {
        StringBuilder sb = new StringBuilder();
        if (getArgs() != null) {
            sb.append(Message.COMMAND_USAGE_ARGUMENT_JOIN.asString(sender.getPlugin().getLocaleManager()));
            for (Argument arg : getArgs()) {
                sb.append(arg.asPrettyString(sender.getPlugin().getLocaleManager())).append(" ");
            }
        }

        Message.COMMAND_USAGE_BRIEF.send(sender, getName(), sb.toString());
    }

    public void sendDetailedUsage(Sender sender) {
        Message.COMMAND_USAGE_DETAILED_HEADER.send(sender, getName(), getDescription());

        if (getArgs() != null) {
            Message.COMMAND_USAGE_DETAILED_ARGS_HEADER.send(sender);
            for (Argument arg : getArgs()) {
                Message.COMMAND_USAGE_DETAILED_ARG.send(sender, arg.asPrettyString(sender.getPlugin().getLocaleManager()), arg.getDescription());
            }
        }
    }

    public boolean isAuthorized(Sender sender, HolderType type) {
        switch (type) {
            case USER:
                return this.userPermission.isAuthorized(sender);
            case GROUP:
                return this.groupPermission.isAuthorized(sender);
            default:
                throw new AssertionError(type);
        }
    }

    public String getDescription() {
        return this.spec.description();
    }

    public List<Argument> getArgs() {
        return this.spec.args();
    }

}
