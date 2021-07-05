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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static javafx.scene.control.TableColumn.*;
import static org.junit.Assert.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;

/**
 * Test cell edit event.
 * 
 * There is no documented precondition on its constructor, so either must be implemented
 * to handle nulls gracefully or specified to not allow null
 */
public class CellEditEventTest {

    private TableView<String> table;
    private TableColumn<String, String> editingColumn;
    private ObservableList<String> model;
    
    /**
     * NPE with not-null table on accessing table.
     */
    @Test
    public void testNullTablePosition() {
        CellEditEvent<String, String> ev = new CellEditEvent<>(table, null, editAnyEvent(), null);
        ev.getTableView();
    }
    
    /**
     * Strengthened precondition: event must handle null source.
     */
    @Test
    public void testNullTable() {
        CellEditEvent<?, ?> ev = new CellEditEvent<Object, Object>(null, // null table
                new TablePosition<>(null, -1, null), editAnyEvent(), null);
    }
    
    @Test
    public void testConstructor() {
        int editingRow = 0;
        String value = "someValue";
        TablePosition<String, String> pos = new TablePosition<>(table, editingRow, editingColumn);
        CellEditEvent<String, String> ev = new CellEditEvent<>(table, pos, editAnyEvent(), value);
        assertCellEditEvent(ev, pos, value, editingRow);
    }
    
    private void assertCellEditEvent(CellEditEvent<?, ?> event, TablePosition<?, ?> pos, 
            Object value, int editingRow) {
        Object rowValue = editingRow < 0 ? null : model.get(editingRow);
        Object oldValue = editingColumn.getCellObservableValue(editingRow);
        assertCellEditEvent(event, table, editingColumn, pos, value, oldValue, rowValue);
    }
    
    /**
     * Assert state of edit event.
     */
    private void assertCellEditEvent(CellEditEvent<?, ?> event, 
            TableView<?> table, TableColumn<?, ?> tableColumn, TablePosition<?, ?> pos, 
            Object newValue, Object oldValue, Object rowValue) {
        assertEquals(table, event.getTableView());
        assertEquals(tableColumn, event.getTableColumn());
        assertEquals(pos, event.getTablePosition());
        assertEquals(newValue, event.getNewValue());
        assertEquals(oldValue, event.getOldValue());
        assertEquals(rowValue, event.getRowValue());
    }
    
    
//------------
    
    @Before public void setup() {
//        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
//            if (throwable instanceof RuntimeException) {
//                throw (RuntimeException)throwable;
//            } else {
//                Thread.currentThread().getThreadGroup().uncaughtException(thread, throwable);
//            }
//        });

        model = FXCollections.observableArrayList("Four", "Five", "Fear"); // "Flop", "Food", "Fizz"
        table = new TableView<String>(model);
        editingColumn = new TableColumn<>("TEST");

    }

    @After
    public void cleanup() {
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }

    
}
