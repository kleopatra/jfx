/*
 * Copyright (c) 2010, 2016, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import com.sun.javafx.scene.control.behavior.ListCellBehavior;

/**
 * Default skin implementation for the {@link ListCell} control.
 *
 * @see ListCell
 * @since 9
 */
public class ListCellSkin<T> extends CellSkinBase<ListCell<T>> {

    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/

    private double fixedCellSize;
    private boolean fixedCellSizeEnabled;
    private BehaviorBase<ListCell<T>> behavior;



    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a new ListCellSkin instance, installing the necessary child
     * nodes into the Control {@link Control#getChildren() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public ListCellSkin(ListCell<T> control) {
        super(control);

        // install default input map for the ListCell control
        behavior = new ListCellBehavior<>(control);
//        control.setInputMap(behavior.getInputMap());

        // ---------- AAAAAAAAAAAAAAAA
        // pattern: manually managed invalidationListeners to parent and child property
        // requires storing value of parentProperty for removal of child listener
        // might be required if we need an InvalidationListener on the parent property
        // (not here, but just for the complete picture)
//        fixedCellSizeListener = o -> updateFixedCellSize();
//        listViewListener = o -> updateListView();
//        control.listViewProperty().addListener(listViewListener);
//        updateListView();
        
        //--------- BBBBBBBBBBBBB
        // pattern: skin api for parent property
//        fixedCellSizeListener = o -> updateFixedCellSize();
//        registerChangeListener(control.listViewProperty(), o -> updateListView());
//        updateListView();
        
        //--------- CCCCCCCCCCCC
        // pattern manual changeListener, no reference to listView
        // updateListView carrying both old/new value
//        fixedCellSizeListener = o -> updateFixedCellSize(getSkinnable().getListView());
//        listViewChangeListener = (src, ov, nv) -> updateListView(ov, nv);
//        control.listViewProperty().addListener(listViewChangeListener);
//        updateListView(null, control.getListView());
        
        //--------- DDDDDDD
        // pattern: manual ChangeListener, no reference to listView, 
        // updateListView carrying oldListView 
        // issue: cleanup of internal state in dispose requires code duplication
        // or additional method
        fixedCellSizeListener = o -> updateFixedCellSize();
        listViewChangeListener = (src, ov, nv) -> updateListView(ov);
        control.listViewProperty().addListener(listViewChangeListener);
        updateListView(null);
        
        // pattern
        // original
//        setupListeners();
    }

//-------------AAAAA scenario: invalidation listener on parent property
    
//    private InvalidationListener fixedCellSizeListener;
//    private InvalidationListener listViewListener;
//    private ListView<T> listView;
//    
//    private void updateListView() {
//        if (listView != null) {
//            listView.fixedCellSizeProperty().removeListener(fixedCellSizeListener);
//        }
//        listView = getSkinnable().getListView();
//        if (listView != null) {
//            listView.fixedCellSizeProperty().addListener(fixedCellSizeListener);
//        }
//        updateFixedCellSize();
//    }
//
//    /**
//     * Callback from listener to the listView's fixedCellSize. think: require
//     * listView != null or not? if yes -> size if of last listView, if not ->
//     * size is reset to -1 if null listView if cleaning up on changing listView,
//     * should we cleanup in dispose?
//     */
//    private void updateFixedCellSize() {
//        this.fixedCellSize = listView == null ? -1 : listView.getFixedCellSize();
//        this.fixedCellSizeEnabled = fixedCellSize > 0;
//    }
//    
//    /** {@inheritDoc} */
//    @Override
//    public void dispose() {
//        if (getSkinnable() == null) return;
//        getSkinnable().listViewProperty().removeListener(listViewListener);
//        if (listView != null) {
//            listView.fixedCellSizeProperty().removeListener(fixedCellSizeListener);
//            listView = null;
//        }
//        // no need to update our own state, we are dead after this!
////        updateFixedCellSize();
//        super.dispose();
//
//        if (behavior != null) {
//            behavior.dispose();
//        }
//    }
//
    
// --------- BBBBBB scenario: skin api for listViewListener
//    private InvalidationListener fixedCellSizeListener;
//    private ListView<T> listView;
//    private void updateListView() {
//        if (listView != null) {
//            listView.fixedCellSizeProperty()
//                    .removeListener(fixedCellSizeListener);
//        }
//        listView = getSkinnable().getListView();
//        if (listView != null) {
//            listView.fixedCellSizeProperty().addListener(fixedCellSizeListener);
//        }
//        updateFixedCellSize();
//    }
//
//    /**
//     * Callback from listener to the listView's fixedCellSize. think: require
//     * listView != null or not? if yes -> size if of last listView, if not ->
//     * size is reset to -1 if null listView if cleaning up on changing listView,
//     * should we cleanup in dispose?
//     */
//    private void updateFixedCellSize() {
//        this.fixedCellSize = listView == null ? -1 : listView.getFixedCellSize();
//        this.fixedCellSizeEnabled = fixedCellSize > 0;
//    }
//    
//    /** {@inheritDoc} */
//    @Override
//    public void dispose() {
//        if (getSkinnable() == null) return;
//        if (listView != null) {
//            listView.fixedCellSizeProperty().removeListener(fixedCellSizeListener);
//            listView = null;
//        }
//        super.dispose();
//
//        if (behavior != null) {
//            behavior.dispose();
//        }
//    }
//

// -------CCCCCC scenario: manual changeListener, updateListView with both old/newValue   
    
//    private InvalidationListener fixedCellSizeListener;
//    private ChangeListener<ListView<T>> listViewChangeListener;
//
//    private void updateListView(ListView<T> oldListView, ListView<T> listView) {
//        if (oldListView != null) {
//            oldListView.fixedCellSizeProperty().removeListener(fixedCellSizeListener);
//        }
//        if (listView != null) {
//            listView.fixedCellSizeProperty().addListener(fixedCellSizeListener);
//        }
//        updateFixedCellSize(listView);
//    }
//    
//    protected void updateFixedCellSize(ListView<T> listView) {
//        this.fixedCellSize = listView == null ? -1
//                : listView.getFixedCellSize();
//        this.fixedCellSizeEnabled = fixedCellSize > 0;
//    }
//
//    /** {@inheritDoc} */
//    @Override public void dispose() {
//        if (getSkinnable() == null) return;
//        getSkinnable().listViewProperty().removeListener(listViewChangeListener);
//        // implicit cleanup ...
//        updateListView(getSkinnable().getListView(), null);
//        super.dispose();
//
//        if (behavior != null) {
//            behavior.dispose();
//        }
//    }

// ------- DDDDDDDDDDDDDDDDdd scenario: manual changeListener, updateListView with oldValue   
    
