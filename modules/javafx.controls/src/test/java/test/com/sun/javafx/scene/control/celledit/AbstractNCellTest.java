/*
 * Created on 09.03.2016
 *
 */
package test.com.sun.javafx.scene.control.celledit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.VirtualFlowTestUtils.*;

import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.util.Callback;
import test.com.sun.javafx.scene.control.infrastructure.StageLoader;
import test.com.sun.javafx.scene.control.infrastructure.VirtualFlowTestUtils;
/**
 * 
 * Base tests that are same/similar to all cell types. Initially copied all from 
 * CellTest, then deleted all tests that are not listCell
 * 
 * Note: N in the name denotes testing without StageLoader by default, 
 * can be added though
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class AbstractNCellTest<C extends Control, I extends IndexedCell> {
    
    protected StageLoader stageLoader;
    protected EditableControl control;

//---------------- contract violations
    
    /**
     * Must not fire null events.
     */
    @Test (expected = NullPointerException.class)
    public void nullEventFireNotFired() {
        control.fireEvent(null);
    }
    
    /**
     * Cell.startEdit doesn't switch into editing if empty. That's
     * the case for a cell without view.
     * But: cell must be attached to a control to switch to editing!
     * ---
     * 
     * JDK-8188026: precondition violation of TextFieldXXCell(probably for all cells 
     * in cell package) - virulent for start
     */
//    @Ignore("JDK-8188026")
    @Test
    public void nullControlOnStartEditMustNotThrow() {
        I cell = createTextFieldCellFactory().call(null);
        cell.startEdit();
        assertFalse("cell without control must not be editing", cell.isEditing());
    }
    
    /**
     * Test cancel with null control
     */
//  @Ignore("JDK-8188026")
    @Test
    public void nullControlOnCancelEditMustNotThrow() {
        I cell = createTextFieldCellFactory().call(null);
        cell.cancelEdit();
    }
    
    /**
     * Test cancel with null control
     */
//  @Ignore("JDK-8188026")
    @Test
    public void nullControlOnCommitEditMustNotThrow() {
        I cell = createTextFieldCellFactory().call(null);
        cell.commitEdit("dummy");
    }
 
//---------------------
    
    /**
     * FIXME: report
     */
    @Test
    public void startOnCellTwiceMustFireSingleEvent() {
        int editIndex = 1;
        IndexedCell cell = createEditableCellAt(control, editIndex);
        AbstractEditReport report = createEditReport(control);
        // start edit on control
        cell.startEdit();
        // start again -> nothing changed, no event
        cell.startEdit();
        // test # all editEvent
        assertEquals(report.getAllEditEventTexts("second start on same must not fire event: "), 
                1, report.getEditEventSize());
    }
    
    @Test
    public void startOnControlTwiceMustFireSingleEvent() {
        int editIndex = 1;
        // need a cell that will fire
        IndexedCell cell = createEditableCellAt(control, editIndex);
        AbstractEditReport report = createEditReport(control);
        // start edit on control
        control.edit(editIndex);
        // working as expected because index unchanged -> no change fired
        control.edit(editIndex);
        // test editEvent
        assertEquals(report.getAllEditEventTexts("second start on same must not fire event: "), 
                1, report.getEditEventSize());
    }
    

//---------------- test editEvents and cell/control state   
    
    /**
     * Test notification/cell/list state with multiple edits
     * 
     * the index of cancel is always incorrect: a cancel is fired with index on the
     * new edit position.
     * Here the incorrect index is fired before the start event.
     * 
     * Here: 
     * edit(1)
     * edit(0)
     * 
     */
    @Test
    public void changeEditIndexOnControlReversed() {
        assertChangeEdit(1,  0);
    }
    
    /**
     * Test notification/cell/table state with multiple edits
     * 
     * the index of cancel is always incorrect: a cancel is fired with index on the
     * new edit position.
     * Here the incorrect index is fired before the start event.
     * 
     * 
     * Here: 
     * edit(0)
     * edit(1)
     */
    @Test
    public void changeEditIndexOnControl() {
        assertChangeEdit(0, 1);
    }
    
    protected void assertChangeEdit(int editIndex, int secondEditIndex) {
        // initial editing index
        IndexedCell editingCell = createEditableCellAt(control, editIndex);
        IndexedCell secondEditingCell = createEditableCellAt(control, secondEditIndex);
        // start edit on control with initial editIndex
        control.edit(editIndex);
        assertTrue(editingCell.isEditing());
        assertEquals(editIndex, editingCell.getIndex());
        AbstractEditReport report = createEditReport(control);
        // switch editing to second
        control.edit(secondEditIndex);
//        LOG.info("" + report.getAllEditEventTexts("edit(0) -> edit(1): "));
        // test cell state
        assertFalse(editingCell.isEditing());
        assertEquals(editIndex, editingCell.getIndex());
        assertTrue(secondEditingCell.isEditing());
        assertEquals(secondEditIndex, secondEditingCell.getIndex());
        // test editEvent
        assertLastStartIndex(report, secondEditIndex, control.getTargetColumn());
        assertLastCancelIndex(report, editIndex, control.getTargetColumn());
        
    }
    
    
    /**
     * start edit on list
     * -> commit edit on cell with newValue (same with identical value)
     * 
     * Here we see 
     * ListView: receives both editCommit (expected) and 
     * editCancel (unexpected) when edit committed
     * 
     * reported:
     * https://bugs.openjdk.java.net/browse/JDK-8187307
     *
     * This fails for ListView in scenegraph, passes with raw cell .. test error?
     * maybe because there is no other cell that could fire?
     * 
     * probably not: analysis from the report: 
     * The underlying reason is that the default commitHandler (like any reasonable implementation would do) 
     * replaces the old value with the edited value in the list's items - skin listens and cancels the edit. 
     * 
     * This is testable only within the scenegraph.
     * 
     * Note: can't commit on control, missing api
     */
