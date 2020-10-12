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

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.sun.javafx.tk.Toolkit;

import static javafx.scene.layout.AnchorPane.*;
import static org.junit.Assert.*;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * TODO: keep in global test branch!
 * 
 * Test for MouseEventFirer.
 * 
 * Separate tests for nodes, local/scene/screen and x/y each .. really needed?
 * They all fail with current implementation, pass with alternative.
 * 
 * Issues:
 * - expected x/y only for default firing into node glued to top-left
 *   implies local == parent == scene == layout (modulo borders/margins?)
 * - incorrect x/y with delta (offset looks being added twice?)  
 */
@RunWith(Parameterized.class)
public class MouseEventFirerExtTest {

    //------------ fields
    
    private Scene scene;
    private Stage stage;
    private AnchorPane content;
    
    private static final double EPS = 1;
    // margins for center node
    private final double VERTICAL_DISTANCE = 100.;
    private final double HORIZONTAL_DISTANCE = 20.;
    
    // string fragment used in text of top/center/bottom nodes
    private static final String TOP_TEXT = "top";
    private static final String CENTER_TEXT = "center";
    private static final String BOTTOM_TEXT = "bot";
    
    private Button topLeft, center, bottomRight;
    
// --------- local coordinates in mouseEvent 
    
    @Test
    public void testLocalDeltaXNegative() {
        assertLocal(getTarget(), - 10, 0);
    }
    
    @Test
    public void testLocalDeltaYNegative() {
        assertLocal(getTarget(), 0, - 5);
    }
    
    @Test
    public void testLocalDeltaY() {
        assertLocal(getTarget(), 0, 5)     ;
    }
    
    @Test
    public void testLocalDeltaX() {
        assertLocal(getTarget(), 10, 0)     ;
    }
    
    @Test
    public void testLocal() {
        assertLocal(getTarget(), 0, 0);
    }
    
    /**
     * Fires a mousePressed with the given x/y location on the given target
     * and asserts the local mouse coordinates.
     */
    protected void assertLocal(Node target, double deltaX, double deltaY) {
        MouseEventFirer firer = new MouseEventFirer(target, useAlternative);
        assertTrue("sanity", ((Labeled) target).getText().contains(textFragment));
        
        target.setOnMousePressed(e -> {
            double width = target.getLayoutBounds().getWidth();
            double height = target.getLayoutBounds().getHeight();
            assertEquals(textFragment + ": local x ", width /2 + deltaX, e.getX(), EPS);
            assertEquals(textFragment + ": local y ", height / 2 + deltaY, e.getY(), EPS);
        });
        firer.fireMousePressed(deltaX, deltaY);
    }
    
//-------------- scene
    
    @Test
    public void testSceneDeltaYNegative() {
        assertMouseCoordinates(getTarget(), 0, -5, true);
    }
    
    @Test
    public void testSceneDeltaXNegative() {
        assertMouseCoordinates(getTarget(), -10, 0, true);
    }
    @Test
    public void testSceneDeltaY() {
        assertMouseCoordinates(getTarget(), 0, 5, true);
    }
    
    @Test
    public void testSceneDeltaX() {
        assertMouseCoordinates(getTarget(), 10, 0, true);
    }
    
    @Test
    public void testScene() {
        assertMouseCoordinates(getTarget(), 0, 0, true);
    }
    
//-------------- screen
    
    @Test
    public void testScreenDeltaYNegative() {
        assertMouseCoordinates(getTarget(), 0, -5, false);
    }
    
    @Test
    public void testScreenDeltaXNegative() {
        assertMouseCoordinates(getTarget(), -10, 0, false);
    }
    @Test
    public void testScreenDeltaY() {
        assertMouseCoordinates(getTarget(), 0, 5, false);
    }
    
    @Test
    public void testScreenDeltaX() {
        assertMouseCoordinates(getTarget(), 10, 0, false);
    }
    
    @Test
    public void testScreen() {
        assertMouseCoordinates(getTarget(), 0, 0, false);
    }
    
    /** 
     * Fires a mousePressed with the given x/y location on the given target 
     *  and asserts basic mouseEvent constraints.
     * 
     */
    protected void assertMouseCoordinates(Node target, double deltaX, double deltaY, boolean isScene) {
        MouseEventFirer firer = new MouseEventFirer(target, useAlternative);
        target.setOnMousePressed(isScene ? this::assertScene : this::assertScreen);
        firer.fireMousePressed(deltaX, deltaY);
    }
    
    /**
     * Asserts scene coordinates of event are same as localToScene.
     */
    protected void assertScene(MouseEvent mouse) {
        assertSame(mouse.getTarget(), mouse.getSource());
        Labeled receiver = (Labeled) mouse.getTarget();
        assertTrue("sanity", receiver.getText().contains(textFragment));
        
        Point2D sceneP = receiver.localToScene(mouse.getX(), mouse.getY());
        assertEquals(textFragment + ": sceneX ", sceneP.getX(), mouse.getSceneX(), EPS);
        assertEquals(textFragment + ": sceneY", sceneP.getY(), mouse.getSceneY(), EPS);
    }
    
