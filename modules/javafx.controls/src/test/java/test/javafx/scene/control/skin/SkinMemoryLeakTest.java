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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static javafx.scene.control.ControlShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory.*;

import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Pagination;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.shape.Rectangle;
import test.com.sun.javafx.scene.control.infrastructure.ControlSkinFactory;

/**
 * Beware: this is different in doKeep - contains tests against future (attempts of) fixes!
 * Changes in master must be merged manually to this to keep in synch with current fixes!
 * <p>
 * Test memory leaks in Skin implementations.
 * <p>
 * This test is parameterized on control type.
 */
@RunWith(Parameterized.class)
public class SkinMemoryLeakTest {

    private Control control;

//--------- tests

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

//------------ parameters

    // Note: name property not supported before junit 4.11
    @Parameterized.Parameters //(name = "{index}: {0} ")
    public static Collection<Object[]> data() {
        List<Class<Control>> controlClasses = getControlClasses();
        // FIXME as part of JDK-8241364
        // The default skins of these controls are leaking
        // step 1: file issues (where not yet done), add informal ignore to entry
        // step 2: fix and remove from list
        List<Class<? extends Control>> leakingClasses = List.of(
                Accordion.class,
                ButtonBar.class,
                ColorPicker.class,
                ComboBox.class,
                DatePicker.class,
                MenuBar.class,
                MenuButton.class,
                Pagination.class,
                PasswordField.class,
                ScrollBar.class,
                ScrollPane.class,
                // @Ignore("8245145")
                Spinner.class,
                SplitMenuButton.class,
                SplitPane.class,
                TableRow.class,
                TableView.class,
                // @Ignore("8242621")
                TabPane.class,
                // @Ignore("8244419")
                TextArea.class,
                // @Ignore("8240506")
                TextField.class,
                TreeTableRow.class,
                TreeTableView.class
                // to report
//                TreeView.class
        );
        // remove the known issues to make the test pass
        controlClasses.removeAll(leakingClasses);
        // instantiate controls
        List<Control> controls = controlClasses.stream()
                .map(ControlSkinFactory::createControl)
                .collect(Collectors.toList());
        // controls with configuration
        Button button = new Button("dummy", new Rectangle());
        CheckBox checkBox = new CheckBox("dummy");
        checkBox.setGraphic(new Rectangle());
        Hyperlink hyperlink = new Hyperlink("dummy", new Rectangle());
        Label label = new Label("dummy", new Rectangle());
        ListCell<?> listCell = new ListCell<>();
        listCell.updateListView(new ListView<>());
        // leaking w/out - fix than add here
        //MenuButton menuButton = new MenuButton("", new Rectangle());
        ToggleButton toggleButton = new ToggleButton("", new Rectangle());
        RadioButton radioButton = new RadioButton("");
        radioButton.setGraphic(new Rectangle());
        TitledPane titled = new TitledPane();
        titled.setGraphic(new Rectangle());
        controls.addAll(List.of(
                button,
                checkBox,
                hyperlink,
                listCell,
                label, 
//                menuButton,
                toggleButton,
                radioButton,
                titled
                ));
        return asArrays(controls);
    }

    public SkinMemoryLeakTest(Control control) {
        this.control = control;
    }

//------------ setup

    @Before
    public void setup() {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException)throwable;
            } else {
                Thread.currentThread().getThreadGroup().uncaughtException(thread, throwable);
            }
        });
        assertNotNull(control);
    }

    @After
    public void cleanup() {
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }

}
