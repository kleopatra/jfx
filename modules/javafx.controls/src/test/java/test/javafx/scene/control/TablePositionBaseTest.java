/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

package test.javafx.scene.control;

import org.junit.Test;

import static org.junit.Assert.*;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;

/**
 * Test specification of Tree/TablePosition.
 */
public class TablePositionBaseTest {

//---------- Table
    
    /**
     * 
     * Doc error (?) instantiate with null throws NPE
     * 
     * No: 
     * - not documented
     * - accidentally introduced when fixing https://bugs.openjdk.java.net/browse/JDK-8093105 (selection related)
     * - api doc warns users to expect null table/column (might be potentially gc'ed, though)
     * - getColumn specified to be -1 if any of table/column is null
     * 
     * slightly unrelated
     * - row/tableColumn might be -1/null to indicate a complete column/row, respectively (spec'ed in 
     *   TableColumnBase
     */
    @Test
    public void testNullTable() {
        new TablePosition<>(null, 2, new TableColumn<>());
    }
    
    @Test
    public void testNullColumn() {
        new TablePosition<>(new TableView<>(), 2, null);
    }
    
    @Test
    public void testUncontainedColumn() {
        new TablePosition<>(new TableView<>(), 2, new TableColumn<>());
    }
    
    @Test
    public void testGetUncontainedColumn() {
        TablePosition<?,?> pos = new TablePosition<>(new TableView<>(), 2, new TableColumn<>());
        int columnIndex = pos.getColumn();
        assertEquals(-1, columnIndex);
        assertEquals(2, pos.getRow());
    }
    
//------------- TreeTable    
    /**
     * Doc error (?) instantiate with null throws NPE
     */
    @Test
    public void testNullTreeTable() {
        new TreeTablePosition<>(null, 2, new TreeTableColumn<>());
    }
}
