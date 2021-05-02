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

import java.util.Optional;

import org.junit.Test;

import static org.junit.Assert.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ListView.EditEvent;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.skin.ListCellSkin;
import javafx.util.Callback;

/**
 * Test core list cell (not in scenegraph by default, can be added though)
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class NListCellTest extends AbstractNCellTest<ListView, ListCell> {

    @Override
    protected void assertValueAt(int index, Object editedValue, EditableControl<ListView, ListCell> control) {
        assertEquals(editedValue, control.getControl().getItems().get(index));
    };
    
    @Override
    protected void assertLastStartIndex(AbstractEditReport report, int index, Object target) {
        Optional<EditEvent> e = report.getLastEditStart();
        assertTrue(e.isPresent());
        assertEquals(report.getAllEditEventTexts("index on start event"), index, e.get().getIndex());
    }
    
    @Override
    protected void assertLastCancelIndex(AbstractEditReport report, int index, Object target) {
        Optional<EditEvent> e = report.getLastEditCancel();
        assertTrue(e.isPresent());
        assertEquals(report.getAllEditEventTexts("index on cancel event"), index, e.get().getIndex());
    }
    
    @Override
    protected void assertLastCommitIndex(AbstractEditReport report, int index, Object target, Object value) {
        Optional<EditEvent> commit = report.getLastEditCommit();
        assertTrue(commit.isPresent());
        assertEquals("index on commit event", index, commit.get().getIndex());
        assertEquals("newValue on commit event", value, commit.get().getNewValue());
        assertEquals(report.getAllEditEventTexts("commit must fire a single event "), 
                1, report.getEditEventSize());
    }

    //--------------------- old bugs, fixed in fx9    
    @Test
    public void testListCellSkinInit() {
        ListCell cell = new ListCell();
        cell.setSkin(new ListCellSkin(cell));
    }
    

    /**
     * Creates and returns an editable List configured with 4 items
     * and TextFieldListCell as cellFactory
     * 
     */
    @Override
    protected EditableControl createEditableControl() {
        EListView control = new EListView(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        control.setEditable(true);
        control.setCellFactory(createTextFieldCellFactory());
        control.getFocusModel().focus(-1);
        return control;
    }

    @Override
    protected Callback<ListView, ListCell> createTextFieldCellFactory() {
        return e -> new TextFieldListCell();
//                (Callback<ListView, ListCell>)TextFieldListCell.forListView();
    }

    @Override
    protected AbstractEditReport createEditReport(EditableControl control) {
        return new ListViewEditReport(control);
    }

    public static class EListView extends ListView 
        implements EditableControl<ListView, ListCell> {

        
        @Override
        public ListView getControl() {
            return this;
        }

        public EListView() {
            super();
        }

        public EListView(ObservableList arg0) {
            super(arg0);
        }

        @Override
        public EventType editAny() {
            return editAnyEvent();
        }

        @Override
        public EventType editCommit() {
            return editCommitEvent();
        }

        @Override
        public EventType editCancel() {
            return editCancelEvent();
        }

        @Override
        public EventType editStart() {
            return editStartEvent();
        }

        @Override
        public <T extends Event> void addEditEventHandler(EventType<T> type,
                EventHandler<? super T> handler) {
            addEventHandler(type, handler);
        }

        @Override
        public ListCell createEditableCell() {
            ListCell cell = (ListCell) getCellFactory().call(this);
            cell.updateListView(getControl());
            return cell;
        }
        
        
    }

}
