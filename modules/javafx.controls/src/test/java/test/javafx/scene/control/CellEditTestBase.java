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
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import javafx.event.Event;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;

/**
 * Test state transitions on editing.
 */
public abstract class CellEditTestBase<C extends IndexedCell, V extends Control> {

    protected EditableView<C, V> editableView;
    protected C cell;
    protected V view;
    
// ------------ testing basics: manage editing on cell
//------------ note: testing commit/cancel should start editing on view    
    
    
    //----- cancelEdit
    @Test
    public void testCellCancelEditFiresEditCancel() {
        int cellIndex = 0;
        cell.updateIndex(cellIndex);
//        cell.startEdit();
        editableView.editView(cellIndex);
        List<?> events = editableView.cancelCellEdit();
        assertNotNull(events);
        assertEquals(1, events.size());
        editableView.assertCancelEditEvent(events.get(0), cellIndex);
    }
    
    @Test
    public void testCellCancelTerminatesViewEditing() {
        int cellIndex = 0;
        cell.updateIndex(cellIndex);
//        cell.startEdit();
        editableView.editView(cellIndex);
        cell.cancelEdit();
        assertFalse(cell.isEditing());
        assertFalse(editableView.isViewEditing());
    }

    //--------- commitEdit
    @Test
    public void testCellCommitEditFiresEditCommit() {
        int cellIndex = 0;
        cell.updateIndex(cellIndex);
        editableView.editView(cellIndex);
//        cell.startEdit();
        String value = "edited";
        List<?> events = editableView.commitCellEdit(value);
        assertNotNull(events);
        assertEquals(1, events.size());
        editableView.assertCommitEditEvent(events.get(0), cellIndex, value);
    }
    
    
    @Test
    public void testCellCommitEditUpdatesItems() {
        int cellIndex = 0;
        cell.updateIndex(cellIndex);
        editableView.editView(cellIndex);
//        cell.startEdit();
        Object edited = "edited";
        cell.commitEdit(edited);
        assertEquals(edited, editableView.getItem(cellIndex));
    }
    
    @Test
    public void testCellCommitTerminatesViewEditing() {
        int cellIndex = 0;
        cell.updateIndex(cellIndex);
        editableView.editView(cellIndex);
//        cell.startEdit();
        Object edited = "edited";
        cell.commitEdit(edited);
        assertFalse(cell.isEditing());
        assertFalse(editableView.isViewEditing());
    }
    
    // start edit
    @Test
    public void testCellStartEditFiresEditStart() {
        int cellIndex = 0;
        cell.updateIndex(cellIndex);
        List<?> events = editableView.startCellEdit();
        assertTrue(cell.isEditing());
        assertEquals(cellIndex, editableView.getViewEditingIndex());
        assertNotNull(events);
        assertEquals(1, events.size());
        editableView.assertStartEditEvent(events.get(0), cellIndex);
    }
    
    @Test
    public void testCellStartEditStartsViewEditing() {
        int cellIndex = 1;
        cell.updateIndex(cellIndex);
        cell.startEdit();
        assertTrue(cell.isEditing());
        assertEquals(cellIndex, editableView.getViewEditingIndex());
    }
    
    @Test
    public void testCellStartEditNotEditableView() {
        editableView.setViewEditable(false);
        cell.updateIndex(1);
        cell.startEdit();
        assertFalse(cell.isEditing());
        assertFalse(editableView.isViewEditing());
    }
    
// ------------ testing basics: manage editing on cell (no preconditons)
    
    @Test
    public void testCellStartEditWithoutView() {
        EditableView<C, V> editableView = createEditableView(false);
        editableView.getCell().updateIndex(1);
        editableView.getCell().startEdit();
    }
    
    @Test
    public void testCellCancelEditWithoutView() {
        EditableView<C, V> editableView = createEditableView(false);
        editableView.getCell().updateIndex(1);
        editableView.getCell().cancelEdit();
    }
    
    @Test
    public void testCellCommitEditWithoutView() {
        EditableView<C, V> editableView = createEditableView(false);
        editableView.getCell().updateIndex(1);
        editableView.getCell().commitEdit(null);
    }
    
//------------- testing basics: manage editing on view
    
