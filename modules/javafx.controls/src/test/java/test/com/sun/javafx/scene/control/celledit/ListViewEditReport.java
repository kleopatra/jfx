/*
 * Created on 29.09.2017
 *
 */
package test.com.sun.javafx.scene.control.celledit;

import java.util.Optional;

import static org.junit.Assert.*;

import javafx.scene.control.ListView;
import javafx.scene.control.ListView.EditEvent;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ListViewEditReport extends EditEventReport<ListView.EditEvent> {

    @Override
    public void assertLastStartIndex(int index, Object target) {
        Optional<EditEvent> e = getLastEditStart();
        assertTrue(e.isPresent());
        assertEquals(getAllEditEventTexts("index on start event"), index, e.get().getIndex());
    }
    
    @Override
    public void assertLastCancelIndex(int index, Object target) {
        Optional<EditEvent> e = getLastEditCancel();
        assertTrue(e.isPresent());
        assertEquals(getAllEditEventTexts("index on cancel event"), index, e.get().getIndex());
    }
    
    @Override
    public void assertLastCommitIndex(int index, Object target, Object value) {
        Optional<EditEvent> commit = getLastEditCommit();
        assertTrue(commit.isPresent());
        assertEquals("index on commit event", index, commit.get().getIndex());
        assertEquals("newValue on commit event", value, commit.get().getNewValue());
        assertEquals(getAllEditEventTexts("commit must fire a single event "), 
                1, getEditEventSize());
    }

    /**
     * @param listView
     */
    public ListViewEditReport(EditableControl listView) {
        super(listView);
        listView.addEditEventHandler(listView.editAny(), e -> addEvent((EditEvent) e));
    }

    @Override
    public String getEditEventText(ListView.EditEvent event) {
      return "[ListViewEditEvent [type: " + event.getEventType() + " index " 
              + event.getIndex() + " newValue " + event.getNewValue() + "]";
      
    }

}
