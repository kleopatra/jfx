/*
 * Created on 29.09.2017
 *
 */
package test.com.sun.javafx.scene.control.celledit.infrastructure;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.util.Callback;

/**
 * Decorator for editable virtualized controls. Useful in testing cells and their editing behaviour.
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public interface EditableControl<C extends Control, I extends IndexedCell> {

//------------ factory methods to create the decorated controls
    

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

}