// Localização: src/main/java/com/gsmart/sources/Device.java
package com.gsmart.sources;

/**
 * Representa um dispositivo do ThingsBoard, contendo seu nome (para exibição)
 * e seu ID (para uso na API).
 * Usar um 'record' é uma forma moderna e concisa para classes que são simples
 * transportadoras de dados imutáveis.
 */
public record Device(String name, String id) {
    /**
     * Este método é chamado pelo JComboBox para exibir o item na lista.
     * Retornamos apenas o nome para uma visualização amigável.
     * @return O nome do dispositivo.
     */
    @Override
    public String toString() {
        return name;
    }
}