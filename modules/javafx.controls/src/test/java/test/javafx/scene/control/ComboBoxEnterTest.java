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
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import com.sun.javafx.tk.Toolkit;

import static java.util.stream.Collectors.*;
import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;
import static org.junit.Assert.*;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxPopupControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import test.com.sun.javafx.pgstub.StubToolkit;
import test.com.sun.javafx.scene.control.infrastructure.KeyEventFirer;
import test.com.sun.javafx.scene.control.infrastructure.KeyModifier;

/**
 * c&p from combo experiment in old rep (local-experiment-enter)
 *
 * Note: register after showing has less false greens (due to InputMap backing out if
 * event is consumed), so do start by testing afterShowing and care about full test
 * matrix later on!
 */
public class ComboBoxEnterTest {

    private Toolkit tk;
    private Scene scene;
    private Stage stage;
    private Pane root;
    private ComboBox<String> comboBox;
    private Button button;
    private TextField textField;

// --- divers --- doesn't belong here, just during collecting combo issues

    /**
     * https://bugs.openjdk.java.net/browse/JDK-8087704
     * no action notification on value change before showing
     */
    @Ignore
    @Test
    public void testActionBeforeShowing() {
        List<ActionEvent> targetActions = new ArrayList<>();
        comboBox.setOnAction(e -> {
            targetActions.add(e);
        });
        comboBox.setValue("new value");
        assertEquals("action must be triggered", 1, targetActions.size());
    }

//----- default button on consume
// note: might all be related to broken event dispatch sequence similar to
// https://bugs.openjdk.java.net/browse/JDK-8229914 (the report is about filter
// on keyEvent Enter)

    /**
     * Test that default button is not triggered if combo action consumed the
     * the ActionEvent.
     *
     * Might be due to copied actionEvent? Or broken event dispatch by firing
     * up to parent?
     *
     * Note: need a change in editor to make combo action triggered.
     */
    @Test
    public void testButtonActions() {
        showAndFocus();
        button.setDefaultButton(true);
        List<Event> buttonActions = new ArrayList<>();
        button.setOnAction(buttonActions::add);
        List<Event> comboActions = new ArrayList<>();
        comboBox.setOnAction(e -> {
            e.consume();
            comboActions.add(e);
        });
        KeyEventFirer firer = new KeyEventFirer(null, scene);
        firer.doKeyTyped(A);
        firer.doKeyPress(ENTER);
        assertEquals("defaultButton must not have fired", 0, buttonActions.size());
    }
    /**
     * Enter without change - comboAction not fired.
     * This is per spec: action for combo is fired if value changed
     */
    @Test
    public void testComboActionsNoChange() {
        showAndFocus();
        List<ActionEvent> comboActions = new ArrayList<>();
        comboBox.setOnAction(e -> {
            e.consume();
            comboActions.add(e);
        });

        KeyEventFirer firer = new KeyEventFirer(null, scene);
        firer.doKeyPress(ENTER);
        assertEquals("comboBox must not have received action", 0, comboActions.size());

    }

    /**
     * Enter with change - comboAction fired.
     */
    @Test
    public void testComboActionsChanged() {
        showAndFocus();
        List<ActionEvent> comboActions = new ArrayList<>();
        comboBox.setOnAction(e -> {
            e.consume();
            comboActions.add(e);
        });

        KeyEventFirer firer = new KeyEventFirer(null, scene);
        firer.doKeyTyped(A);
        firer.doKeyPress(ENTER);
        assertEquals("comboBox must have received action", 1, comboActions.size());
    }

    /**
     * Enter without change - editor.
     *
     */
    @Test
    public void testEditorActionsNoChange() {
        showAndFocus();
        List<ActionEvent> editorActions = new ArrayList<>();
        comboBox.getEditor().setOnAction(e -> {
            e.consume();
            editorActions.add(e);
        });

        KeyEventFirer firer = new KeyEventFirer(null, scene);
        firer.doKeyPress(ENTER);
        assertEquals("editor must have received action", 1, editorActions.size());
    }

