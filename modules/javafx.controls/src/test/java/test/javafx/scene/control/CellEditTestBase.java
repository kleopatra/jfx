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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;

/**
 *
 */
public abstract class CellEditTestBase<C extends IndexedCell, V extends Control> {

    protected EditableView<C, V> editableView;
    protected C cell;
    protected V view;
    
//------------- testing basics: control editing on view
    
    @Test
    public void editOnViewResultsInEditingInCell() {
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
    
//----------------------- init and sanity 
    
    @Test
    public void testIntialEditable() {
        assertTrue(editableView.isEditable());
        assertTrue(editableView.cellCouldStartEdit());
        assertFalse(cell.isEditing());
        assertEquals(-1, cell.getIndex());
        assertFalse(editableView.isViewEditing());
    }
    
    @Before
    public void setup() {
        editableView = createEditableView();
        // convenience caching
        cell = editableView.cell;
        view = editableView.view;
        assertNotNull(editableView);
    }
    
    protected abstract EditableView<C, V> createEditableView();
    
    public static abstract class EditableView<C extends IndexedCell, V extends Control> {
        protected V view;
        protected C cell;
        
        public EditableView() {
            view = createView();
            cell = createCell();
            assertNotNull(view);
            assertNotNull(cell);
            prepareEditableState();
            assertTrue(cellCouldStartEdit());
        }
        
        // control state
        protected abstract boolean isEditable();
        
        
        /** 
         * start/cancel edit on control
         * 
         * @param index the editing location on the control, -1 will cancel
         */
        public abstract void editView(int index);
        public abstract boolean isViewEditing();
        
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
