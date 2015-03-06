package org.hps.monitoring.application.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;

import org.hps.monitoring.subsys.StatusCode;
import org.hps.monitoring.subsys.SystemStatus;
import org.hps.monitoring.subsys.SystemStatusListener;

/**
 * A <code>JTableModel</code> that has a list of {@link org.hps.monitoring.subsys.SystemStatus} objects.
 */
public final class SystemStatusTableModel extends AbstractTableModel implements SystemStatusListener {

    public static final int RESET_COL = 0;
    public static final int ACTIVE_COL = 1;
    public static final int STATUS_COL = 2;
    public static final int SYSTEM_COL = 3;
    public static final int DESCRIPTION_COL = 4;
    public static final int MESSAGE_COL = 5;
    public static final int LAST_CHANGED_COL = 6;
    public static final int CLEARABLE_COL = 7;

    static final String[] columnNames = { "Reset", "Active", "Status", "System", "Description", "Message", "Last Changed", "Clearable" };

    List<SystemStatus> statuses = new ArrayList<SystemStatus>();
    final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM-dd-yyyy HH:mm:ss.SSS");

    public void addSystemStatus(SystemStatus status) {
        statuses.add(status);
        status.addListener(this);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return statuses.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        SystemStatus status = statuses.get(rowIndex);
        switch (columnIndex) {
        case ACTIVE_COL:
            return status.isActive();
        case STATUS_COL:
            return status.getStatusCode().name();
        case SYSTEM_COL:
            return status.getSubsystem().name();
        case DESCRIPTION_COL:
            return status.getDescription();
        case MESSAGE_COL:
            return status.getMessage();
        case LAST_CHANGED_COL:
            return new Date(status.getLastChangedMillis());
        case RESET_COL:
            // If the status is clearable, then it has a button that can be used to
            // manually set the state to CLEARED. If the status is not clearable,
            // then nothing is rendered in this cell.
            if (status.isClearable()) {
                final JButton button = new JButton();
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        SystemStatus status = statuses.get(rowIndex);
                        // Only clearable statuses can have this state set. Check for this
                        // just to be safe, even though no button is available for non-clearable
                        // statuses.
                        if (status.isClearable()) {
                            StatusCode oldStatusCode = status.getStatusCode();
                            status.setStatus(StatusCode.CLEARED, "Cleared from " + oldStatusCode.name() + " state.");
                        }
                    }
                });
                return button;
            } else {
                return null;
            }
        case CLEARABLE_COL:
            return status.isClearable();
        default:
            return null;
        }
    }

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
        case ACTIVE_COL:
            return Boolean.class;
        case LAST_CHANGED_COL:
            return Date.class;
        default:
            return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == ACTIVE_COL)
            return true;
        else
            return false;
    }

    @Override
    public void statusChanged(SystemStatus status) {
        int rowNumber = statuses.indexOf(status);
        fireTableRowsUpdated(rowNumber, rowNumber);
    }

    public void clear() {
        statuses.clear();
        fireTableDataChanged();
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == ACTIVE_COL) {
            statuses.get(row).setActive((Boolean) value);
        }
    }
}
