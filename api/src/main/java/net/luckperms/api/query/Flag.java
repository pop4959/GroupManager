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

/**
 * The flags which can be set for a query.
 *
 * <p>By default (in places like new instances of {@link QueryOptions.Builder} and
 * {@link QueryOptions#defaultContextualOptions()}), all {@link Flag}s are set to true.</p>
 */
public enum Flag {

    /**
     * If parent groups should be resolved
     */
    RESOLVE_INHERITANCE,

    /**
     * If global or non-server-specific nodes should be applied
     */
    INCLUDE_NODES_WITHOUT_SERVER_CONTEXT,

    /**
     * If global or non-world-specific nodes should be applied
     */
    INCLUDE_NODES_WITHOUT_WORLD_CONTEXT,

    /**
     * If global or non-server-specific group memberships should be applied
     */
    APPLY_INHERITANCE_NODES_WITHOUT_SERVER_CONTEXT,

    /**
     * If global or non-world-specific group memberships should be applied
     */
    APPLY_INHERITANCE_NODES_WITHOUT_WORLD_CONTEXT

}
