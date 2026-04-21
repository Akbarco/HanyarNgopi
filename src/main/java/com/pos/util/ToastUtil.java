package com.pos.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.Map;
import java.util.WeakHashMap;

public final class ToastUtil {

    private static final Duration TOAST_DURATION = Duration.seconds(2.4);
    private static final Duration ANIMATION_DURATION = Duration.millis(180);
    private static final double OFFSET_X = 28;
    private static final double OFFSET_Y = 28;
    private static final Map<Window, ToastHandle> ACTIVE_TOASTS = new WeakHashMap<>();

    private ToastUtil() {
    }

    public static void showSuccess(Node anchor, String message) {
        if (anchor == null || anchor.getScene() == null) {
            return;
        }
        showSuccess(anchor.getScene().getWindow(), message);
    }

    public static void showSuccess(Window contextWindow, String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showSuccess(contextWindow, message));
            return;
        }

        Window targetWindow = resolveTargetWindow(contextWindow);
        if (targetWindow == null || !targetWindow.isShowing()) {
            return;
        }

        ToastHandle existing = ACTIVE_TOASTS.remove(targetWindow);
        if (existing != null) {
            existing.hideImmediately();
        }

        ToastHandle handle = new ToastHandle(targetWindow, buildToast(message));
        ACTIVE_TOASTS.put(targetWindow, handle);
        handle.show();
    }

    private static Window resolveTargetWindow(Window contextWindow) {
        if (contextWindow instanceof Stage stage && stage.getOwner() != null) {
            return stage.getOwner();
        }
        return contextWindow;
    }

    private static HBox buildToast(String message) {
        StackPane iconWrap = new StackPane();
        iconWrap.setMinSize(34, 34);
        iconWrap.setPrefSize(34, 34);
        iconWrap.setMaxSize(34, 34);
        iconWrap.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #5B4BFF, #16A34A);" +
                        "-fx-background-radius: 17;"
        );

        Label icon = new Label("✓");
        icon.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 15;" +
                        "-fx-font-weight: bold;"
        );
        iconWrap.getChildren().add(icon);

        Label text = new Label(message);
        text.setWrapText(true);
        text.setMaxWidth(300);
        text.setStyle(
                "-fx-text-fill: #0F172A;" +
                        "-fx-font-size: 13;" +
                        "-fx-font-weight: bold;"
        );

        HBox toast = new HBox(12, iconWrap, text);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setOpacity(0);
        toast.setTranslateY(12);
        toast.setStyle(
                "-fx-background-color: rgba(255,255,255,0.98);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #C7D2FE;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 14 18 14 14;"
        );
        toast.setEffect(new DropShadow(20, Color.rgb(15, 23, 42, 0.18)));
        return toast;
    }

    private static final class ToastHandle {
        private final Window window;
        private final Popup popup;
        private final HBox content;
        private final PauseTransition delay;
        private final ChangeListener<Number> repositionListener;

        private ToastHandle(Window window, HBox content) {
            this.window = window;
            this.content = content;
            this.popup = new Popup();
            this.delay = new PauseTransition(TOAST_DURATION);
            this.repositionListener = (obs, oldValue, newValue) -> positionPopup();

            popup.setAutoFix(true);
            popup.setAutoHide(false);
            popup.setHideOnEscape(false);
            popup.getContent().add(content);
        }

        private void show() {
            popup.show(window);
            attachWindowListeners();
            positionPopup();
            playShowAnimation();
            delay.setOnFinished(event -> playHideAnimation());
            delay.playFromStart();
        }

        private void playShowAnimation() {
            FadeTransition fade = new FadeTransition(ANIMATION_DURATION, content);
            fade.setFromValue(0);
            fade.setToValue(1);

            TranslateTransition slide = new TranslateTransition(ANIMATION_DURATION, content);
            slide.setFromY(12);
            slide.setToY(0);

            new ParallelTransition(fade, slide).play();
        }

        private void playHideAnimation() {
            FadeTransition fade = new FadeTransition(ANIMATION_DURATION, content);
            fade.setFromValue(content.getOpacity());
            fade.setToValue(0);

            TranslateTransition slide = new TranslateTransition(ANIMATION_DURATION, content);
            slide.setFromY(content.getTranslateY());
            slide.setToY(8);

            ParallelTransition exit = new ParallelTransition(fade, slide);
            exit.setOnFinished(event -> hideImmediately());
            exit.play();
        }

        private void hideImmediately() {
            delay.stop();
            detachWindowListeners();
            popup.hide();
            if (ACTIVE_TOASTS.get(window) == this) {
                ACTIVE_TOASTS.remove(window);
            }
        }

        private void attachWindowListeners() {
            window.xProperty().addListener(repositionListener);
            window.yProperty().addListener(repositionListener);
            window.widthProperty().addListener(repositionListener);
            window.heightProperty().addListener(repositionListener);
        }

        private void detachWindowListeners() {
            window.xProperty().removeListener(repositionListener);
            window.yProperty().removeListener(repositionListener);
            window.widthProperty().removeListener(repositionListener);
            window.heightProperty().removeListener(repositionListener);
        }

        private void positionPopup() {
            content.applyCss();
            content.autosize();

            double width = content.prefWidth(-1);
            double height = content.prefHeight(-1);

            popup.setX(window.getX() + window.getWidth() - width - OFFSET_X);
            popup.setY(window.getY() + window.getHeight() - height - OFFSET_Y);
        }
    }
}
