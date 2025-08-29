// Localização: src/com/gsmart/Gui/UserTableModel.java
package main.java.com.gsmart.Gui;

import main.java.com.gsmart.model.User;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de dados (TableModel) para a JTable que exibe os utilizadores do sistema.
 */
public class UserTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Nome de Utilizador", "Perfil de Acesso"};
    private List<User> users;

    public UserTableModel() {
        this.users = new ArrayList<>();
    }

    public void setUsers(List<User> users) {
        this.users = new ArrayList<>(users);
        fireTableDataChanged(); // Notifica a tabela que os dados mudaram
    }

    @Override
    public int getRowCount() { return users.size(); }

    @Override
    public int getColumnCount() { return columnNames.length; }

    @Override
    public String getColumnName(int column) { return columnNames[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        User user = users.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> user.id();
            case 1 -> user.username();
            case 2 -> user.role();
            default -> null;
        };
    }

    public User getUserAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < users.size()) {
            return users.get(rowIndex);
        }
        return null;
    }
}