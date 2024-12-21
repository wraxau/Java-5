package com.elevator.uni;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.elevator.system.ElevatorSystem;
import com.elevator.model.Request;


public class ElevatorUI {
    private final ElevatorSystem system;
    private TextArea log;  // Используем TextArea для многострочного вывода лога
    private TextField startFloorField;
    private TextField endFloorField;
    private Button requestButton;

    public ElevatorUI(ElevatorSystem system) {
        this.system = system;
    }

    public void updateLog(String message) {
        // Обновляем лог в UI потоке
        Platform.runLater(() -> {
            log.appendText(message + "\n");  // Добавляем новое сообщение в лог
            log.setScrollTop(Double.MAX_VALUE);  // Прокручиваем лог вниз
        });
    }

    public void start(Stage stage) {
        VBox root = new VBox();
        root.setSpacing(10);

        // Инициализация компонентов UI
        log = new TextArea();
        log.setEditable(false);  // Лог не должен быть редактируемым
        log.setPromptText("Лог работы системы");

        startFloorField = new TextField();
        startFloorField.setPromptText("Введите начальный этаж");

        endFloorField = new TextField();
        endFloorField.setPromptText("Введите конечный этаж");

        requestButton = new Button("Вызвать лифт");
        requestButton.setOnAction(e -> handleRequestButtonClick());

        root.getChildren().addAll(log, startFloorField, endFloorField, requestButton);

        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Управление лифтами");
        stage.show();

        system.setElevatorUI(this);
        system.startElevators();  // Запускаем работу лифтов
    }

    private void handleRequestButtonClick() {
        try {
            int startFloor = Integer.parseInt(startFloorField.getText());
            int endFloor = Integer.parseInt(endFloorField.getText());

            if (startFloor <= 0 || endFloor <= 0 || startFloor == endFloor) {
                throw new IllegalArgumentException("Этажи должны быть больше 0 и не совпадать.");
            }

            // Генерируем запрос
            Request request = new Request(startFloor, endFloor);
            system.generateRequest(request);

        } catch (NumberFormatException ex) {
            showError("Ошибка: пожалуйста, введите корректные этажи.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        // Отображаем сообщение об ошибке в диалоговом окне
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
