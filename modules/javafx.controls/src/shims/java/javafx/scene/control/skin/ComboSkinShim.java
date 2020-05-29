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

package javafx.scene.control.skin;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;

/**
 * Utility methods to access package-private api in ComboBox-related skins.
 */
public class ComboSkinShim {

    /**
     * Returns the ListView used in given ComboBox's popup if its skin is  
     * of type ComboBoxListViewSkin, or null if not.
     * 
     * @param box the ComboBox to get the ListView from
     * @return the ListView in the combo's popup or null if not available
     */
    public static ListView<?> getListView(ComboBox<?> box) {
        if (box.getSkin() instanceof ComboBoxListViewSkin) {
            return ((ComboBoxListViewSkin<?>) box.getSkin()).getListView();
        }
        return null;
    }
    
    /**
     * Returns the PopupControl of the given ComboBoxBase if its skin is of type
     * ComboBoxPopupControl, or null if not.
     * 
     * @param box the ComboBoxBase to get the popup from
     * @return the combo's popup or null if not available
     */
    public static PopupControl getPopup(ComboBoxBase<?> box) {
        if (box.getSkin() instanceof ComboBoxPopupControl) {
            return ((ComboBoxPopupControl<?>) box.getSkin()).getPopup();
        }
        return null;
    }
    
    private ComboSkinShim() {}
}
