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
    @Test
    public void testTableHeaderRow() {
        WeakReference<TableViewSkin> weakSkin = new WeakReference<>(new TableViewSkin<>(new TableView<>()));
        WeakReference<TableHeaderRow> weakHeader = new WeakReference<>(getTableHeaderRow(weakSkin.get()));
        weakSkin.get().dispose();
        attemptGC(weakSkin);
        assertEquals("skin must be gc'ed", null, weakSkin.get());
        assertEquals("header must be gc'ed", null, weakHeader.get());
//        assertNull(getTableHeaderRow(skin));
    }
    /**
     * Any way to isolate the dangling references?
     */
    @Test
    public void testTableHeaderRowMemoryLeak() {
        TableView<Locale> control =  new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        installDefaultSkin(control);
        //? wrong test assumption: there's still a strong reference from skin ..
        WeakReference<TableHeaderRow> weakHeader = new WeakReference(getTableHeaderRow(control));
        assertNotNull(weakHeader.get());
        WeakReference<?> weakSkin = new WeakReference(replaceSkin(control));
        
        dispose(weakHeader.get());
        attemptGC(weakHeader);
        assertEquals("header must be gc'ed", null, weakHeader.get());
    }
    
    /**
     * Changing padding after skin replace throws NPE 
     * (if TableHeaderRow.updateTableWidth is fixed to _not_ guard against null skinnable)
     */
    @Test
    public void testTablePadding() {
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
     */
    @Test @Ignore("FIXME - how to trigger actual resize?")
    public void testTableWidth() {
        ObservableList<Locale> data = observableArrayList(Locale.getAvailableLocales());
        TableView<Locale> control =  new TableView<>(data);
        TableColumn<Locale, String> column = new TableColumn<>("name");
        column.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        control.getColumns().addAll(column);
        control.setMaxWidth(Region.USE_PREF_SIZE);
//        BorderPane root = new BorderPane(control);
        
//        HBox root = new HBox(control);
        VBox root = new VBox(10, control);
//        StackPane 
        scene.setRoot(root);
        stage.show();
//        showControl(control, true);
        fireMethodPulse();
        control.widthProperty().addListener((e, ov, nv) -> System.out.println("getting width change"));
        root.widthProperty().addListener(e -> 
            System.out.println("getting width change from root: " + root.getWidth() + " table: " + control.getWidth()));
//        replaceSkin(control);
//        stage.setWidth(1000);
        System.out.println("stage width set, before pulse");
        fireMethodPulse();
        System.out.println("stage width set, after pulse");
//        column.setPrefWidth(500);
        control.setPrefWidth(500);
        root.requestLayout();
        System.out.println("control width set, before pulse");
        fireMethodPulse();
        System.out.println("control width set, after pulse " + control.getWidth());
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
