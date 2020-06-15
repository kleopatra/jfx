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
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.javafx.tk.Toolkit;

import static javafx.scene.control.ControlShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 */
public class SkinTableIssuesTest {

    private Scene scene;
    private Stage stage;
    private Pane root;

    private static final boolean showPulse = false;
    private static final boolean methodPulse = true;

//------------- TableView

    /**
     * how to trigger width change of table?
     * TableHeaderRow has listeners that must be removed, how to test without change?
     */
    @Test
    public void testTableWidth() {
        TableView<Locale> control =  new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().addAll(column);
        BorderPane root = new BorderPane(control);
        scene.setRoot(root);
        stage.show();
//        showControl(control, true);
        control.widthProperty().addListener(e -> System.out.println("getting width change"));
        root.widthProperty().addListener(e -> 
            System.out.println("getting width change from root: " + root.getWidth() + " table: " + control.getWidth()));
//        replaceSkin(control);
        column.setPrefWidth(500);
        control.setPrefWidth(500);
        stage.setWidth(1000);
        fireMethodPulse();
    }
    
    @Test
    public void failColumnWidthNPE() {
        TableView<Locale> control =  new TableView<>();
        TableColumn<Locale, String> column = new TableColumn<>("dummy");
        control.getColumns().add(column);
        installDefaultSkin(control);
        replaceSkin(control);
        column.setPrefWidth(200);
    }
    /**
     * This here is the default test, default constructor. Fails before.
     *
     * FIXME: don't add to final push - here just to understand and reducing test time
     */
    @Test
    public void failTableViewMemoryLeak() {
        TableView<?> control =  new TableView<>();
        installDefaultSkin(control);
        WeakReference<?> weakRef = new WeakReference<>(replaceSkin(control));
        assertNotNull(weakRef.get());
        attemptGC(weakRef);
        assertEquals("Skin must be gc'ed", null, weakRef.get());
    }

//---------------- setup and initial

    protected void fireMethodPulse() {
        if (methodPulse) Toolkit.getToolkit().firePulse();
    }

    protected void showControl(Control box, boolean focus) {
        if (!root.getChildren().contains(box)) {
            root.getChildren().add(box);
        }
        stage.show();
        if (focus) {
            stage.requestFocus();
            box.requestFocus();
            assertTrue(box.isFocused());
            assertSame(box, scene.getFocusOwner());

        }
        if (showPulse) Toolkit.getToolkit().firePulse();
    }

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
