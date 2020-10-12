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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.javafx.tk.Toolkit;

import static javafx.scene.layout.AnchorPane.*;
import static org.junit.Assert.*;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * This is the first attempt to try understand what's going on.
 * 
 * --------
 * Test for MouseEventFirer.
 * 
 * Issues:
 * - expected x/y only for default firing into node glued to top-left
 *   implies local == parent == scene == layout (modulo borders/margins?)
 * - incorrect x/y with delta (offset is twice the expected)  
 * 
 * 
 */
public class MouseEventFirerTestOld {

    //------------ fields
    
    private Scene scene;
    private Stage stage;
    private AnchorPane content;
    
    private static final double EPS = 1;
    private final double VERTICAL_DISTANCE = 100.;
    private Node top, middle, bottom;
    
// --------- specify mouseEvent fired
    /**
     * This is used in reporting JDK-8253769
     */
    @Test //@Ignore("8253769")
    public void testStandalone() {
        Button button = new Button("just a button nothing fancy");
        // location (relative to center of node) to fire at
        double deltaX = 20;
        double deltaY = 5;
        button.setOnMousePressed(mouse -> {
            // local coordinates
            double centerX = button.getWidth() / 2;
            double centerY = button.getHeight() / 2;
            assertEquals("local X", centerX + deltaX, mouse.getX(), 0.5);
            assertEquals("local Y", centerY + deltaY, mouse.getY(), 0.5);

            Point2D sceneP = button.localToScene(mouse.getX(), mouse.getY());
            assertEquals("scene X", sceneP.getX(), mouse.getSceneX(), 0.5);
            assertEquals("scene Y", sceneP.getY(), mouse.getSceneY(), 0.5);

            Point2D screenP = button.localToScreen(mouse.getX(), mouse.getY());
            assertEquals("screen X", screenP.getX(), mouse.getScreenX(), 0.5);
            assertEquals("screen Y", screenP.getY(), mouse.getScreenY(), 0.5);
        });
        MouseEventFirer firer = new MouseEventFirer(button);
        firer.fireMousePressed(deltaX, deltaY);
    }

    @Test
    public void testFireDeltaY() {
        MouseEventFirer firer = new MouseEventFirer(top, true);
        double delta = 5;
        top.setOnMousePressed(e -> {
            Bounds bounds = top.getLayoutBounds();
            assertEquals("default x", bounds.getWidth() / 2, e.getX(),EPS);
            assertEquals("y with delta", bounds.getHeight() / 2 + delta, e.getY(), EPS);
        });
        firer.fireMousePressed(0, delta);
    }
    
    @Test
    public void testFireDeltaX() {
        MouseEventFirer firer = new MouseEventFirer(top, true);
        double delta = 10;
        top.setOnMousePressed(e -> {
            Bounds bounds = top.getLayoutBounds();
            assertEquals("default y", bounds.getHeight() / 2, e.getY(), EPS);
            assertEquals("x with delta", bounds.getWidth() / 2 + delta, e.getX(), EPS);
        });
        firer.fireMousePressed(delta, 0);
    }
    
    /**
     * Sanity: zero deltaX/-Y
     */
    @Test
    public void testFireDeltaZero() {
        MouseEventFirer firer = new MouseEventFirer(top, true);
        top.setOnMousePressed(e -> {
            Bounds bounds = top.getLayoutBounds();
            assertEquals(bounds.getWidth() / 2, e.getX(), EPS);
            assertEquals(bounds.getHeight() / 2, e.getY(), EPS);
        });
        firer.fireMousePressed(0, 0);
    }
    
    
    @Test
    public void testFireDefaultRight() {
        setRightAnchor(top, 0.);
        Toolkit.getToolkit().firePulse();
        
        MouseEventFirer firer = new MouseEventFirer(top, true);
        top.setOnMousePressed(e -> {
            Bounds bounds = top.getLayoutBounds();
            assertEquals("y", bounds.getHeight() / 2, e.getY(), EPS);
            assertEquals("x", bounds.getWidth() / 2, e.getX(), EPS);
        });
        firer.fireMousePressed();
    }
    
