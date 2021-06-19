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

import com.sun.javafx.tk.Toolkit;

import static javafx.scene.control.ControlShim.*;
import static javafx.scene.control.skin.TextInputSkinShim.*;
import static javafx.scene.control.skin.UnusedSkinShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import test.com.sun.javafx.scene.control.infrastructure.KeyEventFirer;

/**
 * Temp test for TextField skin issues JDK-8240506
 * Extracted from SkinIssuesTest - to be moved to SkinCleanupTest and SkinMemoryTest for commit.
 *
 * Overall test structure:
 *
 * for each listener installation that's moved to use skin api
 * - have a test that ensures
 *   the listener is working: should fail/pass without the listener - must pass before/after the fix
 *   this guarantees that it's correctly re-wired
 * - have a test for a side-effect: failing before/passing after
 *
 * Note that removing some listeners seems to have no macroscopic effect in test context:
 * - control text, font: also not in visual test
 * - textNode selectionShape: selection highlight visually broken
 *
 */
public class SkinTextFieldIssuesTest {

    private Scene scene;
    private Stage stage;
    private Pane root;


// -- caretPosition

//-------- guarding against listeners not installed

    /**
     * Unclear effect, see related test below and code comment in skin.
     * Seems to be some internal update needed
     *
     * f.i. in a visual test, select text, change font -> selection not updated,
     * broken rectangle (too small or unrelated at all)
     *
     * The selection highlight is not updated correctly in test context..
     */
//    @Test
//    public void testListeningSelectionShape() {
//        // field to get the selectionPath from
//        TextField largeFont = new TextField("dummy");
//        int fontSize = 30;
//        largeFont.setFont(new Font(fontSize));
//        showControl(largeFont, true);
//        Toolkit.getToolkit().firePulse();
////        System.out.println("selected? " + largeFont.getSelection());
//        assertSame(scene.getFocusOwner(), largeFont);
//        Path largeSelectionPath = getSelectionPath(largeFont);
//        Toolkit.getToolkit().firePulse();
//        largeFont.layout();
////        assertFalse("" + selectionPath.getElements(), selectionPath.getElements().isEmpty());
//        // field to test: start with left, then change to right align while showing
//        TextField field = new TextField("dummy");
//        showControl(field, true);
//        Toolkit.getToolkit().firePulse();
//        Path selectionPath = getSelectionPath(field);
//        selectionPath.getElements().addListener((ListChangeListener)c -> {
//            System.out.println("getting change?" + c);
//        });
//        assertFalse("" + selectionPath.getElements(), selectionPath.getElements().isEmpty());
//    }

    /**
     * The textNode is bound directly to the control's font.
     * triggers layout pass only, how to test?
     *
     * No macroscopic effect visible when removing font listener
     */
    @Test
    public void testListeningFont() {
        TextField field = new TextField("some text");
        assertEquals("sanity: ", Pos.CENTER_LEFT, field.getAlignment());
        field.setPrefColumnCount(50);
        showControl(field, true);
        double textHeight = getTextNode(field).getLayoutBounds().getHeight();
        field.setFont(new Font(30));
//        Toolkit.getToolkit().firePulse();
        assertEquals(10, getTextNode(field).getLayoutBounds().getHeight(), 0.1);
    }

    /**
     * Text listener is updating translateX
     *
     * passes independently of whether the text listener is installed?
     */
    @Test
    public void testListeningText() {
//        TextField standin = new TextField("do nothing");
//        standin.setPrefColumnCount(50);
//        showControl(standin, true);
        String first = "some";
        String second = "dummy";
        TextField comparing = new TextField();
        comparing.setPrefColumnCount(50);
        comparing.setAlignment(Pos.CENTER_RIGHT);
        showControl(comparing, true);
        Toolkit.getToolkit().firePulse();
        double compareTranslate = getTextTranslateX(comparing);
        Text textNode = getTextNode(comparing);
        assertEquals(0, textNode.getLayoutBounds().getWidth(), 1);
        comparing.appendText(second);
        Toolkit.getToolkit().firePulse();
        assertEquals(compareTranslate - textNode.getLayoutBounds().getWidth(), getTextTranslateX(comparing), 1);

//        TextField field = new TextField(first);
//        field.setPrefColumnCount(50);
//        field.setAlignment(Pos.CENTER);
//        showControl(field, true);
//        field.appendText(second);
//        Toolkit.getToolkit().firePulse();
////        Path selectionPath = getSelectionPath(comparing);
////        System.out.println(selectionPath.getElements());
//        assertEquals(compareTranslate, getTextTranslateX(field), 1);
    }




//----- TextFieldSkin

