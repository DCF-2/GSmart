// Localização: src/com/gsmart/Gui/windows/UserManagementWindow.java
package main.java.com.gsmart.Gui.windows;

import main.java.com.gsmart.Gui.UserTableModel;
import main.java.com.gsmart.db.DatabaseManager;
import main.java.com.gsmart.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Janela para a gestão de utilizadores do sistema GSmart.
 * <p>
 * Permite a administradores visualizar, adicionar, editar e remover utilizadores
 * através de uma interface tabular. Interage com a {@link UserDialog} para as
 * operações de criação e edição e com o {@link main.java.com.gsmart.db.DatabaseManager}
 * para persistir as alterações na base de dados.
 */
public class UserManagementWindow extends JDialog {

    private JTable userTable;
    private UserTableModel tableModel;

    public UserManagementWindow(Frame owner) {
        super(owner, "Gestão de Utilizadores", true); // O 'true' torna a janela modal
        setSize(500, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Tabela de Utilizadores (Centro) ---
        tableModel = new UserTableModel();
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Painel de Botões (Sul) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Adicionar");
        JButton editButton = new JButton("Editar");
        JButton removeButton = new JButton("Remover");
        JButton closeButton = new JButton("Fechar");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(Box.createHorizontalStrut(20)); // Espaçamento
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Ações dos Botões ---
        closeButton.addActionListener(e -> dispose());

        addButton.addActionListener(e -> {
            // Cria e exibe o diálogo para adicionar um novo utilizador
            UserDialog dialog = new UserDialog(this); // 'this' refere-se à UserManagementWindow
            dialog.setVisible(true);

            // Depois que o diálogo for fechado, verifica se um utilizador foi salvo
            if (dialog.isSaved()) {
                // Se sim, recarrega a lista de utilizadores para atualizar a tabela
                loadUsers();
            }
        });

        editButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();

            // 1. Verifica se um utilizador foi selecionado na tabela
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione um utilizador na tabela para editar.", "Nenhum Utilizador Selecionado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Obtém o objeto User da linha selecionada
            User userToEdit = tableModel.getUserAt(selectedRow);
            if (userToEdit == null) return; // Segurança extra

            // 3. Abre o diálogo de edição, passando o utilizador selecionado
            UserDialog dialog = new UserDialog(this, userToEdit);
            dialog.setVisible(true);

            // 4. Se as alterações foram salvas, atualiza a tabela
            if (dialog.isSaved()) {
                loadUsers();
            }
        });

        removeButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();

            // 1. Verifica se um utilizador foi selecionado
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione um utilizador na tabela para remover.", "Nenhum Utilizador Selecionado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Pede confirmação antes de remover
            User userToRemove = tableModel.getUserAt(selectedRow);
            if (userToRemove == null) return; // Segurança extra

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Tem a certeza que deseja remover o utilizador '" + userToRemove.username() + "'?\nEsta ação é irreversível.",
                    "Confirmar Remoção",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            // 3. Se confirmado, chama o método para remover e atualiza a tabela
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = DatabaseManager.getInstance().removeUser(userToRemove.id());
                if (success) {
                    loadUsers(); // Atualiza a tabela para refletir a remoção
                } else {
                    // A mensagem de erro específica (ex: "não se pode remover o admin")
                    // já é mostrada pelo próprio método do DatabaseManager.
                    // Podemos adicionar um log aqui se quisermos.
                    System.out.println("A remoção do utilizador falhou (ver logs para detalhes).");
                }
            }
        });

        // Carrega os dados na tabela ao abrir a janela
        loadUsers();
    }

    /**
     * Busca os utilizadores da base de dados e atualiza a tabela.
     */
    private void loadUsers() {
        // Usa o método que criámos no DatabaseManager
        tableModel.setUsers(DatabaseManager.getInstance().getAllUsers());
    }
}