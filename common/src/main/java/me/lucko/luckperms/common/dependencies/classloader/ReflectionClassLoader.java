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

package me.lucko.luckperms.common.dependencies.classloader;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import me.lucko.luckperms.common.plugin.bootstrap.LuckPermsBootstrap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class ReflectionClassLoader implements PluginClassLoader {
    private final URLClassLoader classLoader;

    @SuppressWarnings("Guava") // we can't use java.util.Function because old Guava versions are used at runtime
    private final Supplier<Method> addUrlMethod;

    public ReflectionClassLoader(LuckPermsBootstrap bootstrap) throws IllegalStateException {
        ClassLoader classLoader = bootstrap.getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            this.classLoader = (URLClassLoader) classLoader;
        } else {
            throw new IllegalStateException("ClassLoader is not instance of URLClassLoader");
        }

        this.addUrlMethod = Suppliers.memoize(() -> {
            if (isJava9OrNewer()) {
                bootstrap.getPluginLogger().info("It is safe to ignore any warning printed following this message " +
                        "starting with 'WARNING: An illegal reflective access operation has occurred, Illegal reflective " +
                        "access by " + getClass().getName() + "'. This is intended, and will not have any impact on the " +
                        "operation of LuckPerms.");
            }

            try {
                Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addUrlMethod.setAccessible(true);
                return addUrlMethod;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void addJarToClasspath(Path file) {
        try {
            this.addUrlMethod.get().invoke(this.classLoader, file.toUri().toURL());
        } catch (IllegalAccessException | InvocationTargetException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static boolean isJava9OrNewer() {
        try {
            // method was added in the Java 9 release
            Runtime.class.getMethod("version");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