//    @Ignore("JDK-8187307")
    @Test
    public void commitOnCellMustNotFireCancel() {
        int editIndex = 1;
        IndexedCell cell = createEditableCellAt(control, editIndex);
        // start edit on control
        control.edit(editIndex);
        AbstractEditReport report = createEditReport(control);
        // commit value on cell
        String editedValue = "edited";
        cell.commitEdit(editedValue);
        // test control state
        assertEquals(-1, control.getEditingIndex());
        assertValueAt(editIndex, editedValue, control);
//        assertEquals(editedValue, control.getItems().get(editIndex));
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        assertLastCommitIndex(report, editIndex, control.getTargetColumn(), editedValue);
    }
    
//  @Ignore("JDK-8187307")
    @Test
    public void commitOnCellMustNotFireCancelInSceneGraph() {
        int editIndex = 1;
        stageLoader = new StageLoader(control.getControl());
        IndexedCell cell = VirtualFlowTestUtils.getCell(control.getControl(), editIndex);
        // start edit on control
        control.edit(editIndex);
        AbstractEditReport report = createEditReport(control);
        // commit value on cell
        String editedValue = "edited";
        cell.commitEdit(editedValue);
        // test control state
        assertEquals(-1, control.getEditingIndex());
        assertValueAt(editIndex, editedValue, control);
//        assertEquals(editedValue, control.getItems().get(editIndex));
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        assertLastCommitIndex(report, editIndex, control.getTargetColumn(), editedValue);
    }
    
    /**
     * Test that nothing changed by a cancel if not editing. 
     */
    @Test
    public void cancelOnCellNotEditing() {
        int editIndex = 1;
        IndexedCell cell = createEditableCellAt(control, editIndex);
        Object value = cell.getItem();
        AbstractEditReport report = createEditReport(control);
        // cancel edit on cell
        cell.cancelEdit();
        // test control state
        assertEquals(-1, control.getEditingIndex());
        assertValueAt(editIndex, value, control);
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        assertEquals(0, report.getEditEventSize());
    }
    
    /**
     * Test that nothing changed by a commit if not editing. 
     */
    @Test
    public void commitOnCellWhenNotEditing() {
        int editIndex = 1;
        IndexedCell cell = createEditableCellAt(control, editIndex);
        Object value = cell.getItem();
        AbstractEditReport report = createEditReport(control);
        // commit value on cell
        String editedValue = "edited";
        cell.commitEdit(editedValue);
        // test control state
        assertEquals(-1, control.getEditingIndex());
        assertValueAt(editIndex, value, control);
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        assertEquals(0, report.getEditEventSize());
    }
    
    protected abstract void assertValueAt(int index, Object editedValue, EditableControl<C, I> control);
    
    /**
     * Here: cancel the edit with cell.cancelEdit ->
     * the cancel index is correct
     * ListView: EditEvent on cancel has incorrect index
     * 
     * reported: https://bugs.openjdk.java.net/browse/JDK-8187226
     * 
     */
    @Test
    public void cancelOnCellEvent() {
        int editIndex = 1;
        IndexedCell cell = createEditableCellAt(control, editIndex);
        // start edit on control
        control.edit(editIndex);
        AbstractEditReport report = createEditReport(control);
        // cancel edit on cell
        cell.cancelEdit();
        // test cell state
        assertEquals(-1, control.getEditingIndex());
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        assertLastCancelIndex(report, editIndex, control.getTargetColumn());
    }
    
    /**
     * Here: cancel the edit with list.edit(-1)
     * ListView: EditEvent on cancel has incorrect index
     * 
     * reported: https://bugs.openjdk.java.net/browse/JDK-8187226
     * 
     */
    @Test
    public void cancelOnControlEvent() {
        int editIndex = 1;
        IndexedCell cell = createEditableCellAt(control, editIndex);
        // start edit on control
        control.edit(editIndex);
        AbstractEditReport report = createEditReport(control);
        // cancel edit on control
        control.edit(-1);
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        assertLastCancelIndex(report, editIndex, control.getTargetColumn());
    }
    
    /**
     * Incorrect index in editStart event when edit started on cell
     * 
     * reported as
     * https://bugs.openjdk.java.net/browse/JDK-8187432
     * 
     */
    @Test
    public void startOnCellEvent() {
        int editIndex = 1;
        IndexedCell cell = createEditableCellAt(control, editIndex);
        AbstractEditReport report = createEditReport(control);
        // start edit on cell
        cell.startEdit();
        // test cell state
        assertTrue(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        assertLastStartIndex(report, editIndex, control.getTargetColumn());
//        Optional<EditEvent> e = report.getLastEditStart();
//        assertEquals("index on start event", editIndex, e.get().getIndex());
    }
    
    
    @Test
    public void startOnControlEvent() {
        int editIndex = 1;
        IndexedCell cell = createEditableCellAt(control, editIndex);
        AbstractEditReport report = createEditReport(control);
        // start edit on control
        control.edit(editIndex);
        // test cell state
        assertTrue(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        assertLastStartIndex(report, editIndex, control.getTargetColumn());
    }
    
    protected abstract void assertLastCancelIndex(AbstractEditReport report, int index, Object column);
    protected abstract void assertLastStartIndex(AbstractEditReport report, int index, Object column);
    protected abstract void assertLastCommitIndex(AbstractEditReport report, int index, Object target, Object value);

    /**
     * Test update of editing location on control
     */
    @Test
    public void commitOnCellResetsEditingIndex() {
        int editIndex = 1;
        IndexedCell cell = createEditableCellAt(control, editIndex);
        control.edit(editIndex);
        // commit edit on cell
        cell.commitEdit("edited");
        // test editing location
        assertEquals("editingIndex must be updated", -1, control.getEditingIndex());
    }
    
    /**
     * Test update of editing location on control
     */
    @Test
    public void cancelOnCellResetsEditingIndex() {
        int editIndex = 1;
        IndexedCell cell = createEditableCellAt(control, editIndex);
        control.edit(editIndex);
        // cancel edit on cell
        cell.cancelEdit();
        // test editing location
        assertEquals("editingIndex must be updated", -1, control.getEditingIndex());
    }
    
    /**
     * Test update of editing location on control
     */
    @Test
    public void startOnCellSetsEditingIndex() {
        int editIndex = 1;
        IndexedCell cell =  createEditableCellAt(control, editIndex);
        // start edit on cell
        cell.startEdit();
        // test editing location
        assertEquals("editingIndex must be updated", editIndex, control.getEditingIndex());
    }

    /**
     * Creates and configures a cell at the given index.
     * 
     * @param control
     * @param editIndex
     * @return
     */
    protected IndexedCell createEditableCellAt(EditableControl<C,I> control, int editIndex) {
        IndexedCell cell = control.createEditableCell();
        cell.updateIndex(editIndex);
        return cell;
    }

    /**
     * Returns the cell at index from the VirtualFlow.
     * 
     * @param control
     * @param editIndex
     * @return
     */
    protected IndexedCell getCellAt(EditableControl<C,I> control, int editIndex) {
        return getCell(control.getControl(), editIndex);
    }

//----------------------- focus state
    

// ------------------ test default edit handlers
    /**
     * Test default edit handlers: expected none for start/cancel,
     * default that commits
     * 
     * Here: List
     */
    @Test
    public void testEditHandler() {
        assertNull(control.getOnEditCancel());
        assertNull(control.getOnEditStart());
        assertNotNull("listView must have default commit handler", control.getOnEditCommit());
    }
    
    
//------------ infrastructure methods

   
    protected abstract AbstractEditReport createEditReport(EditableControl control);
    protected abstract EditableControl<C, I> createEditableControl();
    
    protected abstract Callback<C, I> createTextFieldCellFactory();
    
//----------------- setup initial/state
    
    @Before
    public void setup() {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException)throwable;
            } else {
                Thread.currentThread().getThreadGroup().uncaughtException(thread, throwable);
            }
        });

        control = createEditableControl();
    }
 
    @After
    public void cleanup() {
        if (stageLoader != null) stageLoader.dispose();
        Thread.currentThread().setUncaughtExceptionHandler(null);
    }


}
