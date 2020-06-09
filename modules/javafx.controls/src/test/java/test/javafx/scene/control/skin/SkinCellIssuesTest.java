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
import org.junit.Ignore;
import org.junit.Test;

import com.sun.javafx.scene.control.behavior.AccordionBehavior;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.ButtonBehavior;
import com.sun.javafx.tk.Toolkit;

import static javafx.scene.control.ControlShim.*;
import static javafx.scene.control.skin.CellSkinShim.*;
import static javafx.scene.control.skin.ComboSkinShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.ListCellSkin;
import javafx.scene.control.skin.SpinnerSkin;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import test.com.sun.javafx.scene.control.infrastructure.KeyEventFirer;

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
    
    @Test
    public void testListCellFixedSizeNullSkin() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> listView = new ListView<>();
        cell.updateListView(listView);
        double fixed = 100;
        listView.setFixedCellSize(fixed);
        installDefaultSkin(cell);
        assertEquals(fixed, getFixedCellSize(cell), 1);
        ListCellSkin oldSkin = (ListCellSkin) cell.getSkin();
        cell.setSkin(null);
        // no internal skin cleanup needed, it's done after dispose
        // without listener, this test doesn't make sense and is wrong
        // must not access disposed skin!
        assertEquals("test error: must not access state of disposed skin", -1, getFixedCellSize(oldSkin), 1);
    }
    
    /**
     * Here we null the listView in the cell -> fixedSizeListener throws NPE
     * because listView is null (in cells skin)
     * 
     * skin -> updateListView -> modify fixedSize > null listView -> modify fixedSize 
     */
    @Test
    public void testNPEListCellFixedSizeListenerNull() {
        ListCell<Object> cell =  new ListCell<>();
        installDefaultSkin(cell);
        ListView<Object> listView = new ListView<>();
        cell.updateListView(listView);
        int fixed = 100;
        listView.setFixedCellSize(fixed);
        assertEquals("skin must update fixed cell size", fixed, getFixedCellSize(cell), 1);
        cell.updateListView(null);
        double replacedFixed = 200;
        // was: NPE because listener not removed (and code not guarded against null listView)
        listView.setFixedCellSize(replacedFixed);
        // was: incorrect test assumption - skin cleans up internal state 
        // think: cleanup or not? 
        // yes if the skin still lives (here), no if the skin is dead (after dispose)
        assertEquals("skin must cleanup size on null listView", -1, getFixedCellSize(cell), 1);
    }
    
    /**
     * Test replaced skin not listening to fixedCellSize after dispose.
     * 
     * replaceSkin -> modify fixedSize 
     * 
     * Works as expected before fix (listener registered with skin api, thus removed
     * on dispose) - need to guarantee that the fix behaves as well.
     */
    @Test
    public void testListCellFixedSizeReplaceSkinNotUpdatedInPreviousSkin() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> listView = new ListView<>();
        cell.updateListView(listView);
        installDefaultSkin(cell);
        ListCellSkin<?> oldSkin = (ListCellSkin<?>) replaceSkin(cell);
        double replaced = 200;
        listView.setFixedCellSize(replaced);
        // the skin might cleanup itself on dispose, important is that 
        // it does not longer change along with source property in listView
        // okay before fix, because the child listener is registered with skin api
        // oldish junit cannot handle notEquals, anyway ;)
//        assertNotEquals("test error - test must not access state of disposed skin", replaced, getFixedCellSize(oldSkin), 1);
//        assertNotEquals("fixed cell size not updated in old skin", replaced, getFixedCellSize(oldSkin), 1);
    }
    
    /**
     * Test that state is updated to value of replaced listView. Fails before.
     */
    @Test
    public void testFailsListCellFixedSizeOnReplaceListView() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> initialLV = new ListView<>();
        initialLV.setFixedCellSize(100);
        cell.updateListView(initialLV);
        installDefaultSkin(cell);
        cell.updateListView(new ListView<>());
        assertEquals("fixed cell set to value of new listView", 
                cell.getListView().getFixedCellSize(), 
                getFixedCellSize(cell), 1);
    }
    
    /**
     * Test that state is updated when changing value in new ListView. Fails before.
     */
    @Test
    public void testFailsListCellFixedSizeReplaceListViewOnChange() {
        ListCell<Object> cell =  new ListCell<>();
        cell.updateListView(new ListView<>());
        installDefaultSkin(cell);
        cell.updateListView(new ListView<>());
        cell.getListView().setFixedCellSize(300);
        assertEquals("fixed cell updated on change of value in new listView", 
                cell.getListView().getFixedCellSize(), 
                getFixedCellSize(cell), 1);
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
    public void testListCellFixedSizeReplaceListView() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> initialLV = new ListView<>();
        cell.updateListView(initialLV);
        installDefaultSkin(cell);
        cell.updateListView(new ListView<>());
        initialLV.setFixedCellSize(300);
        assertEquals("fixed cell updated in new", 
                cell.getListView().getFixedCellSize(), 
                getFixedCellSize(cell), 1);
    }
    
    /**
     * Was: NPE on null listView and modify old listView
     */
    @Test
    public void testNPEListCellNullListView() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> listView = new ListView<>();
        cell.updateListView(listView);
        installDefaultSkin(cell);
        cell.updateListView(null);
        // throws NPE in original
        listView.setFixedCellSize(100);
    }
    
    /**
     * Test skin listening to fixed size.
     */
    @Test
    public void testListCellChangedFixedSize() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> listView = new ListView<>();
        cell.updateListView(listView);
        installDefaultSkin(cell);
        double fixed = 100;
        listView.setFixedCellSize(fixed);
        assertEquals("fixed cell size updated in skin", fixed, getFixedCellSize(cell), 1);
    }
    
    /**
     * Test initial non-default fixed size.
     */
    @Test
    public void testListCellInitialFixedSize() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> listView = new ListView<>();
        double fixed = 100;
        listView.setFixedCellSize(fixed);
        cell.updateListView(listView);
        installDefaultSkin(cell);
        assertEquals("initial not-default cell size in skin", fixed, getFixedCellSize(cell), 1);
    }
    
    /**
     * REMOVE - we know that both are region.computesize
     * updateControl -> skin -> initial size
     */
    @Test
    public void testListCellInitialFixedSizeDefault() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> listView = new ListView<>();
        double fixed = listView.getFixedCellSize();
        cell.updateListView(listView);
        installDefaultSkin(cell);
        assertEquals("initial default fixed cell size in skin", fixed, getFixedCellSize(cell), 1);
    }
    
    
    @Test
    public void testListCellDefaultFixedSize() {
        ListCell<Object> cell =  new ListCell<>();
        installDefaultSkin(cell);
        assertEquals("sanity: default value on listCell skin", Region.USE_COMPUTED_SIZE, getFixedCellSize(cell), 1);
    }
    
    @Test
    public void testListViewDefaultFixedSize() {
        ListView<?> listView = new ListView<>();
        assertEquals("sanity: default value on listView", Region.USE_COMPUTED_SIZE, listView.getFixedCellSize(), 1);
    }
    
    /**
     * FIXME -- not really needed for core misbehavior (not memory leak at least,
     * but still listening to old fixedSize!)
     * Cell not leaking if had listView and replace skin.
     * 
     * skin -> update listView -> null listView -> replaceSkin
     */
    @Test 
    public void testListCellMemoryLeakWithListViewNullAgain() {
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
    public void testListCellMemoryLeakWithListView() {
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
    public void testListCellMemoryLeakListView() {
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
    public void testFailsListCellMemoryLeak() {
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
