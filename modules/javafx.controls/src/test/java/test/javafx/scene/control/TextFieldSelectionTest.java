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

package test.javafx.scene.control;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import test.com.sun.javafx.scene.control.infrastructure.KeyEventFirer;

/**
 * Note: don't need skin, selected text if handled in control.
 * 
 * Bug is happening only if the selectedText binding is forced to be evaluated
 * by a ChangeListener!
 */
public class TextFieldSelectionTest {

    private TextField textField;
    private Scene scene;
    private Stage stage;
    private Pane root;

    
    @Test
    public void testReplaceNotification() {
        String initial = "ABCDEFG";
        String replace = "12";
        int start = 2;
        int end = 5;
        textField.setText(initial);
        textField.selectedTextProperty().addListener((o, ov, nv) -> {
                System.out.println("selectedText: " + ov + " / " + nv);
            });
        textField.selectRange(start,  end);
        textField.textProperty().addListener((src, ov, selection) -> System.out.println(selection));
        textField.replaceText(start, end, replace);
        
    }
    /**
     * From bug report 
     * solved by reverting the order of action in replaceText
     * does it really?
     */
    @Test 
    public void replaceSelectionWithListener() {
        StringBuilder log = new StringBuilder();
        textField.setText("sabce");
        textField.selectRange(2, 3);
        System.out.println("before: " + textField.getSelectedText() + "/range:" + textField.getSelection());
        textField.selectionProperty().addListener((scr, ov, nv) -> System.out.println("notification from range: " + nv));
        textField.selectedTextProperty().addListener((src, ov, selection) -> log.append("|" + selection + "|"));
        textField.insertText(0, "X");
//        textField.deleteText(0, 1);
//        textField.replaceSelection("x");
//        assertEquals("||", log.toString());
        System.out.println("after: " + textField.getSelectedText() + "/range:" + textField.getSelection());
    }

    
    @Test 
    public void testSelectReplaceChangeListener() {
        textField.setText("1234 5678");
        textField.selectionProperty()
            .addListener((o, ov, nv) -> {}); //System.out.println("selection: " + ov + " / " + nv);});
        textField.selectedTextProperty()
            .addListener((o, ov, nv) -> {}); //>System.out.println("selectedText: " + ov + " / " + nv);});
        
        textField.positionCaret(5);
        textField.selectEnd();
        textField.replaceSelection("d");
    }

//------- loosely related
    /**
     * With suggested fix, there's a test failure in comboBoxTest.test_rt35840
     * here we test text not empty, set after showing -> no selection
     */
    @Test
    public void testInsertIntoNotEmptySetAfterShowing() {
        String initial = "initial";
        showAndFocus();
        textField.setText(initial);
        KeyEventFirer keyboard = new KeyEventFirer(textField);
        keyboard.doKeyTyped(KeyCode.T);
        keyboard.doKeyTyped(KeyCode.E);
        keyboard.doKeyTyped(KeyCode.S);
        keyboard.doKeyTyped(KeyCode.T);
        assertEquals("TEST" + initial, textField.getText());
    }
    
    /**
     * With suggested fix, there's a test failure in comboBoxTest.test_rt35840
     * here we test text not empty, set before showing -> initial is selected
     */
    @Test
    public void testInsertIntoNotEmptySetBeforeShowing() {
        String initial = "initial";
        textField.setText(initial);
        showAndFocus();
        assertEquals("sanity: initially selected ", initial, textField.getSelectedText());
        KeyEventFirer keyboard = new KeyEventFirer(textField);
        keyboard.doKeyTyped(KeyCode.T);
        keyboard.doKeyTyped(KeyCode.E);
        keyboard.doKeyTyped(KeyCode.S);
        keyboard.doKeyTyped(KeyCode.T);
        assertEquals("TEST", textField.getText());
    }
    
    
    /**
     * With suggested fix, there's a test failure in comboBoxTest.test_rt35840
     * initial insert is broken - here we do the same for a plain textField.
     */
    @Test
    public void testInsertIntoEmpty() {
        showAndFocus();
        KeyEventFirer keyboard = new KeyEventFirer(textField);
        keyboard.doKeyTyped(KeyCode.T);
        keyboard.doKeyTyped(KeyCode.E);
        keyboard.doKeyTyped(KeyCode.S);
        keyboard.doKeyTyped(KeyCode.T);
        assertEquals("TEST", textField.getText());
    }
    

    
//----------- showing not really needed
    
    /** 
     * Test for JDK-8176270: register invalidationListener
     * 
     */
    @Test 
    public void testSelectReplaceInvalidationListenerShowing() {
        showAndFocus();
        textField.setText("1234 5678");
        textField.selectedTextProperty()
            .addListener((observable -> {}));
        
        
        textField.positionCaret(5);
        // select 2nd word
        textField.selectNextWord();
        textField.replaceSelection("d");
        
    }
    
    /** 
     * Test for JDK-8176270: register changeListener
     * 
     */
    @Test 
    public void testSelectReplaceChangeListenerShowing() {
        showAndFocus();
        textField.setText("1234 5678");
        textField.selectedTextProperty()
            .addListener((o, ov, nv) -> {});
        textField.positionCaret(5);
        // select 2nd word
//        textField.selectNextWord();
        textField.selectEnd();
        textField.replaceSelection("d");
        
    }
    
    //------------------ setup    

    protected void showAndFocus() {
        if (stage == null) {
            root = new VBox();
            scene = new Scene(root);
            stage = new Stage();
            stage.setScene(scene);
        }
        root.getChildren().addAll(textField);
        stage.show();
        stage.requestFocus();
        textField.requestFocus();
        assertTrue(textField.isFocused());
        assertSame(textField, scene.getFocusOwner());
    }
    
    @Before public void setup() throws Exception {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException)throwable;
            } else {
                Thread.currentThread().getThreadGroup().uncaughtException(thread, throwable);
            }
        });
        textField = new TextField();
    }


    @After public void cleanup() {
        Thread.currentThread().setUncaughtExceptionHandler(null);
        if (stage != null) {
            stage.hide();
        }
    }


}