    @Test
    public void testFireDefaultSceneTopRight() {
        setRightAnchor(top, 0.);
        Toolkit.getToolkit().firePulse();
        
        MouseEventFirer firer = new MouseEventFirer(top, true);
        top.setOnMousePressed(e -> {
            Bounds local = top.getBoundsInLocal();
            Bounds sceneBounds = top.localToScene(local);
            Point2D sceneP = top.localToScene(local.getWidth() / 2, local.getHeight() / 2);
            assertEquals("sceneY for top-right", sceneP.getY(), e.getSceneY(), EPS);
            assertEquals("sceneX for top-right", sceneP.getX(), e.getSceneX(), EPS);
//            assertEquals("sceneY for top-right", sceneBounds.getMinY() + sceneBounds.getHeight() / 2, e.getSceneY(), EPS);
//            assertEquals("sceneX for top-right", sceneBounds.getMinX() + sceneBounds.getWidth() / 2, e.getSceneX(), EPS);
        });
        firer.fireMousePressed();
    }
    
    /**
     * Sanity test on top-left glued node and default firing (that is into middle of node). 
     * 
     */
    @Test
    public void testFireEventState() {
        MouseEventFirer firer = new MouseEventFirer(top, true);
        top.setOnMousePressed(e -> {
            assertSame("source: ", top, e.getSource());
            assertSame("source: ", top, e.getTarget());
            
            // x/y is center of layout
            Bounds layoutBounds = top.getLayoutBounds();
            assertEquals("x", layoutBounds.getWidth() / 2, e.getX(), EPS);
            assertEquals("y", layoutBounds.getHeight() / 2, e.getY(), EPS);
            
            // scene 
            Bounds local = top.getBoundsInLocal();
            Bounds sceneBounds = top.localToScene(local);
            assertEquals("scene same as local for top/left", local, sceneBounds);
            
            // local mouse coordinate same as scene coordinated
            assertEquals("sceneX same as x", e.getX(), e.getSceneX(), EPS);
            assertEquals("sceneY same as y", e.getY(), e.getSceneY(), EPS);
        });
        
        firer.fireMousePressed();
    }
    
    @Test
    public void testFireMouse() {
        MouseEventFirer firer = new MouseEventFirer(top, true);
        top.setOnMousePressed(this::assertMouseEventConstraints);
        firer.fireMousePressed();
    }
    
    @Test
    public void testFireMouseTopRight() {
        setRightAnchor(top, 0.);
        Toolkit.getToolkit().firePulse();
        MouseEventFirer firer = new MouseEventFirer(top, true);
        top.setOnMousePressed(this::assertMouseEventConstraints);
        firer.fireMousePressed();
    }
    
    @Test
    public void testFireMouseAllWithDelta() {
        setRightAnchor(top, 0.);
        setRightAnchor(bottom, 0.);
        Toolkit.getToolkit().firePulse();
        content.getChildren().forEach(child -> assertMouseConstraintsDelta(child, 10, 5));
    }
    
    @Test
    public void testTargetCoordinatesDefaultAll() {
        setRightAnchor(top, 0.);
        Toolkit.getToolkit().firePulse();
        content.getChildren().forEach(child -> assertTargetCoordinates(child, 10, 5));
    }
    
    @Test
    public void testMouseConstraintsAll() {
        setRightAnchor(top, 0.);
        setRightAnchor(bottom, 0.);
        Toolkit.getToolkit().firePulse();
        content.getChildren().forEach(this::assertMouseConstraints);
    }
    
    /**
     * Fires a mousePressed with the given x/y location on the given target
     * and asserts the local mouse coordinates.
     * 
     * @param target
     * @param deltaX
     * @param deltaY
     */
    protected void assertTargetCoordinates(Node target, double deltaX, double deltaY) {
        MouseEventFirer firer = new MouseEventFirer(target, true);
        target.setOnMousePressed(e -> {
            double width = target.getLayoutBounds().getWidth();
            double height = target.getLayoutBounds().getHeight();
            assertEquals("x", width /2 + deltaX, e.getX(), EPS);
            assertEquals("y", height / 2 + deltaY, e.getY(), EPS);
        });
        firer.fireMousePressed(deltaX, deltaY);
    }
    
