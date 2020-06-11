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

package me.lucko.luckperms.common.bulkupdate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PreparedStatementBuilder {
    private final StringBuilder sb = new StringBuilder();
    private final List<String> variables = new ArrayList<>();

    public PreparedStatementBuilder() {

    }

    public PreparedStatementBuilder append(String s) {
        this.sb.append(s);
        return this;
    }

    public PreparedStatementBuilder variable(String variable) {
        this.variables.add(variable);
        return this;
    }

    public PreparedStatement build(Connection connection, Function<String, String> mapping) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(mapping.apply(this.sb.toString()));
        for (int i = 0; i < this.variables.size(); i++) {
            String var = this.variables.get(i);
            statement.setString(i + 1, var);
        }
        return statement;
    }

    public String toReadableString() {
        String s = this.sb.toString();
        for (String var : this.variables) {
            s = s.replaceFirst("\\?", var);
        }
        return s;
    }
}
