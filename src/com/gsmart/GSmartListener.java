// Localização: src/main/java/com/gsmart/GSmartListener.java
package com.gsmart;

public interface GSmartListener {
    void onInsight(String message, String type);
    void onAlert(String title, String message);
    void onStatusUpdate(TaskStatus status);
    void onConnectionLost(String errorMessage);
    void onReconnectionAttempt(long delayInSeconds);
    void onConnectionRestored();
}