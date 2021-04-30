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

import org.junit.Test;

import static org.junit.Assert.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ListView.EditEvent;

/**
 *
 */
public class ListCellEditTest extends CellEditTestBase<ListCell, ListView> {

 
    @Test
    public void test() {
        assertTrue(editableView.isViewEditable());
    }
    
    @Override
    protected EditableView<ListCell, ListView> createEditableView(boolean prepareEditing) {
        return new EditableListView(prepareEditing);
    }

    public static class EditableListView extends EditableView<ListCell, ListView> {

        public EditableListView(boolean prepareEditing) {
            super(prepareEditing);
        }

        @Override
        protected boolean isViewEditable() {
            return view.isEditable();
        }

        
        @Override
        public void editView(int index) {
            view.edit(index);
        }
        
        @Override
        protected ListCell createCell() {
            return new ListCell();
        }
        
        @Override
        protected ListView createView() {
            ObservableList<String >model = FXCollections.observableArrayList("Apples", "Oranges", "Pears");
            return new ListView<String>(model);
        }

        @Override
        protected void prepareEditableState() {
            view.setEditable(true);
            cell.updateListView(view);
        }

        @Override
        protected boolean cellCouldStartEdit() {
            return cell.getListView() == view;
        }

        @Override
        public boolean isViewEditing() {
            return view.getEditingIndex() != -1;
        }

        @Override
        protected void setViewEditable(boolean editable) {
            view.setEditable(false);
        }

        @Override
        public int getViewEditingIndex() {
            return view.getEditingIndex();
        }

        @Override
        public List<?> startCellEdit() {
            List<Object> collector = new ArrayList<>();
            view.setOnEditStart(collector::add);
            cell.startEdit();
            return collector;
        }
        
    }

}
