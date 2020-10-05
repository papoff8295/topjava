package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        if (meals == null || meals.size() == 0) throw new IllegalArgumentException();
        List<UserMealWithExcess> list = new ArrayList<>();
        //create map for calories in day
        Map<LocalDate, Integer> map = getCaloriesInDay(meals);
        //create list from args
        for (UserMeal um : meals) {
            LocalTime localTime = um.getDateTime().toLocalTime();
            if (TimeUtil.isBetweenHalfOpen(localTime,startTime, endTime)) {
                boolean excess = false;
                int caloriesInDay = map.get(um.getDateTime().toLocalDate());
                //check excess
                if (caloriesInDay > caloriesPerDay) excess = true;
                list.add(new UserMealWithExcess(um.getDateTime(), um.getDescription(), um.getCalories(), excess));
            }
        }
        return list;
    }
//Optional (Java 8 Stream API)
    private static Map<LocalDate, Integer> getCaloriesInDay(List<UserMeal> meals) {
        return meals.stream().collect(Collectors.toMap(
                userMeal -> userMeal.getDateTime().toLocalDate(),
                UserMeal::getCalories,
                Integer::sum
        ));
    }
//Optional 2 (+5 бонусов, только после выполнения базового и Optional задания!)
    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        if (meals == null || meals.size() == 0) throw new IllegalArgumentException();
        List<UserMealWithExcess> list = new ArrayList<>(); //create list for Excess
        Map<LocalDate, Integer> map = new HashMap<>(); //create map for sum calories in day
            meals.forEach(userMeal -> {
                LocalTime localTime = userMeal.getDateTime().toLocalTime();
                LocalDate day = userMeal.getDateTime().toLocalDate();
                int count = map.getOrDefault(day, 0);
                map.put(day, userMeal.getCalories() + count);
                boolean excess = false;
                //if the sum of calories per day exceeds
                if (map.get(day) > caloriesPerDay) {
                    excess = true;
                    for (UserMealWithExcess u : list) {
                        if (u.getDateTime().toLocalDate().equals(day) && !u.isExcess())
                        u.setExcess(true); // change all values for the day to true
                    }
                }
                if (TimeUtil.isBetweenHalfOpen(localTime, startTime, endTime)) {
                    list.add(new UserMealWithExcess(userMeal.getDateTime(), userMeal.getDescription(), userMeal.getCalories(), excess));
                }
            });
        return list;
    }

    /*public static List<UserMealWithExcess> filteredByStreamsOptional2
            (List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        //meals.stream().collect(Collectors.groupingBy(UserMeal::getDateTime)).entrySet().add()
        Map<LocalDate, Boolean> map = new HashMap<>();
        meals.stream().collect(Collectors.toMap(
                userMeal -> userMeal.getDateTime().toLocalDate(),
                UserMeal::getCalories,
                Integer::sum
        )).forEach((localDate, integer) -> {
            boolean excess = false;
            if (integer > caloriesPerDay) excess = true;
                map.put(localDate, excess);

        }); ;
    }*/

    public static <T> Collector<T, ?, Map<Boolean, List<T>>> newCollector(LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        return Collector.<UserMeal, Map.Entry<Map<LocalDate, Integer>, Map<Boolean, List<UserMealWithExcess>>>, Map<Boolean, List<T>>>of(
                () -> new AbstractMap.SimpleImmutableEntry<> (
                        new HashMap<>(), new HashMap<>()
                ),
                (c, e) -> {
                    LocalDate day = e.getDateTime().toLocalDate();
                    int count = c.getKey().getOrDefault(day, 0);
                    c.getKey().put(day, e.getCalories() + count);
                    if (c.getValue().size() == 0) {
                        c.getValue().put(Boolean.TRUE, new ArrayList<>());
                        c.getValue().put(Boolean.FALSE, new ArrayList<>());
                    }
                    LocalTime localTime = e.getDateTime().toLocalTime();
                    if (c.getKey().get(day) > caloriesPerDay && TimeUtil.isBetweenHalfOpen(localTime, startTime, endTime)) {
                        c.getValue().get(Boolean.TRUE).add(new UserMealWithExcess(e.getDateTime(),
                                e.getDescription(),e.getCalories(), true));

                        //тут продолжить с проверкой ключа false на содержание даты из е,
                        // удалить и вставить в ключь true
                        for (int i = 0; i < c.getValue().get(Boolean.FALSE).size();) { // проходим по части списка
                            if (day.equals(c.getValue().get(Boolean.FALSE).get(i).getDateTime().toLocalDate())) { // если встречается дата с превышением
                                UserMealWithExcess userMealFromFalse =  c.getValue().get(Boolean.FALSE).get(i); //получаем объект
                                c.getValue().get(Boolean.FALSE).remove(i);//удаляем объект из списка с ключом false
                                c.getValue().get(Boolean.TRUE).add(userMealFromFalse); //кладем его в список с ключем true
                            } else i++;
                        }
                    } else if (TimeUtil.isBetweenHalfOpen(localTime, startTime, endTime))
                        c.getValue().get(Boolean.FALSE).add(new UserMealWithExcess(e.getDateTime(),
                            e.getDescription(), e.getCalories(), false));
                },
                (c1, c2) -> c1,
                c -> {
                    Map<Boolean, List<T>> result = new HashMap<>(2);
                    if ()
                    result.put(Boolean.FALSE, c.getKey());
                    result.put(Boolean.TRUE, new ArrayList<>(c.getValue()));
                    return result;
                });
    }
}
