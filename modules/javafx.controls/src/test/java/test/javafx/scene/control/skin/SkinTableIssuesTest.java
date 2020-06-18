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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.javafx.tk.Toolkit;

import static javafx.scene.control.ControlShim.*;
import static javafx.scene.control.skin.TableHeaderRowShim.*;
import static javafx.scene.control.skin.TableSkinShim.*;
import static javafx.scene.control.skin.TableSkinShim.getColumnHeaderFor;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.layout.Pane;
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

//------------- TableViewSkin/Base

    /**
     * Test cleanup of scroll handler added by TableViewSkinBase
     */
    @Test
    public void failSkinScrollToColumn() {
        TableView<Locale> control = new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
        replaceSkin(control);
        // force layout pass before scrollTo - without getting a StackOverflow
        // because scrollHorizontally is waiting until header is layouted
        fireMethodPulse();
        control.scrollToColumn(column);
    }
    
    /**
     * Test cleanup of listener to items content (== rowCountListener) 
     * added by TableViewSkinBase
     */
    @Test
    public void failSkinAddItem() {
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
     * Test cleanup of listener to items property (== itemsChangeListener) 
     * added by TableViewSkinBase
     */
    @Test
    public void failSkinSetItems() {
        TableView<Locale> control = new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
        replaceSkin(control);
        control.setItems(FXCollections.observableArrayList(Locale.GERMAN));
    }
    
    /**
     * Test cleanup of listener to visibleLeafColumns added by TableViewSkinBase.
     */
    @Test
    public void failSkinPlaceHolderAddMoreColumns() {
        TableView<Locale> control = new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
        replaceSkin(control);
        control.getColumns().addAll(new TableColumn("added"));
    }
    
    /**
     * Test cleanup of listener to visibleLeafColumns added by TableViewSkinBase.
     */
    @Test
    public void failSkinPlaceHolderAddColumn() {
        TableView<Locale> control = new TableView<>();
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
        replaceSkin(control);
        control.getColumns().addAll(new TableColumn("added"));
    }
    
    /**
     * Test cleanup of listener to width property of columns added
     * by TableViewSkinBase.
     */
    @Test
    public void failSkinColumnWidth() {
        TableView<Locale> control =  new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().add(column);
        installDefaultSkin(control);
        replaceSkin(control);
        column.setPrefWidth(200);
    }

    /**
     * Changing resizePolicy updates columnn width, so this is just another
     * guard against cleanup of column width listeners. Different in failing only if
     * in active scenegraph.
     */
    @Test
    public void failSkinResizePolicy() {
        TableView<Locale> control = new TableView<>();
        control.getItems().add(Locale.GERMAN);
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        column.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        control.getColumns().addAll(column);
        showControl(control, true);
        replaceSkin(control);
        control.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }


//-------------- Nested/ColumnHeader    
    
    /**
     * columnHeader registers style listener on column which must be removed
     */
    @Test
    public void failColumnHeaderStyleClass() {
        TableView<Locale> control = new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        //installDefaultSkin(control);
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
        TableColumnHeader header = getColumnHeaderFor(column);
        assertNotNull(header);
        replaceSkin(control);
        // trying more fine grained dispose, should we?
//        dispose(header);
        String testStyle = "test-style";
        column.getStyleClass().add(testStyle);
        assertFalse(header.getStyleClass().contains(testStyle));
    }
    
    /**
     * Test cleanup of listener to sortorder of table added by TableColumnHeader.
     */
    @Test
    public void failColumnHeaderSortOrder() {
        TableView<Locale> control = new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        //installDefaultSkin(control);
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
        TableColumnHeader header = getColumnHeaderFor(column);
        assertNotNull(header);
        // PENDING: this is borderline - the header is no longer valid after dispose ..
        List<Change> changes = new ArrayList<>();
        header.getChildrenUnmodifiable().addListener((ListChangeListener)changes::add);
//        replaceSkin(control);
        // trying more fine grained dispose, should we?
        dispose(header);
        changes.clear();
        column.setSortType(SortType.DESCENDING);
        control.getSortOrder().add(column);
        assertEquals("column must not have added sort-related children", 0, changes.size());
    }
    
    @Test
    public void testColumnHeaderWithoutRowHeader() {
        TableView<Locale> control = new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        installDefaultSkin(control);
        // pathological but possible
        TableColumnHeader columnHeader = new TableColumnHeader(column);
        dispose(columnHeader);
    }
    /**
     * Sanity test of listener to sortorder of table: 
     *  adds sort-related children
     */
    @Test
    public void compareColumnHeaderSortOrder() {
        TableView<Locale> control = new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        //installDefaultSkin(control);
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
        TableColumnHeader header = getColumnHeaderFor(column);
        assertNotNull(header);
        List<Change> changes = new ArrayList<>();
        header.getChildrenUnmodifiable().addListener((ListChangeListener)changes::add);
        column.setSortType(SortType.DESCENDING);
        control.getSortOrder().add(column);
        assertFalse("must have added sort-related children", changes.isEmpty());
    }

    /**
     * in visual test, we get NPE in rebuildDragRects - to reproduce, we ignore
     * the one from TableViewSkinBase here .. ??
     * 
     * This fails if TableHeaderRow doesn't cleanup its visibleLeafColumnsListener 
     * (which calls headersNeedsUpdate recursively in the nestedColumnHeader)
     */
    @Test
    public void failNestedColumnHeaderUpdate() {
        TableView<Locale> control = new TableView<>();
        control.getItems().add(Locale.GERMAN);
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        column.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        control.getColumns().addAll(column);
        //installDefaultSkin(control);
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
//        fireMethodPulse();
        replaceSkin(control);
        try {
            control.getColumns().add(new TableColumn<>("added"));
            
        } catch (NullPointerException npe) {
            StackTraceElement element = npe.getStackTrace()[0];
            if (!element.getClassName().contains("TableViewSkinBase")) {
                // rethrow if its not from the known issues in TableViewSkinBase
                throw npe;
            }
        }
        root.layout();
    }
    
    /**
     * listener in NestedColumnHeader is added via registerListener -> removed on dispose
     * (except that nobody calls header dispose? ..) But get it in visual test (both the NPE
     * from skin's width listener and the NPE from updateDragRects)
     * 
     * The latter is triggered from the listener to resizePolicy which is added (via 
     * changeListener api) in setTableHeaderRow of NestedColumnHeader.
     */
    @Test
    public void failNestedColumnHeaderResizePolicy() {
        TableView<Locale> control = new TableView<>();
        control.getItems().add(Locale.GERMAN);
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        column.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        control.getColumns().addAll(column);
        //installDefaultSkin(control);
        // have to build a scenegraph before accessing headers 
        // (there's some lazyness in setup, forgot where exactly)
        showControl(control, true);
        fireMethodPulse();
        replaceSkin(control);
        try {
            control.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        } catch (NullPointerException npe) {
            StackTraceElement element = npe.getStackTrace()[0];
            if (!element.getClassName().contains("TableViewSkinBase")) {
                // rethrow if its not from the known issues in TableViewSkinBase
                throw npe;
            }
        }
        fireMethodPulse();
        root.layout();
    }
    
    
//----------- tableHeaderRow
    
    /**
     * Test items in contextMenu cleared on dispose and not re-filled on adding columns.
     */
    @Test
    public void testHeaderRowContextMenu() {
        TableView<Locale> control =  new TableView<>();
        control.setTableMenuButtonVisible(true);
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        installDefaultSkin(control);
        TableHeaderRow headerRow = getTableHeaderRow(control);
        ContextMenu menu = getColumnPopupMenu(headerRow);
        assertEquals(1, menu.getItems().size());
        replaceSkin(control);
        assertEquals(0, menu.getItems().size());
        control.getColumns().add(new TableColumn("added"));
        assertEquals(0, menu.getItems().size());
    }
    
    @Test
    public void failHeaderRowColumnText() {
        TableView<Locale> control =  new TableView<>();
        control.setTableMenuButtonVisible(true);
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
//        installDefaultSkin(control);
        showControl(control, true);
        replaceSkin(control);
        column.setText("updated");
    }
    
    @Test
    public void failHeaderRowColumnHide() {
        TableView<Locale> control =  new TableView<>();
        control.setTableMenuButtonVisible(true);
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        installDefaultSkin(control);
        replaceSkin(control);
        column.setVisible(false);
    }
    
    @Test
    public void compareHeaderRowColumnHide() {
        TableView<Locale> control =  new TableView<>();
        control.setTableMenuButtonVisible(true);
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        installDefaultSkin(control);
        TableHeaderRow headerRow = getTableHeaderRow(control);
        CheckMenuItem check = getMenuItemFor(headerRow, column);
        assertEquals(column.isVisible(), check.isSelected());
        column.setVisible(false);
        assertEquals(column.isVisible(), check.isSelected());
    }
    
    /**
     * Test cleanup of listener to padding property added by TableHeaderRow.
     * 
     * Changing padding after skin replace throws NPE 
     * (if TableHeaderRow.updateTableWidth is fixed to _not_ guard against null skinnable)
     */
    @Test
    public void failHeaderRowPadding() {
        TableView<Locale> control =  new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        installDefaultSkin(control);
        replaceSkin(control);
        control.setPadding(new Insets(100, 100, 100, 100));
    }
    
    /**
     * Test cleanup of listener to width property of table added by TableHeaderRow.
     * 
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
    public void failHeaderRowWidth() {
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
    
    /**
     * Dispose: clear children doesn't help - naturally, still have a strong
     * ref from control to skin
     */
    @Test
    public void failTableViewNullMemoryLeak() {
        TableView<?> control =  new TableView<>();
        installDefaultSkin(control);
        WeakReference<Skin<?>> weakRef = new WeakReference<>(control.getSkin());
        assertNotNull(weakRef.get());
//        weakRef.get().dispose();
        control.setSkin(null);
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }
    
    /**
     * Dispose: clear children doesn't help - naturally, still have a strong
     * ref from control to skin
     */
    @Test
    public void failTableViewDisposeMemoryLeak() {
        WeakReference<TableView<?>> weakControl =  new WeakReference<>(new TableView<>());
        installDefaultSkin(weakControl.get());
        WeakReference<Skin<?>> weakRef = new WeakReference<>(weakControl.get().getSkin());
        assertNotNull(weakRef.get());
        weakRef.get().dispose();
//        control.setSkin(null);
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
