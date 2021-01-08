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
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static javafx.scene.control.ControlShim.*;
import static javafx.scene.control.skin.TabPaneSkinShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Review: https://bugs.openjdk.java.net/browse/JDK-8242621
 * https://github.com/openjdk/jfx/pull/318
 */
public class SkinTabPaneIssuesTest {
    private Scene scene;
    private Stage stage;
    private Pane root;

//---------- 
    
    
    
//----------- looking for side-effects
    
    /**
     * this passes before, fails after the fix
     * reason is a lost getChildren().remove(contentArea) in skin's removeTab(tab)
     */
    @Test
    public void testRemoveTab() {
        TabPane control = createTabPane();
        installDefaultSkin(control);
        int childCount = control.getChildrenUnmodifiable().size();
        assertEquals("child count: headerArea plus one for each tab", control.getTabs().size() + 1 , childCount);
        control.getTabs().remove(0);
        assertEquals("child count decreased by one", childCount -1, control.getChildrenUnmodifiable().size());
    }
    
    @Test
    public void testChildCount() {
        TabPane control = createTabPane();
        installDefaultSkin(control);
        int childCount = control.getChildrenUnmodifiable().size();
        replaceSkin(control);
        assertEquals("childcount must be unchanged", childCount, control.getChildrenUnmodifiable().size());
    }
    
    /**
     */
    @Test
    public void testChangeTabContent() {
        TabPane control = createTabPane();
        installDefaultSkin(control);
        replaceSkin(control);
        control.getTabs().get(0).setContent(new Label("new tab content"));
    }
    
    /**
     */
    @Test
    public void testChangeTabText() {
        TabPane control = createTabPane();
        installDefaultSkin(control);
        replaceSkin(control);
        control.getTabs().get(0).setText("replaced title");
    }
    
    /**
     */
    @Test
    public void testPopupMenuChangeTabText() {
        TabPane control = createTabPane();
        installDefaultSkin(control);
//        replaceSkin(control);
        Tab first = control.getTabs().get(0);
        ContextMenu contextMenu = getTabsMenu(control);
        assertEquals(first.getText(), contextMenu.getItems().get(0).getText());
        first.setText("replaced title");
        assertEquals(first.getText(), contextMenu.getItems().get(0).getText());
    }
    
    /**
     * side-effect on permutate: NPE in setupPopupMenu
     */
    @Test
    public void testPopupMenu() {
        TabPane control = createTabPane();
        installDefaultSkin(control);
        Tab tab = new Tab("replaced tab", new Label("replaced tab content"));
        control.getTabs().add(0, tab);
        ContextMenu contextMenu = getTabsMenu(control);
        assertEquals(control.getTabs().size(), contextMenu.getItems().size());
    }
    
    /**
     * side-effect on permutate: NPE in setupPopupMenu
     */
    @Test
    public void testModifyTabsSetAll() {
        TabPane control = createTabPane();
        installDefaultSkin(control);
        replaceSkin(control);
        List<Tab> tabs = control.getTabs();
        control.getTabs().setAll(tabs.get(2), tabs.get(1), tabs.get(0));
    }
    
    /**
     * side-effect on add/remove tabs: NPE in setupPopupMenu
     */
    @Test
    public void testModifyTabsAdd() {
        TabPane control = createTabPane();
        installDefaultSkin(control);
        replaceSkin(control);
        control.getTabs().add(new Tab("some"));
    }
    
    @Test
    public void testModifyTabsRemove() {
        TabPane control = createTabPane();
        installDefaultSkin(control);
        replaceSkin(control);
        control.getTabs().remove(0);
    }
    
//-------------- testing under which conditions a child causes a memory leak   
    public static class ListViewSkinX<T> extends ListViewSkin<T> {

        /**
         * @param control
         */
        public ListViewSkinX(ListView<T> control) {
            super(control);
            InnerPane myPane = new InnerPane();
            myPane.getStyleClass().add("inner-pane");
            getChildren().add(myPane);
            StaticPane staticPane = new StaticPane();
            staticPane.getStyleClass().add("static-pane");
            getChildren().add(staticPane);
        }
        
        @Override
        public void dispose() {
            if (getSkinnable() == null) return;
            Node myChild = getSkinnable().lookup(".inner-pane");
            // inner class: must be removed
            getChildren().remove(myChild);
            // static class: no memory leak (other than piling up)
            super.dispose();
        }
        
        private class InnerPane extends StackPane {
        }
        
        private static class StaticPane extends StackPane {
        }

    }
    
    @Test
    public void testInnerSkinClass() {
        ListView<String> listView = new ListView<>();
        listView.setSkin(new ListViewSkinX<>(listView));
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(listView));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
        
    }
    @Test
    public void testCellFactory() {
        ListView<String> listView = new ListView<>();
        listView.setCellFactory(cc -> {
            ListCell<String> c = new ListCell<>() {

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                }
                
            };
            return c;
        });
        installDefaultSkin(listView);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(listView));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }

//-------------- end: general   
    
    /**
     * tabPane with tabs
     * default skin -> set alternative
     */
    @Test
    public void failedMemoryLeakAlternativeSkinWithTabs() {
        TabPane control = createTabPane();
        installDefaultSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(control));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }
    
    /**
     * empty tabPane - covered in SkinMemoryLeakTest
     * default skin -> set alternative
     */
    @Test
    public void failedMemoryLeakAlternativeSkin() {
        TabPane control = new TabPane();
        installDefaultSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(control));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }

    //-------------------- setup

    protected TabPane createTabPane() {
        return createTabPane(3);
    }

    protected TabPane createTabPane(int max) {
        TabPane tabPane = new TabPane();
        for (int i = 0; i < max; i++) {
            Tab tab = new Tab("Tab " + i, new Label("Content for " + i));
            tabPane.getTabs().add(tab);
        }
        return tabPane;
    }

    
    /**
     * Ensures the control is shown in an active scenegraph. Requests
     * focus on the control if focused == true.
     *
     * @param control the control to show
     * @param focused if true, requests focus on the added control
     */
    protected void showControl(Control control, boolean focused) {
        if (root == null) {
            root = new VBox();
            scene = new Scene(root);
            stage = new Stage();
            stage.setScene(scene);
        }
        if (!root.getChildren().contains(control)) {
            root.getChildren().add(control);
        }
        stage.show();
        if (focused) {
            stage.requestFocus();
            control.requestFocus();
            assertTrue(control.isFocused());
            assertSame(control, scene.getFocusOwner());
        }
    }

    
    @After
    public void cleanup() {
        if (stage != null) stage.hide();
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
