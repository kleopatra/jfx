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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.javafx.tk.Toolkit;

import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import test.com.sun.javafx.scene.control.infrastructure.StageLoader;
import test.com.sun.javafx.scene.control.infrastructure.VirtualFlowTestUtils;

/**
 * Additional tests for TreeTableCellTest.
 * 
 * Note: the setup should always be the exact same as parent TreeTableCellTest.
 * To keep manageable, remove those that are included in fixes.
 */
public class TreeTableCellTestExt {

    private TreeTableCell<String, String> cell;
    private TreeTableView<String> tree;
    private TreeTableRow<String> row;

    private static final String ROOT = "Root";
    private static final String APPLES = "Apples";
    private static final String ORANGES = "Oranges";
    private static final String PEARS = "Pears";

    private TreeItem<String> root;
    private TreeItem<String> apples;
    private TreeItem<String> oranges;
    private TreeItem<String> pears;
    private StageLoader stageLoader;

    private TreeTableColumn<String, String> editingColumn;

//------------ testing editCancel event: location on event - JDK-8187229
    

    /**
     * Basic config of treeTable-/cell to allow testing of editEvents: 
     * table has editingColumn and cell is configured with table and column.
     */
    private void setupForEditing() {
        tree.setEditable(true);
        tree.getColumns().add(editingColumn);
        // FIXME: default cell (of tableColumn) needs not-null value for firing cancel
        editingColumn.setCellValueFactory(cc -> new SimpleObjectProperty<>(""));

        cell.updateTreeTableView(tree);
        cell.updateTreeTableColumn(editingColumn);
    }
    
    /**
     * Fails before fix of JDK-8187229.
     */
    @Test
    public void testEditCancelEventAfterModifyItemsWithSanityAsserts() {
        setupForEditing();
        stageLoader = new StageLoader(tree);
        int editingIndex = 2;
        List<CellEditEvent<?, ?>> startEvents = new ArrayList<>();
        editingColumn.setOnEditStart(startEvents::add);
        tree.edit(editingIndex, editingColumn);
        Toolkit.getToolkit().firePulse();
        TreeTablePosition<?, ?> editingPosition = tree.getEditingCell();
        // table
        assertNotNull("sanity: table is editing", editingPosition);
        assertEquals("sanity: editing row", editingIndex, editingPosition.getRow());
        assertEquals("sanity: editing column", editingColumn, editingPosition.getTableColumn());
        // startEvent
        assertEquals("sanity: editingStarted", 1, startEvents.size());
        assertEquals("sanity: position in editStart", editingPosition, startEvents.get(0).getTreeTablePosition());
        
        List<CellEditEvent<?, ?>> events = new ArrayList<>();
        editingColumn.setOnEditCancel(events::add);
        root.getChildren().add(0, new TreeItem<>("added"));
        Toolkit.getToolkit().firePulse();
        
        assertNull("sanity: editing terminated on items modification", tree.getEditingCell());
        assertEquals("column must have received editCancel", 1, events.size());
        assertEquals(editingPosition, events.get(0).getTreeTablePosition());
    }

    /**
     * Test that removing the editing item implicitly cancels an ongoing
     * edit and fires a correct cancel event.
     */
    @Test
    public void testEditCancelEventAfterRemoveEditingItemWithSanityAsserts() {
        setupForEditing();
        stageLoader = new StageLoader(tree);
        int editingIndex = 1;
        List<CellEditEvent<?, ?>> startEvents = new ArrayList<>();
        editingColumn.setOnEditStart(startEvents::add);
        tree.edit(editingIndex, editingColumn);
        Toolkit.getToolkit().firePulse();
        TreeTablePosition<?, ?> editingPosition = tree.getEditingCell();
        // table
        assertNotNull("sanity: table is editing", editingPosition);
        assertEquals("sanity: editing row", editingIndex, editingPosition.getRow());
        assertEquals("sanity: editing column", editingColumn, editingPosition.getTableColumn());
        // startEvent
        assertEquals("sanity: editingStarted", 1, startEvents.size());
        assertEquals("sanity: position in editStart", editingPosition, startEvents.get(0).getTreeTablePosition());
        
        List<CellEditEvent<?, ?>> events = new ArrayList<>();
        editingColumn.setOnEditCancel(events::add);
        root.getChildren().remove(editingIndex - 1);
        Toolkit.getToolkit().firePulse();
        
        assertNull("sanity: editing terminated on items modification", tree.getEditingCell());
        assertEquals("column must have received editCancel", 1, events.size());
        assertEquals(editingPosition, events.get(0).getTreeTablePosition());
    }
    
    /**
     * Test that removing a committed editing item does not cause a memory leak.
     */
    @Test
    public void testEditCommitMemoryLeakAfterRemoveEditingItem() {
        setupForEditing();
        stageLoader = new StageLoader(tree);
        // the item to test for being gc'ed
        TreeItem<String> editingItem = new TreeItem<>("added");
        WeakReference<TreeItem<?>> itemRef = new WeakReference<>(editingItem);
        root.getChildren().add(0, editingItem);
        Toolkit.getToolkit().firePulse();
        int editingIndex = tree.getRow(editingItem);
        tree.edit(editingIndex, editingColumn);
        TreeTableCell<String,String> editingCell = (TreeTableCell<String, String>) VirtualFlowTestUtils.getCell(tree, editingIndex, 0);
        editingCell.commitEdit("added changed");
        root.getChildren().remove(editingItem);
        Toolkit.getToolkit().firePulse();
        assertNull("removing item must cancel edit on tree", tree.getEditingCell());
        editingItem = null;
        attemptGC(itemRef);
        assertEquals("treeItem must be gc'ed", null, itemRef.get());
    }
    


    
// ----------------- setup, init 
    
    @Before public void setup() {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException)throwable;
            } else {
                Thread.currentThread().getThreadGroup().uncaughtException(thread, throwable);
            }
        });

        cell = new TreeTableCell<String, String>();

        root = new TreeItem<>(ROOT);
        apples = new TreeItem<>(APPLES);
        oranges = new TreeItem<>(ORANGES);
        pears = new TreeItem<>(PEARS);
        root.getChildren().addAll(apples, oranges, pears);

        tree = new TreeTableView<String>(root);
        root.setExpanded(true);
        editingColumn = new TreeTableColumn<>("TEST");
        
        row = new TreeTableRow<>();
    }

    @After
    public void cleanup() {
        if (stageLoader != null) stageLoader.dispose();
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }



}
