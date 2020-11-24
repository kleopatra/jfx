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

package test.javafx.scene.control.skin;

import java.lang.ref.WeakReference;

import org.junit.Test;

import static javafx.scene.control.ControlShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Temp test for treeView skin issues JDK-8256821
 * Extracted from SkinIssuesTest - to be moved to SkinCleanupTest and SkinMemoryTest for commit.
 */
public class SkinTreeIssuesTest {

  //---------------- TreeView
    
    /**
     * Sanity: replacing the root has no side-effect, listener to rootProperty
     * is registered with skin api
     */
    @Test
    public void testTreeViewSetRoot() {
        TreeView<String> listView = new TreeView<>(createRoot());
        installDefaultSkin(listView);
        replaceSkin(listView);
        listView.setRoot(createRoot());
    }
    
    /**
     * NPE from event handler to treeModefication of root.
     */
    @Test
    public void testTreeViewAddRootChild() {
        TreeView<String> listView = new TreeView<>(createRoot());
        installDefaultSkin(listView);
        replaceSkin(listView);
        listView.getRoot().getChildren().add(createRoot());
    }
    
    /**
     * NPE from event handler to treeModefication of root.
     */
    @Test
    public void testTreeViewReplaceRootChildren() {
        TreeView<String> listView = new TreeView<>(createRoot());
        installDefaultSkin(listView);
        replaceSkin(listView);
        listView.getRoot().getChildren().setAll(createRoot().getChildren());
    }


    /**
     * NPE due to properties listener not removed
     */
    @Test
    public void testTreeViewRefresh() {
        TreeView<String> listView = new TreeView<>();
        installDefaultSkin(listView);
        replaceSkin(listView);
        listView.refresh();
    }
    
    /**
     * default skin -> set alternative
     */
    @Test
    public void testMemoryLeakAlternativeSkin() {
        TreeView<String> control = new TreeView<>();
        installDefaultSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(control));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }
    
    /**
     * MemoryLeak from root modification listener? not if others are fixed
     */
    @Test
    public void testMemoryLeakAlternativeSkinWithRoot() {
        TreeView<String> control = new TreeView<>(createRoot());
        installDefaultSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(control));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }

    /**
     * Creates and returns an expanded root with two children
     * @return
     */
    private TreeItem<String> createRoot() {
        TreeItem<String> root = new TreeItem<>("root");
        root.setExpanded(true);
        root.getChildren().addAll(new TreeItem<>("child one"), new TreeItem<>("child two"));
        return root;
    }

   

}
