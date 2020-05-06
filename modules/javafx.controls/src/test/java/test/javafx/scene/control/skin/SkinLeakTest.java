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
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
import javafx.scene.control.Skin;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
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
 * Test for issues when replacing skin.
 * <p>
 * 
 * Currently we concentrate on most simple context: default skin -> null skin, no
 * scene.
 * 
 * Here we use a supplier to create the control.
 * <p>
 * This test is parameterized on control class.
 */
@Ignore
@RunWith(Parameterized.class)
public class SkinLeakTest {

    private Scene scene;
    private Stage stage;
    private Pane root;

    private Control control;
    private Supplier<Control> controlSupplier;
    
//----------- in scene
    
//    /**
//     * show control (== implicit default skin) -> set alternative
//     */
//    @Test
//    public void testMemoryLeakShowAlternativeSkin() {
//        showControl();
//        WeakReference<?> weakRef = new WeakReference<>(setAlternativeSkin(control));
//        Toolkit.getToolkit().firePulse();
//        attemptGC(weakRef);
//        assertEquals("Skin must be gc'ed", null, weakRef.get());
//    }
//
//    /**
//     * show control (== implicit default skin) -> set null
//     */
//    @Test
//    public void testMemoryLeakShowNullSkin() {
//        showControl();
//        WeakReference<?> weakRef = new WeakReference<>(control.getSkin());
//        control.setSkin(null);
//        Toolkit.getToolkit().firePulse();
//        attemptGC(weakRef);
//        assertEquals("Skin must be gc'ed", null, weakRef.get());
//    }
//
//    @Test
//    public void testMemoryLeakShowConfigurateAlternativeSkin() {
//        if (configurator == null) return;
//        showControl();
//        configurator.accept(control);
//        WeakReference<?> weakRef = new WeakReference<>(setAlternativeSkin(control));
//        Toolkit.getToolkit().firePulse();
//        attemptGC(weakRef);
//        assertEquals("Skin must be gc'ed", null, weakRef.get());
//    }
//
//    @Test
//    public void testSideEffectShowAlternativeSkin() {
//        if (sideEffect == null) return;
//        showControl();
//        setAlternativeSkin(control);
//        Toolkit.getToolkit().firePulse();
//        sideEffect.accept(control);
//    }
//    
//    @Test
//    public void testSideEffectShowConfigurateAlternativeSkin() {
//        if (sideEffect == null || configurator == null) return;
//        showControl();
//        configurator.accept(control);
//        setAlternativeSkin(control);
//        Toolkit.getToolkit().firePulse();
//        sideEffect.accept(control);
//    }

//    @Test
//    public void testMemoryLeakShowConfigurateNullSkin() {
//        if (configurator == null) return;
//        showControl();
//        configurator.accept(control);
//        WeakReference<?> weakRef = new WeakReference<>(control.getSkin());
//        control.setSkin(null);
//        Toolkit.getToolkit().firePulse();
//        attemptGC(weakRef);
//        assertEquals("Skin must be gc'ed", null, weakRef.get());
//    }
//    
//    @Test
//    public void testSideEffectShowNullSkin() {
//        if (sideEffect == null) return;
//        showControl();
//        control.setSkin(null);
//        Toolkit.getToolkit().firePulse();
//        sideEffect.accept(control);
//    }
//    
//    @Test
//    public void testSideEffectShowConfiguratorNullSkin() {
//        if (sideEffect == null || configurator == null) return;
//        showControl();
//        control.setSkin(null);
//        Toolkit.getToolkit().firePulse();
//        sideEffect.accept(control);
//    }
    
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
    
