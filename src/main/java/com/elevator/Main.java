package com.elevator;

import com.elevator.system.ElevatorSystem;
import com.elevator.uni.ElevatorUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        ElevatorSystem system = new ElevatorSystem(3); // 3 лифта
        system.initializeElevators();  // Инициализация лифтов
        ElevatorUI ui = new ElevatorUI(system);
        ui.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
