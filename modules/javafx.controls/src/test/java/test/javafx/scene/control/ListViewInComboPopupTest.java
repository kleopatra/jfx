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

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.javafx.scene.control.inputmap.KeyBinding;

import static org.junit.Assert.*;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import test.com.sun.javafx.scene.control.infrastructure.KeyEventFirer;
import test.com.sun.javafx.scene.control.infrastructure.KeyModifier;

import static javafx.scene.input.KeyCode.*;
/**
 *
 */
public class ListViewInComboPopupTest {

    private Scene scene;
    private Stage stage;
    private Pane root;

    private ComboBox<String> cb;
    
    protected void assertNavigation(int initial, int expected, KeyCode code, KeyModifier... keyModifiers) {
        showControl(cb, true);
        // select initial
        cb.setValue(cb.getItems().get(initial));
        cb.show();
        KeyEventFirer keyboard = new KeyEventFirer(cb);
        keyboard.doKeyPress(code, keyModifiers);
        assertEquals(cb.getItems().get(expected), cb.getValue());
    }
    
    protected void assertNoNavigation(int initial, KeyCode code, KeyModifier... keyModifiers) {
        assertNavigation(initial, initial, code, keyModifiers);
    }
    
    @Test
    public void testEnd() {
        assertNavigation(0, cb.getItems().size() - 1, KeyCode.END);
    }
    
    @Test
    public void testHome() {
        assertNavigation(cb.getItems().size() - 1, 0, KeyCode.HOME);
    }
    
    @Test
    public void testEndShift() {
        assertNoNavigation(0, KeyCode.END, KeyModifier.SHIFT);
    }
    
    @Test
    public void testHomeShift() {
        assertNoNavigation(cb.getItems().size() - 1, KeyCode.HOME, KeyModifier.SHIFT);
    }
    
    @Test
    public void testEndCtrl() {
        assertNoNavigation(0, KeyCode.END, KeyModifier.CTRL);
    }
    
    @Test
    public void testHomeCtrl() {
        assertNoNavigation(cb.getItems().size() - 1, KeyCode.HOME, KeyModifier.CTRL);
    }
    
    @Test
    public void testEndCtrlShift() {
        assertNoNavigation(0, KeyCode.END, KeyModifier.CTRL, KeyModifier.SHIFT);
    }
    
    @Test
    public void testHomeCtrlShift() {
        assertNoNavigation(cb.getItems().size() - 1, KeyCode.HOME, KeyModifier.CTRL, KeyModifier.SHIFT);
    }
    
    @Test
    public void testKeysNotEditable() {
        showControl(cb, true);
        cb.show();
        int last = cb.getItems().size() - 1;
        assertTrue("sanity: popup showing", cb.isShowing());
        KeyEventFirer keyboard = new KeyEventFirer(cb);
        
        // HOME, END keys with CTRL, SHIFT modifiers
        // Test END key
        keyboard.doKeyPress(KeyCode.END);
        int expected = last;
        assertEquals(cb.getItems().get(expected), cb.getValue());
        
        // Test HOME key
        keyboard.doKeyPress(KeyCode.HOME);
        expected = 0;
        assertEquals(cb.getItems().get(expected), cb.getValue());
        
        // Test SHIFT + END key: no effect
        keyboard.doKeyPress(KeyCode.END, KeyModifier.SHIFT);
        assertEquals(cb.getItems().get(0), cb.getValue());
        
        cb.setValue(cb.getItems().get(last));
        expected = last;
        // Test SHIFT + HOME key: no effect for now
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.SHIFT);
        assertEquals(cb.getItems().get(expected), cb.getValue());
        
        // tbd: moves focus, should be no-op or changed to be move selection
        // Test CTRL + END key .. 
        keyboard.doKeyPress(KeyCode.END, KeyModifier.CTRL);
        assertEquals(cb.getItems().get(last), cb.getValue());
        
