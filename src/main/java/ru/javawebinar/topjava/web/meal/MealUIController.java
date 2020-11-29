package ru.javawebinar.topjava.web.meal;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.to.MealTo;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static ru.javawebinar.topjava.util.DateTimeUtil.parseLocalDate;
import static ru.javawebinar.topjava.util.DateTimeUtil.parseLocalTime;

@RestController
@RequestMapping("/ajax/meals")
public class MealUIController extends AbstractMealController{

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MealTo> getAll() {
        return super.getAll();
    }

    @GetMapping("/filter")
    public List<MealTo> getBetween(@RequestParam String startDate,
                             @RequestParam String endDate,
                             @RequestParam String startTime,
                             @RequestParam String endTime) {
        LocalDate startD = parseLocalDate(startDate);
        LocalDate endD = parseLocalDate(endDate);
        LocalTime startT = parseLocalTime(startTime);
        LocalTime endT = parseLocalTime(endTime);

        return super.getBetween(startD, startT, endD, endT);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public Meal create(@RequestParam String dateTime,
                       @RequestParam String description,
                       @RequestParam String calories) {
        return super.create(new Meal(LocalDateTime.parse(dateTime), description, Integer.parseInt(calories)));
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        super.delete(id);
    }
}
