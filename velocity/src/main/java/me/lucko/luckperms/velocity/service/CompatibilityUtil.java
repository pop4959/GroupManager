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

package me.lucko.luckperms.velocity.service;

import net.luckperms.api.util.Tristate;

import java.util.Objects;

/**
 * Utility class for converting between Velocity and LuckPerms tristate classes
 */
public final class CompatibilityUtil {
    private CompatibilityUtil() {}

    public static com.velocitypowered.api.permission.Tristate convertTristate(Tristate tristate) {
        Objects.requireNonNull(tristate, "tristate");
        switch (tristate) {
            case TRUE:
                return com.velocitypowered.api.permission.Tristate.TRUE;
            case FALSE:
                return com.velocitypowered.api.permission.Tristate.FALSE;
            default:
                return com.velocitypowered.api.permission.Tristate.UNDEFINED;
        }
    }

    public static Tristate convertTristate(com.velocitypowered.api.permission.Tristate tristate) {
        Objects.requireNonNull(tristate, "tristate");
        switch (tristate) {
            case TRUE:
                return Tristate.TRUE;
            case FALSE:
                return Tristate.FALSE;
            default:
                return Tristate.UNDEFINED;
        }
    }

}
