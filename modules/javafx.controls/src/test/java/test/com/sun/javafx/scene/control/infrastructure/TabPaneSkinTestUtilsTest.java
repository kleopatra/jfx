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

package test.com.sun.javafx.scene.control.infrastructure;

import java.util.List;
import static javafx.scene.control.skin.TabPaneSkinShim.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static javafx.scene.control.ControlShim.*;
import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.TabPaneSkinTestUtils.*;

import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
/**
 * Test of TabPaneSkinTestUtils.
 */
public class TabPaneSkinTestUtilsTest {

    private TabPane tabPane;
    
    @Test
    public void testGetTabHeaders() {
        List<Node> headers = getTabHeaders(tabPane);
        assertEquals(tabPane.getTabs().size(), headers.size());
        for (int i = 0; i < headers.size(); i++) {
            assertSame(tabPane.getTabs().get(i), headers.get(i).getProperties().get(Tab.class));
        }
    }
    
    @Test
    public void testGetTabHeaderOffset() {
        List<Node> headers = getTabHeaders(tabPane);
        double headerOffset = 0.0;
        for (int i = 0; i < headers.size(); i++) {
            assertEquals("expected offset for " + i, headerOffset, getTabHeaderOffset(tabPane, tabPane.getTabs().get(i)), 1);
            headerOffset += headers.get(i).prefWidth(-1);
        }
    }
    
    @Test
    public void testGetSelectedTabHeader() {
        Node tabHeader = getSelectedTabHeader(tabPane);
        assertNotNull(tabHeader);
        assertSame(tabPane.getSelectionModel().getSelectedItem(), tabHeader.getProperties().get(Tab.class));
    }
    
//------------ setup
    
    @Test
    public void testSetup() {
        assertNotNull(tabPane);
        assertEquals(30, tabPane.getTabs().size());
        assertNotNull(tabPane.getSkin());
        assertSame(Side.TOP, tabPane.getSide());
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
        tabPane = createTabPane();
        installDefaultSkin(tabPane);
    }

    protected TabPane createTabPane() {
        return createTabPane(30);
    }
    
    protected TabPane createTabPane(int max) {
        TabPane tabPane = new TabPane();
        for (int i = 0; i < max; i++) {
            Tab tab = new Tab("Tab " + i, new Label("Content for " + i));
            tabPane.getTabs().add(tab);
        }
        return tabPane;
    }

    @After
    public void cleanup() {
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }

    
}