    /**
     * Invalidation listener on prompt text fill: calls updateTextPos (no direct access to skinnable)
     * no side-effect, probably no leak (it's a property of the skin)
     */
    @Test
    public void testPromptFill() {
        TextField field = new TextField();
        field.setPromptText("prompt");
        installDefaultSkin(field);
        replaceSkin(field);
        setPromptTextFill(field, Color.MAGENTA);
    }

    /**
     * Prompt text: it's a binding that has no effect?
     * prompt -> replace skin -> changed prompt
     */
    @Test
    public void testPromptTextChange() {
        TextField field = new TextField();
        field.setPromptText("prompt");
        installDefaultSkin(field);
        replaceSkin(field);
        field.setPromptText("new text");
    }

    /**
     * Prompt text: it's a binding that has no effect?
     * prompt -> replace skin -> null prompt
     */
    @Test
    public void testPromptTextNull() {
        TextField field = new TextField();
        field.setPromptText("prompt");
        installDefaultSkin(field);
        replaceSkin(field);
        field.setPromptText(null);
    }

    /**
     * Side-effect: changing text after switching skin effects old textNode
     *
     * General issue: the binding will be gc'ed sooner or later but until then
     * is updating itself it least along with the changes of the field.
     * There are more bindings (untested) doing the same.
     *
     * Question: is that a problem or not? As long as the field isn't updated
     * to stale state, that should be okay? Also, replacement skin does the same.
     *
     * This text was completely wrong: it kept a strong reference to the node ..
     * Replaced by both a weak ref to the skin and the node, trigger gc of skin:
     * NPE because the skin/node indeed is gc'ed.
     *
     * So it's down to the usual: what happens between replacement of the skin and
     * a gc - we have to make sure that the binding doesn't call back into the
     * skinnable.
     */
    @Test @Ignore("incorrect test setup")
    public void debatableTextNodeReplaced() {
        String initial = "some text";
        TextField field = new TextField(initial);
        installDefaultSkin(field);
        WeakReference<Text> textNode = new WeakReference<>(getTextNode(field));
        assertEquals("sanity: text sync'ed to textNode", initial, textNode.get().getText());
        WeakReference<?> weakSkin = new WeakReference<>(replaceSkin(field));
        attemptGC(weakSkin);
        String replaced = "newe text";
        field.setText(replaced);
        assertEquals("text of replaced textNode changed", replaced, getTextNode(field).getText());
        assertEquals("text of initial textNode unchanged", initial, textNode.get().getText());
    }


    /**
     * Test caret position and move - implicit selection change.
     * was: NPE from updateSelection
     *
     * install skin -> set caret -> replace skin -> change caret
     */
    @Test
    public void failedMove() {
        TextField field = new TextField("initial");
        installDefaultSkin(field);
        int index = 2;
        field.positionCaret(index);
        replaceSkin(field);
        assertEquals(index, field.getCaretPosition());
        field.positionCaret(index + 1);
    }

    /**
     * TextNode (the view installed by the skin) has listener on its selectionShape
     * that calls skin updateSelection (which access getSkinnable)
     *
     * deep down is a binding to Text's selectionStart/-End
     *
     * ---------------
     *
     * here trying to find a failing test if the listener to selectionShape
     * is removed.
     *
     * Note: the listener calls skin.updateSelection which sync's from
     * textField to textNode (not the other way round), so can't
     * expect any change in the textField state.
     *
     * Which poses the eternal WHY? Probably not needed..
     *
     * Decision: give up trying to detect changes, just register/remove
     * InvalidationListener (to neither throw NPE nor SO).
     */
    @Test @Ignore("FIXME: unclear reason for having selectionShapeListener")
    public void testTextNodeSelectionShapeShow() {
        TextField field = new TextField("initial words");
        int target = field.getText().indexOf("words");
        showControl(field, true);
        TextFieldSkin skin = (TextFieldSkin) field.getSkin();
        Text textNode = getTextNode(field);
//        skin.setForwardBias(true);
        textNode.setSelectionStart(0);
        textNode.setSelectionEnd(target);
        assertEquals("textNode caret", target, textNode.getCaretPosition());
//        field.nextWord();
        assertEquals(target, field.getCaretPosition());
    }


    /**
     * Sanity: test textNode state for empty selection
     */
    @Test
    public void testTextNodeSelectionEmpty() {
        TextField field = new TextField("some text");
        IndexRange initial = field.getSelection();
        // characterize empty selection state
        assertEquals("sanity: empty selection", 0, initial.getLength());
        assertEquals("sanity: start of empty selection ", 0, initial.getStart());
        assertEquals("sanity: end of empty selection ", 0, initial.getEnd());
        assertEquals("sanity: caret at end", field.getCaretPosition(), initial.getEnd());
        installDefaultSkin(field);
        assertEquals("sanity: initial selection unchanged by skin", initial, field.getSelection());
        Text textNode = getTextNode(field);
        assertEquals("textNode start", -1, textNode.getSelectionStart());
        assertEquals("textNode end", -1, textNode.getSelectionEnd());
        assertEquals("textNode caret", field.getCaretPosition(), textNode.getCaretPosition());
    }

