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
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.javafx.tk.Toolkit;

import static org.junit.Assert.*;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CellShim;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableCellShim;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import test.com.sun.javafx.scene.control.infrastructure.StageLoader;

/**
 * Additional tests for TableCellTest.
 * 
 * Note: the setup should always be the exact same as parent TableCellTest.
 * To keep manageable, remove those that are included in fixes.
 */
public class TableCellTestExt {

    private TableCell<String,String> cell;
    private TableView<String> table;
    private TableColumn<String, String> editingColumn;
    private TableRow<String> tableRow;
    private ObservableList<String> model;
    private StageLoader stageLoader;

    /**
     * Why:
     * start checks for column != null, the other edit methods check for table != null
     * 
     * no table, but column and row
     * tests in cellTest are incomplete: they explicitly test editing for null control 
     * but always without column ..
     * doing so, throws a NPE from CellEditEvent
     * 
     * also playing with handlers: which are notified for combinations of missing 
     * collaborators? incomplete ..
     */
    @Test
    public void testEditStartFireEvent() {
        CellShim.updateItem(tableRow, "TableRow", false);
        cell.updateTableRow(tableRow);
        TableCellShim.set_lockItemOnEdit(cell, true);
        CellShim.updateItem(cell, "something", false);
        // FIXME: default cell (of tableColumn) needs not-null value for firing cancel
//        editingColumn.setCellValueFactory(cc -> new SimpleObjectProperty<>(""));
        cell.updateTableColumn(editingColumn);
        List<CellEditEvent<?, ?>> events = new ArrayList<>();
//        cell.addEventHandler(TableColumn.editAnyEvent(), events::add);
        editingColumn.addEventHandler(TableColumn.editAnyEvent(), events::add);
        cell.startEdit();
        assertTrue(cell.isEditing());
        assertEquals(1, events.size());
    }
    
    @Test
    public void testEditStartFireEventComplete() {
        setupForEditing();
//        CellShim.updateItem(tableRow, "TableRow", false);
        cell.updateTableRow(tableRow);
        cell.updateIndex(0);
//        TableCellShim.set_lockItemOnEdit(cell, true);
//        CellShim.updateItem(cell, "something", false);
        List<CellEditEvent<?, ?>> events = new ArrayList<>();
        cell.addEventHandler(TableColumn.editAnyEvent(), events::add);
        editingColumn.addEventHandler(TableColumn.editAnyEvent(), events::add);
        table.addEventHandler(TableColumn.editAnyEvent(), events::add);
        
        cell.startEdit();
        assertTrue(cell.isEditing());
        assertEquals(1, events.size());
    }
    
    @Test
    public void testEditStartFireEventCompleteInScene() {
        setupForEditing();
        List<CellEditEvent<?, ?>> events = new ArrayList<>();
//        cell.addEventHandler(TableColumn.editAnyEvent(), events::add);
        editingColumn.addEventHandler(TableColumn.editAnyEvent(), events::add);
        table.addEventHandler(TableColumn.editAnyEvent(), events::add);
        stageLoader = new StageLoader(table);
        int editingIndex = 1;
        table.edit(editingIndex, editingColumn);
//        assertTrue(cell.isEditing());
        assertEquals(1, events.size());
    }
    
    @Test
    public void testEditStartNoColumn() {
        table.setEditable(true);
        cell.updateTableView(table);
        // force artificially not-empty cell state
        TableCellShim.set_lockItemOnEdit(cell, true);
        CellShim.updateItem(cell, "something", false);
        
        cell.startEdit();
        assertTrue(cell.isEditing());
    }
    
    @Test
    public void testEditCancelNoColumn() {
        table.setEditable(true);
        cell.updateTableView(table);
        // force artificially not-empty cell state
        TableCellShim.set_lockItemOnEdit(cell, true);
        CellShim.updateItem(cell, "something", false);
        // start edit: succeeds without firing event (null check against column)
        cell.startEdit();
        assertTrue(cell.isEditing());
        // cancel edit: NPE from Event.fire (all params must be != null)
        cell.cancelEdit();
        assertFalse(cell.isEditing());
    }
    
    
  //------------ testing editCancel event: location on event - JDK-8187229
    
    /**
     * Basic config of table-/cell to allow testing of editEvents: 
     * table has editingColumn and cell is configured with table and column.
     */
    private void setupForEditing() {
        table.setEditable(true);
        table.getColumns().add(editingColumn);
        // FIXME: default cell (of tableColumn) needs not-null value for firing cancel
        editingColumn.setCellValueFactory(cc -> new SimpleObjectProperty<>(""));

        cell.updateTableView(table);
        cell.updateTableColumn(editingColumn);
    }
    