        // Test CTRL + HOME key
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.CTRL);
        assertEquals(cb.getItems().get(0), cb.getValue());
        
        // Test CTRL + SHIFT + END key
        keyboard.doKeyPress(KeyCode.END, KeyModifier.CTRL, KeyModifier.SHIFT);
        assertEquals(cb.getItems().get(last), cb.getValue());
        
        // tbd: moves one up, why? same weirdness in single selection stand-alone listView
        // Test CTRL + SHIFT + HOME key
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.CTRL, KeyModifier.SHIFT);
        assertEquals(cb.getItems().get(0), cb.getValue());
        
        // tbd: should not do anything if not editable?
        // Test CTRL + A key
        keyboard.doKeyPress(KeyCode.A, KeyModifier.getShortcutKey());
        assertEquals(cb.getItems().get(last), cb.getValue());
        
        // Sanity
        assertTrue(cb.isShowing());
        
    }
    
    protected void assertNavigatedTo(int expected) {
        
    }
    
    @Test
    public void testKeysEditable() {
        showControl(cb, true);
        cb.setEditable(true);
//        cb.show();
        KeyEventFirer keyboard = new KeyEventFirer(cb);

        // Show the popup
        assertFalse(cb.isShowing());
//        cb.requestFocus();
        cb.getEditor().setText("ABC DEF");
        assertEquals("ABC DEF", cb.getEditor().getText());
        keyboard.doDownArrowPress(KeyModifier.ALT);
        // Sanity
        assertTrue(cb.isShowing());
        assertEquals(0, cb.getEditor().getCaretPosition());

        // LEFT, RIGHT keys with CTRL, SHIFT modifiers
        // Test RIGHT key
        keyboard.doRightArrowPress();
        assertEquals(1, cb.getEditor().getCaretPosition());

        // Test KP_RIGHT key
        keyboard.doKeyPress(KeyCode.KP_RIGHT);
        assertEquals(2, cb.getEditor().getCaretPosition());

        // Test LEFT key
        keyboard.doLeftArrowPress();
        assertEquals(1, cb.getEditor().getCaretPosition());

        // Test KP_LEFT key
        keyboard.doKeyPress(KeyCode.KP_LEFT);
        assertEquals(0, cb.getEditor().getCaretPosition());

        // Test SHIFT + RIGHT key
        keyboard.doKeyPress(KeyCode.RIGHT, KeyModifier.SHIFT);
        assertEquals("A", cb.getEditor().getSelectedText());
        assertEquals(1, cb.getEditor().getCaretPosition());

        // Test SHIFT + LEFT key
        keyboard.doKeyPress(KeyCode.LEFT, KeyModifier.SHIFT);
        assertEquals("", cb.getEditor().getSelectedText());
        assertEquals(0, cb.getEditor().getCaretPosition());

        // Test CTRL + RIGHT key
        keyboard.doKeyPress(KeyCode.RIGHT, KeyModifier.CTRL);
        assertEquals("", cb.getEditor().getSelectedText());
        assertEquals(4, cb.getEditor().getCaretPosition());

        // Test CTRL + LEFT key
        keyboard.doKeyPress(KeyCode.LEFT, KeyModifier.CTRL);
        assertEquals("", cb.getEditor().getSelectedText());
        assertEquals(0, cb.getEditor().getCaretPosition());

        // Test CTRL + SHIFT + RIGHT key
        keyboard.doKeyPress(KeyCode.RIGHT, KeyModifier.CTRL, KeyModifier.SHIFT);
        assertEquals("ABC ", cb.getEditor().getSelectedText());
        assertEquals(4, cb.getEditor().getCaretPosition());

        // Test CTRL + SHIFT + LEFT key
        keyboard.doKeyPress(KeyCode.LEFT, KeyModifier.CTRL, KeyModifier.SHIFT);
        assertEquals("", cb.getEditor().getSelectedText());
        assertEquals(0, cb.getEditor().getCaretPosition());

        // HOME, END keys with CTRL, SHIFT modifiers
        // Test END key
        keyboard.doKeyPress(KeyCode.END);
        assertEquals(7, cb.getEditor().getCaretPosition());

        // Test HOME key
        keyboard.doKeyPress(KeyCode.HOME);
        assertEquals(0, cb.getEditor().getCaretPosition());

        // Test SHIFT + END key
        keyboard.doKeyPress(KeyCode.END, KeyModifier.SHIFT);
        assertEquals(cb.getEditor().getText(), cb.getEditor().getSelectedText());
        assertEquals(7, cb.getEditor().getCaretPosition());

        // Test SHIFT + HOME key
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.SHIFT);
        assertEquals("", cb.getEditor().getSelectedText());
        assertEquals(0, cb.getEditor().getCaretPosition());

        // Test CTRL + END key
        keyboard.doKeyPress(KeyCode.END, KeyModifier.CTRL);
        assertEquals("", cb.getEditor().getSelectedText());
        assertEquals(7, cb.getEditor().getCaretPosition());

        // Test CTRL + HOME key
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.CTRL);
        assertEquals("", cb.getEditor().getSelectedText());
        assertEquals(0, cb.getEditor().getCaretPosition());

        // Test CTRL + SHIFT + END key
        keyboard.doKeyPress(KeyCode.END, KeyModifier.CTRL, KeyModifier.SHIFT);
        assertEquals(cb.getEditor().getText(), cb.getEditor().getSelectedText());
        assertEquals(7, cb.getEditor().getCaretPosition());

        // Test CTRL + SHIFT + HOME key
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.CTRL, KeyModifier.SHIFT);
        assertEquals("", cb.getEditor().getSelectedText());
        assertEquals(0, cb.getEditor().getCaretPosition());

        // Test CTRL + A key
        keyboard.doLeftArrowPress();
        assertEquals("", cb.getEditor().getSelectedText());
        keyboard.doKeyPress(KeyCode.A, KeyModifier.getShortcutKey());
        assertEquals(cb.getEditor().getText(), cb.getEditor().getSelectedText());

        // Sanity
        assertTrue(cb.isShowing());
        
    }
    
    // package-private for usage in testing
    List<KeyBinding> ignoredComboBindings() {
        return List.of(
                new KeyBinding(LEFT),
                new KeyBinding(KP_LEFT),
                new KeyBinding(RIGHT),
                new KeyBinding(KP_RIGHT),
                new KeyBinding(HOME),
                new KeyBinding(HOME).shift(),
                new KeyBinding(HOME).shortcut(),
                new KeyBinding(END),
                new KeyBinding(END).shift(),
                new KeyBinding(END).shortcut(),
                new KeyBinding(A).shortcut(),
                new KeyBinding(HOME).shortcut().shift(),
                new KeyBinding(END).shortcut().shift()
                
                );
    }
   
//------------- setup and initial
    
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
        cb = new ComboBox<>(FXCollections.observableArrayList("a", "b", "c"));
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException)throwable;
            } else {
                Thread.currentThread().getThreadGroup().uncaughtException(thread, throwable);
            }
        });
    }


}
