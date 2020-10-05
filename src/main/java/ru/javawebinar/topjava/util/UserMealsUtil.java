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
        Map<LocalDate, Integer> caloriesSumInDay = meals.stream().collect
                (Collectors.groupingBy(um -> um.getDateTime().toLocalDate(), Collectors.summingInt(UserMeal::getCalories)));
        return meals.stream().filter(um -> TimeUtil.isBetweenHalfOpen(um.getDateTime().toLocalTime(), startTime, endTime))
                .map(um -> new UserMealWithExcess(um.getDateTime() ,
                        um.getDescription(), um.getCalories(), caloriesSumInDay.get(um.getDateTime().toLocalDate()) > caloriesPerDay))
        .collect(Collectors.toList());
    }
}
