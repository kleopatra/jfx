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
import com.sun.javafx.scene.control.behavior.TreeCellBehavior;

import static javafx.collections.FXCollections.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * This test has methods defining the behavior (of the list w/out behavior) and
 * unrelated issues. Those around fixing ?? will be moved into BehaviourCleanupTest 
 * before commit.
 * 
 * Note: do not include in the fix, meant for keeping around in dokeep-branch. 
 */
public class TreeViewBehaviorIssuesTest {

//--------- anchor (== selection listener)

    /**
     * Not (directly?) applicable: 
     * ListViewBehavior has listener to items (which updates the anchor)
     * TreeViewBehavior does not listen to items/treeModification nor to focus
     * Don't move to cleanup test!
     */
    @Test
    public void failedTreeViewBehaviorRemoveItem() {
        TreeView<String> treeView = new TreeView<>(createRoot());
        createBehavior(treeView);
        int last = 2;
        TreeCellBehavior.setAnchor(treeView, last, false);
        assertEquals("behavior must set anchor on select", last, treeView.getProperties().get("anchor"));
        treeView.getRoot().getChildren().remove(0);
        assertEquals("anchor must be updated on items modification",
                last -1, treeView.getProperties().get("anchor"));
    }
    
    /**
     * Creates and returns an expanded treeItem with two children.
     */
    private TreeItem<String> createRoot() {
        TreeItem<String> root = new TreeItem<>("root");
        root.setExpanded(true);
        root.getChildren().addAll(new TreeItem<>("child one"), new TreeItem<>("child two"));
        return root;
    }


//--------------- memory leak (temp)
    
    /**
     * Create behavior -> dispose behavior -> gc
     */
    @Test
    public void failedMemoryLeakDisposeBehavior() {
        TreeView<Object> control = new TreeView<>();
        WeakReference<BehaviorBase<?>> weakRef = new WeakReference<>(createBehavior(control));
        assertNotNull(weakRef.get());
        weakRef.get().dispose();
        attemptGC(weakRef);
        assertNull("behavior must be gc'ed", weakRef.get());
    }

//----------- setup   
    
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
