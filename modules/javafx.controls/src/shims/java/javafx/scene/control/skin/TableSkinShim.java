/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javafx.scene.control.skin;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Utility methods to access package-private api in Table-related skins.
 */
public class TableSkinShim {

    /**
     * Returns the TableHeaderRow of the skin's table if that is of type TableViewSkinBase
     * or null if not.
     * 
     * @param <T>
     * @param table the table to get the TableHeaderRow from
     * @return the tableHeaderRow of the table's skin or null if the skin not of type
     *    TableViewSkinBase
     */
    public static <T> TableHeaderRow getTableHeaderRow(TableView<T> table) {
        if (table.getSkin() instanceof TableViewSkinBase) {
            return getTableHeaderRow((TableViewSkinBase) table.getSkin());
        }
        return null;
    }
    
    /**
     * Returns the TableHeaderRow of the given skin.
     * 
     * @param <T>
     * @param skin the skin to get the TableHeaderRow from
     * @return
     * @throws NullPointerException if skin is null
     */
    public static <T> TableHeaderRow getTableHeaderRow(TableViewSkinBase skin) {
        return skin.getTableHeaderRow();
    }
    
    /**
     * Returns the TableColumnHeader for the given column or null if not available.
     * 
     * @param <T>
     * @param column
     * @return
     */
    public static <T> TableColumnHeader getColumnHeaderFor(TableColumn<T, ?> column) {
        TableView<T> table = column.getTableView();
        TableHeaderRow tableHeader = getTableHeaderRow(table);
        if (tableHeader != null) {
            return tableHeader.getColumnHeaderFor(column);
        }
        return null;
    }

}
