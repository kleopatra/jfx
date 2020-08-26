/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.sun.javafx.tk.Toolkit;

import static java.util.stream.Collectors.*;
import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;
import static org.junit.Assert.*;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.ComboBoxPopupControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import test.com.sun.javafx.pgstub.StubToolkit;
import test.com.sun.javafx.scene.control.infrastructure.KeyEventFirer;

/**
 * 
 * <p>
 * Parameterized in concrete sub of ComboBoxBase and editable.
 *
 * Initial testing of other keys and notification count (for future use,
 * were not added F4 pull request)
 */
@RunWith(Parameterized.class)
public class ComboExtendedSpecialKeyTest {

    private Toolkit tk;
    private Scene scene;
    private Stage stage;
    private Pane root;
    private TextField textField;
    private Button button;

    private ComboBoxBase comboBox;
    private Supplier<ComboBoxBase> comboFactory;
    private String message;
    private boolean editable;

// esc dispatch sequence

    /**
     * this fails also with the fix, will pass only after
     * fixing event dispatch completely
     */
    @Test
    public void testEventSequenceEscapePressedFilter() {
        showAndFocus();
        List<Event> events = new ArrayList<>();
        EventHandler<KeyEvent> adder = events::add;
        scene.addEventFilter(KEY_PRESSED, adder);
        root.addEventFilter(KEY_PRESSED, adder);
        comboBox.addEventFilter(KEY_PRESSED, adder);
        KeyCode key = ESCAPE;
        KeyEventFirer keyFirer = new KeyEventFirer(null, scene);
        keyFirer.doKeyPress(key);
        assertEquals(failPrefix() + " event count", 3, events.size());
        List<Object> expected = List.of(scene, root, comboBox);
        List<Object> sources = events.stream()
                .map(e -> e.getSource())
                .collect(toList());
        assertEquals(failPrefix(), expected, sources);
    }

    /**
     * This passes for core,
     * fails with fix, will pass only after
     * fixing event dispatch completely
     */
    @Test
    public void testEventSequenceEscapeReleasedFilter() {
        showAndFocus();
        List<Event> events = new ArrayList<>();
        EventHandler<KeyEvent> adder = events::add;
        scene.addEventFilter(KEY_RELEASED, adder);
        root.addEventFilter(KEY_RELEASED, adder);
        comboBox.addEventFilter(KEY_RELEASED, adder);
        KeyCode key = ESCAPE;
        KeyEventFirer keyFirer = new KeyEventFirer(null, scene);
        keyFirer.doKeyPress(key);
        assertEquals(failPrefix() + " event count", 3, events.size());
        List<Object> expected = List.of(scene, root, comboBox);
        List<Object> sources = events.stream()
                .map(e -> e.getSource())
                .collect(toList());
        assertEquals(failPrefix(), expected, sources);
    }



//------------ normal key

    /**
     * Expected is a single notification - due to combo firing the event
     * onto the editor (if editable), it creates a nested dispatch
     * chain, so all filters above (the editor) are notified twice)
     */
    @Test
    public void testAComboFilter() {
        showAndFocus();
        List<KeyEvent> events = new ArrayList<>();
        comboBox.addEventFilter(KEY_PRESSED, e -> {
            KeyEvent copy = e.copyFor(e.getSource(), e.getTarget());
            if (e.isConsumed()) copy.consume();
            events.add(copy);
        });
        KeyEventFirer firer = new KeyEventFirer(comboBox);
        firer.doKeyPress(A);
        assertEquals(failPrefix(events) + ": filter must have recieved " , 1, events.size());
        assertFalse(failPrefix() + "event must not be consumed", events.get(0).isConsumed());
    }

    /**
     * This fails because of wrong test assumption: different
     * expected behaving depending not/editable - the editor
     * consumes all normal keys such that they are not passed
     * to the handler of its parent.
     */
    @Test
    public void testAComboHandler() {
        showAndFocus();
        List<KeyEvent> events = new ArrayList<>();
        comboBox.addEventHandler(KEY_PRESSED, e -> {
            KeyEvent copy = e.copyFor(e.getSource(), e.getTarget());
            if (e.isConsumed()) copy.consume();
            events.add(copy);
        });
        KeyEventFirer firer = new KeyEventFirer(comboBox);
        firer.doKeyPress(A);

        assertEquals(failPrefix() + ": handler must have recieved", editable ? 0 : 1, events.size());
        if (events.size() > 0)
            assertFalse(failPrefix() + "event must not be consumed", events.get(0).isConsumed());
    }


// ------------- F10
// trying to test/verify that f10 block in ComboBoxPopupControl.handleKeyEvent
// does nothing (never consumes)

