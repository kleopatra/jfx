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

package test.javafx.scene.control;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static javafx.scene.control.TreeTableColumn.SortType.*;
import static org.junit.Assert.*;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeTableView.TreeTableViewSelectionModel;

/**
 * Additional sort tests
 */
public class TreeTableSortTest {

    @Test
    public void testNPEOnSortAfterRemove() {
        // stand-alone setup
        TreeTableView<String> treeTableView = new TreeTableView<>();
        TreeTableColumn<String, String> col = new TreeTableColumn<String, String>("column");
        col.setSortType(ASCENDING);
        col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<String>(param.getValue().getValue()));
        treeTableView.getColumns().add(col);
        
        TreeItem<String> root = new TreeItem<String>("root");
        root.setExpanded(true);
        root.getChildren().addAll(
                new TreeItem("Apple"),
                new TreeItem("Orange"),
                new TreeItem("Banana"));
        
        treeTableView.setRoot(root);
        // add expanded children
        root.getChildren().forEach(child -> {
            child.setExpanded(true);
            String value = child.getValue();
            for (int i = 1; i <= 3; i++) {
                child.getChildren().add(new TreeItem<>(value + i));
            }    
        });
        assertEquals("sanity", 13, treeTableView.getExpandedItemCount());
        TreeTableViewSelectionModel<String> selectionModel = treeTableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        selectionModel.selectIndices(2, 5, 8, 10);
        assertEquals(10, selectionModel.getSelectedIndex());
        TreeItem<String> lastRootChild = root.getChildren().get(2);
        assertEquals("sanity: selectedItem child of last", 
                lastRootChild, selectionModel.getSelectedItem().getParent());
        // replace children of last root child
//        List<TreeItem<String>> childrenOfLastRootChild = new ArrayList<>(lastRootChild.getChildren());
//        childrenOfLastRootChild.remove(0);
//        lastRootChild.getChildren().setAll(childrenOfLastRootChild);
////        treeTableView.sort();
        lastRootChild.getChildren().remove(0);
        treeTableView.getSortOrder().add(col);
    }
    
    @Test
    public void testNPEOnSortAfterSetAll() {
        // stand-alone setup
        TreeTableView<String> treeTableView = new TreeTableView<>();
        TreeTableColumn<String, String> col = new TreeTableColumn<String, String>("column");
        col.setSortType(ASCENDING);
        col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<String>(param.getValue().getValue()));
        treeTableView.getColumns().add(col);

        TreeItem<String> root = new TreeItem<String>("root");
        root.setExpanded(true);
        root.getChildren().addAll(
                new TreeItem("Apple"),
                new TreeItem("Orange"),
                new TreeItem("Banana"));

        treeTableView.setRoot(root);
        // add expanded children
        root.getChildren().forEach(child -> {
            child.setExpanded(true);
            String value = child.getValue();
            for (int i = 1; i <= 3; i++) {
                child.getChildren().add(new TreeItem<>(value + i));
            }    
        });
        assertEquals("sanity", 13, treeTableView.getExpandedItemCount());
        TreeTableViewSelectionModel<String> selectionModel = treeTableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        selectionModel.selectIndices(2, 5, 8, 10);
        assertEquals(10, selectionModel.getSelectedIndex());
        TreeItem<String> lastRootChild = root.getChildren().get(2);
        assertEquals("sanity: selectedItem child of last", 
                lastRootChild, selectionModel.getSelectedItem().getParent());
        // replace children of last root child
        List<TreeItem<String>> childrenOfLastRootChild = new ArrayList<>(lastRootChild.getChildren());
        childrenOfLastRootChild.remove(0);
        lastRootChild.getChildren().setAll(childrenOfLastRootChild);
//        treeTableView.sort();
        treeTableView.getSortOrder().add(col);
    }
///------------- setup
    
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

    @After
    public void cleanup() {
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }


    
}