    /**
     * default skin -> dispose
     * 
     * test error: on dispose, there's still a strong reference from the control to the skin
     */
//    @Test
//    public void testMemoryLeakDisposeSkin() {
//        installDefaultSkin(control);
//        WeakReference<Skin<?>> weakRef = new WeakReference<>(control.getSkin());
//        assertNotNull(weakRef.get());
//        weakRef.get().dispose();
//        attemptGC(weakRef);
//        assertEquals("Skin must be gc'ed", null, weakRef.get());
//    }
//    
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
    
    
//    @Test
//    public void testMemoryLeakConfigurateAlternativeSkin() {
//        if (configurator == null) return;
//        installDefaultSkin(control);
//        configurator.accept(control);
//        WeakReference<?> weakRef = new WeakReference<>(setAlternativeSkin(control));
//        attemptGC(weakRef);
//        assertEquals("Skin must be gc'ed", null, weakRef.get());
//    }
//    
//    /**
//     * default skin -> alternative skin
//     */
//    @Test
//    public void testSideEffectAlternativeSkin() {
//        if (sideEffect == null) return;
//        installDefaultSkin(control);
//        setAlternativeSkin(control);
//        sideEffect.accept(control);
//    }
//    
//    /**
//     * default skin -> alternative skin
//     */
//    @Test
//    public void testSideEffectConfigurateAlternativeSkin() {
//        if (sideEffect == null || configurator == null) return;
//        installDefaultSkin(control);
//        configurator.accept(control);
//        setAlternativeSkin(control);
//        sideEffect.accept(control);
//    }
//    
//    /**
//     * default skin -> set null
//     */
//    @Test
//    public void testMemoryLeakConfigurateNullSkin() {
//        if (configurator == null) return;
//        installDefaultSkin(control);
//        configurator.accept(control);
//        WeakReference<?> weakRef = new WeakReference<>(control.getSkin());
//        assertNotNull(weakRef.get());
//        control.setSkin(null);
//        attemptGC(weakRef);
//        assertEquals("Skin must be gc'ed", null, weakRef.get());
//    }
//    
//    /**
//     * default skin -> null
//     */
//    @Test
//    public void testSideEffectNullSkin() {
//        if (sideEffect == null) return;
//        installDefaultSkin(control);
//        control.setSkin(null);
//        sideEffect.accept(control);
//    }
//    
//    /**
//     * default skin -> null
//     */
//    @Test
//    public void testSideEffectConfiguratorNullSkin() {
//        if (sideEffect == null || configurator == null) return;
//        installDefaultSkin(control);
//        configurator.accept(control);
//        control.setSkin(null);
//        sideEffect.accept(control);
//    }

//-------------- helper  (FIXME: replace with memoryhelper when available) 
    
    //---------------- parameterized

    // Note: name property not supported before junit 4.11
    @Parameterized.Parameters // (name = "{index}: {0} ")
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
            {(Supplier<Control>)Accordion::new},
            {(Supplier<Control>)Button::new }, 
//            {(Supplier<Control>)() -> new Button("", new Rectangle())},
            {(Supplier<Control>)ButtonBar::new}, 
            {(Supplier<Control>)CheckBox::new}, 
            {(Supplier<Control>)ChoiceBox::new},
            {(Supplier<Control>)ColorPicker::new},
            {(Supplier<Control>)ComboBox::new},
            {(Supplier<Control>)DateCell::new},
            {(Supplier<Control>)DatePicker::new},
            {(Supplier<Control>)Hyperlink::new},
//            {(Supplier<Control>)() -> new Hyperlink("", new Rectangle())},
            {(Supplier<Control>)Label::new},
//            {(Supplier<Control>)() -> new Label("", new Rectangle())},
            {(Supplier<Control>)ListCell::new},
            {(Supplier<Control>)ListView::new},
            {(Supplier<Control>)MenuBar::new},
            {(Supplier<Control>)MenuButton::new},
            {(Supplier<Control>)Pagination::new},
            {(Supplier<Control>)PasswordField::new},
            {(Supplier<Control>)ProgressBar::new},
            {(Supplier<Control>)ProgressIndicator::new},
            {(Supplier<Control>)RadioButton::new},
            {(Supplier<Control>)ScrollBar::new},
            {(Supplier<Control>)ScrollPane::new},
            {(Supplier<Control>)Separator::new},
            {(Supplier<Control>)Slider::new},
            {(Supplier<Control>)Spinner::new},
            {(Supplier<Control>)SplitMenuButton::new},
            {(Supplier<Control>)SplitPane::new},
            {(Supplier<Control>)TableCell::new},
            {(Supplier<Control>)TableRow::new},
            {(Supplier<Control>)TableView::new},
            {(Supplier<Control>)TabPane::new},
//            {(Supplier<Control>)TextArea::new},
            {(Supplier<Control>)TextField::new},
            {(Supplier<Control>)TitledPane::new},
            {(Supplier<Control>)ToggleButton::new},
//            {(Supplier<Control>)() -> new ToggleButton("", new Rectangle())},
            {(Supplier<Control>)ToolBar::new},
            {(Supplier<Control>)TreeCell::new},
            {(Supplier<Control>)TreeTableCell::new},
            {(Supplier<Control>)TreeTableRow::new},
            {(Supplier<Control>)TreeTableView::new},
            {(Supplier<Control>)TreeView::new},
        };

        
        // 0: class of control to test, 
        // 1: consumer for testing for side-effects after skin replaced
        // 2: consumer to configure the control after installing default skin
