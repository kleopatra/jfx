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
import java.util.Objects;
import java.util.Optional;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import static javafx.scene.control.skin.TabPaneSkinShim.*;
/**
 * Utility methods to access skin-related state of TabPane.
 */
public class TabPaneSkinTestUtils {

//    public static List<Node> getTabHeaders(TabPane tabPane) {
//        Objects.requireNonNull(tabPane, "tabPane must not be null");
//        Objects.requireNonNull(tabPane.getSkin(), "tabPane's skin must not be null");
//        StackPane headersRegion = (StackPane) tabPane.lookup(".headers-region");
//        return headersRegion.getChildren();
//    }
// 
    // change to use tab.getTabPane?
    public static double getTabHeaderOffset(TabPane tabPane, Tab tab) {
        Objects.requireNonNull(tabPane, "tabPane must not be null");
        Objects.requireNonNull(tab, "tab must not be null");
        if (!tabPane.getTabs().contains(tab)) throw new IllegalStateException("tab must be contained");
        List<Node> headers = getTabHeaders(tabPane);
        double offset = 0;
        for (Node node : headers) {
            if (getTabFor(node) == tab) break;
            offset += node.prefWidth(-1);
        }
        return offset;
    }
    
    public static Node getSelectedTabHeader(TabPane tabPane) {
        Objects.requireNonNull(tabPane, "tabPane must not be null");
        if (tabPane.getTabs().isEmpty()) throw new IllegalStateException("tabs must not be empty");
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        return getTabHeaderFor(tabPane, tab);
    }
    
    public static Node getTabHeaderFor(TabPane tabPane, Tab tab) {
        Objects.requireNonNull(tabPane, "tabPane must not be null");
        Objects.requireNonNull(tab, "tab must not be null");
        if (!tabPane.getTabs().contains(tab)) throw new IllegalStateException("tab must be contained");
        List<Node> headers = getTabHeaders(tabPane);
        Optional<Node> tabHeader = headers.stream()
                .filter(node -> getTabFor(node) == tab)
                .findFirst();
        return tabHeader.get();
    }
    
    public static Tab getTabFor(Node tabHeader) {
        Objects.requireNonNull(tabHeader, "tabHeader must not be null");
        Object tab = tabHeader.getProperties().get(Tab.class);
        if (tab instanceof Tab) return (Tab) tab;
        throw new IllegalStateException("node is not a tabHeader " + tabHeader);
    }
    
}
