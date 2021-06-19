/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;

/**
 * contains unused accessors (which didn't make it to the code base, used
 * while analysing)
 */
public class UnusedSkinShim {

    /**
     * Sets the fill of the promptText. The skin must be of type TextInputControlSkin.
     */
    public static void setPromptTextFill(TextInputControl control, Paint fill) {
        TextInputControlSkin<?> skin = (TextInputControlSkin<?>) control.getSkin();
        skin.setPromptTextFill(fill);
    }

    /**
     * Returns the textTranslateX from the textField's skin. The skin must be of type
     * TextFieldSkin.
     */
//    public static Path getSelectionPath(TextField textField) {
//        TextFieldSkin skin = (TextFieldSkin) textField.getSkin();
//        return skin.getSelectionPath();
//    }

    /**
     *   deleted from TextFieldSkin: for testing only!
     *   not useful in unit test, in test context selection highlight is not updated
     */
//  Path getSelectionPath() {
//  textNode.getSelectionShape();
//  updateSelection();
//  return selectionHighlightPath;
//}
//

}
