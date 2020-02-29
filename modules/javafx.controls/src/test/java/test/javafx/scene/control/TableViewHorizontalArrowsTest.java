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

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

import javafx.geometry.NodeOrientation;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import test.com.sun.javafx.scene.control.infrastructure.KeyEventFirer;
import test.com.sun.javafx.scene.control.infrastructure.KeyModifier;
import test.com.sun.javafx.scene.control.infrastructure.StageLoader;

/**
 *
 */
@RunWith(Parameterized.class)
public class TableViewHorizontalArrowsTest {
    @Parameterized.Parameters
    public static Collection implementations() {
        return Arrays.asList(new Object[][] { 
            // orientation, forward, backward
                  { NodeOrientation.LEFT_TO_RIGHT, 
                      (BiConsumer<KeyEventFirer, KeyModifier[]>) ((keyboard, modifiers) -> keyboard.doRightArrowPress(modifiers)),
                      (BiConsumer<KeyEventFirer, KeyModifier[]>) ((keyboard, modifiers) -> keyboard.doLeftArrowPress(modifiers)),
                  },
                  { NodeOrientation.RIGHT_TO_LEFT, 
                      (BiConsumer<KeyEventFirer, KeyModifier[]>) ((keyboard, modifiers) -> keyboard.doLeftArrowPress(modifiers)),
                      (BiConsumer<KeyEventFirer, KeyModifier[]>) ((keyboard, modifiers) -> keyboard.doRightArrowPress(modifiers)),
                  } 
                  
        });
    }

    private TableView<String> tableView;
//  private TableSelectionModel<String> sm;
    private TableView.TableViewSelectionModel<String> sm;
    private TableView.TableViewFocusModel<String> fm;
    
    private TableColumn<String, String> col0;
    private TableColumn<String, String> col1;
    private TableColumn<String, String> col2;
    private TableColumn<String, String> col3;
    private TableColumn<String, String> col4;

    private KeyEventFirer keyboard;
    private StageLoader stageLoader;
    private NodeOrientation orientation;

    Consumer<KeyModifier> forward;
    Consumer<KeyModifier> backward;

    BiConsumer<KeyEventFirer, KeyModifier[]> biForward;
    BiConsumer<KeyEventFirer, KeyModifier[]> biBackward;
    
    public TableViewHorizontalArrowsTest(NodeOrientation val, 
            BiConsumer<KeyEventFirer, KeyModifier[]> forward, BiConsumer<KeyEventFirer, KeyModifier[]> backward) {
        orientation = val;
        biForward = forward;
        biBackward = backward;
    }

    @Before
    public void setup() {
        tableView = new TableView<String>();
        tableView.setNodeOrientation(orientation);
        sm = tableView.getSelectionModel();
        fm = tableView.getFocusModel();

        sm.setSelectionMode(SelectionMode.MULTIPLE);
        sm.setCellSelectionEnabled(false);

        tableView.getItems().setAll("1", "2", "3", "4", "5", "6", "7", "8", "9",
                "10", "11", "12");

        col0 = new TableColumn<String, String>("col0");
        col1 = new TableColumn<String, String>("col1");
        col2 = new TableColumn<String, String>("col2");
        col3 = new TableColumn<String, String>("col3");
        col4 = new TableColumn<String, String>("col4");
        tableView.getColumns().setAll(col0, col1, col2, col3, col4);

        keyboard = new KeyEventFirer(tableView);

        forward = orientation == NodeOrientation.LEFT_TO_RIGHT
                ? keys -> keyboard.doRightArrowPress(keys)
                : keys -> keyboard.doLeftArrowPress(keys);

        backward = orientation == NodeOrientation.LEFT_TO_RIGHT
                ? keys -> keyboard.doLeftArrowPress(keys)
                : keys -> keyboard.doRightArrowPress(keys);

        stageLoader = new StageLoader(tableView);
        stageLoader.getStage().show();
    }

    @After
    public void tearDown() {
        tableView.getSkin().dispose();
        stageLoader.dispose();
    }

//---------- tests
    
    @Test
    public void testForwardFocus() {
        sm.setCellSelectionEnabled(true);
        sm.select(0, col0);
        forward(KeyModifier.getShortcutKey());
        assertTrue("selected cell must still be selected", sm.isSelected(0, col0));
        assertFalse("next cell must not be selected", sm.isSelected(0, col1));
        TablePosition focusedCell = fm.getFocusedCell();
        assertEquals("focused cell must moved to next", col1, focusedCell.getTableColumn());
    }
    
    /**
     * Orientation-aware horizontal navigation with arrow keys.
     * @param modifiers the modifiers to use on keyboard
     */
    protected void forward(KeyModifier... modifiers) {
        biForward.accept(keyboard, modifiers);
    }
    


}
