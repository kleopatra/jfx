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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import test.com.sun.javafx.scene.control.infrastructure.KeyEventFirer;

/**
 * Contains tests for issues that turned up in SkinDisposeTest (and
 * are unrelated contract violation of dispose).
 * 
 * MenuBarSkin: throws IOOB if focussing empty bar - 
 *     issue is caused by select(0) without range check
 * DatePickerSkin/ComboBoxSkin: throws NPE in listener to control's focusedProperty when 
 *    hiding stage after dispose/setSkin
 *    issue is not removing the manually registered listener in dispose
 *    
 * These three are combined in this test because getting a failing/passing test before/after
 * the fix requires a redirected uncaughtExceptionHandler. 
 * 
 * TextAreaSkin: throws UnsupportedException in dispose - 
 *     issue is incredible ..
 * can be moved elsewhere? done - moved into TextAreaTest
 */
public class SkinIssuesTest {

    private Scene scene;
    private Stage stage;
    private Pane root;

    private static final boolean showPulse = false; 
    private static final boolean methodPulse = true; 
    
//------------- cells
    
    @Test
    public void testListCellFixedSizeNullSkin() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> first = new ListView<>();
        assertEquals("sanity: default fixed cell size", -1, first.getFixedCellSize(), 1);
        cell.updateListView(first);
        int firstFixed = 100;
        first.setFixedCellSize(firstFixed);
        assertEquals("sanity", firstFixed, first.getFixedCellSize(), 1);
        installDefaultSkin(cell);
        assertEquals(firstFixed, getFixedCellSize(cell), 1);
        ListCellSkin oldSkin = (ListCellSkin) cell.getSkin();
        cell.setSkin(null);
        assertEquals(-1, getFixedCellSize(oldSkin), 1);
    }
    @Test
    public void testListCellInitialFixedSize() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> first = new ListView<>();
        assertEquals("sanity: default fixed cell size", -1, first.getFixedCellSize(), 1);
        cell.updateListView(first);
        int firstFixed = 100;
        first.setFixedCellSize(firstFixed);
        assertEquals("sanity", firstFixed, first.getFixedCellSize(), 1);
        installDefaultSkin(cell);
        assertEquals(firstFixed, getFixedCellSize(cell), 1);
    }
    
    @Test
    public void testListCellInitialFixedSizeDefault() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> first = new ListView<>();
        assertEquals("sanity: default fixed cell size", -1, first.getFixedCellSize(), 1);
        cell.updateListView(first);
        installDefaultSkin(cell);
        assertEquals("initial default fixed cell size in skin", -1, getFixedCellSize(cell), 1);
    }
    /**
     * Here we null the listView in the cell -> fixedSizeListener throws NPE
     * because listView is null (in cells skin)
     * 
     * skin -> updateListView -> modify fixedSize > null listView -> modify fixedSize 
     */
    @Test
    public void testListCellFixedSizeListenerNull() {
        ListCell<Object> cell =  new ListCell<>();
        installDefaultSkin(cell);
        ListView<Object> first = new ListView<>();
        cell.updateListView(first);
        int firstFixed = 100;
        first.setFixedCellSize(firstFixed);
        assertEquals(firstFixed, getFixedCellSize(cell), 1);
        cell.updateListView(null);
        double replacedFixed = 200;
        // NPE because listener not removed (and code not guarded against null listView)
        first.setFixedCellSize(replacedFixed);
        // was: incorrect test assumption - skin cleans up internal state 
        // think: cleanup or not? does it matter?
        assertEquals(-1, getFixedCellSize(cell), 1);
    }
    
    /**
     * Test listener to fixedCellSize.
     * here we replace the skin: the fixedSizeListener is removed in dispose.
     * 
     * skin -> updateListView -> modify fixedSize > replaceSkin -> modify fixedSize 
     * 
     */
    @Test
    public void testListCellFixedSizeListenerReplaceSkin() {
        ListCell<Object> cell =  new ListCell<>();
        installDefaultSkin(cell);
        ListView<Object> first = new ListView<>();
        cell.updateListView(first);
        int firstFixed = 100;
        first.setFixedCellSize(firstFixed);
        assertEquals(firstFixed, getFixedCellSize(cell), 1);
        ListCellSkin oldSkin = (ListCellSkin) replaceSkin(cell);
        double replacedFixed = 200;
        first.setFixedCellSize(replacedFixed);
        assertEquals("fixedCellSize in oldSkin must be unchanged", firstFixed, getFixedCellSize(oldSkin), 1);
    }
    
    /**
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
     * FIXME: don't add to final push - here just to understand and reducing test time
     * Cell not leaking if had listView and replace skin.
     * 
     * (cell with lv) skin -> replaceSkin
     */
    @Test 
    public void testListCellMemoryLeakInitialListView() {
        ListCell<Object> cell =  new ListCell<>();
        ListView<Object> lv = new ListView<>();
        cell.updateListView(lv);
        installDefaultSkin(cell);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(cell));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }
    
    
    /**
     * FIXME: don't add to final push - here just to understand and reducing test time
     * Cell not leaking if had listView and replace skin.
     * 
     * (cell without lv) skin -> replaceSkin
     */
    @Test 
    public void testListCellMemoryLeak() {
        ListCell<?> cell =  new ListCell<>();
        installDefaultSkin(cell);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(cell));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }


    
