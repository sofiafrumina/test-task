package org.unisafe.pj;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class UniSafeTestAssignment {

    public static final double kMulX = 7.0843;
    public static final double kMulY = 7.0855;

    /*
     Это тестовое задание на собеседование в UniSafe llc
     На должность Junior Java Developer
     Чтобы задание считалось выполненным необходимо выполнить все 4 пункта
     Нам интересен твой подход к решению и условный % выполнения задания
     Вопросы можно задать тут galaev@team.usafe.ru

     В этом списке (listOfFigures) фигуры записанные координатами
     фигура может быть из 2 или 6 координат, это прямые и кривые, соответственно
     кривые фигуры записываются 5 координатами, потому что последняя 6ая - это первая координата следующей фигуры
     Все фигуры замкнуты
     List< ... > - список больших фигур
     List<List< ... > - список элементов одной фигуры
     List<List<List<Integer>>> - координаты элемента фигуры

     фигуры отправляются на плоттер и вырезаются на защитной пленке
     координаты для реза идут в том порядке, что и в списке
     соответственно есть начало реза по координатам и направление этого реза
     плоттер не всегда дорезает фигуры до конца, поэтому нужно всегда повторять первый элемент фигуры последним
     todo 1: напиши функцию чтобы добавлять первый элемент фигуры последним к каждой фигуре
     при вырезе мелкие фигуры могут задевать большие, поэтому порядок реза важен
     todo 2: напиши функцию чтобы изменить порядок реза фигур от самой маленькой к самой большой
     чтобы повысить качество реза нужно проделать несколько шагов
     чтобы нож не создавал брак пока разворачивает лезвие на большой градус,
     нужно чтобы все фигуры вырезались по часовой (изначально они случайны)
     todo 3: напиши функцию, которая разворачивает все фигуры по часовой меняя координаты местами
     у плоттеров есть особенность, нож которым вырезаются фигуры имеет направление
     todo 4: напиши функцию, которая меняет начало реза этой фигуры в направлении окончания реза предыдущей
     Например круг состоит из 5 кривых, это 4 четверти круга + 1 первая которую мы добавили в конце (чтобы круг хорошо прорезался).
     Если начало реза прошлого круга было справа относительно центра круга, то рез закончится через 5 четвертей, т.е. снизу.
     Таким образом последний элемент фигуры это кривая справа-вниз по часовой.
     После нее лезвие ножа направлено на лево.
     Соответственно, следующая фигура после этого круга должна начинаться в направлении лево.
    */

    public static void main(String[] args) {

        // todo замени путь к файлу
        String filePath = "D:\\Downloads\\unisafeTest\\camera_block.eps";

        List<List<List<Integer>>> listOfFigures = getFromEps(filePath);
        ShowList(listOfFigures);

    }

    public static void ShowList (List<List<List<Integer>>> listOfFigures) {
        for (List<List<Integer>> listOfFigure : listOfFigures) {
            System.out.println(listOfFigure);
        }
    }

    public static List<List<List<Integer>>> getFromEps(String filePath){
        List<List<List<Integer>>> listOfFigures = new ArrayList<>();

        List<String> blocks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean reachedEndData = false;
            boolean reachedBeginData = false;
            boolean blockStarted = false;

            while ((line = reader.readLine()) != null) {
                if (!reachedBeginData) {
                    if (line.trim().startsWith("%%EndPageSetup")) {
                        reachedBeginData = true;
                    }
                } else if (!reachedEndData) {

                    if (line.startsWith("%ADO")) {
                        reachedEndData = true;
                    } else {
                        if(line.contains("mo") && Character.isDigit(line.charAt(0))){
                            listOfFigures.add(new ArrayList<>());
                            blockStarted = true;
                            blocks.add(line);
                        } else if (line.contains("m") && Character.isDigit(line.charAt(0))) {
                            listOfFigures.add(new ArrayList<>());
                            blockStarted = true;
                            blocks.add(line);
                        } else if (line.trim().equals("cp") || line.trim().equals("@c") || line.trim().equals("@")) {
                            blockStarted = false;
                        } else if (blockStarted) {
                            blocks.add(line);
                        }
                    }
                } else {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading EPS file: " + e.getMessage());
            return new ArrayList<>();
        }

        int current_figure = -1;
        for (String block : blocks) {
            String[] line_parts = block.split(" ");

            if (Objects.equals(line_parts[line_parts.length - 1], "mo") || Objects.equals(line_parts[line_parts.length - 1], "m")) {
                List<Integer> listN = new ArrayList<>();
                current_figure++;
                getNumericalWithDot(current_figure, listOfFigures, line_parts, listN);
            } else if (Objects.equals(line_parts[line_parts.length - 1], "li")) {
                List<Integer> listN = new ArrayList<>();
                getNumericalWithDot(current_figure, listOfFigures, line_parts, listN);
            } else if (Objects.equals(line_parts[line_parts.length - 1], "cv") || Objects.equals(line_parts[line_parts.length - 1], "C")) {
                List<Integer> listN = new ArrayList<>();
                getNumericalWithDot(current_figure, listOfFigures, line_parts, listN);
            }
        }

        removeEmptyLists(listOfFigures);
        removeNotCycledFigures(listOfFigures);

        return listOfFigures;
    }

    public static void removeEmptyLists(List<List<List<Integer>>> listOfFigures) {
        listOfFigures.removeIf(List::isEmpty);
    }
    public static void removeNotCycledFigures(List<List<List<Integer>>> listOfFigures) {
        Iterator<List<List<Integer>>> iterator = listOfFigures.iterator();
        while (iterator.hasNext()) {
            List<List<Integer>> listOfFigure = iterator.next();
            int last_x = listOfFigure.get(listOfFigure.size() - 1).get(listOfFigure.get(listOfFigure.size() - 1).size() - 2);
            int last_y = listOfFigure.get(listOfFigure.size() - 1).get(listOfFigure.get(listOfFigure.size() - 1).size() - 1);
            int first_x = listOfFigure.get(0).get(0);
            int first_y = listOfFigure.get(0).get(1);
            if (first_x != last_x || first_y != last_y) {
                iterator.remove();
            }
        }
    }

    private static void getNumericalWithDot(int current_figure, List<List<List<Integer>>> listOfFigures, String[] line_parts, List<Integer> listN) {
        for (int j = 0; j < line_parts.length - 1; j++) {
            if (line_parts[j].startsWith(".")) {
                line_parts[j] = "0" + line_parts[j];
            }
            double calk;
            if (j % 2 != 0) {
                calk = (Double.parseDouble(line_parts[j]) + 1.5) * kMulX;
            } else {
                calk = (Double.parseDouble(line_parts[j]) + 1.5) * kMulY;
            }
            int this_int = (int) Math.round(calk);
            listN.add(this_int);
        }
        listOfFigures.get(current_figure).add(listN);
    }
    //todo 1: Замыкание фигуры
    public static void ensureFiguresAreClosed(List<List<List<Integer>>> listOfFigures) {
        for (List<List<Integer>> figure : listOfFigures) {
            if (!figure.isEmpty()) {
                // Добавляем первую точку (голову) в конец списка точек фигуры
                figure.add(new ArrayList<>(figure.get(0)));
            }
        }
    }

    //todo 2: Сортирует список фигур по размеру от меньшего к большему(Java Stream API).
    public static void sortFiguresBySize(List<List<List<Integer>>> listOfFigures) {
        List<List<List<Integer>>> sortedFigures = listOfFigures.stream()
                .sorted((figure1, figure2) -> Integer.compare(
                        calculateFigureSize(figure1),
                        calculateFigureSize(figure2)))
                .collect(Collectors.toList());

        listOfFigures.clear();
        listOfFigures.addAll(sortedFigures);
    }


    //доп. функция. Вычисляет общую длину всех отрезков в фигуре.
    private static int calculateFigureSize(List<List<Integer>> figure) {
        return figure.stream()
                .mapToInt(point -> calculateDistance(point, figure.get((figure.indexOf(point) + 1) % figure.size())))
                .sum();
    }


    //доп. функция. Вычисляет расстояние между двумя точками.

    private static int calculateDistance(List<Integer> point1, List<Integer> point2) {
        int xDiff = Math.abs(point1.get(0) - point2.get(0));
        int yDiff = Math.abs(point1.get(1) - point2.get(1));
        return xDiff + yDiff; // Простое приближение расстояния между точками
    }

    //todo 3: Проверка по часовой расположена фигура или против часовой, если последнее то переворачиваем


    /**
     *Разворачивает все фигуры по часовой стрелке, если они были перевернуты против часовой.
     *@param listOfFigures Список фигур.
     */
    public static void rotateFiguresIfAntiClockwise(List<List<List<Integer>>> listOfFigures) {
        for (List<List<Integer>> figure : listOfFigures) {
            if (!isClockwise(figure)) { // Проверяем, перевернута ли фигура против часовой стрелки
                Collections.reverse(figure); // Если да, разворачиваем фигуру
            }
        }
    }

    /**
     * Проверяет, перевернуты ли точки фигуры против часовой стрелки.
     *
     * @param figure Список точек фигуры.
     * @return true, если фигура перевернута против часовой стрелки, false — против.
     */
    private static boolean isClockwise(List<List<Integer>> figure) {
        double area = 0.0;
        for (int i = 0; i < figure.size(); i++) {
            List<Integer> point1 = figure.get(i);
            List<Integer> point2 = figure.get((i + 1) % figure.size()); // Используем модуль для замкнутости фигуры
            area += (double) point1.get(0) * point2.get(1) - point1.get(1) * point2.get(0);
        }
        return area > 0;
    }
//todo 4:

    //Изменяет начало реза текущей фигуры в направлении конца реза предыдущей.
    public static void adjustStartToPreviousEnd(List<List<List<Integer>>> listOfFigures) {
        if (listOfFigures.isEmpty()) {
            return;
        }

        // Определяем направление реза последней фигуры
        List<List<Integer>> lastFigure = listOfFigures.get(listOfFigures.size() - 1);
        boolean lastCutWasRightToLeft = isRightToLeft(lastFigure);

        // Находим индекс следующей фигуры, которую нужно скорректировать
        int nextIndex = listOfFigures.size() - 1;
        for (int i = listOfFigures.size() - 2; i >= 0; i--) {
            if (!lastCutWasRightToLeft) {
                break; // Если рез был слева направо, следующая фигура не нуждается в коррекции
            }
            nextIndex = i;
        }

        // Корректируем начальную точку реза следующей фигуры
        List<List<Integer>> nextFigure = listOfFigures.get(nextIndex);
        if (nextIndex != 0) { // Если следующая фигура не первая, корректируем ее начало
            nextFigure.set(0, listOfFigures.get(nextIndex - 1).get(listOfFigures.get(nextIndex - 1).size() - 1));
        } else { // Если следующая фигура первая, корректируем ее конец
            nextFigure.set(nextFigure.size() - 1, listOfFigures.get(nextIndex).get(0));
        }
    }


    /**
     * Определяет, был ли рез последней фигуры справа налево.
     * @return true, если рез был справа налево, false — слева направо.
     */
    private static boolean isRightToLeft(List<List<Integer>> figure) {
        if (figure.size() < 2) {
            throw new IllegalArgumentException("Фигура должна содержать хотя бы две точки");
        }

        List<Integer> lastPoint = figure.get(figure.size() - 1);
        List<Integer> secondLastPoint = figure.get(figure.size() - 2);

        // Определяем направление по оси X
        int dx = lastPoint.get(0) - secondLastPoint.get(0);
        // Определяем направление по оси Y
        int dy = lastPoint.get(1) - secondLastPoint.get(1);

        // Если последняя точка находится правее или ниже предпоследней, то направление реза справа налево
        return dx > 0 || dy > 0;
    }
}
