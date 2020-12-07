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

package test.com.sun.javafx.scene.control.behavior;

import java.lang.ref.WeakReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;

import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory;

/**
 * This test has methods defining the behavior (of the textField w/out behavior) and
 * unrelated issues. Those around fixing ?? will be moved into BehaviourCleanupTest 
 * before commit.
 * 
 * Note: do not include in the fix, meant for keeping around in dokeep-branch. 
 * 
 * TODO: make sure there are tests that keyPad mappings are still working (install
 * was changed!), both in parent and child mappings
 */
public class TextFieldBehaviorIssuesTest {

    private Scene scene;
    private Stage stage;
    private Pane root;
    

//------------ 
    
    /**
     * Sanity test: mappings to key pad keys.
     */
    @Test
    public void testKeyPadMapping() {
        TextField control = new TextField("some text");
        TextFieldBehavior behavior = (TextFieldBehavior) createBehavior(control);
        InputMap<?> inputMap = behavior.getInputMap();
        // FIXME: test for all? 
        // Here we take one of the expected only - assumption being that
        // if the one is properly registered, it's siblings are handled as well
        KeyCode expectedCode = KeyCode.KP_LEFT;
        KeyMapping expectedMapping = new KeyMapping(expectedCode, null);
        assertTrue(inputMap.getMappings().contains(expectedMapping));
    }
    
    /**
     * Sanity test: child mappings to key pad keys.
     */
    @Test
    public void testKeyPadMappingChildInputMap() {
        TextField control = new TextField("some text");
        TextFieldBehavior behavior = (TextFieldBehavior) createBehavior(control);
        InputMap<?> inputMap = behavior.getInputMap();
        // FIXME: test for all? 
        // Here we take one of the expected only - assumption being that
        // if the one is properly registered, it's siblings are handled as well
        KeyCode expectedCode = KeyCode.KP_LEFT;
        // test os specific child mappings
        InputMap<?> childInputMapMac = inputMap.getChildInputMaps().get(0);
        KeyMapping expectedMac = new KeyMapping(new KeyBinding(expectedCode).shortcut(), null);
        assertTrue(childInputMapMac.getMappings().contains(expectedMac));
        
        InputMap<?> childInputMapNotMac = inputMap.getChildInputMaps().get(1);
        KeyMapping expectedNotMac = new KeyMapping(new KeyBinding(expectedCode).ctrl(), null);
        assertTrue(childInputMapNotMac.getMappings().contains(expectedNotMac));
    }
    
//--------------- memory leak (temp)
    
    /**
     * Test behavior memory leak from scene listener.
     * Wrong setup?: behaviour cannot be gc`d if the skin isn't?
     * 
     * But: without skin being gc'd (no attempt), this passes if the skin
     * null its behavior (temporary change to not have behavior final)
     */
    @Test
    public void failedMemoryLeakBehaviorInScene() {
        TextField control = new TextField();
        showControl(control);
        WeakReference<?> weakSkin = new WeakReference<>(control.getSkin());
        WeakReference<BehaviorBase<?>> weakRef = new WeakReference<>(getBehavior(control.getSkin()));
        assertNotNull(weakRef.get());
        replaceSkin(control);
//        attemptGC(weakSkin);
//        assertNull("skin must be gc'ed", weakSkin.get());
        attemptGC(weakRef);
        assertNull("behavior must be gc'ed", weakRef.get());
    }
    
    /**
     * Create behavior -> dispose behavior -> gc
     */
    @Test
    public void failedMemoryLeakDisposeBehavior() {
        TextField control = new TextField();
        WeakReference<BehaviorBase<?>> weakRef = new WeakReference<>(createBehavior(control));
        assertNotNull(weakRef.get());
        weakRef.get().dispose();
        attemptGC(weakRef);
        assertNull("behavior must be gc'ed", weakRef.get());
    }

//----------- setup   
    
    /**
     * Ensures the control is shown and focused in an active scenegraph. 
     * 
     * @param control the control to show
     */
    protected void showControl(Control control) {
        showControl(control, true);
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
        if (stage != null) {
            stage.hide();
        }
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