    private InvalidationListener fixedCellSizeListener;
    private ChangeListener<ListView<T>> listViewChangeListener;
    
    private void updateListView(ListView<T> oldListView) {
        if (oldListView != null) {
            oldListView.fixedCellSizeProperty().removeListener(fixedCellSizeListener);
        }
        ListView<T> listView = getSkinnable().getListView();
        if (listView != null) {
            listView.fixedCellSizeProperty().addListener(fixedCellSizeListener);
        }
        updateFixedCellSize();
    }
    /**
     * Callback from listener to the listView's fixedCellSize.
     * think: require listView != null or not? 
     * if yes -> size if of last listView, if not -> size is reset to -1 if null listView
     * if cleaning up on changing listView, should we cleanup in dispose?
     */
    private void updateFixedCellSize() {
        ListView<T> listView = getSkinnable().getListView();
        this.fixedCellSize = listView == null ? -1 : listView.getFixedCellSize();
        this.fixedCellSizeEnabled = fixedCellSize > 0;
    }

    /** {@inheritDoc} */
    @Override public void dispose() {
        if (getSkinnable() == null) return;
        getSkinnable().listViewProperty().removeListener(listViewChangeListener);
        ListView<T> listView = getSkinnable().getListView();
        if (listView != null) {
            // decided: cleanup internal state - no, we are dead after this
            // cleanup of externals only (not applicable here)
            listView.fixedCellSizeProperty().removeListener(fixedCellSizeListener);
        }
        super.dispose();

        if (behavior != null) {
            behavior.dispose();
        }
    }
    
//------------------------- manually managed ??
    
//    
//    private void updateFixedCellSize(ListView<T> listView) {
//        this.fixedCellSize = listView == null ? -1 : listView.getFixedCellSize();
//        this.fixedCellSizeEnabled = fixedCellSize > 0;
//    }
//

    /**
     * Callback from listener to the cell's listView
     */
//    private void updateListView() {
//        if (listView != null) {
//            listView.fixedCellSizeProperty().removeListener(fixedCellSizeListener);
//        }
//        listView = getSkinnable().getListView();
//        if (listView != null) {
//            listView.fixedCellSizeProperty().addListener(fixedCellSizeListener);
//        }
//        updateFixedCellSize();
//    }
//

 //------------------- old listener wiring
    

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/


//    @Override public void dispose() {
//        super.dispose();
//
//        if (behavior != null) {
//            behavior.dispose();
//        }
//        
//    }
    // this is basically setting up a listener to a path property that's
    // not updated on updating the listView
    // doesn't happen (?) in current useage, but is brittle
    // replace with usual pattern: listen to parent property, rewire listeners to child property
    private void setupListeners() {
        ListView listView = getSkinnable().getListView();
        if (listView == null) {
            // this listener hangs around if cell never attached to listView
            getSkinnable().listViewProperty().addListener(new InvalidationListener() {
                @Override public void invalidated(Observable observable) {
                    getSkinnable().listViewProperty().removeListener(this);
                    setupListeners();
                }
            });
        } else {
            this.fixedCellSize = listView.getFixedCellSize();
            this.fixedCellSizeEnabled = fixedCellSize > 0;
            // this listener is not removed from the old listView
            // if listView reset to null
            // skin api is not suited for repeated add/remove: removes all (on a given property)
            // with no possibility to access just the one we added into the chain
            registerChangeListener(listView.fixedCellSizeProperty(), e -> {
                // this throws npe because getListView == null
                this.fixedCellSize = getSkinnable().getListView().getFixedCellSize();
                this.fixedCellSizeEnabled = fixedCellSize > 0;
            });
        }
    }


//----------- end old listener wiring

    /** {@inheritDoc} */
    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double pref = super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
        ListView<T> listView = getSkinnable().getListView();
        return listView == null ? 0 :
            listView.getOrientation() == Orientation.VERTICAL ? pref : Math.max(pref, getCellSize());
    }

    /** {@inheritDoc} */
    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (fixedCellSizeEnabled) {
            return fixedCellSize;
        }

        // Added the comparison between the default cell size and the requested
        // cell size to prevent the issue identified in RT-19873.
        final double cellSize = getCellSize();
        final double prefHeight = cellSize == DEFAULT_CELL_SIZE ? super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset) : cellSize;
        return prefHeight;
    }

    /** {@inheritDoc} */
    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (fixedCellSizeEnabled) {
            return fixedCellSize;
        }

        return super.computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /** {@inheritDoc} */
    @Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (fixedCellSizeEnabled) {
            return fixedCellSize;
        }

        return super.computeMaxHeight(width, topInset, rightInset, bottomInset, leftInset);
    }
    
    // test only
    double getFixedCellSize() {
        return fixedCellSize;
    }
}
