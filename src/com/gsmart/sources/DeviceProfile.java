// Localização: src/main/java/com/gsmart/sources/DeviceProfile.java
package com.gsmart.sources;

/**
 * Representa um Perfil de Dispositivo (Device Profile) do ThingsBoard.
 * Contém o nome (para exibição na interface) e o ID (para uso nas chamadas de API).
 */
public record DeviceProfile(String name, String id) {
    /**
     * Garante que o JComboBox exiba apenas o nome do perfil.
     */
    @Override
    public String toString() {
        return name;
    }
}