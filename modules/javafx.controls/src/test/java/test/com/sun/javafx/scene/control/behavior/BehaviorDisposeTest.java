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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;

import static javafx.scene.control.ControlShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Pagination;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;

/**
 * Outdated and ignored - kept for experiments only!
 * 
 * Test for Behavior on dispose. There are two variants to create/check for gc of behaviour:
 * 
 * - install the default skin, get behavior, replace skin
 * - directly create behavior, dispose behavior
 * 
 * For some, the behavior can be gc'ed in the second case but not in the first - why?
 * 
 * Currently: here focus on simple behavior (no skin) to detect leaks specific to 
 *    behavior. Interaction with skin might have more complex issues. So first fix
 *    the behavior, then the interaction.
 * <p>
 * This test is parameterized on control class.
 */
@Ignore
@RunWith(Parameterized.class)
public class BehaviorDisposeTest {


    private Control control;
    private Class<Control> controlClass;
    
    /**
     * Create behavior -> dispose behavior -> gc
     */
    @Test
    public void testMemoryLeakDisposeBehavior() {
        WeakReference<BehaviorBase> weakRef = new WeakReference<>(createBehavior(control));
        assertNotNull(weakRef.get());
        weakRef.get().dispose();
        attemptGC(weakRef);
        assertNull("behavior must be gc'ed", weakRef.get());
    }
    
//    /**
//     * default skin -> getBehavior -> skin.dispoce -> gc old behavior
//     * 
//     * incorrect test assumption? control still has skin and skin has reference to 
//     * behavior ..
//     */
//    @Test
//    public void testMemoryLeakDisposeSkin() {
//        installDefaultSkin(control);
//        WeakReference<?> weakRef = new WeakReference<>(getBehavior(control.getSkin()));
//        assertNotNull(weakRef.get());
//        control.getSkin().dispose();
//        attemptGC(weakRef);
//        assertNull("behavior must be gc'ed", weakRef.get());
//    }
//    
//    /**
//     * default skin -> getBehavior -> replace skin -> gc old behavior
//     */
//    @Test
//    public void testMemoryLeakAlternativeSkin() {
//        installDefaultSkin(control);
//        WeakReference<?> weakRef = new WeakReference<>(getBehavior(control.getSkin()));
//        assertNotNull(weakRef.get());
//        setAlternativeSkin(control);
//        attemptGC(weakRef);
//        assertNull("behavior must be gc'ed", weakRef.get());
//    }
//    
    //---------------- parameterized

    // Note: name property not supported before junit 4.11
    @Parameterized.Parameters //(name = "{index}: {0} ")
    public static Collection<Class<Control>> data() {
        List<Class<Control>> controlClasses = getControlClassesWithBehavior();
        List<Class<? extends Control>> failing = List.of(
                // @Ignore("8245282")
//                Button.class,
                // @Ignore("8245282")
//                CheckBox.class,
                ColorPicker.class,
                ComboBox.class,
                DatePicker.class,
                // @Ignore("8245282")
//                Hyperlink.class,
                ListView.class,
                // @Ignore("8245282")
//                MenuButton.class,
                PasswordField.class,
                // @Ignore("8245282")
//                RadioButton.class,
                // @Ignore("8245282")
//                SplitMenuButton.class,
                TableView.class,
                TextArea.class,
                TextField.class,
                // @Ignore("8245282")
//                ToggleButton.class,
                TreeTableView.class,
                TreeView.class
                
                );
//        controlClasses.removeAll(failing);
        return controlClasses;
    }

    public BehaviorDisposeTest(Class<Control> controlClass) { 
        this.controlClass = controlClass;
    }

//------------------- setup    
    @After
    public void cleanup() {
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

        control = createControl(controlClass);
        
    }

   

}
