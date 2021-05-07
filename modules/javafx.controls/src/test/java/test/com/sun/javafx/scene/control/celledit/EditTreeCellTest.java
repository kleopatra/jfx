/*
 * Created on 29.09.2017
 *
 */
package test.com.sun.javafx.scene.control.celledit;

import java.util.Optional;

import org.junit.Test;

import static org.junit.Assert.*;

import javafx.scene.control.IndexedCell;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeView.EditEvent;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.control.skin.TreeCellSkin;
import javafx.util.Callback;

/**
 * Test editing in TreeCell (no scenegraph)
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class EditTreeCellTest extends AbstractEditCellTestBase<TreeView, TreeCell> {

    @Test
    public void testCommitEditRespectHandler() {
        EditableControl<TreeView, TreeCell> tree = control;//createEditableControl();
//        new StageLoader(control.getControl());
        int editIndex = 1;
        IndexedCell cell = createEditableCellAt(tree, editIndex);
        TreeItem<String> editItem = tree.getControl().getTreeItem(editIndex);
        String oldValue = editItem.getValue();
        // do nothing
        tree.setOnEditCommit(e -> new String("dummy"));
        // start edit on control
        tree.edit(editIndex);
        EditEventReport report = createEditReport(tree);
        String editedValue = "edited";
        // commit edit on cell
        cell.commitEdit(editedValue);
        // test data
        assertEquals("value must not be changed", oldValue, tree.getControl().getTreeItem(editIndex).getValue());
        assertEquals(1, report.getEditEventSize());
    }
    
  //--------------------- old bugs, fixed in fx9    
    @Test
    public void testTreeCellSkin() {
        TreeCell cell = new TreeCell();
        cell.setSkin(new TreeCellSkin(cell));
    }

//----------------- implement super's assertions in terms of TreeView
    
    @Override
    protected void assertValueAt(int index, Object editedValue,
            EditableControl<TreeView, TreeCell> control) {
        TreeItem item = control.getControl().getTreeItem(index);
        assertEquals("value must be committed", editedValue, item.getValue());
    }

    @Override
    protected void assertLastCancelIndex(EditEventReport report, int index,
            Object column) {
        Optional<EditEvent> e = report.getLastEditCancel();
        assertTrue(e.isPresent());
        TreeItem item = e.get().getSource().getTreeItem(index);
        assertEquals(item, e.get().getTreeItem());
    }

    @Override
    protected void assertLastStartIndex(EditEventReport report, int index,
            Object column) {
        Optional<EditEvent> e = report.getLastEditStart();
        assertTrue(e.isPresent());
        TreeItem item = e.get().getSource().getTreeItem(index);
        assertEquals(item, e.get().getTreeItem());
    }

    @Override
    protected void assertLastCommitIndex(EditEventReport report, int index,
            Object target, Object value) {
        Optional<EditEvent> e = report.getLastEditCommit();
        assertTrue(e.isPresent());
        TreeItem item = e.get().getSource().getTreeItem(index);
        assertEquals(item, e.get().getTreeItem());
        
    }

    @Override
    protected EditEventReport createEditReport(EditableControl control) {
        return new TreeViewEditReport(control);
    }

    @Override
    protected EditableControl<TreeView, TreeCell> createEditableControl() {
        TreeItem rootItem = new TreeItem<>("root");
        rootItem.getChildren().addAll(
                new TreeItem<>("one"),
                new TreeItem<>("two"),
                new TreeItem<>("three")
                
                );
        EditableControl.ETreeView treeView = new EditableControl.ETreeView(rootItem);
        treeView.setShowRoot(false);
        treeView.setEditable(true);
        treeView.setCellFactory(TextFieldTreeCell.forTreeView()); //createTextFieldCellFactory());
        treeView.getFocusModel().focus(-1);
        return treeView;
    }

    @Override
    protected Callback<TreeView, TreeCell> createTextFieldCellFactory() {
        return e -> new TextFieldTreeCell();
    }
}
