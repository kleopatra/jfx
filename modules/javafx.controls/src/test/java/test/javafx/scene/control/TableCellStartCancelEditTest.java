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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.sun.javafx.tk.Toolkit;

import static org.junit.Assert.*;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import test.com.sun.javafx.scene.control.infrastructure.StageLoader;

/**
 * Tests around missing cancel for null values.
 * 
 * Only happens for default tableCell impl on TableColumn: it's not calling super.updateItem
 * for same item. Effect not understood and definitely wrong, but practically not important - the
 * default cell cannot be used for editing anyway (or can it?) 
 * 
 * Decision: post-pone - but report!
 */
@RunWith(Parameterized.class)
public class TableCellStartCancelEditTest {

//    private TableCell<MenuItem,String> cell;
//    private TableRow<MenuItem> row;
    private TableView<MenuItem> table;
    private TableColumn<MenuItem, String> editingColumn;
    private ObservableList<MenuItem> model;
    
    private Callback cellFactory;
    
    private StageLoader stageLoader;

    /**
     * editingColumn must have a cellValueFactory returning a not-null
     * observable with a not-null value to trigger editCancel.
     * FIXME: WHY? 
     * 
     * seems to be default implementation only (on TableColumn) - others fail
     * with incorrect edit location
     * culprit seems to be override of updateItem: backs out if given item == getItem (which
     * is the case with item == null)
     * 
     */
    @Test
    public void testEditCancelSetupCellValueFactoryNull() {
        setupForEditing();
        editingColumn.setCellValueFactory(null);
        assertStartCancelEditEvents(1);
    }
    
    @Test
    public void testEditCancelSetupCellValueFactoryNullObservable() {
        setupForEditing();
        editingColumn.setCellValueFactory(cc -> null);
        assertStartCancelEditEvents(1);
    }
    
    @Test
    public void testEditCancelSetupCellValueFactoryNullObservableValue() {
        setupForEditing();
        editingColumn.setCellValueFactory(cc -> new SimpleObjectProperty<>());
        assertStartCancelEditEvents(1);
    }
    
    @Test
    public void testEditCancelSetupCellValueFactoryNotNullObservableValue() {
        setupForEditing();
        editingColumn.setCellValueFactory(cc -> new SimpleObjectProperty<>(""));
        assertStartCancelEditEvents(1);
    }
    
    @Test
    public void testEditCancelSetupCellValueFactory() {
        setupForEditing();
        editingColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
        assertStartCancelEditEvents(1);
    }
    
    @Test
    public void testEditCancelSetupCellValueFactoryNullContent() {
        setupForEditing();
        editingColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
        int editingIndex = 1;
        model.get(editingIndex).setText(null);
        assertStartCancelEditEvents(editingIndex);
    }
    
    private void assertStartCancelEditEvents(int editingIndex) {
        stageLoader = new StageLoader(table);
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
        table.getItems().add(0, new MenuItem("added"));
        Toolkit.getToolkit().firePulse();

        assertNull("sanity: editing terminated on items modification", table.getEditingCell());
        assertEquals("column must have received editCancel", 1, events.size());
        assertEquals(editingPosition, events.get(0).getTablePosition());
    }
    
// ---- parameterized
    
    @Parameterized.Parameters (name = "{index}: celltype {1}")
    public static Collection<Object[]> data() {
     // [0] is cellIndex, [1] is editingIndex
        Object[][] data = new Object[][] {
            {null, "default"}, // normal
            {(Callback) cc -> new TableCell(), "TableCell"}, 
            {(Callback) cc -> new TextFieldTableCell(), "TextFieldTableCell"}, 
            
        };
        return Arrays.asList(data);
    }


    public TableCellStartCancelEditTest(Callback factory, String message) {
        this.cellFactory = factory;
    }
//------------------  init, setup  
    
    /**
     * Basic config of table-/cell to allow testing of editEvents: 
     * table has editingColumn and cell is configured with table and column.
     */
    private void setupForEditing() {
        table.setEditable(true);
        table.getColumns().add(editingColumn);
//        cell.updateTableView(table);
//        cell.updateTableColumn(editingColumn);
    }
    

    @Before public void setup() {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException)throwable;
            } else {
                Thread.currentThread().getThreadGroup().uncaughtException(thread, throwable);
            }
        });

//        cell = new TableCell<MenuItem,String>();
//        row = new TableRow<>();
        model = FXCollections.observableArrayList(new MenuItem("Four"), new MenuItem("Five"), new MenuItem("Fear")); // "Flop", "Food", "Fizz"
        table = new TableView<MenuItem>(model);
        editingColumn = new TableColumn<>("TEST");
        if (cellFactory != null) {
            editingColumn.setCellFactory(cellFactory);
        }

    }

    @After
    public void cleanup() {
        if (stageLoader != null) stageLoader.dispose();
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }



}
