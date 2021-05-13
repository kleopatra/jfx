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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.ListView.EditEvent;

/**
 * Test TableCell editing state updated on re-use (aka after updateIndex(old, new)).
 *
 * This test is parameterized in cellIndex and editingIndex.
 */
@RunWith(Parameterized.class)
public class ModifiedTableCellEditingTest {
    private TableCell<String,String> cell;
    private TableView<String> table;
    private TableColumn<String, String> editingColumn;
    private ObservableList<String> model;

    private int cellIndex;
    private int editingIndex;

//--------------- change off editing index

    @Test
    public void testOffEditingIndex() {
        cell.updateIndex(editingIndex);
        table.edit(editingIndex, editingColumn);
        cell.updateIndex(cellIndex);
        assertEquals("sanity: cell index changed", cellIndex, cell.getIndex());
        assertEquals("sanity: table editingIndex must be unchanged", editingIndex, table.getEditingCell().getRow());
        assertEquals("sanity: table editingColumn must be unchanged", editingColumn, table.getEditingCell().getTableColumn());
        assertFalse("cell must not be editing on update from editingIndex" + editingIndex
                + " to cellIndex " + cellIndex, cell.isEditing());
    }

    @Test
    public void testCancelOffEditingIndex() {
        cell.updateIndex(editingIndex);
        table.edit(editingIndex, editingColumn);
        List<CellEditEvent<String, String>> events = new ArrayList<>();
        editingColumn.setOnEditCancel(e -> {
            events.add(e);
        });
        cell.updateIndex(cellIndex);
        assertEquals("cell must have fired edit cancel", 1, events.size());
        assertEquals("cancel event index must be same as editingIndex", editingIndex,
                events.get(0).getTablePosition().getRow());
        // forgot in integrated version
        assertEquals(editingIndex, table.getEditingCell().getRow());
    }

//--------------- change to editing index

    @Test
    public void testToEditingIndex() {
        cell.updateIndex(cellIndex);
        table.edit(editingIndex, editingColumn);
        cell.updateIndex(editingIndex);
        assertEquals("sanity: cell at editing index", editingIndex, cell.getIndex());
        assertEquals("sanity: table editingIndex must be unchanged", editingIndex, table.getEditingCell().getRow());
        assertEquals("sanity: table editingColumn must be unchanged", editingColumn, table.getEditingCell().getTableColumn());
        assertTrue("cell must be editing on update from " + cellIndex
                + " to editingIndex " + editingIndex, cell.isEditing());
    }

    @Test
    public void testStartEvent() {
        cell.updateIndex(cellIndex);
        table.edit(editingIndex, editingColumn);
        List<CellEditEvent<String, String>> events = new ArrayList<>();
        editingColumn.setOnEditStart(e -> {
            events.add(e);
        });
        cell.updateIndex(editingIndex);
        assertEquals("cell must have fired edit start on update from " + cellIndex + " to " + editingIndex,
                1, events.size());
        assertEquals("start event index must be same as editingIndex", editingIndex,
                events.get(0).getTablePosition().getRow());
    }

//------------- parameterized

    // Note: name property not supported before junit 4.11
    @Parameterized.Parameters // (name = "{index}: cellIndex {0}, editingIndex {1}")
    public static Collection<Object[]> data() {
     // [0] is cellIndex, [1] is editingIndex
        Object[][] data = new Object[][] {
            {1, 2}, // normal
            {0, 1}, // zero cell index
            {1, 0}, // zero editing index
            {-1, 1}, // negative cell - JDK-8265206
        };
        return Arrays.asList(data);
    }

    public ModifiedTableCellEditingTest(int cellIndex, int editingIndex) {
        this.cellIndex = cellIndex;
        this.editingIndex = editingIndex;
    }

//-------------- setup and sanity

    /**
     * Sanity: cell editing state updated when on editing index.
     */
    @Test
    public void testEditOnCellIndex() {
        cell.updateIndex(editingIndex);
        table.edit(editingIndex, editingColumn);
        assertTrue("sanity: cell must be editing", cell.isEditing());
    }

