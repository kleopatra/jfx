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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.javafx.tk.Toolkit;

import static javafx.scene.control.ControlShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 */
public class SkinCellIssuesTest {

    private Scene scene;
    private Stage stage;
    private Pane root;

    private static final boolean showPulse = false;
    private static final boolean methodPulse = true;

//------------- ListCell
// note: core ListCellSkin doesn't re-wire path property on changing listView
// for max failures make sure to install the listView before skin

    /**
     * Test that min/max/pref height respect fixedCellSize.
     * Sanity test when fixing JDK-8246745.
     */
    @Test
    public void testListCellHeights() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> listView = new ListView<>();
        cell.updateListView(listView);
        installDefaultSkin(cell);
        listView.setFixedCellSize(100);
        assertEquals("pref height must be fixedCellSize",
                listView.getFixedCellSize(),
                cell.prefHeight(-1), 1);
        assertEquals("min height must be fixedCellSize",
                listView.getFixedCellSize(),
                cell.minHeight(-1), 1);
        assertEquals("max height must be fixedCellSize",
                listView.getFixedCellSize(),
                cell.maxHeight(-1), 1);
    }


    /**
     * Here we null the listView in the cell -> fixedSizeListener throws NPE
     * because listView is null (in cells skin)
     *
     * skin -> listView -> null
     */
    @Test
    public void failListCellSkinWithListViewNullListView() {
        ListCell<Object> cell =  new ListCell<>();
        installDefaultSkin(cell);
        ListView<Object> listView = new ListView<>();
        cell.updateListView(listView);
        cell.updateListView(null);
        // was: NPE because listener not removed (and code not guarded against null listView)
        listView.setFixedCellSize(100);
    }

    /**
     * Was: NPE on null listView and modify old listView
     *
     * listView -> skin -> null
     */
    @Test
    public void failListCellWithListViewSkinNullListView() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> listView = new ListView<>();
        cell.updateListView(listView);
        installDefaultSkin(cell);
        cell.updateListView(null);
        // throws NPE in original
        listView.setFixedCellSize(100);
    }

    /**
     * Test that state is updated to value of replaced listView. Fails before.
     *
     * listView -> skin -> replace listView
     */
    @Test
    public void failListCellPrefHeightOnReplaceListView() {
        ListCell<Object> cell =  new ListCell<>();
        cell.updateListView(new ListView<>());
        installDefaultSkin(cell);
        ListView<Object> listView = new ListView<>();
        listView.setFixedCellSize(100);
        cell.updateListView(listView);
        assertEquals("fixed cell set to value of new listView",
                cell.getListView().getFixedCellSize(),
                cell.prefHeight(-1), 1);
    }

    /**
     * Test that state is updated when changing value in new ListView. Fails before.
     *
     * listView -> skin -> replace listView -> change fixedSize on replaced
     * always failing in pair with the test above, changing before/after update
     * doesn't make a difference?
     */
    @Test
    public void failListCellPrefHeightReplaceListViewOnChange() {
        ListCell<Object> cell =  new ListCell<>();
        cell.updateListView(new ListView<>());
        installDefaultSkin(cell);
        cell.updateListView(new ListView<>());
        cell.getListView().setFixedCellSize(300);
        assertEquals("pref height respects fixedCellSize new listView",
                cell.getListView().getFixedCellSize(),
                cell.prefHeight(-1), 1);
    }

    /**
     * Test that state is not updated on replacing listView and changing
     * property on old. Passes accidentally.
     *
     * Note: this passes accidentally before the fix - the skin is
     * listening to changes of the property on first listView and updates itself
     * to state of the property on current listView!
     *
     *  FIXME: any way to write a test such that it fails before and passes after?
     */
    @Test
    public void removeListCellPrefHeightReplaceListView() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> initialLV = new ListView<>();
        cell.updateListView(initialLV);
        installDefaultSkin(cell);
        ListView<Object> listView = new ListView<>();
        listView.setFixedCellSize(100);
        cell.updateListView(listView);
        initialLV.setFixedCellSize(300);
        assertEquals("fixed cell updated in new",
                cell.getListView().getFixedCellSize(),
                cell.prefHeight(-1), 1);
    }

    /**
     * Test skin listening to fixed size.
     */
    @Test
    public void testListCellPrefHeightFixedSize() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> listView = new ListView<>();
        cell.updateListView(listView);
        installDefaultSkin(cell);
        listView.setFixedCellSize(100);
        assertEquals("fixed cell size updated in skin",
                listView.getFixedCellSize(),
                cell.prefHeight(-1), 1);
    }

    /**
     * Test initial non-default fixed size.
     */
    @Test
    public void testListCellPrefHeightInitialFixedSize() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> listView = new ListView<>();
        listView.setFixedCellSize(100);
        cell.updateListView(listView);
        installDefaultSkin(cell);
        assertEquals("initial not-default cell size in skin",
                listView.getFixedCellSize(),
                cell.prefHeight(-1), 1);
    }

    /**
     * FIXME -- not really needed for core misbehavior (not memory leak at least,
     * but still listening to old fixedSize!)
     * Cell not leaking if had listView and replace skin.
     *
     * skin -> update listView -> null listView -> replaceSkin
     */
    @Test
    public void removeListCellMemoryLeakWithListViewNullAgain() {
        ListCell<Object> cell =  new ListCell<>();
        installDefaultSkin(cell);
        ListView<Object> first = new ListView<>();
        cell.updateListView(first);
        cell.updateListView(null);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(cell));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }

    /**
     * do we expect a difference if listView added before/after skin?
     * core skin listener to listView only registered if null initially!
     *
     * Cell not leaking if had listView and replace skin.
     *
     * skin -> update listView -> replaceSkin
     */
    @Test
    public void removeListCellMemoryLeakWithListView() {
        ListCell<Object> cell =  new ListCell<>();
        installDefaultSkin(cell);
        ListView<Object> lv = new ListView<>();
        cell.updateListView(lv);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(cell));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }

    /**
     * This can be added to skinMemoryLeakTest. Does not fail before. Passing accidentally, no listener added.
     *
     * FIXME: don't add to final push - here just to understand and reducing test time
     * Cell not leaking if had listView and replace skin. That's because the listener
     * is a once-only, then removed
     *
     * (cell with lv) skin -> replaceSkin
     */
    @Test
    public void removeListCellMemoryLeakListView() {
        ListCell<Object> cell =  new ListCell<>();
        cell.updateListView(new ListView<>());
        installDefaultSkin(cell);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(cell));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }


    /**
     * This here is the default test, default constructor. Fails before.
     *
     * FIXME: don't add to final push - here just to understand and reducing test time
     * Cell skin is leaking if has listView
     *
     * (cell without lv) skin -> replaceSkin
     */
    @Test
    public void failListCellMemoryLeak() {
        ListCell<?> cell =  new ListCell<>();
        installDefaultSkin(cell);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(cell));
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
