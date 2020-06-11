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

package net.luckperms.api.query;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

final class SimpleOptionKey<T> implements OptionKey<T> {
    private final String name;
    private final Class<T> type;
    private final int hashCode;

    SimpleOptionKey(String name, Class<T> type) {
        this.name = name.toLowerCase();
        this.type = type;
        this.hashCode = Objects.hash(this.name, this.type); // cache hashcode
    }

    @Override
    public @NonNull String name() {
        return this.name;
    }

    @Override
    public @NonNull Class<T> type() {
        return this.type;
    }

    @Override
    public String toString() {
        return "OptionKey(name=" + this.name + ", type=" + this.type.getName() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleOptionKey<?> that = (SimpleOptionKey<?>) o;
        return this.name.equals(that.name) &&
                this.type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
