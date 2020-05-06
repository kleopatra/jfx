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

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.sun.javafx.tk.Toolkit;

import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import test.com.sun.javafx.scene.control.infrastructure.KeyEventFirer;

/**
 * Contains tests for issues that turned up in SkinDisposeTest (and
 * are unrelated contract violation of dispose).
 * 
 * DatePickerSkin/ComboBoxSkin: throws NPE in listener to control's focusedProperty when 
 *    hiding stage after dispose/setSkin
 *    issue is not removing the manually registered listener in dispose
 *    
 * These three are combined in this test because getting a failing/passing test before/after
 * the fix requires a redirected uncaughtExceptionHandler. 
 * 
 * Test sequence: show -> focus -> manipulate skin -> hide stage
 * 
 * Note: dispose is extremely whacky, doing so seems to introduce test instabilities (some
 * global state broken?), decision: don't!
 * 
 * As there is no difference between when/if pulses are fired, maybe good enough to not
 * fire at all?
 * 
 */
@RunWith(Parameterized.class)
public class SkinComboPickerTest {

    private Scene scene;
    private Stage stage;
    private Pane root;
    
    private Class<Control> controlClass;
    private Control control;
    
    private boolean showPulse; 
    private boolean methodPulse; 

    /**
     * Issue: event filter not removed.
     * 
     * alternative skin -> press enter
     */
    @Test
    public void testAlternativeSkinEnter() {
        showControl(control, true);
        KeyEventFirer keyboard = new KeyEventFirer(control);
        replaceSkin(control);
        fireMethodPulse();
        keyboard.doKeyPress(KeyCode.ENTER);
    }
    
    /**
     * Issue: event filter not removed.
     * 
     * alternative skin -> press enter
     */
    @Test
    public void testNullSkinEnter() {
        showControl(control, true);
        KeyEventFirer keyboard = new KeyEventFirer(control);
        control.setSkin(null);
        fireMethodPulse();
        keyboard.doKeyPress(KeyCode.ENTER);
    }
    
    /**
     * Issue: skin dispose doesn't cleanup listeners - focusListener still active.
     * 
     * alternative skin -> hide stage
     */
    @Test
    public void testAlternativeSkinHide() {
        showControl(control, true);
        replaceSkin(control);
        fireMethodPulse();
        stage.hide();
    }

    /**
     * Issue: skin dispose doesn't cleanup listeners - focusListener still active.
     * 
     * null skin -> hide stage.
     */
    @Test
    public void testNullSkinHide() {
        showControl(control, true);
        control.setSkin(null);
        fireMethodPulse();
        stage.hide();
    }
    
    /**
     * Issue: skin dispose doesn't cleanup listeners - focusListener still active.
     * 
     * alternative skin -> transfer focus.
     */
    @Test
    public void testAlternativeSkinFocus() {
        showControl(control, true);
        Button other = new Button("dummy");
        showControl(other, false);
        replaceSkin(control);
        fireMethodPulse();
        other.requestFocus();
    }
    
    /**
     * Issue: skin dispose doesn't cleanup listeners - focusListener still active.
     * 
     * null skin -> transfer focus
     */
    @Test
    public void testNullSkinFocus() {
        showControl(control, true);
        Button other = new Button("dummy");
        showControl(other, false);
        control.setSkin(null);
        fireMethodPulse();
        other.requestFocus();
    }

    /**
     * Issue: skin dispose doesn't cleanup listeners - focusListener still active.
     * 
     * Here we dispose, then hide the containing stage.
     * Over the top whacky - don't!
     */
//    @Test
//    public void testDisposeSkin() {
//        showControl(control, true);
//        control.getSkin().dispose();
//        fireMethodPulse();
//        stage.hide();
//    }
    
//----------------- helper
    
    protected void fireMethodPulse() {
        if (methodPulse) Toolkit.getToolkit().firePulse();
    }
    
//---------- parameterized
    
    @Parameterized.Parameters //(name = "{index}: {0} showPulse: {1} methodPulse {2}")
    public static Collection<Object[]> data() {
        // class of control to test, pulse in show, pulse in method
        Object[][] data = new Object[][] {
            {ComboBox.class, true, true }, 
            {ComboBox.class, false, true }, 
            {ComboBox.class, false, false }, 
            {ComboBox.class, true, false }, 
            {DatePicker.class, true, true, }, 
            {DatePicker.class, false, true, }, 
            {DatePicker.class, false, false, }, 
            {DatePicker.class, true, false, }, 
            {ColorPicker.class, true, true, }, 
            {ColorPicker.class, false, true, }, 
            {ColorPicker.class, false, false, }, 
            {ColorPicker.class, true, false, }, 
            {Hyperlink.class, true, true, }, 
        };
        return Arrays.asList(data);
    }

    public SkinComboPickerTest(Class<Control> controlClass, boolean showPulse, boolean methodPulse) { 
        this.controlClass = controlClass;
        this.showPulse = showPulse;
        this.methodPulse = methodPulse;
    }
//---------------- setup and initial
    
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
            
            if (showPulse) Toolkit.getToolkit().firePulse();
        }
    }

    @After
    public void cleanup() {
        stage.hide();
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }

    @Before
    public void setup() {
        assertNotNull(controlClass);
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
        control = createControl(controlClass);
    }

}
