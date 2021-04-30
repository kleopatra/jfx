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

    @Test
    public void testCellStartEditFiresEditStart() {
        int cellIndex = 0;
        cell.updateIndex(cellIndex);
        List<?> events = editableView.startCellEdit();
        assertTrue(cell.isEditing());
        assertEquals(cellIndex, editableView.getViewEditingIndex());
        assertNotNull(events);
        assertEquals(1, events.size());
    }
    
    @Test
    public void testCellStartEditUpdatesListEditing() {
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
    
// ------------ testing basics: manage editing on cell (no preconditon)
    
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
        editableView = createEditableView(true);
        // convenience caching
        cell = editableView.cell;
        view = editableView.view;
        assertNotNull(editableView);
    }
    
    protected abstract EditableView<C, V> createEditableView(boolean prepareEditable);
    
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
    
    public static class EventCollector<T extends Event> implements Consumer<T> {
        List<Event> events = new ArrayList<>();

        @Override
        public void accept(T t) {
            events.add(t);
        }
    }
}
