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
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.SpinnerSkin;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
    

    protected void fireMethodPulse() {
        if (methodPulse) Toolkit.getToolkit().firePulse();
    }
    
    /**
     * Table children not removed - nothing removed, so old flow with
     * old rows/cells still floating around?
     */
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
    @Test
    public void testSpinnerSkin() {
        Spinner<?> spinner = new Spinner<>();
        spinner.setSkin(new SpinnerSkin<>(spinner));
        spinner.setSkin(new SpinnerSkin<>(spinner));
    }
    
    @Test
    public void testSpinnerChildren() {
        Spinner<?> spinner = new Spinner<>();
        spinner.setSkin(new SpinnerSkin<>(spinner));
        int childCount = spinner.getChildrenUnmodifiable().size();
        spinner.setSkin(new SpinnerSkin<>(spinner));
        assertEquals(childCount, spinner.getChildrenUnmodifiable().size());
    }
    
    @Test
    public void testButtonBehaviorCreate() {
        Button button = new Button();
        BehaviorBase behavior = createBehavior(button);
        
    }
    
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