    /**
     * fails before/after fix: textNode caret not updated to textField caret
     *
     * install skin -> select
     *
     * So here we have a wrong test assumption!
     */
    @Test @Ignore("wrong test assumption: control width must be > 0")
    public void failedTextNodeCaretInstallSkin() {
        TextField field = new TextField("some text");
        installDefaultSkin(field);
        Text textNode = getTextNode(field);
        field.selectAll();
        assertEquals("textNode caret", field.getCaretPosition(), textNode.getCaretPosition());
    }

    /**
     * Sanity: initial textNode caret
     *
     * select -> install skin
     *
     */
    @Test
    public void testTextNodeCaretInitial() {
        TextField field = new TextField("some text");
        field.selectAll();
        installDefaultSkin(field);
        Text textNode = getTextNode(field);
        assertEquals("textNode caret", field.getCaretPosition(), textNode.getCaretPosition());
    }

    /**
     * NPE from listener to caretPosition
     * without scene, this is the same update/failure path as selection
     *
     * Note: the listener itself a no-op if control.width <= 0, indirect effect
     * PENDING: remove from test?
     */
    @Test
    public void testTextFieldCaretPosition() {
        TextField field = new TextField("initial");
        installDefaultSkin(field);
        int index = 2;
        field.positionCaret(index);
        replaceSkin(field);
        field.positionCaret(index + 1);
    }

    /**
     * NPE from listener to (skin internal) selectionShape
     */
    @Test
    public void testTextFieldSelectionShape() {
        TextField field = new TextField();
        field.setText("initial");
        installDefaultSkin(field);
        int index = 2;
        field.positionCaret(index);
        replaceSkin(field);
        assertEquals(index, field.getCaretPosition());
        field.deleteNextChar();
    }


    /**
     * listener to skin's property - no problem?
     * There are many  more internal bindings .. all with the same caveat as noted below
     *
     * changeable only from skin internals (on mouse events from skin/behaviour), so
     * don't expect macroscopic residues after switching skin?
     */
    @Test
    public void testForwardBias() {
        TextField field = new TextField("some text");
        field.nextWord();
        showControl(field, true);
        TextFieldSkin skin = (TextFieldSkin) field.getSkin();
    }

//------- TextInputControlSkin

//---------- listeners/eventhandler

    /**
     * package-private method accessing getSkinnable,
     * used in private nextCharVisually which is called in public skin.moveCaret
     * which is called in TextInputControlBehavior.nextCharVisually (access of _current skin_
     * not the one stored!)
     *
     * should not cause trouble: the mappings are removed - if any, it's a problem of the
     * behavior (how to test?)
     *
     * Sanity: old behavior doesn't interfere.
     */
    @Test
    public void testIsRTL() {
        TextField field = new TextField("some text");
        showControl(field, true);
        int caret = 2;
        field.positionCaret(caret);
        KeyEventFirer firer = new KeyEventFirer(field);
        firer.doRightArrowPress();
        assertEquals(caret + 1, field.getCaretPosition());
        replaceSkin(field);
        firer.doRightArrowPress();
        assertEquals(caret + 2, field.getCaretPosition());
    }

    /**
     * Trying to test effect of listener textProperty in TextInputControlBehavior.
     * Was added twice, need to make certain that it's still functional
     * after cleanup
     *
     * Problem: navigation keys occasionally not working at all (in visual test)
     * and direction always the same - misunderstanding of caretPosition?
     *
     * there's an open bug: https://bugs.openjdk.java.net/browse/JDK-8242616
     * SO: https://stackoverflow.com/q/61184745/203657
     *
     */
    @Ignore("8242616")
    @Test
    public void testBidiListener() {
        TextField field = new TextField("some text");
        showControl(field, true);
        int caret = 2;
        field.positionCaret(caret);
        KeyEventFirer firer = new KeyEventFirer(field);
        firer.doRightArrowPress();
        assertEquals(caret + 1, field.getCaretPosition());
        field.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        firer.doRightArrowPress();
        assertEquals(caret, field.getCaretPosition());
    }

    @Ignore("8242616")
    @Test
    public void testRToLArrows() {
        TextField field = new TextField("some text");
        field.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        showControl(field, true);
        int caret = 3;
        field.positionCaret(caret);
        assertEquals(caret, field.getCaretPosition());
        KeyEventFirer firer = new KeyEventFirer(field);
        firer.doRightArrowPress();
        assertEquals(caret - 1, field.getCaretPosition());
    }

