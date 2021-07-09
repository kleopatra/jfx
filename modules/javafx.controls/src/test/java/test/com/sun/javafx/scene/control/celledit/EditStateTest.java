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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.*;

import javafx.scene.control.CellShim;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableCellShim;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableCellShim;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.Callback;
import test.com.sun.javafx.scene.control.celledit.infrastructure.EditableControl;
import test.com.sun.javafx.scene.control.celledit.infrastructure.EditableControlFactory;
import test.com.sun.javafx.scene.control.infrastructure.StageLoader;

/**
 * Test editing state of cell (and control?) after transitions.
 * TBD: Test notifications on editing transitions - either here or in a separate test?.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
@RunWith(Parameterized.class)
public class EditStateTest {

    private EditableControl editableControl;
    private StageLoader stageLoader;
    private Supplier<EditableControl> controlSupplier;
    private String typeMessage;
    private Callback cellFactory;

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

    /**
     * Sanity: fix doesn't interfere with RT-31165
     */
    @Test
    public void testUpdateSameIndexWhileNotEditing() {
        int cellIndex = 2;
        int editingIndex = 1;
        IndexedCell cell = createEditableCellAt(cellIndex);
        editableControl.edit(editingIndex);
        cell.updateIndex(cellIndex);
        assertEquals("sanity: cell index changed", cellIndex, cell.getIndex());
        assertFalse("cell must not be editing", cell.isEditing());
        assertEquals("control editing must be unchanged", editingIndex, editableControl.getEditingIndex());
    }

    /**
     * Sanity: fix doesn't interfere with RT-31165
     */
    @Test
    public void testUpdateSameIndexWhileEditing() {
        int editingIndex = 1;
        IndexedCell cell = createEditableCellAt(editingIndex);
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
        editableControl.edit(-1);
        assertEquals("sanity: cell index unchanged", editingIndex, cell.getIndex());
        assertFalse("cell must not be editing", cell.isEditing());
    }

    @Test
    public void testToggleEditOnControl() {
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
        assertEquals("sanity: cell must be attached to control", eControl.getControl(), eControl.getCellControl(cell));
        assertEquals(eControl.getValueAt(editingIndex), cell.getItem());
    }

 //------------- test editing state when changing related properties
    
    @Ignore("model change")
    @Test
    public void testRemoveItemBeforeUpdatesEditingIndex() {
        int editingIndex = 2;
        Object item = editableControl.getValueAt(editingIndex);
        editableControl.edit(editingIndex);
        editableControl.removeItemAt(editingIndex -1);
        assertEquals(editingIndex - 1, editableControl.getEditingIndex());
    }
    
    @Ignore("model change")
    @Test
    public void testRemoveItemBeforeKeepsEditingItem() {
        int editingIndex = 2;
        Object item = editableControl.getValueAt(editingIndex);
        editableControl.edit(editingIndex);
        editableControl.removeItemAt(editingIndex -1);
        assertEquals(item, editableControl.getValueAt(editingIndex));
    }
    
    /**
     * FIXME: cell must cancel its editing when cell editable is false while editing
     * here: in scenegraph
     */
    @Ignore("editable change")
    @Test
    public void testCellEditingOnSetEditableFalseInScene() {
        new StageLoader(editableControl.getControl());
        int editingIndex = 1;
        IndexedCell cell = createEditableCellAt(editingIndex);
        editableControl.edit(editingIndex);
        cell.setEditable(false);
        assertFalse("cell must not be editing when editable is set to false while editing", cell.isEditing());
    }
    
    /**
     * FIXME: cell must cancel its editing when cell editable is false while editing
     */
    @Ignore("editable change")
    @Test
    public void testCellEditingOnSetEditableFalse() {
        int editingIndex = 1;
        IndexedCell cell = createEditableCellAt(editingIndex);
        editableControl.edit(editingIndex);
        cell.setEditable(false);
        assertFalse("cell must not be editing when editable is set to false while editing", cell.isEditing());
    }
    
    /**
     * FIXME: control must cancel its editing when control editable is false while editing
     * here: in scenegraph
     */
    @Ignore("editable change")
    @Test
    public void testControlEditingOnSetEditableFalseInScene() {
        new StageLoader(editableControl.getControl());
        editableControl.edit(1);
        editableControl.setEditable(false);
        assertEquals("control must not be editing when editable is set to false while editing", -1, editableControl.getEditingIndex());
    }
    
    /**
     * FIXME: control must cancel its editing when editable is false while editing
     */
    @Ignore("editable change")
    @Test
    public void testControlEditingOnSetEditableFalse() {
        editableControl.edit(1);
        editableControl.setEditable(false);
        assertEquals("control must not be editing when editable is set to false while editing", -1, editableControl.getEditingIndex());
    }
    
 //------------ test state without tree
    
    
    /**
     * JDK-8188026: all cell implementations in package cell violate their contract by
     * strengthening their precondition
     * 
     * Note: for base xxCells, there are tests (if there _are_ tests about editing ;)
     * for the contrary, that is explicitly allow null control, f.i. 
     * ListCellTest.editCellWithNullListViewResultsInNoExceptions
     * 
     * Note: going even further are tests in CellTest - f.i. updatingACellBeingEditedDoesNotResultInACancelOfEdit
     * test that startEdit switches a cell (all base cells) into editing as long as it is not empty
     */