    /** 
     * Fires a mousePressed with the given x/y location on the given target 
     *  and asserts basic mouseEvent constraints.
     * 
     * @param target
     * @param deltaX
     * @param deltaY
     */
    protected void assertMouseConstraintsDelta(Node target, double deltaX, double deltaY) {
        MouseEventFirer firer = new MouseEventFirer(target, true);
        target.setOnMousePressed(this::assertMouseEventConstraints);
        firer.fireMousePressed(deltaX, deltaY);
    }
    
    /**
     * Fires a mousePressed on the given target and asserts mouse counstraints.
     * @param target
     */
    protected void assertMouseConstraints(Node target) {
        MouseEventFirer firer = new MouseEventFirer(target, true);
        target.setOnMousePressed(this::assertMouseEventConstraints);
        firer.fireMousePressed();
    }
    
    /**
     * Asserts basic state (in particular, coordinates) of a mouseEvent
     * fired on and received by a node in root.
     * @param mouse
     */
    protected void assertMouseEventConstraints(MouseEvent mouse) {
        assertSame(mouse.getTarget(), mouse.getSource());
        Node receiver = (Node) mouse.getTarget();
        assertSame(scene, receiver.getScene());
        assertSame(content, receiver.getParent());
        assertSame(stage, receiver.getScene().getWindow());
        Point2D sceneP = receiver.localToScene(mouse.getX(), mouse.getY());
        assertEquals("sceneX", sceneP.getX(), mouse.getSceneX(), EPS);
        assertEquals("sceneY", sceneP.getY(), mouse.getSceneY(), EPS);
        Point2D screenP = receiver.localToScreen(mouse.getX(), mouse.getY());
        assertEquals("screenx", screenP.getX(), mouse.getScreenX(), EPS);
        assertEquals("screenY", screenP.getY(), mouse.getScreenY(), EPS);
    }
 // ------------ setup/cleanup/intial
    
    @Test
    public void testFirer() {
        MouseEventFirer firer = new MouseEventFirer(top, true);
        assertSame("sanity: firer must not change hierarchy", scene, top.getScene());
        assertSame("sanity: firer must not change hierarchy", stage, top.getScene().getWindow());
    }
    
    @Test
    public void testAnchorRight() {
        setRightAnchor(top, 0.);
        Toolkit.getToolkit().firePulse();
        assertEquals(content.getWidth() - top.prefWidth(-1), top.getBoundsInParent().getMinX(), EPS);
    }
    
    @Test
    public void testLayoutBounds() {
        Bounds initial = top.getLayoutBounds();
        setRightAnchor(top, 0.);
        Toolkit.getToolkit().firePulse();
        assertEquals("sanity: layout bounds unchanged", initial, top.getLayoutBounds());
    }
    
    @Test
    public void testInitialLayout() {
        assertTrue(stage.isShowing());
        // content sizing controlled by big middle node
        assertEquals(2* VERTICAL_DISTANCE + middle.prefHeight(-1), content.getHeight(), EPS);
        assertEquals(middle.prefWidth(-1), content.getWidth(), EPS);
        // middle
        assertEquals(0, middle.getBoundsInParent().getMinX(), EPS);
        assertEquals(VERTICAL_DISTANCE, middle.getBoundsInParent().getMinY(),EPS);
        // top
        assertEquals(0, top.getBoundsInParent().getMinX(), EPS);
        assertEquals(0, top.getBoundsInParent().getMinY(), EPS);
        // bottom
        assertEquals(0, bottom.getBoundsInParent().getMinX(), EPS);
        assertEquals(content.getHeight() - bottom.prefHeight(-1), bottom.getBoundsInParent().getMinY(), EPS);
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
        top = new Button("top");
        // glue to top
        setTopAnchor(top, 0.);
        middle = new Button("middle: a longish text filling all horizontal space");
        // glue vertically into center
        setTopAnchor(middle, VERTICAL_DISTANCE);
        setBottomAnchor(middle, VERTICAL_DISTANCE);
        bottom = new Button("bot");
        // glue to bottom
        setBottomAnchor(bottom, 0.);
        content = new AnchorPane(top, middle, bottom);
        scene = new Scene(content);
        stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }

    @After public void tearDown() {
        if (stage != null) stage.hide();
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }

}
