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

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

import javafx.collections.FXCollections;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeView.EditEvent;

/**
 * DO NOT INCLUDE!
 */
public class TreeCellEditTest extends CellEditTestBase<TreeCell, TreeView> {

//----------- overridden test: failures understood - 

    /**
     * Bug: Tree editing state not updated from cell.startEdit
     */
//    @Ignore("JDK-8187474")
    @Test
    @Override
    public void testCellStartEditStartsViewEditing() {
        int cellIndex = 1;
        cell.updateIndex(cellIndex);
        cell.startEdit();
        assertTrue(cell.isEditing());
        assertEquals(cellIndex, editableView.getViewEditingIndex());
    }
    
    /**
     * Bug: Tree editing state not updated from cell.startEdit
     */
    //    @Ignore("JDK-8187474")
    @Test
    @Override
    public void testCellStartEditFiresEditStart() {
        int cellIndex = 0;
        cell.updateIndex(cellIndex);
        List<?> events = editableView.startCellEdit();
        assertTrue(cell.isEditing());
        assertEquals(cellIndex, editableView.getViewEditingIndex());
        assertNotNull(events);
        assertEquals(1, events.size());
        // Note: we don't get here until start updates tree editing state!
        editableView.assertStartEditEvent(events.get(0), cellIndex);
    }
    

    /**
     * 
     * treeCell.cancelEdit throws NPE if at the time of calling the tree's 
     * editingItem == null (which it is after tree.edit(null).
     * 
     * No: was caused in this branch by trying to fix editing event state.
     * 
     * No test in TreeCellTest!
     */
    @Override
    @Test //@Ignore
    public void editOnViewSyncsEditingInCell() {
        int editingIndex = 1;
        cell.updateIndex(editingIndex);
        editableView.editView(editingIndex);
        assertTrue(cell.isEditing());
        editableView.editView(-1);
        assertFalse(cell.isEditing());
    }

    /**
     * the commit assert is hampered by 
     * https://bugs.openjdk.java.net/browse/JDK-8187473 and
     * https://bugs.openjdk.java.net/browse/JDK-8187309
     * 
     * Both roughly the same: cell commit changes the value of the treeItem
     * directly (vs. having a default handler doing so)
     */
//    @Ignore("JDK-8187473, JDK-8187309")
    @Override
    @Test
    public void testCellCommitEditFiresEditCommit() {
        int cellIndex = 0;
        cell.updateIndex(cellIndex);
//        cell.startEdit();
        editableView.editView(cellIndex);
        String value = "edited";
        List<?> events = editableView.commitCellEdit(value);
        assertNotNull(events);
        assertEquals(1, events.size());
        editableView.assertCommitEditEvent(events.get(0), cellIndex, value);
    }
    

//--------- overridden tests: failures not fully understood/ not reported    
    @Override
    @Test //@Ignore
    public void testCellCancelTerminatesViewEditing() {
        int cellIndex = 0;
        cell.updateIndex(cellIndex);
        editableView.editView(cellIndex);
//        cell.startEdit();
        cell.cancelEdit();
        assertFalse(cell.isEditing());
        assertFalse(editableView.isViewEditing());
    }


    @Override
    protected EditableView<TreeCell, TreeView> createEditableView(boolean prepareEditable) {
        return new EditableTreeView(prepareEditable);
    }

    public static class EditableTreeView extends EditableView<TreeCell, TreeView> {

        /**
         * @param prepareEditable
         */
        public EditableTreeView(boolean prepareEditable) {
            super(prepareEditable);
        }

        @Override
        public List<?> startCellEdit() {
            List<Object> collector = new ArrayList<>();
            view.setOnEditStart(collector::add);
            cell.startEdit();
            return collector;
        }

        @Override
        public void assertStartEditEvent(Object event, int index) {
            fail("tbd");
            EditEvent editEvent = (EditEvent) event;
            assertEquals(TreeView.editStartEvent(), editEvent.getEventType());
            assertNotNull(editEvent.getTreeItem());
            TreeItem itemAtIndex = view.getTreeItem(index);
            assertEquals(itemAtIndex, editEvent.getTreeItem());
        }

        @Override
        public List<?> commitCellEdit(Object value) {
            List<Object> collector = new ArrayList<>();
            view.setOnEditCommit(collector::add);
            cell.commitEdit(value);
            return collector;
        }

        @Override
        public void assertCommitEditEvent(Object event, int index, Object value) {
            EditEvent editEvent = (EditEvent) event;
            assertEquals(TreeView.editCommitEvent(), editEvent.getEventType());
            assertNotNull(editEvent.getTreeItem());
            assertEquals(value, editEvent.getNewValue());
            TreeItem itemAtIndex = view.getTreeItem(index);
            assertEquals(itemAtIndex, editEvent.getTreeItem());
            assertEquals(itemAtIndex.getValue(), editEvent.getOldValue());
        }

        @Override
        public List<?> cancelCellEdit() {
            List<Object> collector = new ArrayList<>();
            view.setOnEditCancel(collector::add);
            cell.cancelEdit();
            return collector;
        }

        @Override
        public void assertCancelEditEvent(Object event, int index) {
            EditEvent editEvent = (EditEvent) event;
            assertEquals(TreeView.editCancelEvent(), editEvent.getEventType());
            TreeItem itemAtIndex = view.getTreeItem(index);
            assertEquals(itemAtIndex, editEvent.getTreeItem());
        }

        @Override
        protected boolean isViewEditable() {
            return view.isEditable();
        }

        @Override
        protected void setViewEditable(boolean editable) {
            view.setEditable(editable);
            
        }

        @Override
        public void editView(int index) {
            TreeItem editingItem = view.getTreeItem(index);
            view.edit(editingItem);
        }

        @Override
        public boolean isViewEditing() {
            return view.getEditingItem() != null;
        }

        @Override
        public int getViewEditingIndex() {
            TreeItem item = view.getEditingItem();
            return view.getRow(item);
        }

        @Override
        public Object getItem(int index) {
            return view.getTreeItem(index).getValue();
        }

        @Override
        protected void prepareEditableState() {
            view.getFocusModel().focus(-1);
            view.setEditable(true);
            cell.updateTreeView(view);
        }

        @Override
        protected boolean cellCouldStartEdit() {
            return cell.getTreeView() == view;
        }

        @Override
        protected TreeCell createCell() {
            return new TreeCell();
        }

        @Override
        protected TreeView createView() {
            TreeItem<String> root = new TreeItem<>("root");
            root.setExpanded(true);
            root.getChildren().addAll(FXCollections.observableArrayList(new TreeItem<>("Four"), 
                new TreeItem<>("Five"), new TreeItem<>("Fear"))); // "Flop", "Food", "Fizz")
            TreeView<String> tree = new TreeView<>(root);
            return tree;
        }
        
    }
}
