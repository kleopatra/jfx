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

package test.com.sun.javafx.scene.control.celledit;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.Callback;

/**
 * Factory to create EditableControls. Contains facade implementations 
 * for Tree-/TableView, ListView, TreeView
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class EditableControlFactory {
    
    public static EditableControl<ListView, ListCell> createEditableListView() {
        EListView control = new EListView(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        control.setEditable(true);
        control.setCellFactory(TextFieldListCell.forListView());
        control.getFocusModel().focus(-1);
        return control;
    }

    public static EditableControl<TreeView, TreeCell> createEditableTreeView() {
        TreeItem rootItem = new TreeItem<>("root");
        rootItem.getChildren().addAll(
                new TreeItem<>("zero"),
                new TreeItem<>("one"),
                new TreeItem<>("two")
                
                );
        EditableControlFactory.ETreeView treeView = new EditableControlFactory.ETreeView(rootItem);
        treeView.setShowRoot(false);
        treeView.setEditable(true);
        treeView.setCellFactory(TextFieldTreeCell.forTreeView());
        treeView.getFocusModel().focus(-1);
        return treeView;
    }
    
    public static EditableControl<TreeTableView, TreeTableCell> createEditableTreeTableView() {
        ObservableList<MenuItem> items =
//              withExtractor
//              ? FXCollections.observableArrayList(
//                      e -> new Observable[] { e.textProperty() })
//              :
                  FXCollections.observableArrayList();
        items.addAll(new MenuItem("first"), new MenuItem("second"),
              new MenuItem("third"));
        TreeItem root = new TreeItem(new MenuItem("root"));
        items.forEach(menuItem -> root.getChildren().add(new TreeItem(menuItem)));
        EditableControlFactory.ETreeTableView treeTable = new EditableControlFactory.ETreeTableView(root);
        treeTable.setShowRoot(false);
        treeTable.setEditable(true);
        
        TreeTableColumn<MenuItem, String> column = new TreeTableColumn<>("Text"); 
        column.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        column.setCellValueFactory(new TreeItemPropertyValueFactory<>("text"));
        treeTable.getColumns().addAll(column);
        treeTable.getFocusModel().focus(-1);
        return treeTable;
    }

    
    public static EditableControl<TableView, TableCell> createEditableTableView() {
        ObservableList<TableColumn> items =
//                withExtractor
//                ? FXCollections.observableArrayList(
//                        e -> new Observable[] { e.textProperty() })
//                :
                    FXCollections.observableArrayList();
        items.addAll(new TableColumn("first"), new TableColumn("second"),
                new TableColumn("third"));
        EditableControlFactory.ETableView table = new EditableControlFactory.ETableView(items);
        table.setEditable(true);
//        table.getSelectionModel().setCellSelectionEnabled(cellSelectionEnabled);

        TableColumn<TableColumn, String> first = new TableColumn<>("Text");
        first.setCellFactory(TextFieldTableCell.forTableColumn());
        first.setCellValueFactory(new PropertyValueFactory<>("text"));

        table.getColumns().addAll(first);
        table.getFocusModel().focus(-1);
        return table;
    }


    

    public static class EListView extends ListView implements EditableControl<ListView, ListCell> {
    
        @Override
        public ListView getControl() {
            return this;
        }
    
        public EListView() {
            super();
        }
    
        public EListView(ObservableList arg0) {
            super(arg0);
        }
    
        @Override
        public EventType editAny() {
            return editAnyEvent();
        }
    
        @Override
        public EventType editCommit() {
            return editCommitEvent();
        }
    
        @Override
        public EventType editCancel() {
            return editCancelEvent();
        }
    
        @Override
        public EventType editStart() {
            return editStartEvent();
        }
    
        @Override
        public <T extends Event> void addEditEventHandler(EventType<T> type,
                EventHandler<? super T> handler) {
            addEventHandler(type, handler);
        }
    
        @Override
        public ListCell createEditableCell() {
            ListCell cell = createCell();
            cell.updateListView(getControl());
            return cell;
        }
    
        @Override
        public ListCell createCell() {
            return (ListCell) getCellFactory().call(this);
        }
        
        @Override
        public ListView getCellControl(ListCell cell) {
            return cell.getListView();
        }
    
        @Override
        public Object getValueAt(int index) {
            return getItems().get(index);
        }
    
        @Override
        public EditEventReport createEditReport() {
            return new ListViewEditReport(this);
        }
    
        @Override
        public void removeItemAt(int index) {
            getItems().remove(index);
        }
    
        @Override
        public void addItemAt(int index) {
            getItems().add(index, "added" + index);
        }
    
    }

    /**
     * A TableView decorated as EditableControl. Note that the table must be instantiated with at least one
     * column and all column related edit api is passed to the target column.
     */
    public static class ETableView extends TableView implements EditableControl<TableView, TableCell> {
    
        public ETableView() {
            super();
        }
    
        public ETableView(ObservableList items) {
            super(items);
        }
    
        @Override
        public void setCellFactory(Callback<TableView, TableCell> factory) {
            getTargetColumn().setCellFactory(factory);
        }
    
        @Override
        public Callback<TableView, TableCell> getCellFactory() {
            return getTargetColumn().getCellFactory();
        }
    
        @Override
        public TableColumn getTargetColumn() {
            return (TableColumn) getColumns().get(0);
        }
    
        @Override
        public Object getValueAt(int index) {
            TableColumn column = getTargetColumn();
            return column.getCellObservableValue(index).getValue();
        }
    
        @Override
        public EventHandler getOnEditCommit() {
            return getTargetColumn().getOnEditCommit();
        }
    
        @Override
        public EventHandler getOnEditCancel() {
            return getTargetColumn().getOnEditCancel();
        }
    
        @Override
        public EventHandler getOnEditStart() {
            return getTargetColumn().getOnEditStart();
        }
    
        @Override
        public void setOnEditCommit(EventHandler handler) {
            getTargetColumn().setOnEditCommit(handler);
        }
    
        @Override
        public void setOnEditCancel(EventHandler handler) {
            getTargetColumn().setOnEditCancel(handler);
        }
    
        @Override
        public void setOnEditStart(EventHandler handler) {
            getTargetColumn().setOnEditStart(handler);
        }
    
        @Override
        public EventType editCommit() {
            return TableColumn.editCommitEvent();
        }
    
        @Override
        public EventType editCancel() {
            return TableColumn.editCancelEvent();
        }
    
        @Override
        public EventType editStart() {
            return TableColumn.editStartEvent();
        }
    
        @Override
        public EventType editAny() {
            return TableColumn.editAnyEvent();
        }
    
        @Override
        public TableView getControl() {
            return this;
        }
    
        @Override
        public int getEditingIndex() {
            TablePosition pos = getEditingCell();
            return pos != null ? pos.getRow() : -1;
        }
    
        @Override
        public void edit(int index) {
            TableColumn column = index < 0 ? null : getTargetColumn();
            edit(index, column);
        }
    
        @Override
        public <T extends Event> void addEditEventHandler(EventType<T> type,
                EventHandler<? super T> handler) {
            getTargetColumn().addEventHandler(type, handler);
        }
    
        @Override
        public TableCell createEditableCell() {
            TableCell cell = createCell();
            cell.updateTableColumn(getTargetColumn());
            cell.updateTableView(getControl());
            return cell;
        }
    
        @Override
        public TableCell createCell() {
            TableCell cell = (TableCell) getTargetColumn().getCellFactory().call(getTargetColumn());
            return cell;
        }
    
        @Override
        public EditEventReport createEditReport() {
            return new TableViewEditReport(this);
        }
    
        @Override
        public TableView getCellControl(TableCell cell) {
            return cell.getTableView();
        }
    
        @Override
        public void removeItemAt(int index) {
            getItems().remove(index);
            
        }
    
        @Override
        public void addItemAt(int index) {
            getItems().add(index, "added" + index);
        }
    
    
    }

    public static class ETreeTableView extends TreeTableView implements EditableControl<TreeTableView, TreeTableCell> {
        
    
        public ETreeTableView() {
            super();
        }
    
        public ETreeTableView(TreeItem root) {
            super(root);
        }
    
        @Override
        public void setCellFactory(Callback<TreeTableView, TreeTableCell> factory) {
            getTargetColumn().setCellFactory(factory);
        }
    
        @Override
        public Callback<TreeTableView, TreeTableCell> getCellFactory() {
            return getTargetColumn().getCellFactory();
        }
    
        @Override
        public TreeTableColumn getTargetColumn() {
            return (TreeTableColumn) getColumns().get(0);
        }
    
        @Override
        public Object getValueAt(int index) {
            TreeTableColumn column = getTargetColumn();
            return column.getCellObservableValue(index).getValue();
        }
    
        @Override
        public EventHandler getOnEditCommit() {
            return getTargetColumn().getOnEditCommit();
        }
    
        @Override
        public EventHandler getOnEditCancel() {
            return getTargetColumn().getOnEditCancel();
        }
    
        @Override
        public EventHandler getOnEditStart() {
            return getTargetColumn().getOnEditStart();
        }
    
        @Override
        public void setOnEditCommit(EventHandler handler) {
            getTargetColumn().setOnEditCommit(handler);
        }
    
        @Override
        public void setOnEditCancel(EventHandler handler) {
            getTargetColumn().setOnEditCancel(handler);
        }
    
        @Override
        public void setOnEditStart(EventHandler handler) {
            getTargetColumn().setOnEditStart(handler);
        }
    
        @Override
        public EventType editCommit() {
            return TreeTableColumn.editCommitEvent();
        }
    
        @Override
        public EventType editCancel() {
            return TreeTableColumn.editCancelEvent();
        }
    
        @Override
        public EventType editStart() {
            return TreeTableColumn.editStartEvent();
        }
    
        @Override
        public EventType editAny() {
            return TreeTableColumn.editAnyEvent();
        }
    
        @Override
        public TreeTableView getControl() {
            return this;
        }
    
        @Override
        public int getEditingIndex() {
            TreeTablePosition pos = getEditingCell();
            return pos != null ? pos.getRow() : -1;
        }
    
        @Override
        public void edit(int index) {
            TreeTableColumn column = index < 0 ? null : getTargetColumn();
            edit(index, column);
        }
    
        @Override
        public <T extends Event> void addEditEventHandler(EventType<T> type,
                EventHandler<? super T> handler) {
            getTargetColumn().addEventHandler(type, handler);
        }
    
        @Override
        public TreeTableCell createEditableCell() {
            TreeTableCell cell = createCell();
            cell.updateTreeTableColumn(getTargetColumn());
            cell.updateTreeTableView(getControl());
            return cell;
        }
    
        @Override
        public TreeTableCell createCell() {
            TreeTableCell cell = (TreeTableCell) getTargetColumn().getCellFactory().call(getTargetColumn());
            return cell;
        }
    
        @Override
        public EditEventReport createEditReport() {
            return new TreeTableViewEditReport(this);
        }
    
        @Override
        public TreeTableView getCellControl(TreeTableCell cell) {
            return cell.getTreeTableView();
        }
    
        @Override
        public void removeItemAt(int index) {
            TreeItem item = getTreeItem(index);
            TreeItem parent = item.getParent();
            if (parent != null) {
                parent.getChildren().remove(item);
            }
        }
    
        @Override
        public void addItemAt(int index) {
            TreeItem item = getTreeItem(index);
            TreeItem parent = item.getParent();
            if (parent != null) {
                int indexInChildren = parent.getChildren().indexOf(item);
                parent.getChildren().add(indexInChildren, new TreeItem("added" + indexInChildren));
            }
        }
    
    }

    public static class ETreeView extends TreeView implements EditableControl<TreeView, TreeCell> {
    
        public ETreeView() {
            super();
        }
    
        public ETreeView(TreeItem root) {
            super(root);
        }
    
        @Override
        public <T extends Event> void addEditEventHandler(EventType<T> type,
                EventHandler<? super T> handler) {
            addEventHandler(type, handler);
    
        }
    
        @Override
        public EventType editCommit() {
            return editCommitEvent();
        }
    
        @Override
        public EventType editCancel() {
            return editCancelEvent();
        }
    
        @Override
        public EventType editStart() {
            return editStartEvent();
        }
    
        @Override
        public EventType editAny() {
            return editAnyEvent();
        }
    
        @Override
        public TreeView getControl() {
            return this;
        }
    
        @Override
        public int getEditingIndex() {
            TreeItem item = getEditingItem();
            return getRow(item);
        }
    
        @Override
        public void edit(int index) {
            TreeItem item = getTreeItem(index);
            edit(item);
        }
    
    
        @Override
        public Object getValueAt(int index) {
            TreeItem item = getTreeItem(index);
            return item != null ? item.getValue() : null;
        }
    
        @Override
        public TreeCell createEditableCell() {
            TreeCell cell = createCell(); //(TreeCell) getCellFactory().call(this);
            cell.updateTreeView(this);
            return cell;
        }
    
        @Override
        public TreeCell createCell() {
            TreeCell cell = (TreeCell) getCellFactory().call(this);
            return cell;
        }
    
        @Override
        public EditEventReport createEditReport() {
            return new TreeViewEditReport(this);
        }
    
        @Override
        public TreeView getCellControl(TreeCell cell) {
            return cell.getTreeView();
        }
    
        @Override
        public void removeItemAt(int index) {
            TreeItem item = getTreeItem(index);
            TreeItem parent = item.getParent();
            if (parent != null) {
                parent.getChildren().remove(item);
            }
        }
    
        @Override
        public void addItemAt(int index) {
            TreeItem item = getTreeItem(index);
            TreeItem parent = item.getParent();
            if (parent != null) {
                int indexInChildren = parent.getChildren().indexOf(item);
                parent.getChildren().add(indexInChildren, new TreeItem("added" + indexInChildren));
            }
        }
    
    }

    private EditableControlFactory() {}

}