    /**
     * Enter change - editor.
     */
    @Test
    public void testEditorActionsChanged() {
        showAndFocus();
        List<ActionEvent> comboActions = new ArrayList<>();
        comboBox.getEditor().setOnAction(e -> {
            e.consume();
            comboActions.add(e);
        });

        KeyEventFirer firer = new KeyEventFirer(null, scene);
        firer.doKeyTyped(A);
        firer.doKeyPress(ENTER);
        assertEquals("editor must have received action", 1, comboActions.size());
    }

    /**
     * sanity: stand-alone textfield
     * Enter without change - textfield fired.
     */
    @Test
    public void testTextFieldActionsNoChange() {
        showAndFocus(textField);
        List<ActionEvent> fieldActions = new ArrayList<>();
        textField.setOnAction(e -> {
            e.consume();
            fieldActions.add(e);
        });
        KeyEventFirer firer = new KeyEventFirer(null, scene);
        firer.doKeyPress(ENTER);
        assertEquals("textField must have received action", 1, fieldActions.size());
    }


// --- revisit old tests from ComboBoxTest that fire on editor

    /**
     * @see ComboBoxTest.test_rt36280_editable_altDownShowsPopup_onTextField
     */
    @Test
    public void testAltDownShowPopupEditable() {
        showAndFocus();
        KeyEventFirer firer = new KeyEventFirer(null, scene);
        assertFalse(comboBox.isShowing());
        assertTrue(comboBox.getEditor().getText().isEmpty());
        firer.doKeyPress(KeyCode.DOWN, KeyModifier.ALT);  // show the popup
        assertTrue(comboBox.isShowing());
        assertTrue(comboBox.getEditor().getText().isEmpty());

    }

    /**
     * Guard against StackOverflow on Enter - and also commit on enter.
     * Note: this test is too broad, just getting a feeling ..
     *
     * @see ComboBoxTest.test_rt36717
     */
    @Test
    public void testStackOverFlow() {
        showAndFocus();
        comboBox.setEditable(false);
        comboBox.setEditable(true);
        String initial = comboBox.getValue();
        assertTrue(comboBox.isFocused());
        assertSame(scene.getFocusOwner(), comboBox);
        assertTrue(comboBox.getEditor().isFocused());

        List<ActionEvent> comboActions = new ArrayList<>();
        comboBox.setOnAction(comboActions::add);
        KeyEventFirer firer = new KeyEventFirer(null, scene);
        firer.doKeyTyped(A);
        assertEquals("A", comboBox.getEditor().getText());
        assertEquals(0, comboActions.size());
        assertEquals(initial, comboBox.getValue());
        firer.doKeyPress(ENTER);
        assertEquals("A", comboBox.getValue());
        assertEquals("comboBox must have received action", 1, comboActions.size());
    }


// ---------- test notification of enter eventFilters registered on the editor of a editable combo
// released is fine, pressed is not delivered - before/after showing doesn't make a difference

    /**
     * Regression: filter on editor is not notified.
     * original (fixed): https://bugs.openjdk.java.net/browse/JDK-8145515
     * again (new): https://bugs.openjdk.java.net/browse/JDK-8229914
     *
     * Note: this test passes always when using Keyfirer the old way, that is
     * firing onto the editor and delivers the key directly on
     * the editor. In real life, scene _does not_ deliver the key to the
     * editor (it's _not_ the focusOwner), but to the comboBox
     *
     * This now is using the new infrastructure, delivering the key to the scene.
     */
    @Test
    public void testRegisterEnterPressedFilterBeforeShowing() {
        List<KeyEvent> keys = new ArrayList<>();
        comboBox.getEditor().addEventFilter(KEY_PRESSED, keys::add);
        showAndFocus();
        KeyEventFirer keyboard = new KeyEventFirer(null, scene);
        keyboard.doKeyPress(ENTER);
        assertEquals(1, keys.size());
    }

    @Test
    public void testRegisterEnterReleasedFilterBeforeShowing() {
        List<KeyEvent> keys = new ArrayList<>();
        comboBox.getEditor().addEventFilter(KEY_RELEASED, keys::add);
        showAndFocus();
        KeyEventFirer keyboard = new KeyEventFirer(null, scene);
        keyboard.doKeyPress(ENTER);
        assertEquals(1, keys.size());
    }

