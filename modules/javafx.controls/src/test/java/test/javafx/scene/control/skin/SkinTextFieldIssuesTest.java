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
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;
import static javafx.scene.control.skin.TextInputSkinShim.*;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextInputSkinShim;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
        field.setText("newe text");
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
     * TextNode (the view installed by the skin) has listener on its selectionShape
     * that calls skin updateSelection (which access getSkinnable)
     * 
     * deep down is a binding to Text's selectionStart/-End
     */
    @Test
    public void testTextNodeSelectionShape() {
        
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
     * Note: textNode caretPosition only updated if control.width > 0 - why?
     */
    @Test
    public void failedTextNodeCaret() {
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
     * manual changeListener to control.caretPosition
     * -> replace with api
     * 
     * show -> select
     * 
     * Note: textNode caretPosition only updated if control.width > 0 - why?
     */
    @Test
    public void testTextNodeCaretPositionUpdate() {
        TextField field = new TextField("some text");
        showControl(field, true);
        Text textNode = getTextNode(field);
        field.selectAll();
        assertEquals("textNode caret", field.getCaretPosition(), textNode.getCaretPosition());
    }
    
    /**
     * Children accumulating? textNode added via addAll (vs. setAll)
     */
    @Test
    public void failedChildren() {
        TextField field = new TextField("some text");
        installDefaultSkin(field);
        int children = field.getChildrenUnmodifiable().size();
        replaceSkin(field);
        assertEquals("children size must be unchanged: ", children, field.getChildrenUnmodifiable().size());
    }
    /**
     * listener to skin's property - no problem?
     * There are many  more internal bindings .. all with the same caveat as noted below
     */
    @Test
    public void testForwardBias() {
        
    }
//------- TextInputControlSkin  
    
//---------- listeners/eventhandler 
    
    /**
     * package-private method accessing getSkinnable, 
     * used in private nextCharVisually which is called in public skin.moveCaret
     * which is called in TextInputControlBehavior.nextCharVisually (access of _current skin_
     * not the one stored!)
     */
    @Test
    public void testIsRTL() {
        
    }
    /**
     * replaced by second skin - no effect expected?
     */
    @Test
    public void testInputMethodRequests() {
        
    }
    /**
     * singleton eventHandler: calls handleInputMethodEvent -> access getSkinnable
     * expected: NPE
     * also: only set if null, that is the second skin does not resets it
     */
    @Test
    public void testOnInputMethodTextChanged() {
        
    }
    
//------- bindings
    
    /**
     * Binding to several control properties determining the caret visibility
     * 
     * Used in TextFieldSkin to toggle opacity of caretPath (in a binding, also)
     */
    @Test
    public void testCaretVisible() {
        
    }
    
    /**
     * binding to control.fontProperty
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
