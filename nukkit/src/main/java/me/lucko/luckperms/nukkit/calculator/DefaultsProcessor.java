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

package me.lucko.luckperms.nukkit.calculator;

import me.lucko.luckperms.common.calculator.processor.PermissionProcessor;
import me.lucko.luckperms.common.calculator.result.TristateResult;
import me.lucko.luckperms.nukkit.LPNukkitPlugin;
import me.lucko.luckperms.nukkit.inject.PermissionDefault;

import net.luckperms.api.util.Tristate;

/**
 * Permission Processor for Nukkits "default" permission system.
 */
public class DefaultsProcessor implements PermissionProcessor {
    private static final TristateResult.Factory DEFAULT_PERMISSION_MAP_RESULT_FACTORY = new TristateResult.Factory(DefaultsProcessor.class, "default permission map");
    private static final TristateResult.Factory PERMISSION_MAP_RESULT_FACTORY = new TristateResult.Factory(DefaultsProcessor.class, "permission map");

    private final LPNukkitPlugin plugin;
    private final boolean isOp;

    public DefaultsProcessor(LPNukkitPlugin plugin, boolean isOp) {
        this.plugin = plugin;
        this.isOp = isOp;
    }

    @Override
    public TristateResult hasPermission(String permission) {
        Tristate t = this.plugin.getDefaultPermissionMap().lookupDefaultPermission(permission, this.isOp);
        if (t != Tristate.UNDEFINED) {
            return DEFAULT_PERMISSION_MAP_RESULT_FACTORY.result(t);
        }

        PermissionDefault def = PermissionDefault.fromPermission(this.plugin.getPermissionMap().get(permission));
        if (def == null) {
            return TristateResult.UNDEFINED;
        }
        return PERMISSION_MAP_RESULT_FACTORY.result(Tristate.of(def.getValue(this.isOp)));
    }
}