//    @Ignore("JDK-8188026")
    @Test
    public void testStartEditNullControlAllowed() {
        IndexedCell cell = editableControl.createCell();
        cell.startEdit();
        // Note: not starting edit is a side-effect of being empty
        assertFalse("sanity", cell.isEditing());
    }
    
    /**
     * This is cellTest.updatingACellBeingEditedDoesNotResultInACancelOfEdit
     * 
     * FIXME: cells for tabular controls need to be configured with a row to
     * be not empty? At least that's what CellTest does in setup (no table, no column)
     * 
     * But: also lockItemOnEdit - that is bypasses updateItem(-1), so don't need the row?
     * 
     * without tableRow introduces failures in cellTest:
     * - selectingANonEmptyCellIsOK: tableCell.updateSelected is overridden to do nothing without row, that is
     *    selected == false
     * - updatingSelectedToTrueResultsIn_selected_pseudoClass, same: pseudoClass "selected" trivially not
     *   set if selected == false
     *   
     * Summary: don't need tableRow for testing editing, do need lockItem == true
     * FIXME: lookitem is fishy .. forcing an invalid state?  
     */
    @Test
    public void testStartEditNotEmptyNullControl() {
        IndexedCell cell = editableControl.createCell();
        if (cell instanceof TableCell) {
            TableCellShim.set_lockItemOnEdit((TableCell) cell, true);
        }
        if (cell instanceof TreeTableCell) {
            TreeTableCellShim.set_lockItemOnEdit((TreeTableCell) cell, true); 
        }
        CellShim.updateItem(cell, "value", false);
        cell.startEdit();
        assertTrue("not empty cell should be editing", cell.isEditing());
    }
    
    @Test
    public void testStartEditNotEmptyNullControlTwice() {
        IndexedCell cell = editableControl.createCell();
        if (cell instanceof TableCell) {
            TableCellShim.set_lockItemOnEdit((TableCell) cell, true);
        }
        if (cell instanceof TreeTableCell) {
            TreeTableCellShim.set_lockItemOnEdit((TreeTableCell) cell, true); 
        }
        CellShim.updateItem(cell, "value", false);
        cell.startEdit();
        cell.startEdit();
        assertTrue("not empty cell should be editing", cell.isEditing());
    }
    
//    @Ignore("JDK-8188026")
    @Test
    public void testCancelEditNullControlAllowed() {
        IndexedCell cell = editableControl.createCell();
        cell.cancelEdit();
    }
    
//    @Ignore("JDK-8188026")
    @Test
    public void testCommitEditNullControlAllowed() {
        IndexedCell cell = editableControl.createCell();
        cell.commitEdit("edited");
    }
    
 //-----------------------

    /**
     * Creates and returns an editable cell at the given index.
     * Note: neither control nor cell are in editing state!
     */
    private IndexedCell createEditableCellAt(int index) {
        IndexedCell cell = editableControl.createEditableCell();
        cell.updateIndex(index);
        return cell;
    }

//----------- parameterized in xxCell

    @Parameters(name = "{index} - {1}")
    public static Collection parameters() {
        // control factory, type message, cell factory
        return Arrays.asList(new Object[][] {
            { (Supplier) EditableControlFactory::createEditableListView, 
                    "ListView, ListCell", (Callback) lv -> new ListCell<>()},
            { (Supplier) EditableControlFactory::createEditableTableView, 
                    "TableView, TableCell", (Callback) lv -> new TableCell<>()},
            { (Supplier) EditableControlFactory::createEditableTreeView, 
                        "TreeView, TreeCell", (Callback) lv -> new TreeCell<>()},
            { (Supplier) EditableControlFactory::createEditableTreeTableView, 
                            "TreeTableView, TreeTableCell", (Callback) lv -> new TreeTableCell<>()},
            { (Supplier) EditableControlFactory::createEditableListView, 
                    "ListView, TextFieldListCell", TextFieldListCell.forListView()},
            { (Supplier) EditableControlFactory::createEditableTableView, 
                    "TableView, TextFieldTableCell", TextFieldTableCell.forTableColumn()},
            { (Supplier) EditableControlFactory::createEditableTreeView, 
                    "TreeView, TextFieldTreeCell", TextFieldTreeCell.forTreeView()},
            { (Supplier) EditableControlFactory::createEditableTreeTableView, 
                    "TreeTableView, TextFieldTreeTableCell", TextFieldTreeTableCell.forTreeTableColumn()},
        });
    }

    public EditStateTest(Supplier controlSupplier, String typeMessage, Callback cellFactory) {
        this.controlSupplier = controlSupplier;
        this.typeMessage = typeMessage;
        
        this.cellFactory = cellFactory;
    }
//-------------- setup

    @Test
    public void testInitialState() {
        // control
        assertTrue("control must be editable", editableControl.isEditable());
        assertEquals("control must not be editing", -1, editableControl.getEditingIndex());
        assertNotNull("control must have cellFactory", editableControl.getCellFactory());
        assertSame("cellFactory must be same", this.cellFactory, editableControl.getCellFactory());
        // editable cell
        IndexedCell cell = editableControl.createEditableCell();
        assertTrue("cell must be editable", cell.isEditable());
        assertFalse("cell must not be editing", cell.isEditing());
        assertTrue("cell must be empty", cell.isEmpty());
        assertEquals("cell index must be negative", -1, cell.getIndex());
        assertEquals("sanity: cell must be attached to control", 
              editableControl.getControl(), editableControl.getCellControl(cell));
        // not editable cell
        cell = editableControl.createCell();
        assertNull(editableControl.getCellControl(cell));
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
        editableControl = controlSupplier.get();
        if (this.cellFactory != null) {
            editableControl.setCellFactory(cellFactory);
        }
    }


    @After
    public void cleanup() {
        if (stageLoader != null) stageLoader.dispose();
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }


}
