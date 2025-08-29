// Localização: src/com/gsmart/model/User.java
package main.java.com.gsmart.model;

/**
 * Representa um único utilizador do sistema GSmart.
 *
 * É um 'record' imutável, usado para transportar dados do utilizador entre a
 * base de dados e a interface gráfica.
 *
 * @param id O identificador único do utilizador na base de dados.
 * @param username O nome de login do utilizador.
 * @param role O perfil de acesso do utilizador (ex: "ADMINISTRATOR").
 */
public record User(int id, String username, String role) {
}