// ------------ combos
    
    @Test
    public void testComboBoxAddItems() {
        ComboBox<String> box = new ComboBox<>();
        installDefaultSkin(box);
        replaceSkin(box);
        box.getItems().add("added");
    }
    
    @Test
    public void testComboBoxSetItems() {
        ComboBox<String> box = new ComboBox<>();
        installDefaultSkin(box);
        replaceSkin(box);
        box.setItems(FXCollections.observableArrayList("one", "other"));
    }
    
    /**
     * guard against inconsistent state when nulling scene
     */
    @Test
    public void testComboBoxHideOnRemove() {
        ComboBox<String> box = new ComboBox<>();
        box.setItems(FXCollections.observableArrayList("one", "other"));
        showControl(box, true);
        box.show();
        assertTrue(box.isShowing());
        root.getChildren().remove(box);
        assertFalse(box.isShowing());
    }
    
    @Test
    public void testComboBoxSelectedItemListener() {
        ComboBox<String> box = new ComboBox<>();
        box.setItems(FXCollections.observableArrayList("one", "other"));
        showControl(box, true);
        int index = 1;
        box.getSelectionModel().select(box.getItems().get(index));
        ListView<?> listView = getListView(box);
        assertEquals(index, listView.getSelectionModel().getSelectedIndex());
    }
    
    /**
     * Borderline test:  switch skin while popup is open -> NPE on hiding the old popup
     * It's a marker: ComboBoxPopupControl registers an eventHandler to its windows
     * onHidden .. 
     * 
     * Think, though: explicit hide in dispose? here seems to be okay, but not 
     * in visual test
     */
    @Test
    public void testComboBoxSwitchSkinOpenPopup() {
        ComboBox<String> box = new ComboBox<>();
        box.setItems(FXCollections.observableArrayList("one", "other"));
        showControl(box, true);
        box.show();
        PopupControl oldPopup = getPopup(box);
        replaceSkin(box);
        fireMethodPulse();
        assertFalse("old popup must be hidden", oldPopup.isShowing());
        assertTrue("replaced popup must be showing ", getPopup(box).isShowing());
    }
    
    /**
     * rewired listener to listView's selectedIndex
     */
    @Test
    public void testComboBoxValueOnListSelection() {
        ComboBox<String> box = new ComboBox<>();
        box.setItems(FXCollections.observableArrayList("one", "other"));
        installDefaultSkin(box);
        ListView<?> listView = getListView(box);
        listView.getSelectionModel().select(1);
        assertEquals(box.getItems().get(1), box.getValue());
    }
    
    /**
     * ComboPopupControl registers layout listener to combo that's
     * never removed -  might produce leak and NPE? No, happens in itemsListener
     * this is example from 
     * https://bugs.openjdk.java.net/browse/JDK-8115587 (was: RT-21207)
     */
    @Test
    public void testComboBoxLayoutListener() {
        ComboBox<String> cb = new ComboBox<>();
//        cb.getItems().add("" + System.currentTimeMillis()+ "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
//        cb.setMaxWidth(100);
//        cb.setMinWidth(100);
        cb.setOnShowing(e -> 
                cb.getItems().setAll("" + System.currentTimeMillis()+ "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
        );
        
//        cb.setEditable(true);
//
//        cb.setPromptText("X");
        showControl(cb, true);
        replaceSkin(cb);
        cb.show();
        
    }
    
    /**
     * Test for memory leak, just during evaluation - remove!
     */
    @Test @Ignore("combobox memory leak")
    public void testComboBoxMemoryLeak() {
        ComboBox<?> control =  new ComboBox<>();
        installDefaultSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(control));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }

    
