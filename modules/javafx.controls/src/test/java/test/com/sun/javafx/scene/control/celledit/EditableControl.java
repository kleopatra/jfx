/*
 * Created on 29.09.2017
 *
 */
package test.com.sun.javafx.scene.control.celledit;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * Decorator for editable virtualized controls. Useful in testing cells and their editing behaviour.
 *
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public interface EditableControl<C extends Control, I extends IndexedCell> {

    void setEditable(boolean editable);
    boolean isEditable();
    /**
     *  Note: this method is the same as the control's except for
     *  tabular controls: they must implement this to delegate
     *  to the target column.
     *
     * @param factory
     */
    void setCellFactory(Callback<C, I> factory);

    Callback<C, I> getCellFactory();
    /**
     * Creates and returns a cell that's fully configured to allow switching into editing state. It does use
     * the control's cellFactory.
     */
    I createEditableCell();

    /**
     * Creates and returns a cell produced with the cellFactory.
     */
    I createCell();
    
    /**
     * Returns the control that's associated with the given cell.
     * 
     */
    C getCellControl(I cell);

    void fireEvent(Event ev);

    EventHandler getOnEditCommit();

    EventHandler getOnEditCancel();

    EventHandler getOnEditStart();

    void setOnEditCommit(EventHandler handler);

    void setOnEditCancel(EventHandler handler);

    void setOnEditStart(EventHandler handler);

    <T extends Event> void addEditEventHandler(EventType<T> type, EventHandler<? super T> handler);

    EventType editCommit();

    EventType editCancel();

    EventType editStart();

    EventType editAny();

    C getControl();

    int getEditingIndex();

    void edit(int index);

    /**
     * Returns the value that might be changed by cell editing.
     */
    Object getValueAt(int index);

    void removeItemAt(int index);
    void addItemAt(int index);
    
    /**
     * Returns the value at index if targetColumn is null or at index and targetColumn if not.
     *
     * @param index
     * @return
     */
    default Object getTargetColumn() {
        return null;
    }

    EditEventReport createEditReport();

    class EListView extends ListView implements EditableControl<ListView, ListCell> {

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
     *
     * @author Jeanette Winzenburg, Berlin
     */
    class ETableView extends TableView implements EditableControl<TableView, TableCell> {

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
            // TODO Auto-generated method stub
            getItems().add(index, "added" + index);
            
        }


    }

    class ETreeView extends TreeView implements EditableControl<TreeView, TreeCell> {

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

}