    @Ignore("8242616")
    @Test
    public void testLToRArrows() {
        TextField field = new TextField("some text");
        field.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        showControl(field, true);
        int caret = 3;
        field.positionCaret(caret);
        assertEquals("sanity caret position", caret, field.getCaretPosition());
        KeyEventFirer firer = new KeyEventFirer(field);
        firer.doRightArrowPress();
        assertEquals("incremented caret by key-right", caret + 1, field.getCaretPosition());
    }

//------- bindings

    /**
     * Binding to several control properties determining the caret visibility
     *
     * Used in TextFieldSkin to toggle opacity of caretPath (in a binding, also)
     *
     * Bindings as such are not a problem, as long as their value is not computed?
     * Also, they remove themselves from the gc'ed - they don't here but: already are
     * invalidated in the replaced skin, no collaborator tries to query them
     * (because the skin is no longer part of the scenegraph) so
     * they don't blow, even when they are accessing control properties.
     */
    @Test
    public void testCaretVisibleShow() {
        TextField field = new TextField("some text");
        showControl(field, true);
        replaceSkin(field);
        field.setEditable(false);
        field.nextWord();
    }

    @Test
    public void testCaretVisibleSkin() {
        TextField field = new TextField("some text");
        installDefaultSkin(field);
        replaceSkin(field);
        field.setEditable(false);
        field.nextWord();
    }

    /**
     * binding to control.fontProperty
     *
     * calling invalidateMetrics: no-op for textFieldSkin, resetting internal
     * cache in TextAreaSkin
     *
     * no side-effect expected
     */
    @Test
    public void testFontMetrics() {

    }
//--------------

    /**
     * Can't test?: a manually installed listener if VK is enabled.
     * Changed to use skin api.
     *
     * Sanity test would be to test if still functional.
     * Test: not/leaking after/before
     */
    @Ignore("FXVK")
    @Test
    public void testFXVKMemoryLeak() {
        System.out.println("fxvk: " + Platform.isSupported(ConditionalFeature.VIRTUAL_KEYBOARD));
        fail("tbd: test listener registered to focusedProperty with FXVK");
    }
    /**
     * default skin -> set alternative
     */
    @Test
    public void failedMemoryLeakAlternativeSkin() {
        TextField control = new TextField();
        installDefaultSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(control));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }

    @Ignore("JDK-?? leaking when showing")
    @Test
    public void failedMemoryLeakAlternativeSkinShowing() {
        TextField control = new TextField();
        showControl(control, true);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(control));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }


//-------------------- setup
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
    }

 // Testing binding: suspect that bindings in skin are not cleaned?

    // Note: bindings will remove themselves as listener from the long-lived property
    // if they are no longer reachable at the time of the notification
    // drawbacks:
    // a) cleanup not happening if the long-lived property doesn't change
    // b) active between dispose and gc
    // Note: all bindings access the parameter (vs. getSkinnable)
    // so there's no NPE after dispose

    public static class LongLived {

        BooleanProperty flag = new SimpleBooleanProperty();
        ObjectProperty<ShortLived> shortLived;
        String collected = "A";

        public LongLived() {
            shortLived = new SimpleObjectProperty<>();
            setShortLived(new ShortLived(this));
        }

        public ObjectProperty<ShortLived> shortLivedProperty() {
            return shortLived;
        }

        public void setShortLived(ShortLived shortLived) {
            shortLivedProperty().set(shortLived);
        }

        public ShortLived getShortLived() {
            return shortLivedProperty().get();
        }

        public void addLog(String t) {
            collected += t;
        }
    }

    public static class ShortLived {
        static int counter;
        int id;
        LongLived longLived;
        ObservableBooleanValue flagged;

        public ShortLived(LongLived longLived) {
            id = counter++;
            this.longLived = longLived;
            flagged = new BooleanBinding() {
                {
                    bind(longLived.flag);
                }

                @Override
                protected boolean computeValue() {
//                    System.out.println("compute in: " + id);
                    longLived.addLog("" + id);
                    return !longLived.flag.get();
                }

            };
        }
    }

    /**
     * Memory does not leak, binding still active after reset "skin" - only on access, though.
     *
     * Still sitting in memory until the longLived prop _does_ change.
     */
    @Test @Ignore
    public void testBindingMemoryLeak() {
        LongLived longLived = new LongLived();
        WeakReference<ShortLived> weakRef = new WeakReference<>(longLived.getShortLived());
//        System.out.println("before: ---- old:" + weakRef.get().flagged.getValue() + " new: "
//                + longLived.getShortLived().flagged.getValue() + " collected: " + longLived.collected);
        longLived.setShortLived(new ShortLived(longLived));
        longLived.flag.set(true);
        System.out.println("after: ------ old:" + weakRef.get().flagged.getValue() + " new: "
                + longLived.getShortLived().flagged.getValue() + " collected: " + longLived.collected);
        attemptGC(weakRef);
        assertEquals("binding must be gc'ed", null, weakRef.get());
        System.out.println(longLived.collected);
    }

//------------------ end

}
