/*
 * Created on 29.09.2017
 *
 */
package test.com.sun.javafx.scene.control.celledit.old.editablecontrol;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.*;
import static test.com.sun.javafx.scene.control.infrastructure.VirtualFlowTestUtils.*;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.skin.TreeTableRowSkin;
import javafx.util.Callback;
import test.com.sun.javafx.scene.control.celledit.infrastructure.EditEventReport;
import test.com.sun.javafx.scene.control.celledit.infrastructure.EditableControl;
import test.com.sun.javafx.scene.control.celledit.infrastructure.EditableControlFactory;
import test.com.sun.javafx.scene.control.celledit.infrastructure.TableViewEditReport;
import test.com.sun.javafx.scene.control.infrastructure.StageLoader;

/**
 * core tableView/cell test
 *
 * moved cellSelection and extractor testing into this, not applicable in abstract layer.
 *
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(Parameterized.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class STableCellTest extends AbstractSCellTest<TableView, TableCell> {

    protected boolean cellSelectionEnabled;

    @Parameters(name = "{index} - cell {0}")
    public static Collection selectionModes() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    /**
     *
     */
    public STableCellTest(boolean cellSelection) {
        this.cellSelectionEnabled = cellSelection;
    }

    @Test
    public void testTableEditCommitCellSelection() {
        EditableControlFactory.ETableView control = (EditableControlFactory.ETableView) createEditableControl(true);
        TableColumn<TableColumn, String> column = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        assertEquals(cellSelectionEnabled, control.getSelectionModel().isCellSelectionEnabled());
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        // start edit on control
        control.edit(editIndex, column);
        EditEventReport report = createEditReport(control);
        String editedValue = "edited";
        cell.commitEdit(editedValue);
        assertEquals("tableCell must fire a single event", 1, report.getEditEventSize());
    }

    /**
     * Focus on event count: fires incorrect cancel if items has extractor
     * on edited column.
     */
    @Test
    public void testTableEditCommitOnCellEventCount() {
        EditableControlFactory.ETableView control = (EditableControlFactory.ETableView) createEditableControl(true);
        TableColumn<TableColumn, String> column = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        // start edit on control
        control.edit(editIndex, column);;
        EditEventReport report = createEditReport(control);
        String editedValue = "edited";
        cell.commitEdit(editedValue);
        assertEquals("tableCell must fire a single event", 1, report.getEditEventSize());
    }

    /**
     * Test about treeTableRowSkin: registers
     * a listener on the treeTableView treeColumn in constructor
     * - throws if not yet bound to a treeTable
     *
     * reported:
     * https://bugs.openjdk.java.net/browse/JDK-8151524
     */
    @Test
    public void testTreeTableRowSkinInit() {
        TreeTableRow row = new TreeTableRow();
        row.setSkin(new TreeTableRowSkin(row));
    }


    @Override
    protected void assertValueAt(int index, Object editedValue,
            EditableControl<TableView, TableCell> control) {
        EditableControlFactory.ETableView table = (EditableControlFactory.ETableView) control;
        TableColumn column = table.getTargetColumn();
        assertEquals("editedValue must be committed", editedValue,
                column.getCellObservableValue(index).getValue());
    }

    @Override
    protected void assertLastStartIndex(EditEventReport report, int index, Object first) {
        Optional<CellEditEvent> e = report.getLastEditStart();
        assertTrue(e.isPresent());
//        LOG.info("what do we get?" + report.getEditText(e.get()));
        assertNotNull("position on event must not be null", e.get().getTablePosition());
        assertEquals("index on start event", index, e.get().getTablePosition().getRow());
        assertEquals("column on start event", first, e.get().getTablePosition().getTableColumn());

    }

    @Override
    protected void assertLastCancelIndex(EditEventReport report, int index, Object first) {
        Optional<CellEditEvent> e = report.getLastEditCancel();
        assertTrue(e.isPresent());
//        LOG.info("what do we get?" + report.getEditText(e.get()));
        assertNotNull("position on event must not be null", e.get().getTablePosition());
        assertEquals("index on cancel event", index, e.get().getTablePosition().getRow());
        assertEquals("column on cancel event", first, e.get().getTablePosition().getTableColumn());

    }

    @Override
    protected void assertLastCommitIndex(EditEventReport report, int index, Object first, Object value) {
        Optional<CellEditEvent> e = report.getLastEditCommit();
        assertTrue(e.isPresent());
//        LOG.info("what do we get?" + report.getEditText(e.get()));
        assertNotNull("position on event must not be null", e.get().getTablePosition());
        assertEquals("index on commit event", index, e.get().getTablePosition().getRow());
        assertEquals("column on commit event", first, e.get().getTablePosition().getTableColumn());
        assertEquals("new value on commit event", value, e.get().getNewValue());
    }


    @Override
    protected IndexedCell getCellAt(
            EditableControl<TableView, TableCell> control, int editIndex) {
        return getCell(control.getControl(), editIndex, 0);
    }



    /**
     * Creates and returns an editable Table of TableColumns (as items ;)
     * configured with 3 items
     * and TextFieldTableCell as cellFactory on first column (which represents
     * the textProperty of a TableColumn, no extractor, cellSelectionEnabled
     * as defined by parameter.
     *
     * @return
     */
    @Override
    protected EditableControl<TableView, TableCell> createEditableControl() {
        return createEditableControl(false);
    }

    /**
      * Creates and returns an editable Table of TableColumns (as items ;)
     * configured with 3 items
     * and TextFieldTableCell as cellFactory on first column (which represents
     * the textProperty of a TableColumn, extractor as requested cellSelectionEnabled
     * as defined by parameter.
     *
     *
     * @param withExtractor
     * @return
     */
    protected EditableControl<TableView, TableCell> createEditableControl(
            boolean withExtractor) {

        ObservableList<TableColumn> items = withExtractor
                ? FXCollections.observableArrayList(
                        e -> new Observable[] { e.textProperty() })
                : FXCollections.observableArrayList();
        items.addAll(new TableColumn("first"), new TableColumn("second"),
                new TableColumn("third"));
        EditableControlFactory.ETableView table = new EditableControlFactory.ETableView(items);
        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(cellSelectionEnabled);

        TableColumn<TableColumn, String> first = new TableColumn<>("Text");
        first.setCellFactory(createTextFieldCellFactory());
        first.setCellValueFactory(new PropertyValueFactory<>("text"));

        table.getColumns().addAll(first);
        return table;
    }

    @Override
    protected Callback createTextFieldCellFactory() {
        return e -> new TextFieldTableCell();
//                (Callback<ListView, ListCell>)TextFieldListCell.forListView();
    }

    @Override
    protected EditEventReport createEditReport(EditableControl control) {
        return new TableViewEditReport(control);
    }

}
