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

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.javafx.tk.Toolkit;

import static javafx.scene.control.skin.TabPaneSkinShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.TabPaneSkinTestUtils.*;

import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.control.skin.TabPaneSkinShim;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Tests around keeping the selected tab in visible range of tab header area.
 * Note: this is ununsed in fix branch - successor is TabPaneHeaderScrollTest.
 * Keep for additional tests (after verifying that here is nothing that is also in
 * the real test)
 */
public class TabPaneScrollHeaderTest {

    private static final int TAB_COUNT = 30;
    private static final int THIRD_OF = TAB_COUNT / 3;
    
    private Scene scene;
    private Stage stage;
    private Pane root;
    private TabPane tabPane;

    
//-------------- test scroll update on resize
    /**
     * Test scroll on changing tabPane width (by changing stage width).
     * This passes with and without the fix.
     * 
     * Changing stage with in tests is unreliable, sometimes throws a
     * burst of exceptions in scenePulseLogger (or so).
     * 
     * DONT!
     */
    @Test
    public void testStageWidth() {
        TabPane tabPane = createTabPane(7);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
        showTabPane(tabPane);
        double scrollOffset = getHeaderAreaScrollOffset(tabPane);
        assertEquals(0, scrollOffset, 1);
        Node header = getSelectedTabHeader(tabPane);
        double minX = header.getBoundsInParent().getMinX();
        double tabOffset = getTabHeaderOffset(tabPane, tabPane.getSelectionModel().getSelectedItem());
        assertEquals("bounds minX", tabOffset, minX, 1);
        root.widthProperty().addListener((src, ov, nv) -> {
            Toolkit.getToolkit().firePulse();
        });
        tabPane.widthProperty().addListener((src, ov, nv) -> {
            Toolkit.getToolkit().firePulse();
            double scrOff = getHeaderAreaScrollOffset(tabPane);
            System.out.println(scrOff + " minX " + header.getBoundsInParent().getMinX() + " tabOffs " + tabOffset);
            assertTrue(scrOff < 0);
            assertFalse(isTabsFit(tabPane));
            // always passing, doesn't matter whether control width change triggers re-validate scrollOffset or not
            assertEquals(tabOffset, - scrOff + header.getBoundsInParent().getMinX(), 0);
        });
        double stageWidth = stage.getWidth();
        stage.setWidth(stageWidth / 2);
        Toolkit.getToolkit().firePulse();
    }
    
//------------- setup and initial state testing
    
    protected void showTabPane() {
        showTabPane(tabPane);
    }
    
    /**
     * Ensures the control is shown in an active scenegraph. Requests focus on
     * the control if focused == true.
     *
     * @param control the control to show
     * @param focused if true, requests focus on the added control
     */
    protected void showTabPane(TabPane tabPane) {
        if (root == null) {
            root = 
//                    new StackPane(); 
            new VBox();
//            new BorderPane();        
            // need to bound the size here, otherwise it's just as big as needed
            scene = new Scene(root, 600, 600);
            stage = new Stage();
            stage.setScene(scene);
            stage.show();
        }
        // for certain layouts (hbox, vbox, pane),
        // need single child, otherwise the outcome might depend on layout
//        root.setCenter(tabPane);
        root.getChildren().setAll(tabPane);
        // fire to really attach the skin
        // needed if the hierarchy is changed after showing the stage
        Toolkit.getToolkit().firePulse();
        TabPaneSkinShim.disableAnimations((TabPaneSkin) tabPane.getSkin());
    }
    
//----------------- setup and initial
    
    
    @Test
    public void testShowAlternativeTabPane() {
        // show default tabPane
        showTabPane();
        List<Node> expected = List.of(tabPane);
        assertEquals(expected, root.getChildren());
        // show alternative
        TabPane alternativeTabPane = createTabPane();
        showTabPane(alternativeTabPane);
        List<Node> alternative = List.of(alternativeTabPane);
        assertEquals(alternative, root.getChildren());
    }
    
    @Test
    public void testShowSingleTabPane() {
        TabPane tabPane = createTabPane();
        showTabPane(tabPane);
        List<Node> expected = List.of(tabPane);
        assertEquals(expected, root.getChildren());
    }
    
    @Test
    public void testShowTabPane() {
        assertNotNull(tabPane);
        assertSame(Side.TOP, tabPane.getSide());
        showTabPane();
        List<Node> expected = List.of(tabPane);
        assertEquals(expected, root.getChildren());
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
        tabPane = createTabPane();
    }

    protected TabPane createTabPane() {
        return createTabPane(TAB_COUNT);
    }
    
    protected TabPane createTabPane(int max) {
        TabPane tabPane = new TabPane();
        for (int i = 0; i < max; i++) {
            Tab tab = new Tab("Tab " + i, new Label("Content for " + i));
            tabPane.getTabs().add(tab);
        }
        return tabPane;
    }

    @After
    public void cleanup() {
        if (stage != null) stage.hide();
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }


}