    @Test
    public void testF10ComboFilter() {
        showAndFocus();
        List<KeyEvent> events = new ArrayList<>();
        comboBox.addEventFilter(KEY_PRESSED, e -> {
            KeyEvent copy = e.copyFor(e.getSource(), e.getTarget());
            if (e.isConsumed()) copy.consume();
            events.add(copy);
        });
        KeyEventFirer firer = new KeyEventFirer(comboBox);
        firer.doKeyPress(F10);
        assertEquals(failPrefix() + ": filter must have recieved", 1, events.size());
        assertFalse(failPrefix() + "event must not be consumed", events.get(0).isConsumed());
    }

    /**
     * This fails because ComboBoxBaseBehavior has a auto-consuming keyMapping
     * on F10 which fires the event to parent ..
     */
    @Test
    public void testF10ComboHandler() {
        showAndFocus();
        List<KeyEvent> events = new ArrayList<>();
        comboBox.addEventHandler(KEY_PRESSED, e -> {
            KeyEvent copy = e.copyFor(e.getSource(), e.getTarget());
            if (e.isConsumed()) copy.consume();
            events.add(copy);
        });
        KeyEventFirer firer = new KeyEventFirer(comboBox);
        firer.doKeyPress(F10);
        assertEquals(failPrefix() + ": handler must have recieved", 1, events.size());
        assertFalse(failPrefix() + ": event must not be consumed", events.get(0).isConsumed());
    }

//-------------- F4


    protected String failPrefix(List<KeyEvent> events) {
        String failPrefix = failPrefix();
        for (int i = 0; i < events.size(); i++) {
            failPrefix += "\n" + events.get(i);
        }
        return failPrefix;
    }
    /**
     * @return
     */
    protected String failPrefix() {
        String failPrefix = message + " editable = " + editable;
        return failPrefix;
    }

//----------------- filter notification count

    @Test
    public void testF4FilterNotified() {
        showAndFocus();
        List<KeyEvent> events = new ArrayList<>();
        comboBox.addEventFilter(KEY_RELEASED, e -> {
            if (e.getCode() == F4) {
                events.add(e);
            }
        });
        KeyEventFirer firer = new KeyEventFirer(comboBox);
        firer.doKeyPress(F4);
        assertEquals(failPrefix(events) +": Filter released F4 must have received ", 1, events.size());
    }


//---------------- parameterized

    // Note: name property not supported before junit 4.11
    @Parameterized.Parameters(name = "{index}: class {2} editable {1} ")
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
            {(Supplier)ComboBox::new, false, "comboBox" },
            {(Supplier)ComboBox::new, true, "comboBox" },
            {(Supplier)DatePicker::new, false, "datePicker" },
            {(Supplier)DatePicker::new, true, "datePicker" },
            {(Supplier)ColorPicker::new, false, "colorPicker" },
            // editable doesn't make a difference for colorPicker
        };
        return Arrays.asList(data);
    }

    public ComboExtendedSpecialKeyTest(Supplier<ComboBoxBase> factory, boolean editable, String message) {
        this.comboFactory = factory;
        this.editable = editable;
        this.message = message;
    }

// --- initial and setup

    @Test
    public void testInitialState() {
        assertNotNull(comboBox);
        assertEquals(editable, comboBox.isEditable());
    }

     protected void showAndFocus() {
        showAndFocus(comboBox);
    }

    protected void showAndFocus(Node control) {
        stage.show();
        stage.requestFocus();
        control.requestFocus();
        assertTrue(control.isFocused());
        assertSame(control, scene.getFocusOwner());
    }

    @After
    public void cleanup() {
        stage.hide();
    }

    @Before
    public void setup() {
        ComboBoxPopupControl c;
        // This step is not needed (Just to make sure StubToolkit is
        // loaded into VM)
        tk = (StubToolkit) Toolkit.getToolkit();
        root = new VBox();
        scene = new Scene(root);
        stage = new Stage();
        stage.setScene(scene);
        comboBox = comboFactory.get();
        comboBox.setEditable(editable);
        button = new Button("some button");
        textField = new TextField("plain field");
        root.getChildren().addAll(comboBox, button, textField);
    }


}
