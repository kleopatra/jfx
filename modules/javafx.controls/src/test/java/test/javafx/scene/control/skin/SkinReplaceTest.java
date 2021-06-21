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
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.tk.Toolkit;

import static javafx.scene.control.ControlShim.*;
import static javafx.scene.control.skin.UnusedSkinShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Outdated and ignored - kept for experiments only!
 * Test for issues when replacing skin.
 * <p>
 * KEEP in doKeep branch! Here we have 
 * a) the complete control list with annotations
 * b) side-effects and configuration support
 * 
 * DELETED SkinLeakTest - just a stepping stone.
 * 
 * ---------
 * Currently we concentrate on most simple context: default skin -> null skin, no
 * scene.
 * 
 * Here we use the control class to create the control (via controlSkinFactory) in setup.
 * 
 * <p>
 * This test is parameterized on control class.
 */
//@Ignore
@RunWith(Parameterized.class)
public class SkinReplaceTest {

    private Scene scene;
    private Stage stage;
    private Pane root;

    private Control control;
    private Class<Control> controlClass;
    private Consumer<Control> sideEffect;
    private Consumer<Control> configurator;
    
//----------- in scene
    
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
    
    @Test
    public void testBehavior() {
        installDefaultSkin(control);
        BehaviorBase<?> behavior = getBehavior(control.getSkin());
        Skin<?> replaceSkin = replaceSkin(control);
        WeakReference<?> weakSkinRef = new WeakReference<>(replaceSkin);
        WeakReference<?> weakBehaviorRef = new WeakReference<>(behavior);
        behavior = null;
        replaceSkin = null;
        attemptGC(weakSkinRef);
        attemptGC(weakBehaviorRef);
        assertEquals("skin must be gc'ed", null, weakSkinRef.get());
        assertEquals("flow must be gc'ed", null, weakBehaviorRef.get());
        
    }
    
    @Test
    public void testMemoryLeakShowConfigurateAlternativeSkin() {
        if (configurator == null) return;
        showControl();
        configurator.accept(control);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(control));
        Toolkit.getToolkit().firePulse();
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }

    @Test
    public void testSideEffectShowAlternativeSkin() {
        if (sideEffect == null) return;
        showControl();
        replaceSkin(control);
        Toolkit.getToolkit().firePulse();
        sideEffect.accept(control);
    }
    
    @Test
    public void testSideEffectShowConfigurateAlternativeSkin() {
        if (sideEffect == null || configurator == null) return;
        showControl();
        configurator.accept(control);
        replaceSkin(control);
        Toolkit.getToolkit().firePulse();
        sideEffect.accept(control);
    }

    /**
     * show control (== implicit default skin) -> set null
     */
    @Test
    public void testMemoryLeakShowNullSkin() {
        showControl();
        WeakReference<?> weakRef = new WeakReference<>(control.getSkin());
        control.setSkin(null);
        Toolkit.getToolkit().firePulse();
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }
    @Test
    public void testMemoryLeakShowConfigurateNullSkin() {
        if (configurator == null) return;
        showControl();
        configurator.accept(control);
        WeakReference<?> weakRef = new WeakReference<>(control.getSkin());
        control.setSkin(null);
        Toolkit.getToolkit().firePulse();
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }
    
    @Test
    public void testSideEffectShowNullSkin() {
        if (sideEffect == null) return;
        showControl();
        control.setSkin(null);
        Toolkit.getToolkit().firePulse();
        sideEffect.accept(control);
    }
    
    @Test
    public void testSideEffectShowConfiguratorNullSkin() {
        if (sideEffect == null || configurator == null) return;
        showControl();
        control.setSkin(null);
        Toolkit.getToolkit().firePulse();
        sideEffect.accept(control);
    }
    
