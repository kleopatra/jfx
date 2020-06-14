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

import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static javafx.scene.control.ControlShim.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Test issues with Labeled that turned up in cleanup task.
 * <p>
 * This test is parameterized on type of Labeled.
 */
@RunWith(Parameterized.class)
public class SkinLabeledIssuesTest {

    private Scene scene;
    private Stage stage;
    private Pane root;

    private Labeled control;

// ------------ test issues around graphic
    
    /**
     * Cleanup graphic listener -
     */
    @Test
    public void testLabeledGraphicDispose() {
        Rectangle graphic = (Rectangle) control.getGraphic();
        installDefaultSkin(control);
        control.getSkin().dispose();
        graphic.setWidth(500);
    }
    
//----------- parameterized
    
    @Parameterized.Parameters //(name = "{index}: {0} ")
    public static Collection<Object[]> data() {
        Button button = new Button("dummy", new Rectangle());
        CheckBox checkBox = new CheckBox("dummy");
        checkBox.setGraphic(new Rectangle());
        Hyperlink hyperlink = new Hyperlink("dummy", new Rectangle());
        Label label = new Label("dummy", new Rectangle());
        // leaking w/out - fix than add here
        //MenuButton menuButton = new MenuButton("", new Rectangle());
        ToggleButton toggleButton = new ToggleButton("", new Rectangle());
        RadioButton radioButton = new RadioButton("");
        radioButton.setGraphic(new Rectangle());
        TitledPane titled = new TitledPane();
        titled.setGraphic(new Rectangle());
        List<Labeled> controls = List.of(                
                button,
                checkBox,
                hyperlink,
                label, 
//                menuButton,
                toggleButton,
                radioButton,
                titled
                );
        return asArrays(controls);
    }
    
    public SkinLabeledIssuesTest(Labeled control) {
        this.control = control;
    }
//---------------- setup/cleanup
    
    @After
    public void cleanup() {
        stage.hide();
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

        root = new VBox();
        scene = new Scene(root);
        stage = new Stage();
        stage.setScene(scene);
    }


}
