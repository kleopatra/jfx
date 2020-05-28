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
import org.junit.Ignore;
import org.junit.Test;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.ListCellBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;

import static javafx.scene.input.KeyCode.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import static javafx.scene.control.ControlShim.*;
/**
 * Test for misbehavior of individual implementations that turned
 * up in binch testing.
 */
public class BehaviorIssuesTest {
    
    /**
     * Test cleanup of item listener in ListViewBehavior.
     * 
     * Using anchor as marker: it must not change on selection change after behavior is
     * disposed
     * 
     * select -> behavior sets anchor 
     * dispose -> anchor cleared
     * force anchor to selection
     * remove item -> anchor must be unchanged (if not, side-effect of listener cleanup in 
     * behavior)
     */
    @Test
    public void testListViewBehaviorRemoveItem() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        WeakReference<BehaviorBase<?>> weakRef = new WeakReference<>(createBehavior(listView));
        weakRef.get().dispose();
        int last = 1;
        ListCellBehavior.setAnchor(listView, last, false);
        listView.getItems().remove(0);
        assertEquals("anchor must not be changed on removing item", 
                last,
                listView.getProperties().get("anchor"));
    }
 
    /**
     * for comparison: anchor unchanged on removing item without behavior
     */
    @Test
    public void testListViewAnchorRemoveItem() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        int last = 1;
        listView.getSelectionModel().select(last);
        ListCellBehavior.setAnchor(listView, last, false);
        assertEquals(last, listView.getProperties().get("anchor"));
        listView.getItems().remove(0);
        assertEquals(last, listView.getProperties().get("anchor"));
    }
    

    /**
     * Test cleanup of selection listeners in ListViewBehavior.
     * 
     * anchor update with behavior
     * select -> behavior sets anchor 
     * dispose -> anchor cleared
     * select -> anchor must remain cleared (if not, side-effect of listener cleanup in 
     * behavior)
     */
    @Test
    public void testListViewBehaviorSelect() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        WeakReference<BehaviorBase<?>> weakRef = new WeakReference<>(createBehavior(listView));
        int last = 1;
        listView.getSelectionModel().select(last);
        weakRef.get().dispose();
        
        listView.getSelectionModel().select(0);
        assertNull("anchor must remain cleared when selecting without behavior", listView.getProperties().get("anchor"));
    }

    @Test
    public void testListViewBehaviorCleanupAnchor() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        WeakReference<BehaviorBase<?>> weakRef = new WeakReference<>(createBehavior(listView));
        listView.getSelectionModel().select(1);
        weakRef.get().dispose();
        assertNull("anchor must be cleared after dispose", listView.getProperties().get("anchor"));
    }
    
    /**
     * Anchor set with behavior.
     */
    @Test
    public void testListViewBehaviorSelectAnchor() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        WeakReference<BehaviorBase<?>> weakRef = new WeakReference<>(createBehavior(listView));
        int last = 1;
        listView.getSelectionModel().select(last);
        assertEquals("anchor must be set with behavior", last, listView.getProperties().get("anchor"));
    }
    /**
     * for comparison: anchor not set without behavior
     * (which may or may not be the correct behavior - missing anchor semantics in model!)
     */
    @Test
    public void testListViewSelectAnchor() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two");
        ListView<String> listView = new ListView<>(data);
        int last = 1;
        listView.getSelectionModel().select(last);
        assertEquals("selectionModel does not set anchor", null, listView.getProperties().get("anchor"));
    }
    
    /**
     * Bug: inconsistent behavior - clearAndSelect sets anchor, select doesn't
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
     * Bug: anchor not set if behavior installed after selection
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
     * Bug: anchor not set if skin installed after selection
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
     * https://bugs.openjdk.java.net/browse/JDK-8245303
     */
    @Test @Ignore("inputmap")
    public void testInputMapMemoryLeak() {
        Label label = new Label();
        WeakReference<InputMap<?>> inputMap = new WeakReference<>(new InputMap<Label>(label));
        // do-nothing mapping
        KeyMapping mapping = new KeyMapping(SPACE, KeyEvent.KEY_PRESSED, e -> {} );
        inputMap.get().getMappings().add(mapping);
        assertEquals("sanity: mapping added", 1, inputMap.get().getMappings().size());
        inputMap.get().getMappings().remove(mapping);
        assertEquals("sanity: mapping removed", 0, inputMap.get().getMappings().size());
        attemptGC(inputMap);
        assertNull("inputMap must be gc'ed", inputMap.get());
    }
    
    @Test @Ignore("inputmap")
    public void testInputMapButtonBehavior() {
        Button button = new Button();
        WeakReference<BehaviorBase> weakRef = new WeakReference<>(createBehavior(button));
        WeakReference<?> inputMapRef = new WeakReference(weakRef.get().getInputMap());
        weakRef.get().dispose();
        attemptGC(inputMapRef);
        assertNull("inputMap must be gc'ed", inputMapRef.get());
    }
    
    

    @Test
    public void testButtonBehaviorMemoryLeak() {
        Button control = new Button();
        WeakReference<BehaviorBase<?>> weakRef = new WeakReference<>(createBehavior(control));
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
