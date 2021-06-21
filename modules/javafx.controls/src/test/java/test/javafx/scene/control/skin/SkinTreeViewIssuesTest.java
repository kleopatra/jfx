/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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
import static javafx.scene.control.skin.UnusedSkinShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 */
public class SkinTreeViewIssuesTest {
    private Scene scene;
    private Stage stage;
    private Pane root;

    private TreeView<String> control;

    /**
     * Reported: https://bugs.openjdk.java.net/browse/JDK-8269081
     * set custom cell factory before showing: no leak
     * The reason is that skin creates the default cell with a not-static method - 
     * leading to cells having an implicit strong ref? Why?
     */
    @Test
    public void testMemoryLeakShowAlternativeSkinWithCustomCell() {
        ((TreeView<?>) control).setCellFactory(cc -> new TreeCell<>());
        showControl();
        Skin<?> replacedSkin = replaceSkin(control);
        Toolkit.getToolkit().firePulse();
        WeakReference<?> weakRef = new WeakReference<>(replacedSkin);
        replacedSkin = null;
        // beware: this is important - we might get false leaks without!
        Toolkit.getToolkit().firePulse();
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }
    
    /**
     * set custom cell factory after showing
     */
    @Test
    public void testMemoryLeakShowAlternativeSkinWithCustomCellChangeAfter() {
        showControl();
        ((TreeView<?>) control).setCellFactory(cc -> new TreeCell<>());
        Toolkit.getToolkit().firePulse();
        Skin<?> replacedSkin = replaceSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(replacedSkin);
        replacedSkin = null;
        // beware: this is important - we might get false leaks without!
        Toolkit.getToolkit().firePulse();
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }
    
    /**
     * show control (== implicit default skin) -> set alternative
     */
    @Test
    public void testMemoryLeakShowAlternativeSkin() {
        showControl();
        Skin<?> replacedSkin = replaceSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(replacedSkin);
        replacedSkin = null;
        // beware: this is important - we might get false leaks without!
        Toolkit.getToolkit().firePulse();
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }
    
    @Test
    public void testFlow() {
        installDefaultSkin(control);
        VirtualFlow<?> flow = getVirtualFlow(control);
        if (flow == null) return;
        Skin<?> replaceSkin = replaceSkin(control);
        WeakReference<?> weakSkinRef = new WeakReference<>(replaceSkin);
        WeakReference<?> weakFlowRef = new WeakReference<>(flow);
        flow = null;
        replaceSkin = null;
        attemptGC(weakSkinRef);
        attemptGC(weakFlowRef);
        assertEquals("skin must be gc'ed", null, weakSkinRef.get());
        assertEquals("flow must be gc'ed", null, weakFlowRef.get());
    }
    

//------------ setup
    
    protected void showControl() {
        showControl(control);
    }

    protected void showControl(Control box) {
        if (stage == null) {
            root = new VBox();
            scene = new Scene(root);
            stage = new Stage();
            stage.setScene(scene);
        }
        if (!root.getChildren().contains(box)) {
            root.getChildren().add(box);
        }
        stage.show();
        stage.requestFocus();
        box.requestFocus();
        assertTrue(box.isFocused());
        assertSame(box, scene.getFocusOwner());
    }

    @After
    public void cleanup() {
        if (stage != null) stage.hide();
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

        control = new TreeView<>();
    }
    

}
