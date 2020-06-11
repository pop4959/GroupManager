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

package me.lucko.luckperms.common.commands.log;

import me.lucko.luckperms.common.actionlog.Log;
import me.lucko.luckperms.common.actionlog.LoggedAction;
import me.lucko.luckperms.common.command.CommandResult;
import me.lucko.luckperms.common.command.abstraction.ChildCommand;
import me.lucko.luckperms.common.command.access.CommandPermission;
import me.lucko.luckperms.common.locale.LocaleManager;
import me.lucko.luckperms.common.locale.command.CommandSpec;
import me.lucko.luckperms.common.locale.message.Message;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;
import me.lucko.luckperms.common.sender.Sender;
import me.lucko.luckperms.common.util.DurationFormatter;
import me.lucko.luckperms.common.util.Paginated;
import me.lucko.luckperms.common.util.Predicates;

import java.util.List;

public class LogSearch extends ChildCommand<Log> {
    private static final int ENTRIES_PER_PAGE = 10;

    public LogSearch(LocaleManager locale) {
        super(CommandSpec.LOG_SEARCH.localize(locale), "search", CommandPermission.LOG_SEARCH, Predicates.is(0));
    }

    @Override
    public CommandResult execute(LuckPermsPlugin plugin, Sender sender, Log log, List<String> args, String label) {
        int page = Integer.MIN_VALUE;
        if (args.size() > 1) {
            try {
                page = Integer.parseInt(args.get(args.size() - 1));
                args.remove(args.size() - 1);
            } catch (NumberFormatException e) {
                // ignored
            }
        }

        final String query = String.join(" ", args);
        Paginated<LoggedAction> content = new Paginated<>(log.getSearch(query));

        if (page != Integer.MIN_VALUE) {
            return showLog(page, query, sender, content);
        } else {
            return showLog(content.getMaxPages(ENTRIES_PER_PAGE), query, sender, content);
        }
    }

    private static CommandResult showLog(int page, String query, Sender sender, Paginated<LoggedAction> log) {
        int maxPage = log.getMaxPages(ENTRIES_PER_PAGE);
        if (maxPage == 0) {
            Message.LOG_NO_ENTRIES.send(sender);
            return CommandResult.STATE_ERROR;
        }

        if (page == Integer.MIN_VALUE) {
            page = maxPage;
        }

        if (page < 1 || page > maxPage) {
            Message.LOG_INVALID_PAGE_RANGE.send(sender, maxPage);
            return CommandResult.INVALID_ARGS;
        }

        List<Paginated.Entry<LoggedAction>> entries = log.getPage(page, ENTRIES_PER_PAGE);
        Message.LOG_SEARCH_HEADER.send(sender, query, page, maxPage);

        for (Paginated.Entry<LoggedAction> e : entries) {
            Message.LOG_ENTRY.send(sender,
                    e.position(),
                    DurationFormatter.CONCISE_LOW_ACCURACY.format(e.value().getDurationSince()),
                    e.value().getSourceFriendlyString(),
                    Character.toString(LoggedAction.getTypeCharacter(e.value().getTarget().getType())),
                    e.value().getTargetFriendlyString(),
                    e.value().getDescription()
            );
        }

        return CommandResult.SUCCESS;
    }
}
