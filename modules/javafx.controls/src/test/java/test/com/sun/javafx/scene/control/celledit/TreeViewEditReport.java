/*
 * Created on 29.09.2017
 *
 */
package test.com.sun.javafx.scene.control.celledit;

import javafx.scene.control.TreeView.EditEvent;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeViewEditReport extends EditEventReport<EditEvent> {

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
