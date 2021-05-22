/*
 * Created on 29.09.2017
 *
 */
package test.com.sun.javafx.scene.control.celledit.infrastructure;

import java.util.Optional;

import static javafx.scene.control.TableColumn.*;
import static org.junit.Assert.*;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableViewEditReport extends EditEventReport<CellEditEvent> {

    @Override
    public void assertLastStartIndex(int index, Object first) {
        Optional<CellEditEvent> e = getLastEditStart();
        assertTrue(e.isPresent());
        assertNotNull("position on event must not be null", e.get().getTablePosition());
        assertEquals(getAllEditEventTexts("index on start event"), index, e.get().getTablePosition().getRow());
        assertEquals("column on start event", first, e.get().getTablePosition().getTableColumn());

    }

    @Override
    public void assertLastCancelIndex(int index, Object first) {
        Optional<CellEditEvent> e = getLastEditCancel();
        assertTrue(e.isPresent());
        assertNotNull("position on event must not be null", e.get().getTablePosition());
        assertEquals(getAllEditEventTexts("index on cancel event"), index, e.get().getTablePosition().getRow());
        assertEquals("column on cancel event", first, e.get().getTablePosition().getTableColumn());

    }

    @Override
    public void assertLastCommitIndex(int index, Object first, Object value) {
        Optional<CellEditEvent> e = getLastEditCommit();
        assertTrue(e.isPresent());
        assertNotNull("position on event must not be null", e.get().getTablePosition());
        assertEquals(getAllEditEventTexts("index on commit event"), index, e.get().getTablePosition().getRow());
        assertEquals("column on commit event", first, e.get().getTablePosition().getTableColumn());
        assertEquals("new value on commit event", value, e.get().getNewValue());
    }


    /**
     * @param listView
     */
    public TableViewEditReport(EditableControl listView) {
        super(listView);
        listView.addEditEventHandler(listView.editAny(), e -> addEvent((CellEditEvent) e));
    }

    @Override
    public String getEditEventText(CellEditEvent event) {
        // table, tablePosition (aka: row/column), eventType, newValue
        TablePosition pos = event.getTablePosition();
        TableColumn column = pos != null ? event.getTableColumn() :null;
        int row = pos != null ? pos.getRow() : -1;
        Object oldValue = pos != null ? event.getOldValue() : null;
        Object rowValue = pos != null ? event.getRowValue() : null;
        return "[tableViewEditEvent [ type: " + event.getEventType() + " pos: " + pos + " rowValue: " + rowValue + " oldValue: "
                + oldValue + " newValue: " + event.getNewValue();

    }

}
