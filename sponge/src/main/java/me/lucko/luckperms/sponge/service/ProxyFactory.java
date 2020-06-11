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

package me.lucko.luckperms.sponge.service;

import me.lucko.luckperms.sponge.service.model.LPPermissionDescription;
import me.lucko.luckperms.sponge.service.model.LPPermissionService;
import me.lucko.luckperms.sponge.service.model.LPSubject;
import me.lucko.luckperms.sponge.service.model.LPSubjectCollection;
import me.lucko.luckperms.sponge.service.model.LPSubjectData;
import me.lucko.luckperms.sponge.service.model.ProxiedSubject;

import net.luckperms.api.model.data.DataType;

import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;

/**
 * Provides proxy instances which implement the SpongeAPI using the LuckPerms model.
 */
public final class ProxyFactory {
    private ProxyFactory() {}

    private static final boolean IS_API_7 = isApi7();
    private static boolean isApi7() {
        try {
            Subject.class.getDeclaredMethod("asSubjectReference");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static PermissionService toSponge(LPPermissionService luckPerms) {
        return IS_API_7 ?
                new me.lucko.luckperms.sponge.service.proxy.api7.PermissionServiceProxy(luckPerms) :
                new me.lucko.luckperms.sponge.service.proxy.api6.PermissionServiceProxy(luckPerms);
    }

    public static SubjectCollection toSponge(LPSubjectCollection luckPerms) {
        return IS_API_7 ?
                new me.lucko.luckperms.sponge.service.proxy.api7.SubjectCollectionProxy(luckPerms) :
                new me.lucko.luckperms.sponge.service.proxy.api6.SubjectCollectionProxy(luckPerms.getService(), luckPerms);
    }

    public static ProxiedSubject toSponge(LPSubject luckPerms) {
        return IS_API_7 ?
                new me.lucko.luckperms.sponge.service.proxy.api7.SubjectProxy(luckPerms.getService(), luckPerms.toReference()) :
                new me.lucko.luckperms.sponge.service.proxy.api6.SubjectProxy(luckPerms.getService(), luckPerms.toReference());
    }

    public static SubjectData toSponge(LPSubjectData luckPerms) {
        LPSubject parentSubject = luckPerms.getParentSubject();
        return IS_API_7 ?
                new me.lucko.luckperms.sponge.service.proxy.api7.SubjectDataProxy(parentSubject.getService(), parentSubject.toReference(), luckPerms.getType() == DataType.NORMAL) :
                new me.lucko.luckperms.sponge.service.proxy.api6.SubjectDataProxy(parentSubject.getService(), parentSubject.toReference(), luckPerms.getType() == DataType.NORMAL);
    }

    public static PermissionDescription toSponge(LPPermissionDescription luckPerms) {
        return IS_API_7 ?
                new me.lucko.luckperms.sponge.service.proxy.api7.PermissionDescriptionProxy(luckPerms.getService(), luckPerms) :
                new me.lucko.luckperms.sponge.service.proxy.api6.PermissionDescriptionProxy(luckPerms.getService(), luckPerms);
    }

    public static LPPermissionDescription registerDescription(LPPermissionService service, PermissionDescription description) {
        return IS_API_7 ?
                me.lucko.luckperms.sponge.service.proxy.api7.DescriptionBuilder.registerDescription(service, description) :
                me.lucko.luckperms.sponge.service.proxy.api6.DescriptionBuilder.registerDescription(service, description);
    }

}