    @Test
    public void editOnViewSyncsEditingInCell() {
        int editingIndex = 1;
        cell.updateIndex(editingIndex);
        editableView.editView(editingIndex);
        assertTrue(cell.isEditing());
        editableView.editView(-1);
        assertFalse(cell.isEditing());
    }
    
    @Test
    public void editOnViewResultsInNotEditingInCellWhenDifferentIndex() {
        int cellIndex = 0;
        int editingIndex = 1;
        cell.updateIndex(cellIndex);
        editableView.editView(editingIndex);
        assertFalse(cell.isEditing());
    }
    
//------------sanity: test facade methods
    
    @Test
    public void testViewEditingIndex() {
        int editingIndex = 1;
        editableView.editView(editingIndex);
        assertEquals(editingIndex, editableView.getViewEditingIndex());
    }
    
    @Test
    public void testViewEditable() {
        EditableView<C, V> facade = createEditableView(true);
        assertTrue(facade.isViewEditable());
        facade.setViewEditable(false);
        assertFalse(facade.isViewEditable());
    }
    
//----------------------- init and sanity 
    
    
    @Test
    public void testIntialEditable() {
        assertTrue(editableView.isViewEditable());
        assertTrue(editableView.cellCouldStartEdit());
        assertFalse(cell.isEditing());
        assertEquals(-1, cell.getIndex());
        assertFalse(editableView.isViewEditing());
        assertEquals(-1, editableView.getViewEditingIndex());
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

        editableView = createEditableView(true);
        // convenience caching
        cell = editableView.cell;
        view = editableView.view;
        assertNotNull(editableView);
    }

    @After
    public void cleanup() {
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }


    protected abstract EditableView<C, V> createEditableView(boolean prepareEditable);
    
    /**
     * Facade for editable virtualized controls.
     */
    public static abstract class EditableView<C extends IndexedCell, V extends Control> {
        protected V view;
        protected C cell;
        
        /**
         * Instantiates an EditableView with view/cell prepared for editing.
         */
        public EditableView() {
            this(true);
        }
        
        public EditableView(boolean prepareForEditing) {
            view = createView();
            cell = createCell();
            assertNotNull(view);
            assertNotNull(cell);
            if (prepareForEditing) {
                prepareEditableState();
                assertTrue(cellCouldStartEdit());
            }
        }
        
        public abstract List<?> startCellEdit();
        
        /**
         * asserts that the event is a startEdit event at location index.
         * 
         * @param event and object representing a editEvent in the view, must not be null
         * @param index the expected editing location of the editEvent, must be valid
         */
        public void assertStartEditEvent(Object event, int index) {
            fail("subclasses must implement the assert");
        };
        
        public abstract List<?> commitCellEdit(Object value);
        /**
         * asserts that the event is a commitEdit event at location index with value.
         * 
         * @param event and object representing a editEvent in the view, must not be null
         * @param index the expected editing location of the editEvent, must be valid
         * @param value the new value
         */
        public void assertCommitEditEvent(Object event, int index, Object value) {
            fail("subclasses must implement the assert");
        }
        
        public abstract List<?> cancelCellEdit();
        
        /**
         * asserts that the event is a startEdit event at location index.
         * 
         * @param event and object representing a editEvent in the view, must not be null
         * @param index the expected editing location of the editEvent, must be valid
         */
        public void assertCancelEditEvent(Object event, int index) {
            fail("subclasses must implement the assert");
        }
        
        
        // view state
        protected abstract boolean isViewEditable();
        protected abstract void setViewEditable(boolean editable);
        
        /** 
         * start/cancel edit on control
         * 
         * @param index the editing location on the control, -1 will cancel
         */
        public abstract void editView(int index);
        public abstract boolean isViewEditing();
        public abstract int getViewEditingIndex();
        
        /**
         * Model state.
         */
        public abstract Object getItem(int index);
        
        /**
         *  
         */
        protected abstract void prepareEditableState();
        protected abstract boolean cellCouldStartEdit();
        
        public C getCell() {
            return cell;
        };
        public V getView() {
            return view;
        };
        
        // factory methods for cell/view
        protected abstract C createCell();
        protected abstract V createView();
        
    }
    
}
