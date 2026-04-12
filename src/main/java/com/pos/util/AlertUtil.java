package com.pos.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import java.util.Optional;

public class AlertUtil {

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void styleAlert(Alert alert) {
        DialogPane dp = alert.getDialogPane();

        dp.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 8;"
        );

        dp.setGraphic(null);

        dp.getButtonTypes().forEach(bt -> {
            var btn = dp.lookupButton(bt);
            if (bt == ButtonType.OK) {
                btn.setStyle(
                        "-fx-background-color: #111827;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-pref-height: 36;" +
                                "-fx-pref-width: 90;" +
                                "-fx-font-weight: bold;"
                );
            } else if (bt == ButtonType.CANCEL) {
                btn.setStyle(
                        "-fx-background-color: #F3F4F6;" +
                                "-fx-text-fill: #374151;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-pref-height: 36;" +
                                "-fx-pref-width: 90;"
                );
            }
        });
    }
}