//-------------- listView    
    @Test
    public void testListViewSelectUp() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two", "three", "four");
        ListView<String> listView = new ListView<>(data);
        int last = data.size() -1;
        showControl(listView, true);
        listView.getSelectionModel().select(last);
        WeakReference<BehaviorBase<?>> weakRef = new WeakReference<>(getBehavior(listView.getSkin()));
        
        replaceSkin(listView);
        // no skin, no behavior: not moved
//        listView.setSkin(null);
        KeyEventFirer keyboard = new KeyEventFirer(listView);
        // working as expected: the handlers installed by the behavior are cleaned
        // no call from handler, no call of functions
        keyboard.doUpArrowPress();
        assertEquals(last - 1, listView.getSelectionModel().getSelectedIndex());
        assertEquals(last -1, listView.getProperties().get("anchor"));
    }
    
    
    @Test
    public void testListViewSelectNoSkin() {
        ObservableList<String> data = FXCollections.observableArrayList("one", "two", "three", "four");
        ListView<String> listView = new ListView<>(data);
        int last = data.size() -1;
        showControl(listView, true);
        listView.getSelectionModel().select(last);
        WeakReference<BehaviorBase<?>> weakRef = new WeakReference<>(getBehavior(listView.getSkin()));

//        replaceSkin(listView);
        // no skin, no behavior: not moved
        listView.setSkin(null);
        assertNull(listView.getProperties().get("anchor"));
        listView.getSelectionModel().select(last -1);
        assertEquals(last - 1, listView.getSelectionModel().getSelectedIndex());
        assertEquals(null, listView.getProperties().get("anchor"));
    }
    

    /**
     * Sanity: isolated list is gc'ed
     */
    @Test
    public void testListViewGC() {
        WeakReference<ListView<?>> ref = new WeakReference<>(new ListView<>());
        assertNotNull(ref.get());
        attemptGC(ref);
        assertNull("listView must be gc'ed", ref.get());
    }
    
    /**
     * Sanity: isolated list with skin is gc'ed
     */
    @Test
    public void testListViewWithSkinGC() {
        WeakReference<ListView<?>> ref = new WeakReference<>(new ListView<>());
        assertNotNull(ref.get());
        installDefaultSkin(ref.get());
        assertNotNull(ref.get().getSkin());
        attemptGC(ref);
        assertNull("listView must be gc'ed", ref.get());
    }
    
    /**
     * isolated test for memory leak in listViewSkin - keep it short
     */
    @Test
    public void testListViewSkinLeak() {
        ListView<?> listView = new ListView<>();
        installDefaultSkin(listView);
        WeakReference<?> ref = new WeakReference<>(replaceSkin(listView));
        assertNotNull(ref.get());
        attemptGC(ref);
        assertNull("listViewSkin must be gc'ed", ref.get());
    }
    @Test
    public void testListViewAddItems() {
        ListView<String> listView = new ListView<>();
        installDefaultSkin(listView);
        replaceSkin(listView);
        listView.getItems().add("addded");
    }
    
    @Test
    public void testListViewRefresh() {
        ListView<String> listView = new ListView<>();
        installDefaultSkin(listView);
        replaceSkin(listView);
        listView.refresh();
    }
    
    @Test
    public void testListViewSetItems() {
        ListView<String> listView = new ListView<>();
        installDefaultSkin(listView);
        replaceSkin(listView);
        listView.setItems(FXCollections.observableArrayList());
    }

    /**
     * Table children not removed - nothing removed, so old flow with
     * old rows/cells still floating around?
     */
    @Ignore("tableskin children")
    @Test
    public void testTableViewSkinAccumulateChildren() {
        TableView table = new TableView();
        installDefaultSkin(table);
        int childCount = table.getChildrenUnmodifiable().size();
        System.out.println("before replace: " +table.getChildrenUnmodifiable());
        replaceSkin(table);
        System.out.println("after replace: " +table.getChildrenUnmodifiable());
    }
    
    /**
     * https://bugs.openjdk.java.net/browse/JDK-8245145
     */
    @Ignore("8245145")
    @Test
    public void testSpinnerSkin() {
        Spinner<?> spinner = new Spinner<>();
        spinner.setSkin(new SpinnerSkin<>(spinner));
        spinner.setSkin(new SpinnerSkin<>(spinner));
    }
    
    @Ignore("8245145")
    @Test
    public void testSpinnerChildren() {
        Spinner<?> spinner = new Spinner<>();
        spinner.setSkin(new SpinnerSkin<>(spinner));
        int childCount = spinner.getChildrenUnmodifiable().size();
        spinner.setSkin(new SpinnerSkin<>(spinner));
        assertEquals(childCount, spinner.getChildrenUnmodifiable().size());
    }
    
    @Ignore("Accordion")
    @Test
    public void testAccordionBehavior() {
        Accordion button = new Accordion();
        installDefaultSkin(button);
        WeakReference<?> weakRef = new WeakReference<>(getBehavior(button.getSkin()));
        replaceSkin(button);
        attemptGC(weakRef);
        assertNull("behavior must be gc'ed", weakRef.get());
    }
    
    @Test
    public void testAccordionBehaviorDirect() {
        Accordion button = new Accordion();
        WeakReference<AccordionBehavior> behaviorRef = new WeakReference<>(new AccordionBehavior(button));
        behaviorRef.get().dispose();
        attemptGC(behaviorRef);
        assertNull("behavior must be gc'ed", behaviorRef.get());
    }
    
    @Test
    public void testButtonBehavior() {
        Button button = new Button();
        installDefaultSkin(button);
        WeakReference<?> weakRef = new WeakReference<>(getBehavior(button.getSkin()));
        replaceSkin(button);
        attemptGC(weakRef);
        assertNull("behavior must be gc'ed", weakRef.get());
    }
    
    @Test
    public void testButtonBehaviorDirect() {
        Button button = new Button();
        WeakReference<ButtonBehavior<Button>> behaviorRef = new WeakReference<>(new ButtonBehavior<>(button));
        behaviorRef.get().dispose();
        attemptGC(behaviorRef);
        assertNull("behavior must be gc'ed", behaviorRef.get());
    }
    
    @Ignore("InputMap")
    @Test
    public void testInputMapButtonBehavior() {
        Button button = new Button();
        installDefaultSkin(button);
        WeakReference<BehaviorBase> weakRef = new WeakReference<>(getBehavior(button.getSkin()));
        WeakReference<?> inputMapRef = new WeakReference(weakRef.get().getInputMap());
        replaceSkin(button);
        attemptGC(weakRef);
        assertNull("inputMap must be gc'ed", inputMapRef.get());
    }
    
    /**
     * NPE in itemsListener on setting skin to null 
     */
    @Test
    public void testChoiceBoxSideEffects() {
        ChoiceBox<String> box = new ChoiceBox<>();
        installDefaultSkin(box);
        box.setSkin(null);
        box.getItems().add("after null");
    }
    
    /**
     * Issue: requestFocus on empty bar throws in listener to focusedProperty
     * 
     */
    @Test //@Ignore("8244418")
    public void testFocusEmptyMenuBar() {
        MenuBar bar = new MenuBar();
        showControl(bar, false);
        bar.requestFocus();
    }
    
   /**
     * TextAreaSkin throws UnsupportedOperation in dispose.
     */
    @Test @Ignore("JDK-8244419")
    public void testTextAreaSkinUnsupportedDispose() {
        TextArea textArea = new TextArea();
        installDefaultSkin(textArea);
        textArea.getSkin().dispose();
    }
    
    
    /**
     * TextAreaSkin throws UnsupportedOperation in dispose (which prevents
     * replacing its skin).
     */
    @Test @Ignore("JDK-8244419")
    public void testTextAreaSkinUnsupportedSetSkin() {
        TextArea textArea = new TextArea();
        installDefaultSkin(textArea);
        replaceSkin(textArea);
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
