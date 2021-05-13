/*
 * Created on 29.09.2017
 *
 */
package test.com.sun.javafx.scene.control.celledit;

import java.util.Optional;

import static org.junit.Assert.*;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView.EditEvent;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeViewEditReport extends EditEventReport<EditEvent> {



    @Override
    public void assertLastCancelIndex(int index, Object column) {
        Optional<EditEvent> e = getLastEditCancel();
        assertTrue(e.isPresent());
        TreeItem item = e.get().getSource().getTreeItem(index);
        assertEquals(item, e.get().getTreeItem());
    }

    @Override
    public void assertLastStartIndex(int index, Object column) {
        Optional<EditEvent> e = getLastEditStart();
        assertTrue(e.isPresent());
        TreeItem item = e.get().getSource().getTreeItem(index);
        assertEquals(item, e.get().getTreeItem());
    }

    @Override
    public void assertLastCommitIndex(int index, Object target, Object value) {
        Optional<EditEvent> e = getLastEditCommit();
        assertTrue(e.isPresent());
        TreeItem item = e.get().getSource().getTreeItem(index);
        assertEquals(item, e.get().getTreeItem());
    }

    /**
     * @param listView
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public TreeViewEditReport(EditableControl listView) {
        super(listView);
        listView.addEditEventHandler(listView.editAny(), e -> addEvent((EditEvent) e));
    }

    @Override
    public String getEditEventText(EditEvent event) {
        return "[TreeViewEditEvent [type: " + event.getEventType() + " treeItem "
                + event.getTreeItem() + " newValue " + event.getNewValue() + "]";
    }


}
