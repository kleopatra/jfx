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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.sun.javafx.tk.Toolkit;

import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.skin.ProgressBarSkin;
import javafx.scene.control.skin.ProgressIndicatorSkin;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Test for memory leak when replacing skin.
 * 
 * Parameterized on control class.
 * 
 * When changing (some? which?) state of a control in an active scenegraph,
 * need to firePulse to ensure real updating of state (including detangle all 
 * listeners or other relations)
 */
public class ProgressMemoryLeakTest {

    private Scene scene;
    private Stage stage;
    private Pane root;

    private Control control;
    
    @Test
    public void testMemoryLeak() {
        showControl();
        WeakReference<?> weakRef = new WeakReference<>(control.getSkin());
//                new WeakReference<>(replaceSkin(control));
        assertNotNull(weakRef.get());
        replaceSkin(control);
        Toolkit.getToolkit().firePulse();
        attemptGC(weakRef);
        assertNull("Skin must be gc'ed", weakRef.get());
    }
    
    @Test
    public void testMemoryLeakNullSkin() {
        showControl();
        WeakReference<?> weakRef = new WeakReference(control.getSkin());
        assertNotNull(weakRef.get());
        control.setSkin(null);;
        Toolkit.getToolkit().firePulse();
        attemptGC(weakRef);
        assertNull("Skin must be gc'ed", weakRef.get());
    }
    
    /**
     *  Test from recently updated ProgressBarSkinTest
     *  without adding to active scenegraph: gc'ed
     */
    @Test
    public void testProgressIndicatorSkin() {
        ProgressIndicator bar = new ProgressIndicator();
        bar.setSkin(new ProgressIndicatorSkin(bar));
        WeakReference<?> weakRef = new WeakReference<>(bar.getSkin());
        bar.setSkin(null);
        attemptGC(weakRef);
        assertNull("Skin must be gc'ed", weakRef.get());
    }
    
    /**
     *  Test from recently updated ProgressBarSkinTest
     *  but with adding to active scenegraph: not gc'ed
     */
    @Test
    public void testProgressIndicatorSkinInScene() {
        ProgressIndicator bar = new ProgressIndicator();
        bar.setSkin(new ProgressIndicatorSkin(bar));
        WeakReference<?> weakRef = new WeakReference<>(bar.getSkin());
        showControl(bar);
        assertSame(weakRef.get(), bar.getSkin());
        bar.setSkin(null);
        Toolkit.getToolkit().firePulse();
        attemptGC(weakRef);
        assertNull("Skin must be gc'ed", weakRef.get());
    }
    
    /**
     *  adapted system test for 8236259: change the determinator before
     *  showing -> gc'ed
     */
    @Test
    public void testProgressIndicatorSkinInnerInScene() {
        ProgressIndicator bar = new ProgressIndicator();
        bar.setSkin(new ProgressIndicatorSkin(bar));
        bar.setProgress(1);
        assertEquals("size is wrong", 1, bar.getChildrenUnmodifiable().size());
        WeakReference<?> weakRef = new WeakReference<>(bar.getChildrenUnmodifiable().get(0));
        bar.setProgress(-1.0);
        bar.setProgress(1.0);
        showControl(bar);
        attemptGC(weakRef);
        assertNull("indicator must be gc'ed", weakRef.get());
    }
    
    /**
     *  adapted system test for 8236259: change the determinator after
     *  showing -> not gc'ed
     *  
     *  bug or test artefact? the latter: must force the scene to update
     *  with Toolkit.getToolkit().firePulse
     */
    @Test
    public void testProgressIndicatorSkinInnerInSceneAfterShowing() {
        ProgressIndicator bar = new ProgressIndicator();
        bar.setSkin(new ProgressIndicatorSkin(bar));
        bar.setProgress(1);
        assertEquals("size is wrong", 1, bar.getChildrenUnmodifiable().size());
        WeakReference<?> weakRef = new WeakReference<>(bar.getChildrenUnmodifiable().get(0));
        showControl(bar);
        bar.setProgress(-1.0);
        bar.setProgress(.5);
        Toolkit.getToolkit().firePulse();
        attemptGC(weakRef);
        assertNull("indicator must be gc'ed", weakRef.get());
    }
    
    /**
     *  Test from recently updated ProgressBarSkinTest
     *  without adding to active scenegraph: gc'ed
     */
    @Test
    public void testProgressBarSkin() {
        ProgressBar bar = new ProgressBar();
        bar.setSkin(new ProgressBarSkin(bar));
        WeakReference<?> weakRef = new WeakReference<>(bar.getSkin());
        bar.setSkin(null);
        attemptGC(weakRef);
        assertNull("Skin must be gc'ed", weakRef.get());
    }
    
    /**
     *  Test from recently updated ProgressBarSkinTest
     *  but with adding to active scenegraph: not gc'ed
     */
    @Test
    public void testProgressBarSkinInScene() {
        ProgressBar bar = new ProgressBar();
        bar.setSkin(new ProgressBarSkin(bar));
        WeakReference<?> weakRef = new WeakReference<>(bar.getSkin());
        showControl(bar);
        assertSame(weakRef.get(), bar.getSkin());
        bar.setSkin(null);
        Toolkit.getToolkit().firePulse();
        attemptGC(weakRef);
        assertNull("Skin must be gc'ed", weakRef.get());
    }
    
    private void attemptGC(WeakReference<?> weakRef) {
        for (int i = 0; i < 10; i++) {
            System.gc();
            System.runFinalization();

            if (weakRef.get() == null) {
                break;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                fail("InterruptedException occurred during Thread.sleep()");
            }
        }
    }

    

//------------------- setup    
    @Test
    public void testSetupState() {
        assertNotNull(control);
        showControl();
        List<Node> expected = List.of(control);
        assertEquals(expected, root.getChildren());
    }

    protected void showControl() {
        showControl(control);
    }

    protected void showControl(Control box) {
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
        control = new ProgressBar(); //createControl(controlClass);
    }

   

}