    /**
     * Fails before fix.
     * 
     * FIXME: remove sanity tests in-between - we rely on the editing mechanism itself
     * working as expected, anyway.
     */
    @Test
    public void testEditCancelEventAfterModifyItemsWithSanityAsserts() {
        setupForEditing();
        stageLoader = new StageLoader(table);
        int editingIndex = 1;
        List<CellEditEvent<?, ?>> startEvents = new ArrayList<>();
        editingColumn.setOnEditStart(startEvents::add);
        table.edit(editingIndex, editingColumn);
        Toolkit.getToolkit().firePulse();
        TablePosition<?, ?> editingPosition = table.getEditingCell();
        // table
        assertNotNull("sanity: table is editing", editingPosition);
        assertEquals("sanity: editing row", editingIndex, editingPosition.getRow());
        assertEquals("sanity: editing column", editingColumn, editingPosition.getTableColumn());
        // startEvent
        assertEquals("sanity: editingStarted", 1, startEvents.size());
        assertEquals("sanity: position in editStart", editingPosition, startEvents.get(0).getTablePosition());
        
        List<CellEditEvent<?, ?>> events = new ArrayList<>();
        editingColumn.setOnEditCancel(events::add);
        table.getItems().add(0, "added");
        Toolkit.getToolkit().firePulse();
        
        assertNull("sanity: editing terminated on items modification", table.getEditingCell());
        assertEquals("column must have received editCancel", 1, events.size());
        assertEquals(editingPosition, events.get(0).getTablePosition());
    }

    /**
     * Test that removing the editing item implicitly cancels an ongoing
     * edit and fires a correct cancel event.
     * 
     * Fails before fix.
     * 
     * FIXME: remove sanity tests in-between - we rely on the editing mechanism itself
     * working as expected, anyway.
     */
    @Test
    public void testEditCancelEventAfterRemoveEditingItemWithSanityAsserts() {
        setupForEditing();
        stageLoader = new StageLoader(table);
        int editingIndex = 1;
        List<CellEditEvent<?, ?>> startEvents = new ArrayList<>();
        editingColumn.setOnEditStart(startEvents::add);
        table.edit(editingIndex, editingColumn);
        Toolkit.getToolkit().firePulse();
        TablePosition<?, ?> editingPosition = table.getEditingCell();
        // table
        assertNotNull("sanity: table is editing", editingPosition);
        assertEquals("sanity: editing row", editingIndex, editingPosition.getRow());
        assertEquals("sanity: editing column", editingColumn, editingPosition.getTableColumn());
        // startEvent
        assertEquals("sanity: editingStarted", 1, startEvents.size());
        assertEquals("sanity: position in editStart", editingPosition, startEvents.get(0).getTablePosition());
        
        List<CellEditEvent<?, ?>> events = new ArrayList<>();
        editingColumn.setOnEditCancel(events::add);
        table.getItems().remove(editingIndex);
        Toolkit.getToolkit().firePulse();
        
        assertNull("sanity: editing terminated on items modification", table.getEditingCell());
        assertEquals("column must have received editCancel", 1, events.size());
        assertEquals(editingPosition, events.get(0).getTablePosition());
    }
    
    /**
     * Note: ideally we would test a macroscopic effect of missing cleanup of 
     * the (cell local) storage at the editing cell (f.i. a memory leak as in
     * TreeCellTest) - there is none (good!) nevertheless it should be cleaned
     * for sanity. So testing with brute force lookup of impl detail ..
     */
//    @Test
//    public void testEditCancelEditingCellAtStartCleanupAfterCancel() {
//        setupForEditing();
//        int editingIndex = 1;
//        cell.updateIndex(editingIndex);
//        table.edit(editingIndex, editingColumn);
//        assertEquals("sanity: cell has editing cell ref", table.getEditingCell(), getEditingCellAtStart(cell));
//        cell.cancelEdit();
//        assertNull(getEditingCellAtStart(cell));
//    }
//    
//    @Test
//    public void testEditCancelEditingCellAtStartCleanupAfterCommit() {
//        setupForEditing();
//        int editingIndex = 1;
//        cell.updateIndex(editingIndex);
//        table.edit(editingIndex, editingColumn);
//        assertEquals("sanity: cell has editing cell ref", table.getEditingCell(), getEditingCellAtStart(cell));
//        assertEquals(table.getEditingCell(), getEditingCellAtStart(cell));
//        cell.commitEdit("edited");
//        assertNull(getEditingCellAtStart(cell));
//    }
    
    /**
     * Doesn't make sense: there's no reference to the cell except here.
     */
//    @Test
//    public void testEditCancelMemoryLeakCell() {
//        setupForEditing();
//        int editingIndex = 1;
//        cell.updateIndex(editingIndex);
//        table.edit(editingIndex, editingColumn);
//        assertEquals("sanity: cell has editing cell ref", table.getEditingCell(), getEditingCellAtStart(cell));
//        assertEquals(table.getEditingCell(), getEditingCellAtStart(cell));
//        cell.cancelEdit();
//        WeakReference<TableCell> cellRef = new WeakReference<>(cell);
//        cell = null;
//        attemptGC(cellRef);
//        assertEquals("cell must be gc'ed", null, cellRef.get());
//        
//    }


//-------------- setup, initial
    
    @Before public void setup() {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException)throwable;
            } else {
                Thread.currentThread().getThreadGroup().uncaughtException(thread, throwable);
            }
        });

        cell = new TableCell<String,String>();
        model = FXCollections.observableArrayList("Four", "Five", "Fear"); // "Flop", "Food", "Fizz"
        table = new TableView<String>(model);
        editingColumn = new TableColumn<>("TEST");

        tableRow = new TableRow<>();
    }

    @After
    public void cleanup() {
        if (stageLoader != null) stageLoader.dispose();
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }



}
