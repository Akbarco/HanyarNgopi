package com.pos.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import java.util.Optional;

public class AlertUtil {

    public static void showError(String title, String message) {
        Alert alert = buildAlert(Alert.AlertType.ERROR, title, message);
        alert.showAndWait();
    }

    public static boolean showConfirm(String title, String message) {
        Alert alert = buildAlert(Alert.AlertType.CONFIRMATION, title, message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static Alert buildAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        return alert;
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
            var node = dp.lookupButton(bt);
            if (!(node instanceof ButtonBase btn)) {
                return;
            }
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
                btn.setText("Batal");
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
