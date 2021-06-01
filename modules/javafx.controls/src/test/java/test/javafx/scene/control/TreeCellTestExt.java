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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.javafx.tk.Toolkit;

import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeView.EditEvent;
import test.com.sun.javafx.scene.control.infrastructure.StageLoader;
import test.com.sun.javafx.scene.control.infrastructure.VirtualFlowTestUtils;

/**
 * Additional tests for TreeCellTest.
 * 
 * Note: the setup should always be the exact same as parent TreeCellTest.
 * To keep manageable, remove those that are included in fixes.
 */
public class TreeCellTestExt {
    
    
    private TreeCell<String> cell;
    private TreeView<String> tree;

    private static final String ROOT = "Root";
    private static final String APPLES = "Apples";
    private static final String ORANGES = "Oranges";
    private static final String PEARS = "Pears";

    private TreeItem<String> root;
    private TreeItem<String> apples;
    private TreeItem<String> oranges;
    private TreeItem<String> pears;
    private StageLoader stageLoader;
    
//------------ experimenting with potential memory leak
    
    
    /**
     * Document correct test setup: must fire pulse after modifications.
     */
    @Test
    public void testFindCellForAddedItem() {
        stageLoader = new StageLoader(tree);
        tree.setEditable(true);
        TreeItem<String> editingItem = new TreeItem<>("added");
        root.getChildren().add(0, editingItem);
        Toolkit.getToolkit().firePulse();
        int editingIndex = tree.getRow(editingItem);
        tree.edit(editingItem);
        assertEquals("sanity", editingItem, tree.getEditingItem());
        TreeCell<?> editingCell = (TreeCell<?>) VirtualFlowTestUtils.getCell(tree, editingIndex);
        assertEquals(editingItem, editingCell.getTreeItem());
        assertTrue("cell must be editing", editingCell.isEditing());
    }
    
    /**
     * Sanity test: no reference to item after it's removed
     */
    @Test
    public void testMemoryLeakAfterRemoveItem() {
        // need a new item, test setup holds reference to all items
        TreeItem<String> editingItem = new TreeItem<>("added");
        root.getChildren().add(editingItem);
        WeakReference<TreeItem<?>> itemRef = new WeakReference<>(editingItem);
        root.getChildren().remove(editingItem);
        editingItem = null;
        attemptGC(itemRef);
        assertEquals("item must be gc'ed", null, itemRef.get());
    }
    
    /**
     * Note: cancel edit on removing the editingItem seems to be handled in 
     * skins (which? not cell, must be treeViewSkin?) - without the tree's editingItem
     * still holds the old.
     */
    @Ignore("treeView - editingItem spec")
    @Test
    public void testEditingItemAfterRemove() {
        tree.setEditable(true);
        int editingIndex = 2;
        TreeItem<String> editingItem = tree.getTreeItem(editingIndex);
        tree.edit(editingItem);
        root.getChildren().remove(editingItem);
        assertNull("removing item must cancel edit on tree", tree.getEditingItem());
    }
    
    @Ignore("treeView - editingItem spec")
    @Test
    public void testEditingItemUncontained() {
        tree.setEditable(true);
        TreeItem<String> editingItem = new TreeItem<>("uncontained");
        tree.edit(editingItem);
        assertNull("must not edit uncontained", tree.getEditingItem());
    }
    

//----------- end testing potential memory leak
    

    
//--------- init
    
    @Before public void setup() {
        cell = new TreeCell<String>();

        root = new TreeItem<>(ROOT);
        apples = new TreeItem<>(APPLES);
        oranges = new TreeItem<>(ORANGES);
        pears = new TreeItem<>(PEARS);
        root.getChildren().addAll(apples, oranges, pears);

        tree = new TreeView<String>(root);
        root.setExpanded(true);
    }

    @After
    public void cleanup() {
        if (stageLoader != null) stageLoader.dispose();
    }


}
