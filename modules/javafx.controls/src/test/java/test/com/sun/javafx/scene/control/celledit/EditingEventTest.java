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

package test.com.sun.javafx.scene.control.celledit;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import test.com.sun.javafx.scene.control.infrastructure.StageLoader;

/**
 * Test editing state of cell (and control?) after transitions.
 * TBD: Test notifications on editing transitions - either here or in a separate test?.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
@RunWith(Parameterized.class)
public class EditingEventTest {

    private EditableControl editableControl;
    private StageLoader stageLoader;
    private Supplier<EditableControl> controlSupplier;
    private String typeMessage;

//----------------- test editEvents    
    
//----------------- test state transitions
    
    @Test
    public void testUpdateIndexOffEditing() {
        int cellIndex = -1;
        int editingIndex = 1;
        IndexedCell cell = createEditableCellAt(editingIndex);
        editableControl.edit(editingIndex);
        assertEquals("sanity: cell index unchanged", editingIndex, cell.getIndex());
        assertTrue("sanity: cell must be editing", cell.isEditing());
        cell.updateIndex(cellIndex);
        assertEquals("sanity: cell index changed", cellIndex, cell.getIndex());
        assertFalse("cell must not be editing", cell.isEditing());
        assertEquals("control editing must be unchanged", editingIndex, editableControl.getEditingIndex());
    }
    
    @Test
    public void testUpdateIndexToEditing() {
        int cellIndex = -1;
        int editingIndex = 1;
        IndexedCell cell = createEditableCellAt(cellIndex);
        editableControl.edit(editingIndex);
        cell.updateIndex(editingIndex);
        assertEditingCellInvariant(editableControl, cell, editingIndex);
    }
    
    @Test
    public void testCommitEditOnCell() {
        int editingIndex = 1;
        String edited = "edited";
        IndexedCell cell = createEditableCellAt(editingIndex);
        editableControl.edit(editingIndex);
        cell.commitEdit(edited);
        assertEquals("sanity: cell index unchanged", editingIndex, cell.getIndex());
        assertFalse("sanity: cell must not be editing", cell.isEditing());
        assertEquals("control editingIndex must be reset", -1, editableControl.getEditingIndex());
        assertEquals("edited value must be committed", edited, editableControl.getValueAt(editingIndex));
    }
    
    @Test
    public void testCancelEditOnCell() {
        int editingIndex = 1;
        IndexedCell cell = createEditableCellAt(editingIndex);
        editableControl.edit(editingIndex);
        cell.cancelEdit();
        assertEquals("sanity: cell index unchanged", editingIndex, cell.getIndex());
        assertFalse("sanity: cell must not be editing", cell.isEditing());
        assertEquals("control editingIndex must be reset", -1, editableControl.getEditingIndex());
    }
    
    @Test
    public void testStartEditOnCell() {
        int editingIndex = 1;
        IndexedCell cell = createEditableCellAt(editingIndex);
        cell.startEdit();
        assertEditingCellInvariant(editableControl, cell, editingIndex);
    }
    
    @Test
    public void testCancelEditOnControl() {
        int editingIndex = 1;
        IndexedCell cell = createEditableCellAt(editingIndex);
        editableControl.edit(editingIndex);
        assertEditingCellInvariant(editableControl, cell, editingIndex);
        EditEventReport report = editableControl.createEditReport();
        editableControl.edit(-1);
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        report.assertLastCancelIndex(editingIndex, editableControl.getTargetColumn());
    }
    
    @Test
    public void testToggleEditOnControl() {
        fail("TBD");
        int editingIndex = 1;
        int nextEditingIndex = 2;
        IndexedCell cell = createEditableCellAt(editingIndex);
        IndexedCell nextCell = createEditableCellAt(nextEditingIndex);
        editableControl.edit(editingIndex);
        assertEditingCellInvariant(editableControl, cell, editingIndex);
        editableControl.edit(nextEditingIndex);
        // first cell state: must be not editing
        assertEquals("sanity: cell index unchanged", editingIndex, cell.getIndex());
        assertFalse("cell must not be editing", cell.isEditing());
        // next cell state: must be editing
        assertEquals("sanity: cell index unchanged", nextEditingIndex, nextCell.getIndex());
        assertTrue("cell must not be editing", nextCell.isEditing());
    }

    private void assertEditingCellInvariant(EditableControl eControl, IndexedCell cell, int editingIndex) {
        assertFalse("sanity: editingIndex not negative", -1 == editingIndex);
        assertTrue("sanity: control must be editable", eControl.isEditable());
        // fails for Tree/TableView and TreeView
        assertEquals("sanity: control must be editing at index", editingIndex, eControl.getEditingIndex());
        assertTrue("cell must be editing", cell.isEditing());
        assertEquals("cell index must be same as control editingIndex", eControl.getEditingIndex(), cell.getIndex());
        // FIXME: add api to EditableControl?
//        assertEquals("sanity: cell must be attached to control", eControl.getControl(), cell.getControl());
        assertEquals(eControl.getValueAt(editingIndex), cell.getItem());
    }
 
 //-----------------------
    
    private IndexedCell createEditableCellAt(int editingIndex) {
        IndexedCell cell = editableControl.createEditableCell();
        cell.updateIndex(editingIndex);
        return cell;
    }
    
//----------- parameterized in xxCell
    
    @Parameters(name = "{index} - {1}")
    public static Collection selectionModes() {
        return Arrays.asList(new Object[][] { 
            { (Supplier) EditingEventTest::createEditableListView, "ListView/-Cell"},
            { (Supplier) EditingEventTest::createEditableTableView, "TableView/-Cell"},
            { (Supplier) EditingEventTest::createEditableTreeView, "TreeView/-Cell"},
        });
    }

    public EditingEventTest(Supplier controlSupplier, String typeMessage) {
        this.controlSupplier = controlSupplier;
        this.typeMessage = typeMessage;
    }
//-------------- setup   
    
    @Test
    public void testInitialState() {
        // control
        assertTrue("control must be editable", editableControl.isEditable());
        assertEquals("control must not be editing", -1, editableControl.getEditingIndex());
        assertNotNull("control must have cellFactory", editableControl.getCellFactory());
        // editable cell
        IndexedCell cell = editableControl.createEditableCell();
        assertTrue("cell must be editable", cell.isEditable());
        assertFalse("cell must not be editing", cell.isEditing());
        assertTrue("cell must be empty", cell.isEmpty());
        assertEquals("cell index must be negative", -1, cell.getIndex());
        // FIXME: add api to EditableControl?
//      assertEquals("sanity: cell must be attached to control", eControl.getControl(), cell.getControl());
   }
    
    public static EditableControl<ListView, ListCell> createEditableListView() {
        EditableControl.EListView control = new EditableControl.EListView(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        control.setEditable(true);
        control.setCellFactory(TextFieldListCell.forListView());
        control.getFocusModel().focus(-1);
        return control;
    }
    
    public static EditableControl<TreeView, TreeCell> createEditableTreeView() {
        TreeItem rootItem = new TreeItem<>("root");
        rootItem.getChildren().addAll(
                new TreeItem<>("one"),
                new TreeItem<>("two"),
                new TreeItem<>("three")
                
                );
        EditableControl.ETreeView treeView = new EditableControl.ETreeView(rootItem);
        treeView.setShowRoot(false);
        treeView.setEditable(true);
        treeView.setCellFactory(TextFieldTreeCell.forTreeView());
        treeView.getFocusModel().focus(-1);
        return treeView;
    }
    
    public static EditableControl<TableView, TableCell> createEditableTableView() {
        ObservableList<TableColumn> items = 
//                withExtractor
//                ? FXCollections.observableArrayList(
//                        e -> new Observable[] { e.textProperty() })
//                : 
                    FXCollections.observableArrayList();
        items.addAll(new TableColumn("first"), new TableColumn("second"),
                new TableColumn("third"));
        EditableControl.ETableView table = new EditableControl.ETableView(items);
        table.setEditable(true);
//        table.getSelectionModel().setCellSelectionEnabled(cellSelectionEnabled);

        TableColumn<TableColumn, String> first = new TableColumn<>("Text");
        first.setCellFactory(TextFieldTableCell.forTableColumn());
        first.setCellValueFactory(new PropertyValueFactory<>("text"));

        table.getColumns().addAll(first);
        table.getFocusModel().focus(-1);
        return table;
    }
    
    /**
     * @return
     */
    private EditableControl createEditableControl() {
//        return createEditableListView();
        return createEditableTreeView();
//        return createEditableTableView();
    }
    
    @Before
    public void setup() {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException)throwable;
            } else {
                Thread.currentThread().getThreadGroup().uncaughtException(thread, throwable);
            }
        });
//        editableControl = createEditableControl();
        editableControl = controlSupplier.get();
    }
 

    @After
    public void cleanup() {
        if (stageLoader != null) stageLoader.dispose();
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }


}