//        Object[][] data = new Object[][] {
////            @Ignore("JDK-8241364")
////            {Accordion.class, null, null, },
//            {Button.class, null, null, }, 
////            @Ignore("JDK-8241364")
////            {ButtonBar.class, null, null, }, 
//            {CheckBox.class, null, null, },
//            // @Ignore("JDK-8244657 memory leak and side-effect")
//            {ChoiceBox.class, 
//                (Consumer<Control>) c -> ((ChoiceBox) c).getItems().add("added"),
//                null, },
////            @Ignore("JDK-8241364")
////            {ColorPicker.class, null, null, },
////            @Ignore("JDK-8241364")
//            {ComboBox.class, null, null, },
//            {DateCell.class, null, null, },
////            @Ignore("JDK-8241364")
////            {DatePicker.class, null, null, },
//            {Hyperlink.class, null, null, },
//            {Label.class, 
//                null, 
//                // LabeledSkin keeps an invalidationListener on graphic
//                (Consumer<Control>) c -> ((Label) c).setGraphic(new Rectangle())
//                },
////            @Ignore("JDK-8241364")
////            {ListCell.class, null, null, },
////            @Ignore("JDK-8241364")
////            {ListView.class, null, null, },
////            @Ignore("JDK-8241364")
////            {MenuBar.class, null, null, },
////            @Ignore("JDK-8241364")
////            {MenuButton.class, null, null, },
////            @Ignore("JDK-8241364")
////            {Pagination.class, null, null, },
////            @Ignore("JDK-8241364")
////            {PasswordField.class, null, null, },
//            {ProgressBar.class, null, null, },
//            {ProgressIndicator.class, null, null, },
//            {RadioButton.class, null, null, },
////            @Ignore("JDK-8241364")
////            {ScrollBar.class, null, null, },
////            @Ignore("JDK-8241364")
////            {ScrollPane.class, null, null, },
//            {Separator.class, null, null, },
//            {Slider.class, null, null, },
////            @Ignore("JDK-8241364")
////            {Spinner.class, null, null, },
////            @Ignore("JDK-8241364")
////            {SplitMenuButton.class, null, null, },
////            @Ignore("JDK-8241364")
////            {SplitPane.class, null, null, },
//            {TableCell.class, null, null, },
////            @Ignore("JDK-8241364")
////            {TableRow.class, null, null, },
////            @Ignore("JDK-8241364")
////            {TableView.class, null, null, },
////            @Ignore("JDK-8241364")
////            {TabPane.class, null, null, },
////            // @Ignore("8244419")
////            // {TextArea.class, null, null, },
////            {TextField.class, null, null, },
//            {TitledPane.class, null, null, },
//            {ToggleButton.class, null, null, },
////            @Ignore("JDK-8241364")
////            {ToolBar.class, null, null, },
////            @Ignore("JDK-8241364")
////            {TreeCell.class, null, null, },
//            {TreeTableCell.class, null, null, },
////            @Ignore("JDK-8241364")
////            {TreeTableRow.class, null, null, },
////            @Ignore("JDK-8241364")
////            {TreeTableView.class, null, null, },
////            @Ignore("JDK-8241364")
////            {TreeView.class, null, null, },
//
//        };
        return Arrays.asList(data);
    }

    public SkinLeakTest(Supplier<Control> controlClass) { 
        this.controlSupplier = controlClass;
//        this.sideEffect = sideEffect;
//        this.configurator = configurator;
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
        assertNotNull(controlSupplier);
        
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException)throwable;
            } else {
                Thread.currentThread().getThreadGroup().uncaughtException(thread, throwable);
            }
        });

        control = controlSupplier.get();
    }

   

}
