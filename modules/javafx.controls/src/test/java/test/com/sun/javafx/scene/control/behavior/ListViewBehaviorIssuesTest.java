/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

package test.com.sun.javafx.scene.control.behavior;

import java.lang.ref.WeakReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.ListCellBehavior;

import static javafx.scene.control.ControlShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
/**
 * This test has methods defining the behavior (of the list w/out behavior) and
 * unrelated issues. Those around fixing 8246195 are in BehaviourCleanupTest.
 * 
 * Note: do not include in the fix, meant for keeping around in dokeep-branch. 
 */
public class ListViewBehaviorIssuesTest {
 
 //------------------- ListView  
    
 //-------------- here only comparing listView without behavior
    
    /**

 // ------------ remove item    
   
 
    /**
     * for comparison: anchor unchanged on removing item without behavior
     */
    @Test
    public void testListViewRemoveItem() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        int last = 1;
        listView.getSelectionModel().select(last);
        ListCellBehavior.setAnchor(listView, last, false);
        assertEquals(last, listView.getProperties().get("anchor"));
        listView.getItems().remove(0);
        assertEquals(last, listView.getProperties().get("anchor"));
    }
    
//------------ select
    

    /**
     * Anchor set by behavior on select.
     */
    @Test
    public void testListViewBehaviorSelect() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        createBehavior(listView);
        int last = 1;
        listView.getSelectionModel().select(last);
        assertEquals("anchor must be set with behavior", last, listView.getProperties().get("anchor"));
    }
 
    /**
     * for comparison: anchor not set without behavior
     * (which may or may not be the correct behavior - missing anchor semantics in model!)
     */
    @Test
    public void testListViewSelect() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        listView.getSelectionModel().select(1);
        assertEquals("selectionModel does not set anchor", null, listView.getProperties().get("anchor"));
    }

//----------- unrelated/ inconclusive/ incorrect test assumptions ..?

    /**
     * for comparison: anchor not set without behavior
     * (which may or may not be the correct behavior - missing anchor semantics in model!)
     */
    @Test
    public void testListViewSelectSetItemsAnchor() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        int last = 1;
        listView.getSelectionModel().select(last);
        ObservableList<String> data2 = FXCollections.observableArrayList("other", "again");
        listView.setItems(data2);
        assertEquals("selectionModel does not set anchor", null, listView.getProperties().get("anchor"));
    }
    
    /**
     * Bug (?) anchor not cleared on setItems - selectedIndex is set to -1: 
     * behavior of anchor select/set items
     */
    @Test
    public void testListViewBehaviorSetItemsAnchor() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        createBehavior(listView);
        int last = 1;
        listView.getSelectionModel().select(last);
        assertEquals("behavior must set anchor on select", last, listView.getProperties().get("anchor"));
        ObservableList<String> data2 = FXCollections.observableArrayList("other", "again");
        listView.setItems(data2);
        listView.getSelectionModel().select(last);
        
//        assertEquals(last, listView.getSelectionModel().getSelectedIndex());
        assertEquals("behavior must update anchor on items modification", 
                last -1, listView.getProperties().get("anchor"));
    }

   
    /**
     * unrelated Bug: inconsistent behavior - clearAndSelect sets anchor, select doesn't
     * which might be correct (modulo missing anchor semantics) for multiple selection
     * but not for single selection (which is default)
     */
    @Test
    public void testListViewClearAndSelectAnchor() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        int last = 1;
        listView.getSelectionModel().clearAndSelect(last);
        assertEquals(null, listView.getProperties().get("anchor"));
    }
    
    /**
     * unrelated Bug: anchor not set if behavior installed after selection
     */
    @Test
    public void testListViewBehaviorAnchorInitialSelect() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        int last = 1;
        listView.getSelectionModel().select(last);
        WeakReference<BehaviorBase<?>> weakRef = new WeakReference<>(createBehavior(listView));
        assertEquals("anchor must be set when creating behavior", last, listView.getProperties().get("anchor"));
    }
    
    /**
     * unrelated Bug: anchor not set if skin installed after selection
     */
    @Test
    public void testListViewSkinAnchorInitialSelect() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        int last = 1;
        listView.getSelectionModel().select(last);
        installDefaultSkin(listView);
        assertEquals("anchor must be set when creating skin", last, listView.getProperties().get("anchor"));
    }
    
    /**
     * just for shortening test time during fixing, remove!!
     */
    @Test
    public void testListViewBehaviorLeak() {
//        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>();
        WeakReference<BehaviorBase<?>> weakRef = new WeakReference<>(createBehavior(listView));
        assertNotNull(weakRef.get());
        weakRef.get().dispose();
        attemptGC(weakRef);
        assertNull("behavior must be gc'ed", weakRef.get());
    }
    
    
//------------------ setup/cleanup    
    @After
    public void cleanup() {
        Thread.currentThread().setUncaughtExceptionHandler(null);
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

        
    }


}
