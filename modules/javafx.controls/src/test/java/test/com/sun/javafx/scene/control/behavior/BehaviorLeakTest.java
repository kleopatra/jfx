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
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.tk.Toolkit;

import static javafx.scene.control.ControlShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Pagination;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Outdated and ignored - kept for experiments only!
 * Test for issues when replacing skin.
 * <p>
 * 
 * Currently we concentrate on most simple context: default skin -> null skin, no
 * scene.
 * 
 * <p>
 * This test is parameterized on control class.
 */
@Ignore
@RunWith(Parameterized.class)
public class BehaviorLeakTest {

    private Scene scene;
    private Stage stage;
    private Pane root;

    private Control control;
    private Class<Control> controlClass;
    
   
//--------------- no scene
    
//    /**
//     * default skin -> set alternative
//     */
//    @Test
//    public void testMemoryLeakAlternativeSkin() {
//        installDefaultSkin(control);
//        WeakReference<?> weakRef = new WeakReference<>(getBehavior(control.getSkin()));
//        replaceSkin(control);
//        attemptGC(weakRef);
//        assertNull("behavior must be gc'ed", weakRef.get());
//    }
//    
    /**
     * Test gc of behavior
     * default skin -> set null skin
     * 
     * Note: this will pass only if the skin is not leaking elsewhere?
     */
    @Test
    public void testMemoryLeakBehaviorNullSkin() {
        installDefaultSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(getBehavior(control.getSkin()));
        control.setSkin(null);
        attemptGC(weakRef);
        assertNull("behavior must be gc'ed", weakRef.get());
    }
    
    /**
     * Test gc of skin
     * default skin -> set alternative
     */
    @Test
    public void testMemoryLeakSkinNullSkin() {
        installDefaultSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(control.getSkin());
        control.setSkin(null);
        attemptGC(weakRef);
        assertNull("behavior must be gc'ed", weakRef.get());
    }
    
    
//    /**
//     * Create behavior -> dispose behavior -> gc
//     */
//    @Test
//    public void testMemoryLeakDisposeBehavior() {
//        WeakReference<BehaviorBase<?>> weakRef = new WeakReference<>(createBehavior(control));
//        assertNotNull(weakRef.get());
//        weakRef.get().dispose();
//        attemptGC(weakRef);
//        assertNull("behavior must be gc'ed", weakRef.get());
//    }
//    
    //---------------- parameterized

    // Note: name property not supported before junit 4.11
    @Parameterized.Parameters //(name = "{index}: {0} ")
    public static Collection<Class<Control>> data() {
        return getControlClassesWithBehavior();
    }

    public BehaviorLeakTest(Class<Control> controlClass) { 
        this.controlClass = controlClass;
    }

//------------------- setup    
//    @Test
//    public void testSetupState() {
//        assertNotNull(control);
//        showControl();
//        List<Node> expected = List.of(control);
//        assertEquals(expected, root.getChildren());
//    }

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