    /**
     * https://bugs.openjdk.java.net/browse/JDK-8165214
     * index of cancel is incorrect
     *
     * also related:
     * https://bugs.openjdk.java.net/browse/JDK-8187226
     *
     * Tree/TableCell specific: https://bugs.openjdk.java.net/browse/JDK-8187229
     * fires NPE on accessing the row
     */
    @Test
    public void testCancelEditOnControl() {
        cell.updateIndex(editingIndex);
        table.edit(editingIndex, editingColumn);
        TablePosition editingCell = table.getEditingCell();
        List<CellEditEvent> events = new ArrayList<CellEditEvent>();
        editingColumn.setOnEditCancel(e -> {
            events.add(e);
        });
        table.edit(-1, null);
        assertEquals(1, events.size());
        CellEditEvent cancelEvent = events.get(0);
        assertEquals("editingCell must be same", editingCell, cancelEvent.getTablePosition());
        assertEquals(editingIndex, cancelEvent.getTablePosition().getRow());
    }


    /**
     * Sanity: cell editing state unchanged when off editing index.
     */
    @Test
    public void testEditOffCellIndex() {
        cell.updateIndex(cellIndex);
        table.edit(editingIndex, editingColumn);
        assertFalse("sanity: cell editing must be unchanged", cell.isEditing());
    }

    @Before
    public void setup() {
        cell = new TableCell<String,String>();
        model = FXCollections.observableArrayList("Four", "Five", "Fear"); // "Flop", "Food", "Fizz"
        table = new TableView<String>(model);
        table.setEditable(true);
        editingColumn = new TableColumn<>("TEST");
        editingColumn.setCellValueFactory(param -> null);
        table.getColumns().add(editingColumn);
        cell.updateTableView(table);
        cell.updateTableColumn(editingColumn);
        // make sure that focus change doesn't interfere with tests
        // (editing cell loosing focus will be canceled from focusListener in Cell)
        // Note: not really needed for Tree/TableCell because the cell is never focused
        // if !cellSelectionEnabled nor if not in Tree/TableRow
        // done here for consistency across analogous tests for List/Tree/Cell
        table.getFocusModel().focus(-1);
        assertFalse("sanity: cellIndex not same as editingIndex", cellIndex == editingIndex);
        assertTrue("sanity: valid editingIndex", editingIndex < model.size());
    }

//-------------- KEEP for now, even though they don't belong here

    /**
     * some properties (like focused) are updated to true only if tablecell is configured with a
     * row. Doing so interferes with testing of editing state, though (because a cell's listener
     * to focusProperty cancels the edit), so don't include here. For other cell types, make
     * sure the focused is -1 in all tests.
     */
//    private void setupWithTableRow() {
//        // need a row for relevant tests
//        tableRow = new TableRow<>();
//        tableRow.updateTableView(table);
//        cell.updateTableRow(tableRow);
////         revert default focus - otherwise we get test failures on changing cell index
////         keep focus at -1 to remove interference
//        table.getSelectionModel().setCellSelectionEnabled(true);
//        table.getFocusModel().focus(-1);
//    }
    /**
     * Sanity: test setup - we need the tableCell updated with a tableRow for
     * complete setup _AND_ update both cell and row indices manually.
     */
//    @Test
//    public void testRowIndex() {
//        tableRow.updateIndex(cellIndex);
//        cell.updateIndex(cellIndex);
//        assertEquals(cellIndex, tableRow.getIndex());
//        assertEquals(cellIndex, cell.getIndex());
//    }
    /**
     * Sanity: complete cleanup on updateIndex to -1
     * belongs into basic tableCell test - only makes sense when
     * cell has its tableRow configure. BUT: if so, the focused
     * property interferes with the editing tests (by calling cancel
     * from listener to focused property)
     */
//    @Test
//    public void testIndexNegativeInvariants() {
//        table.getSelectionModel().select(editingIndex, editingColumn);
//        tableRow.updateIndex(editingIndex);
//        cell.updateIndex(editingIndex);
//        // fails if cell not contained in row
//        assertTrue(cell.isSelected());
//        assertTrue(cell.isFocused());
//        assertFalse(cell.isEditing());
//        tableRow.updateIndex(-1);
//        cell.updateIndex(-1);
//        // nothing updated if empty?
//        assertFalse(cell.isSelected());
//        assertFalse(cell.isFocused());
//        assertFalse(cell.isEditing());
//
//    }


}
