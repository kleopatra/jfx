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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.ListView.EditEvent;

/**
 * Test listCell editing state updated on re-use (aka: updateIndex(old, new)).
 *
 * This test is parameterized in cellIndex and editingIndex.
 *
 */
@RunWith(Parameterized.class)
public class ListCellEditingTest {
    private ListCell<String> cell;
    private ListView<String> list;
    private ObservableList<String> model;

    private int cellIndex;
    private int editingIndex;

//--------------- change off editing index

    @Test
    public void testOffEditingIndex() {

        cell.updateIndex(editingIndex);
        list.edit(editingIndex);
        cell.updateIndex(cellIndex);
        assertEquals("sanity: cell index changed", cellIndex, cell.getIndex());
        assertEquals("sanity: list editing Index must be unchanged", editingIndex, list.getEditingIndex());
        assertFalse("cell must not be editing on update from editingIndex" + editingIndex
                + " to cellIndex " + cellIndex, cell.isEditing());
    }

    @Test
    public void testCancelOffEditingIndex() {
        cell.updateIndex(editingIndex);
        list.edit(editingIndex);
        List<EditEvent> events = new ArrayList<EditEvent>();
        list.setOnEditCancel(e -> {
            events.add(e);
        });
        cell.updateIndex(cellIndex);
        assertEquals("cell must have fired edit cancel", 1, events.size());
        assertEquals("cancel event index must be same as editingIndex", editingIndex, events.get(0).getIndex());
    }

//--------------- change to editing index


    @Test
    public void testToEditingIndex() {
        cell.updateIndex(cellIndex);
        list.edit(editingIndex);
        cell.updateIndex(editingIndex);
        assertEquals("sanity: cell at editing index", editingIndex, cell.getIndex());
        assertEquals("sanity: list editing Index must be unchanged", editingIndex, list.getEditingIndex());
        assertTrue("cell must be editing on update from " + cellIndex
                + " to editingIndex " + editingIndex, cell.isEditing());
    }

    @Test
    public void testStartEvent() {
        cell.updateIndex(cellIndex);
        list.edit(editingIndex);
        List<EditEvent> events = new ArrayList<EditEvent>();
        list.setOnEditStart(e -> {
            events.add(e);
        });
        cell.updateIndex(editingIndex);
        assertEquals("cell must have fired edit start on update from " + cellIndex + " to " + editingIndex,
                1, events.size());
        assertEquals("start event index must be same as editingIndex", editingIndex, events.get(0).getIndex());
    }

//------------- parameterized

    // Note: name property not supported before junit 4.11
    @Parameterized.Parameters // (name = "{index}: cellIndex {0}, editingIndex {1}")
    public static Collection<Object[]> data() {
     // [0] is cellIndex, [1] is editingIndex
        Object[][] data = new Object[][] {
            {1, 2}, // normal
            {0, 1}, // zero cell index
            {1, 0}, // zero editing index
            {-1, 1}, // negative cell
        };
        return Arrays.asList(data);
    }


    public ListCellEditingTest(int cellIndex, int editingIndex) {
        this.cellIndex = cellIndex;
        this.editingIndex = editingIndex;
    }
//-------------- setup and helpers

    /**
     * Sanity: cell editing state updated when on editing index.
     */
    @Test
    public void testEditOnCellIndex() {
        cell.updateIndex(editingIndex);
        list.edit(editingIndex);
        assertTrue("sanity: cell must be editing", cell.isEditing());
    }

    /**
     * https://bugs.openjdk.java.net/browse/JDK-8165214
     * index of cancel is incorrect
     *
     * also related:
     * https://bugs.openjdk.java.net/browse/JDK-8187226
     */
    @Test
    public void testCancelEditOnControl() {
        cell.updateIndex(editingIndex);
        list.edit(editingIndex);
        List<EditEvent> events = new ArrayList<EditEvent>();
        list.setOnEditCancel(e -> {
            events.add(e);
        });
        list.edit(-1);
        assertEquals(1, events.size());
        EditEvent cancelEvent = events.get(0);
        assertEquals(editingIndex, cancelEvent.getIndex());
    }


    /**
     * Sanity: cell editing state unchanged when off editing index.
     */
    @Test
    public void testEditOffCellIndex() {
        cell.updateIndex(cellIndex);
        list.edit(editingIndex);
        assertFalse("sanity: cell editing must be unchanged", cell.isEditing());
    }

    /**
     * Sanity: edit on list does not fire start.
     */
    @Test
    public void testStartEventEditOffCellIndex() {
        cell.updateIndex(cellIndex);
        List<EditEvent> events = new ArrayList<EditEvent>();
        list.setOnEditStart(e -> {
            events.add(e);
        });
        list.edit(editingIndex);
        assertEquals("sanity: edit list must not fire editStart", 0, events.size());
    }
    /**
     * Sanity: fix doesn't interfere with RT-31165
     */
    @Test
    public void testUpdateIndexSameWhileNotEditing() {
        cell.updateIndex(cellIndex);
        list.edit(editingIndex);
        List<EditEvent> events = new ArrayList<EditEvent>();
        list.setOnEditStart(events::add);
        list.setOnEditCancel(events::add);
        list.setOnEditCommit(events::add);
        cell.updateIndex(cellIndex);
        assertEquals(editingIndex, list.getEditingIndex());
        assertFalse(cell.isEditing());
        assertEquals(0, events.size());
    }

    /**
     * Sanity: fix doesn't interfere with RT-31165
     */
    @Test
    public void testUpdateIndexSameWhileEditing() {
        cell.updateIndex(editingIndex);
        list.edit(editingIndex);
        List<EditEvent> events = new ArrayList<EditEvent>();
        list.setOnEditStart(events::add);
        list.setOnEditCancel(events::add);
        list.setOnEditCommit(events::add);
        cell.updateIndex(editingIndex);
        assertEquals(editingIndex, list.getEditingIndex());
        assertTrue(cell.isEditing());
        assertEquals(0, events.size());
    }

    @Before public void setup() {
        cell = new ListCell<String>();
        model = FXCollections.observableArrayList("Apples", "Oranges", "Pears");
        list = new ListView<String>(model);
        list.setEditable(true);
        // doesn't matter whether updateList is called before/after fiddling with focus
        cell.updateListView(list);
        // failures for cellIndex == 0 -> editingIndex for default focus
//        list.getFocusModel().focus(0);
        // init to cellIndex lead to broken listEditingIndex
        // in all toXX for Florian's fix (updateEditing before updateFocus
        // in all offXX for mine (updateEditing after updateFocus, similar to TableCell)
//        list.getFocusModel().focus(cellIndex);
        // can be fixed by setup focus state to either editing index or -1
        // note: that's different from TableCell - why?
//        list.getFocusModel().focus(editingIndex);
        list.getFocusModel().focus(-1);
        assertFalse("sanity: cellIndex not same as editingIndex", cellIndex == editingIndex);
        assertTrue("sanity: valid editingIndex", editingIndex < model.size());
//        System.out.println("changed?");

    }

}