    @Test
    public void testRegisterEnterReleasedFilterAfterShowing() {
        showAndFocus();
        List<KeyEvent> keys = new ArrayList<>();
        comboBox.getEditor().addEventFilter(KEY_RELEASED, keys::add);
        KeyEventFirer keyboard = new KeyEventFirer(null, scene);
        keyboard.doKeyPress(ENTER);
        assertEquals(1, keys.size());
    }

    @Test
    public void testRegisterEnterPressedFilterAfterShowing() {
        showAndFocus();
        List<KeyEvent> keys = new ArrayList<>();
        comboBox.getEditor().addEventFilter(KEY_PRESSED, keys::add);
        KeyEventFirer keyboard = new KeyEventFirer(null, scene);
        keyboard.doKeyPress(ENTER);
        assertEquals(1, keys.size());
    }

 //-------- test dispatch sequence

    /**
     * https://bugs.openjdk.java.net/browse/JDK-8229924
     * part of https://bugs.openjdk.java.net/browse/JDK-8149622
     * that's not fixed - editable combo
     */
    @Test
    public void testDispatchSequenceEnterReleasedEditable() {
        assertDispatchSequence(KEY_RELEASED);
    }

    /**
     * Guard against regression:
     * https://bugs.openjdk.java.net/browse/JDK-8149622 broken event
     * sequence in dispatch of keyReleased of enter
     */
    @Test
    public void testDispatchSequenceEnterReleasedNotEditable() {
        comboBox.setEditable(false);
        assertDispatchSequence(KEY_RELEASED);
    }

    @Test
    public void testDispatchSequenceEnterPressedEditable() {
        assertDispatchSequence(KEY_PRESSED);
    }

    @Test
    public void testDispatchSequenceEnterPressedNotEditable() {
        comboBox.setEditable(false);
        assertDispatchSequence(KEY_PRESSED);
    }

    /**
     * @param type
     */
    protected void assertDispatchSequence(EventType<KeyEvent> type) {
        showAndFocus();
        List<Event> keys = new ArrayList<>();
        comboBox.addEventFilter(type, keys::add);
        comboBox.addEventHandler(type, keys::add);
        scene.addEventFilter(type, keys::add);
        scene.addEventHandler(type, keys::add);
//        if (comboBox.isEditable()) {
//            comboBox.getEditor().addEventFilter(type,  keys::add);
//            comboBox.getEditor().addEventHandler(type, keys::add);
//        }

        List<Object> expected =
//                comboBox.isEditable() ?
//                List.of(scene, comboBox, comboBox.getEditor(), comboBox.getEditor(), comboBox, scene) :
                List.of(scene, comboBox, comboBox, scene);
        KeyEventFirer keyboard = new KeyEventFirer(null, scene);
        keyboard.doKeyPress(ENTER);
//        assertEquals("each handler/filter must be notified once for " + type, expected.size(), keys.size());
        List<Object> actual = keys.stream()
                .map(e -> e.getSource())
                .collect(toList());
        assertEquals("events" + failPrefix(keys), expected.size(), keys.size());
        assertEquals("event sources" + failPrefix(actual), expected.size(), actual.size());
    }

    @Test
    public void testDisableForwardFlag() {
        stage.show();
        assertEquals(true, comboBox.getEditor().getProperties().get("TextInputControlBehavior.disableForwardToParent"));
    }
//-------------- helpers

    protected String failPrefix(List<?> events) {
        String failPrefix = ": ";
        for (int i = 0; i < events.size(); i++) {
            failPrefix += "\n" + events.get(i);
        }
        return failPrefix;
    }


//--------------- initial and setup

    protected void showAndFocus() {
        showAndFocus(comboBox);
    }

    protected void showAndFocus(Node control) {
        stage.show();
        stage.requestFocus();
        control.requestFocus();
    }
    @After
    public void cleanup() {
        if (stage != null) {
            stage.hide();
        }
    }

    @Before
    public void setup() {
        ComboBoxPopupControl c;
        root = new VBox();
        scene = new Scene(root);
        stage = new Stage();
        stage.setScene(scene);
        comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Test", "hello", "world");
        comboBox.setEditable(true);
        button = new Button("some button");
        textField = new TextField("plain field");
        root.getChildren().addAll(comboBox, button, textField);
    }

 }