//--------------- no scene
    
    /**
     * default skin -> set alternative
     */
    @Test
    public void testMemoryLeakAlternativeSkin() {
        installDefaultSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(control));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }
    
    @Test
    public void testMemoryLeakConfigurateAlternativeSkin() {
        if (configurator == null) return;
        installDefaultSkin(control);
        configurator.accept(control);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(control));
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }
    
    /**
     * default skin -> alternative skin
     */
    @Test
    public void testSideEffectAlternativeSkin() {
        if (sideEffect == null) return;
        installDefaultSkin(control);
        replaceSkin(control);
        sideEffect.accept(control);
    }
    
    /**
     * default skin -> alternative skin
     */
    @Test
    public void testSideEffectConfigurateAlternativeSkin() {
        if (sideEffect == null || configurator == null) return;
        installDefaultSkin(control);
        configurator.accept(control);
        replaceSkin(control);
        sideEffect.accept(control);
    }
    
    /**
     * default skin -> set null
     */
    @Test
    public void testMemoryLeakNullSkin() {
        installDefaultSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(control.getSkin());
        assertNotNull(weakRef.get());
        control.setSkin(null);
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }
    
    /**
     * default skin -> set null
     */
    @Test
    public void testMemoryLeakConfigurateNullSkin() {
        if (configurator == null) return;
        installDefaultSkin(control);
        configurator.accept(control);
        WeakReference<?> weakRef = new WeakReference<>(control.getSkin());
        assertNotNull(weakRef.get());
        control.setSkin(null);
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }
    
    /**
     * default skin -> null
     */
    @Test
    public void testSideEffectNullSkin() {
        if (sideEffect == null) return;
        installDefaultSkin(control);
        control.setSkin(null);
        sideEffect.accept(control);
    }
    
    /**
     * default skin -> null
     */
    @Test
    public void testSideEffectConfiguratorNullSkin() {
        if (sideEffect == null || configurator == null) return;
        installDefaultSkin(control);
        configurator.accept(control);
        control.setSkin(null);
        sideEffect.accept(control);
    }

    //---------------- parameterized

    // Note: name property not supported before junit 4.11
    @Parameterized.Parameters (name = "{index}: {0} ")
    public static Collection<Object[]> data() {
        // 0: class of control to test, 
        // 1: consumer for testing for side-effects after skin replaced
        // 2: consumer to configure the control after installing default skin
        Object[][] data = new Object[][] {
//            {Accordion.class, null, null, }, // @Ignore("JDK-8241364")
//            {Button.class, null, null, }, 
//            {ButtonBar.class, null, null, }, // @Ignore("JDK-8241364")
//            {CheckBox.class, null, null, },
//            // @Ignore("JDK-8244657 memory leak and side-effect")
//            {ChoiceBox.class, 
//                (Consumer<Control>) c -> ((ChoiceBox) c).getItems().add("added"),
//                null, },
//            {ColorPicker.class, null, null, }, //  @Ignore("JDK-8241364")
//            {ComboBox.class, null, null, 
//                }, // @Ignore("JDK-8241364")
//            {ComboBox.class, null, 
//                    // editable combo
//                    (Consumer<Control>) c -> ((ComboBox) c).setEditable(true), 
//                }, // @Ignore("JDK-8241364")
//            {DateCell.class, null, null, },
//            {DatePicker.class, null, null, }, //  @Ignore("JDK-8241364")
//            {Hyperlink.class, null, null, },
//            {Label.class, 
//                null, 
//                // LabeledSkin keeps an invalidationListener on graphic
//                (Consumer<Control>) c -> ((Label) c).setGraphic(new Rectangle())
//                },
//            {ListCell.class, null, null, },// @Ignore("JDK-8241364")
            {ListView.class, null, null, }, // @Ignore("JDK-8241364")
//            {MenuBar.class, null, null, }, //  @Ignore("JDK-8241364")
//            {MenuButton.class, null, null, }, //   @Ignore("JDK-8241364")
//            {Pagination.class, null, null, }, //  @Ignore("JDK-8241364")
//            {PasswordField.class, null, null, }, //  @Ignore("JDK-8241364")
//            {ProgressBar.class, null, null, },
//            {ProgressIndicator.class, null, null, },
//            {RadioButton.class, null, null, },
//            {ScrollBar.class, null, null, }, //  @Ignore("JDK-8241364")
//            {ScrollPane.class, null, null, }, // @Ignore("JDK-8241364")
//            {Separator.class, null, null, },
//            {Slider.class, null, null, },
//            {Spinner.class, null, null, }, // @Ignore("JDK-8241364")
//            {SplitMenuButton.class, null, null, }, //  @Ignore("JDK-8241364")
//            {SplitPane.class, null, null, }, // @Ignore("JDK-8241364")
//            {TableCell.class, null, null, },
//            {TableRow.class, null, null, }, // @Ignore("JDK-8241364")
//            {TableView.class, null, null, }, //  @Ignore("JDK-8241364")
//            {TabPane.class, null, null, }, //    @Ignore("JDK-8241364")
//            // {TextArea.class, null, null, }, // @Ignore("8244419")
//            {TextField.class, null, null, },
//            {TitledPane.class, null, null, },
//            {ToggleButton.class, null, null, },
//            {ToolBar.class, null, null, },//            @Ignore("JDK-8241364")
//            {TreeCell.class, null, null, },//            @Ignore("JDK-8241364")
//            {TreeTableCell.class, null, null, },
//            {TreeTableRow.class, null, null, },//            @Ignore("JDK-8241364")
//            {TreeTableView.class, null, null, },//            @Ignore("JDK-8241364")
            {TreeView.class, null, null, },//            @Ignore("JDK-8241364")
//
        };
        return Arrays.asList(data);
    }

    public SkinReplaceTest(Class<Control> controlClass, Consumer<Control> sideEffect, Consumer<Control> configurator) { 
        this.controlClass = controlClass;
        this.sideEffect = sideEffect;
        this.configurator = configurator;
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
    
    @BeforeClass
    public static void checkParameters() {
        System.out.println(data());
    }

   

}
