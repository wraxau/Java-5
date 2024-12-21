package com.elevator.system;

import com.elevator.model.Elevator;
import com.elevator.model.Request;
import com.elevator.uni.ElevatorUI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ElevatorSystem {
    private final List<Elevator> elevators;  // Список всех лифтов
    private ElevatorUI elevatorUI;            // Интерфейс для обновления UI
    private final int numElevators;           // Количество лифтов в системе
    private final ExecutorService executor;   // Для многозадачности

    public ElevatorSystem(int numElevators) {
        this.numElevators = numElevators;
        this.elevators = new ArrayList<>();
        this.executor = Executors.newFixedThreadPool(numElevators);  // Создаем пул потоков
    }

    // Инициализация лифтов
    public void initializeElevators() {
        for (int i = 0; i < numElevators; i++) {
            elevators.add(new Elevator(i + 1, this));  // Нумерация лифтов с 1
        }
    }

    // Установка интерфейса UI
    public void setElevatorUI(ElevatorUI elevatorUI) {
        this.elevatorUI = elevatorUI;
    }

    // Генерация нового запроса
    public void generateRequest(Request request) {
        Elevator bestElevator = getBestElevator(request);

        if (bestElevator != null) {
            bestElevator.addRequest(request);  // Добавляем запрос в лучший лифт

            // Логируем назначение лифта для запроса
            elevatorUI.updateLog("Добавлен новый запрос: с этажа " + request.getStartFloor() + " на этаж " + request.getEndFloor());
            elevatorUI.updateLog("Запрос назначен лифту " + bestElevator.getId());
        } else {
            // Если нет доступных лифтов
            elevatorUI.updateLog("Ошибка: нет доступных лифтов для обработки запроса.");
        }
    }

    // Метод для выбора лучшего лифта
    private Elevator getBestElevator(Request request) {
        Elevator bestElevator = null;
        int minDistance = Integer.MAX_VALUE;

        // Выбираем лифт, который наименьше всего удален от этажа запроса
        for (Elevator elevator : elevators) {
            // Если лифт свободен или выполняет запрос
            if (elevator.isIdle() || elevator.isRequestOnTheWay(request)) {
                int distance = Math.abs(elevator.getCurrentFloor() - request.getStartFloor());
                if (distance < minDistance) {
                    minDistance = distance;
                    bestElevator = elevator;
                }
            }
        }

        // Если все лифты заняты, выбираем лифт с минимальным количеством запросов
        if (bestElevator == null) {
            for (Elevator elevator : elevators) {
                if (bestElevator == null || elevator.getPendingRequestsCount() < bestElevator.getPendingRequestsCount()) {
                    bestElevator = elevator;
                }
            }
        }

        return bestElevator;
    }

    // Запуск лифтов в многозадачном режиме
    public void startElevators() {
        for (Elevator elevator : elevators) {
            executor.submit(elevator);  // Отправляем лифт на выполнение в отдельном потоке
        }
    }

    // Получение списка всех лифтов
    public List<Elevator> getElevators() {
        return elevators;
    }

    // Обновление UI
    public void updateLog(String message) {
        if (elevatorUI != null) {
            elevatorUI.updateLog(message);
        }
    }
}
