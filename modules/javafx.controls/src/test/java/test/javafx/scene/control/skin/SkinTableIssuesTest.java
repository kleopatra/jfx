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
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.javafx.tk.Toolkit;

import static javafx.collections.FXCollections.*;
import static javafx.scene.control.ControlShim.*;
import static javafx.scene.control.skin.TableHeaderRowShim.*;
import static javafx.scene.control.skin.TableSkinShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 */
public class SkinTableIssuesTest {

    private Scene scene;
    private Stage stage;
    private Pane root;

    private static final boolean showPulse = false;
    private static final boolean methodPulse = true;

//------------- TableView

    
    
    /**
     * This fails in scrollHorizontally
     */
    @Test
    public void failScrollToColumn() {
        TableView<Locale> control = new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
        replaceSkin(control);
        control.scrollToColumn(column);
    }
    
    /**
     * This fails in rowcountListener of TableViewSkinBase
     */
    @Test
    public void failAddData() {
        TableView<Locale> control = new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
        replaceSkin(control);
        control.getItems().add(Locale.GERMAN);
    }
    
    /**
     * This fails in the listener installed by TableViewSkinBase
     * when updating the 
     */
    @Test
    public void failPlaceHolderAddMoreColumns() {
        TableView<Locale> control = new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
        replaceSkin(control);
        control.getColumns().addAll(new TableColumn("added"));
    }
    
    @Test
    public void failPlaceHolderAddColumns() {
        TableView<Locale> control = new TableView<>();
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
        replaceSkin(control);
        control.getColumns().addAll(new TableColumn("added"));
    }
    
    @Test
    public void testColumnHeaderStyleClass() {
        TableView<Locale> control = new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        //installDefaultSkin(control);
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
        TableColumnHeader header = getColumnHeaderFor(column);
        assertNotNull(header);
        dispose(header);
        String testStyle = "test-style";
        column.getStyleClass().add(testStyle);
        assertFalse(header.getStyleClass().contains(testStyle));
    }

    /**
     * Changing padding after skin replace throws NPE 
     * (if TableHeaderRow.updateTableWidth is fixed to _not_ guard against null skinnable)
     */
    @Test
    public void failTablePadding() {
        TableView<Locale> control =  new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        installDefaultSkin(control);
        replaceSkin(control);
        control.setPadding(new Insets(100, 100, 100, 100));
    }
    
    /**
     * how to trigger width change of table?
     * TableHeaderRow has listeners that must be removed, how to test without change?
     * 
     * doesn't throw but old is still listening? yes, it's listening but table width is never
     * updated, how to force it? region.setWidth is package .. 
     * 
     * headerRow.updateTableWidth guards against null skinnable - shouldn't if the skin
     * is disposed. For now, removed the guard.
     */
    @Test //@Ignore("FIXME - how to trigger actual resize?")
    public void failTableWidth() {
        TableView<Locale> control =  new TableView<>();
        // here: control in a layout doesnt force re-layout on resizing stage
        // in "normal" context: does resize
//        HBox root = new HBox(control);
        // setting the control as root
        scene.setRoot(control);
        stage.show();
        replaceSkin(control);
        stage.setWidth(1000);
        // need this pulse to force re-sizing of control
        fireMethodPulse();
    }
    
    @Test
    public void failColumnWidthNPE() {
        TableView<Locale> control =  new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().add(column);
        installDefaultSkin(control);
        replaceSkin(control);
        column.setPrefWidth(200);
    }
    /**
     * This here is the default test, default constructor. Fails before.
     *
     * FIXME: don't add to final push - here just to understand and reducing test time
     */
    @Test
    public void failTableViewMemoryLeak() {
        TableView<?> control =  new TableView<>();
        installDefaultSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(control));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }

//---------------- setup and initial

    protected void fireMethodPulse() {
        if (methodPulse) Toolkit.getToolkit().firePulse();
    }

    protected void showControl(Control box, boolean focus) {
        if (!root.getChildren().contains(box)) {
            root.getChildren().add(box);
        }
        stage.show();
        if (focus) {
            stage.requestFocus();
            box.requestFocus();
            assertTrue(box.isFocused());
            assertSame(box, scene.getFocusOwner());

        }
        if (showPulse) Toolkit.getToolkit().firePulse();
    }

    @After
    public void cleanup() {
        stage.hide();
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

        root = new VBox();
        scene = new Scene(root);
        stage = new Stage();
        stage.setScene(scene);
    }

}
