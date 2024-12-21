package com.elevator.model;

import com.elevator.system.ElevatorSystem;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

public class Elevator implements Runnable {
    private final int id;
    private int currentFloor = 1;  // Начинаем с 1 этажа
    private final ElevatorSystem system;
    private boolean busy = false;
    private final List<Request> requestQueue = new ArrayList<>();  // Список запросов
    private boolean movingUp = true;  // Направление движения лифта

    public Elevator(int id, ElevatorSystem system) {
        this.id = id;
        this.system = system;
    }

    public int getId() {
        return id;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    // Проверка, свободен ли лифт
    public boolean isIdle() {
        return requestQueue.isEmpty() && !busy;
    }

    // Проверка, движется ли лифт по запросу
    public boolean isRequestOnTheWay(Request request) {
        return requestQueue.stream().anyMatch(r -> r.getStartFloor() == request.getStartFloor() && r.getEndFloor() == request.getEndFloor());
    }

    // Добавление запроса в очередь и сортировка
    public synchronized void addRequest(Request request) {
        if (!isRequestOnTheWay(request)) {
            requestQueue.add(request);  // Добавляем запрос в список
            // Сортируем запросы по разнице между текущим этажом лифта и этажом запроса
            requestQueue.sort((r1, r2) -> Math.abs(r1.getStartFloor() - currentFloor) - Math.abs(r2.getStartFloor() - currentFloor));
        } else {
            // Лифт уже двигается по этому запросу
            system.updateLog("Лифт " + id + " уже движется по запросу с этажа " + request.getStartFloor() + " на этаж " + request.getEndFloor());
        }
    }

    // Получаем количество оставшихся запросов в очереди
    public int getPendingRequestsCount() {
        return requestQueue.size();
    }

    // Двигаем лифт вверх
    private void moveUp() {
        currentFloor++;
        updatePosition();
    }

    // Двигаем лифт вниз
    private void moveDown() {
        currentFloor--;
        updatePosition();
    }

    // Обновляем информацию о текущем положении лифта
    private void updatePosition() {
        Platform.runLater(() -> {
            system.updateLog("Лифт " + id + " находится на этаже " + currentFloor);
        });
    }

    // Метод для остановки на этаже
    private void stopAtFloor(int floor) {
        Platform.runLater(() -> {
            system.updateLog("Лифт " + id + " остановился на этаже " + floor);
        });
        busy = false;
    }

    // Обработка запроса
    public void processRequests() {
        while (!requestQueue.isEmpty()) {
            Request currentRequest = requestQueue.get(0);  // Получаем первый запрос из списка

            // Двигаем лифт к началу запроса
            while (currentFloor != currentRequest.getStartFloor()) {
                if (currentFloor < currentRequest.getStartFloor()) {
                    moveUp();
                } else if (currentFloor > currentRequest.getStartFloor()) {
                    moveDown();
                }

                try {
                    Thread.sleep(1000); // Задержка в 1 секунду, чтобы видеть движение лифта
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Лифт достиг начального этажа
            stopAtFloor(currentRequest.getStartFloor());
            system.updateLog("Лифт " + id + " забрал пассажира с этажа " + currentFloor);

            // Теперь двигаем лифт к конечному этажу
            while (currentFloor != currentRequest.getEndFloor()) {
                if (currentFloor < currentRequest.getEndFloor()) {
                    moveUp();
                } else if (currentFloor > currentRequest.getEndFloor()) {
                    moveDown();
                }

                try {
                    Thread.sleep(1000); // Задержка в 1 секунду, чтобы видеть движение лифта
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Лифт достиг конечного этажа
            stopAtFloor(currentRequest.getEndFloor());
            system.updateLog("Лифт " + id + " высадил пассажира на этаже " + currentFloor);

            // Удаляем выполненный запрос
            requestQueue.remove(0);  // Убираем первый запрос из списка
        }

        // После завершения всех запросов
        system.updateLog("Лифт " + id + " завершил выполнение всех запросов.");
        updateAllElevatorsPosition();
    }

    // Метод для обновления местоположения всех лифтов в системе
    private void updateAllElevatorsPosition() {
        StringBuilder log = new StringBuilder("Текущее положение всех лифтов: \n");
        for (Elevator elevator : system.getElevators()) {
            log.append("Лифт ").append(elevator.getId()).append(" на этаже ").append(elevator.getCurrentFloor()).append("\n");
        }

        // Выводим информацию о местоположении всех лифтов
        Platform.runLater(() -> {
            system.updateLog(log.toString());
        });
    }

    @Override
    public void run() {
        while (true) {
            if (!requestQueue.isEmpty()) {
                processRequests();
            } else {
                try {
                    Thread.sleep(100); // Простой лифта
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
