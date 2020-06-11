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

package me.lucko.luckperms.sponge.calculator;

import me.lucko.luckperms.common.calculator.processor.PermissionProcessor;
import me.lucko.luckperms.common.calculator.result.TristateResult;
import me.lucko.luckperms.sponge.service.model.LPPermissionService;
import me.lucko.luckperms.sponge.service.model.LPSubject;

import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;

public abstract class DefaultsProcessor implements PermissionProcessor {
    private static final TristateResult.Factory TYPE_DEFAULTS_RESULT_FACTORY = new TristateResult.Factory(DefaultsProcessor.class, "type defaults");
    private static final TristateResult.Factory ROOT_DEFAULTS_RESULT_FACTORY = new TristateResult.Factory(DefaultsProcessor.class, "root defaults");

    protected final LPPermissionService service;
    private final QueryOptions queryOptions;

    public DefaultsProcessor(LPPermissionService service, QueryOptions queryOptions) {
        this.service = service;
        this.queryOptions = queryOptions;
    }

    protected abstract LPSubject getTypeDefaults();

    @Override
    public TristateResult hasPermission(String permission) {
        Tristate t = getTypeDefaults().getPermissionValue(this.queryOptions, permission);
        if (t != Tristate.UNDEFINED) {
            return TYPE_DEFAULTS_RESULT_FACTORY.result(t);
        }

        t = this.service.getRootDefaults().getPermissionValue(this.queryOptions, permission);
        if (t != Tristate.UNDEFINED) {
            return ROOT_DEFAULTS_RESULT_FACTORY.result(t);
        }

        return TristateResult.UNDEFINED;
    }
}
