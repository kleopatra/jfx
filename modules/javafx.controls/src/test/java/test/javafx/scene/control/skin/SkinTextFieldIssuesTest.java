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

import static javafx.scene.control.ControlShim.*;
import static javafx.scene.control.skin.TextInputSkinShim.*;
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
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.control.skin.TextInputSkinShim;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.KeyCode;
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
 */
public class SkinTextFieldIssuesTest {

    private Scene scene;
    private Stage stage;
    private Pane root;
    

//-------- empty test methods are stand-ins for testing effects of binding/mnanually registered listeners
    
    
//----- TextFieldSkin
    
    /**
     * Test deleteNextChar
     * NPE from updateSelection
     */
    @Test
    public void failedDeleteNextChar() {
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
     * Invalidation listener on prompt text fill: calls updateTextPos (no direct access to skinnable)
     * no side-effect, probably no leak (it's a property of the skin)
     */
    @Test
    public void testPromptFill() {
        TextField field = new TextField();
        field.setPromptText("prompt");
        installDefaultSkin(field);
        replaceSkin(field);
        TextInputSkinShim.setPromptTextFill(field, Color.MAGENTA);
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
        field.setPromptText("newe text");
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
     * Prompt text: it's a binding that has no effect?
     * 
     * null prompt -> replace skin -> set prompt
     * NPE in createPromptNode
     * 
     * called from skin's listener to usePrompt binding (which is not yet
     * cleaned out)
     * 
     * open: fixed by overriding dispose in usePromptText binding and calling dispose
     * in skin.dispose.
     */
    @Test
    public void failedPromptText() {
        TextField field = new TextField();
        installDefaultSkin(field);
        replaceSkin(field);
        field.setPromptText("prompt");
    }
    
    /**
     * InvalidationListener to textProperty -> NPE
     * 
     * Note: bubbles up in updateSelection, caused somewhere else?
     * - from listener to textNode.selectionShapeProperty ... must be some direct
     *  binding from textNode/text internals?
     */
    @Test
    public void failedText() {
        TextField field = new TextField("some text");
        installDefaultSkin(field);
        replaceSkin(field);
        field.setText("replaced");
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
     */
    @Test
    public void debatableTextNodeReplaced() {
        String initial = "some text";
        TextField field = new TextField(initial);
        installDefaultSkin(field);
        Text textNode = getTextNode(field);
        assertEquals("sanity: text sync'ed to textNode", initial, textNode.getText());
        replaceSkin(field);
        String replaced = "newe text";
        field.setText(replaced);
        assertEquals("text of replaced textNode changed", replaced, getTextNode(field).getText());
        assertEquals("text of initial textNode unchanged", initial, textNode.getText());
    }
    
    
    /**
     * InvalidationListener on alignmentProperty -> NPE
     * fixed by using skin api
     */
    @Test
    public void failedAlignment() {
        TextField field = new TextField("some text");
        showControl(field, true);
        assertTrue(field.getWidth() > 0);
        replaceSkin(field);
        field.setAlignment(Pos.TOP_RIGHT);
    }
    
    /**
     * InvalidationListener to font - not removed -> NPE
     * bubbles up in updateSelection from manually installed listener to textNode.selectionShapeProperty 
     * not fixed by using skin api on control.fontProperty: 
     *          textNode font is bound to control.font
     *          
     * PENDING - analysis no longer correct? after removing selectionShapeProperty 
     * this doesn't blow         
     */
    @Test
    public void failedFont() {
        TextField field = new TextField("some text");
        installDefaultSkin(field);
        replaceSkin(field);
        // doesn't help: the skin isn't gc'ed, the binding not gc'ed
//        WeakReference<?> weakSkin = new WeakReference<>(replaceSkin(field));
//        attemptGC(weakSkin);
        field.setFont(new Font(30));
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
     * Test caret position and move - implicit selection change.
     * 
     * show -> set caret -> replace skin -> change caret
     * 
     * was: a) NPE from updateSelection b) NPE from listener to caretPosition
     * 
     * Note: the listener to caretPosition is a no-op if control.width <= 0
     */
    @Test
    public void failedMoveShow() {
        TextField field = new TextField("initial");
        showControl(field, true);
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
     * InvalidationListener to selection - not removed -> was: NPE 
     */
    @Test
    public void failedSelectionUpdate() {
        TextField field = new TextField("some text");
        installDefaultSkin(field);
        replaceSkin(field);
        field.selectAll();
    }
    
    /**
     * Sanity test: ensure that skin's updating itself on selection change
     */
    @Test
    public void testTextNodeSelectionUpdate() {
        TextField field = new TextField("some text");
        installDefaultSkin(field);
        Text textNode = getTextNode(field);
        field.selectAll();
        int end = field.getLength();
        assertEquals("sanity: field caret moved to end", end, field.getCaretPosition());
        assertEquals("sanity: field selection updated", end, field.getSelection().getEnd());
        assertEquals("textNode end", end, textNode.getSelectionEnd());
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
     * 
     * Sanity: textNode caret must be updated on change of control caret.
     * 
     * manual changeListener to control.caretPosition
     * -> replace with api
     * 
     * show -> select
     * 
     * Note: textNode caretPosition only updated if control.width > 0 - why?
     * There are more locations that guard against width > 0 .. 
     * 
     * Even though not understood: all tests trying to see effects of those
     * syncs must install via the skin via show!
     * 
     */
    @Test
    public void testTextNodeCaretShow() {
        TextField field = new TextField("some text");
        showControl(field, true);
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
    
    /**
     * Children accumulating? textNode added via addAll (vs. setAll)
     * This is jdk-?? (todo: find issue) - not yet reported?
     * Only private test here, copied to special case in SpinnerSkin,
     * JDK-8245145
     */
    @Test @Ignore("JDK-8245145")
    public void failedChildren() {
        TextField field = new TextField("some text");
        installDefaultSkin(field);
        int children = field.getChildrenUnmodifiable().size();
        replaceSkin(field);
        assertEquals("children size must be unchanged: ", children, field.getChildrenUnmodifiable().size());
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
     * replaced by second skin - no effect expected?
     * 
     * Nevertheless, cleanup required
     */
    @Test
    public void testInputMethodRequests() {
        TextField field = new TextField("some text");
        InputMethodRequests im = field.getInputMethodRequests();
        installDefaultSkin(field);
//        showControl(field, true);
        assertNotNull("skin must have set inputMethodRequests", field.getInputMethodRequests());
        field.getSkin().dispose();
        assertEquals("inputMethodRequests must be reset", im, field.getInputMethodRequests());
    }
    
    /**
     * singleton eventHandler: calls handleInputMethodEvent -> access getSkinnable
     * expected: NPE
     * also: only set if null, that is the second skin does not reset it
     * 
     * no effect on memory when commented? It's about input methods (composed text?
     * touch screens? embedded only? language specific like chinese glyphs?)
     * not supported on my desktop - should remove in skin, even though not testable?
     * 
     */
    @Test
    public void testOnInputMethodTextChanged() {
        TextField field = new TextField("some text");
        EventHandler<?> handler = field.getOnInputMethodTextChanged();
        installDefaultSkin(field);
//        showControl(field, true);
        if (handler != null) {
            assertSame("inputMethodTextChanged handler must be unchanged",
                 handler, field.getOnInputMethodTextChanged());
        } else {
            assertNotNull("inputMethodTextChanged handler must be set", field.getOnInputMethodTextChanged());
        }
        field.getSkin().dispose();
        assertSame("inputMethodTextChanged handler must be reset", handler, field.getOnInputMethodTextChanged());
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
