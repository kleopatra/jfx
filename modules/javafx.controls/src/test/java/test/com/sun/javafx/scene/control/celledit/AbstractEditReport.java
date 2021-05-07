/*
 * Created on 09.09.2017
 *
 */
package test.com.sun.javafx.scene.control.celledit;

import java.util.Optional;

import static org.junit.Assert.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventType;
/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class AbstractEditReport<E extends Event> {


    private EditableControl source;
    
    protected ObservableList<E> editEvents = FXCollections.<E>observableArrayList();
    
    public AbstractEditReport(EditableControl editableControl) {
        this.source = editableControl;
    }
    
    public EditableControl getSource() {
        return source;
    }
    /**
     * Returns the list of editEvents as unmodifiable list, most recent first.
     * @return
     */
    public ObservableList<E> getEditEvents(){
        return FXCollections.unmodifiableObservableList(editEvents);
    }
    /**
     * Clears list of received events. 
     */
    public void clear() {
        editEvents.clear();
    }
    
    /**
     * Returns # of all events.
     */
    public int getEditEventSize() {
        return editEvents.size();
    }
    
    /**
     * Returns # of events of given type.
     */
    public int getEventTypeSize(EventType type) {
        return (int) editEvents.stream()
                .filter(e -> e.getEventType().equals(type))
                .count();
    }
    
    public Optional<E> getLastEditStart() {
        return editEvents.stream()
                .filter(e -> e.getEventType().equals(source.editStart()))
                .findFirst();
    }
    
    public Optional<E> getLastEditCancel() {
        return editEvents.stream()
                .filter(e -> e.getEventType().equals(source.editCancel()))
                .findFirst();
    }
    public Optional<E> getLastEditCommit() {
        return editEvents.stream()
                .filter(e -> e.getEventType().equals(source.editCommit()))
                .findFirst();
    }
    
    /**
     * Returns true if the last event in the received events represents editStart,
     * false otherwise.
     * @return
     */
    public boolean isLastEditStart() {
        return hasEditEvents() ? getLastAnyEvent().getEventType().equals(source.editStart()) : false;
    }
    
    /**
     * Returns true if the last event in the received events represents editCommit,
     * false otherwise.
     * @return
     */
    public boolean isLastEditCommit() {
        return hasEditEvents() ? getLastAnyEvent().getEventType().equals(source.editCommit()) : false;
    }
    
    /**
     * Returns true if the last event in the received events represents editCancel,
     * false otherwise.
     * @return
     */
    public boolean isLastEditCancel() {
        return hasEditEvents() ? getLastAnyEvent().getEventType().equals(source.editCancel()) : false;
    }
    
    public E getLastAnyEvent() {
        return hasEditEvents() ? editEvents.get(0) : null;
    }
    
    public boolean hasEditEvents() {
        return !editEvents.isEmpty();
    }
    
    /**
     * Adds the given event.
     * 
     * Impl. note: adds at 0.
     */
    protected void addEvent(E event) {
        editEvents.add(0, event);
    }
    
    /**
     * Returns the enhanced edit text of all events received, last received first. 
     */
    public String getAllEditEventTexts(String message) {
        if (!hasEditEvents()) return "noEvents";
        String edits = message + "\n";
        for (E editEvent : editEvents) {
            edits += getEditEventText(editEvent) + "\n";
        }
        return edits;
    }
    
    public String getEditEventText(E event) {
        return event.toString();
    }; 
    
    public void assertLastCancelIndex(int index, Object column) {
        fail("not yet implemented");
    }
    public void assertLastStartIndex(int index, Object column) {
        fail("not yet implemented");
    }
    public void assertLastCommitIndex(int index, Object target, Object value) {
        fail("not yet implemented");
    }

    
}