    /**
     * Asserts screen coordinates of event are same as localToScreen.
     */
    protected void assertScreen(MouseEvent mouse) {
        assertSame(mouse.getTarget(), mouse.getSource());
        Labeled receiver = (Labeled) mouse.getTarget();
        assertTrue("sanity", receiver.getText().contains(textFragment));
        Point2D screenP = receiver.localToScreen(mouse.getX(), mouse.getY());
        assertEquals(textFragment + ": screenX", screenP.getX(), mouse.getScreenX(), EPS);
        assertEquals(textFragment + ": screenY", screenP.getY(), mouse.getScreenY(), EPS);
    }
    
   
 // ------------- parameterized in not/alternative mouseEvent creation
    
    private boolean useAlternative;
    private String textFragment;
    @Parameterized.Parameters //
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
            {false, TOP_TEXT},
            {false, CENTER_TEXT},
            {false, BOTTOM_TEXT},
            {true, TOP_TEXT},
            {true, CENTER_TEXT},
            {true, BOTTOM_TEXT},
        };
        return Arrays.asList(data);
    }

    public MouseEventFirerExtTest(boolean useAlternative, String textFragment) {
        this.useAlternative = useAlternative;
        this.textFragment = textFragment;
    }
    
    
    protected Labeled getTarget() {
        return content.getChildren().stream()
                .map(c -> (Labeled) c)
                .filter(l -> l.getText().contains(textFragment))
                .findFirst().get();
    }
    
 // ------------ setup/cleanup/intial
    
    @Test
    public void testFirer() {
        new MouseEventFirer(topLeft, true);
        assertSame("sanity: firer must not change hierarchy", scene, topLeft.getScene());
        assertSame("sanity: firer must not change hierarchy", stage, topLeft.getScene().getWindow());
    }
    
    @Test
    public void testAnchorRight() {
        setLeftAnchor(topLeft, null);
        setRightAnchor(topLeft, 0.);
        Toolkit.getToolkit().firePulse();
        assertEquals(content.getWidth() - topLeft.prefWidth(-1), topLeft.getBoundsInParent().getMinX(), EPS);
    }
    
    @Test
    public void testLayoutBounds() {
        Bounds initial = topLeft.getLayoutBounds();
        setLeftAnchor(topLeft, null);
        setRightAnchor(topLeft, 0.);
        Toolkit.getToolkit().firePulse();
        assertEquals("sanity: layout bounds unchanged", initial, topLeft.getLayoutBounds());
    }
    
    @Test
    public void testContentLayout() {
        assertTrue(stage.isShowing());
        // content sizing controlled by big middle node
        assertEquals(2* VERTICAL_DISTANCE + center.prefHeight(-1), content.getHeight(), EPS);
        assertEquals(2* HORIZONTAL_DISTANCE + center.prefWidth(-1), content.getWidth(), EPS);
        // middle
        assertEquals(HORIZONTAL_DISTANCE, center.getBoundsInParent().getMinX(), EPS);
        assertEquals(VERTICAL_DISTANCE, center.getBoundsInParent().getMinY(),EPS);
        // top
        assertEquals(0, topLeft.getBoundsInParent().getMinX(), EPS);
        assertEquals(0, topLeft.getBoundsInParent().getMinY(), EPS);
        // bottom
        assertEquals(content.getWidth() - bottomRight.prefWidth(-1), bottomRight.getBoundsInParent().getMinX(), EPS);
        assertEquals(content.getHeight() - bottomRight.prefHeight(-1), bottomRight.getBoundsInParent().getMinY(), EPS);
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
        topLeft = new Button("topLeft");
        // glue to topLeft
        setTopAnchor(topLeft, 0.);
        setLeftAnchor(topLeft, 0.);
        
        center = new Button("center: a longish text horizontally");
        // glue into center
        setTopAnchor(center, VERTICAL_DISTANCE);
        setBottomAnchor(center, VERTICAL_DISTANCE);
        setLeftAnchor(center, HORIZONTAL_DISTANCE);
        setRightAnchor(center, HORIZONTAL_DISTANCE);
        
        bottomRight = new Button("botRight");
        // glue to bottom-right
        setBottomAnchor(bottomRight, 0.);
        setRightAnchor(bottomRight, 0.);
        
        content = new AnchorPane(topLeft, center, bottomRight);
        scene = new Scene(content);
        stage = new Stage();
        stage.setScene(scene);
        stage.show();
        Toolkit.getToolkit().firePulse();
        //System.out.println("scene/screen size " + scene.getWidth() + " / " + stage.getWidth());
    }

    @After public void tearDown() {
        if (stage != null) stage.hide();
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }

}
