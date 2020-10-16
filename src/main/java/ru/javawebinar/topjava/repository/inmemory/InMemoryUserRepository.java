package ru.javawebinar.topjava.repository.inmemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryUserRepository implements UserRepository {
    //private static final Logger log = LoggerFactory.getLogger(InMemoryUserRepository.class);
    public static final int USER_ID = 1;
    public static final int ADMIN_ID = 2;
    private final Map<Integer, User> repository = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public boolean delete(int id) {
        return repository.remove(id) != null;
    }

    @Override
    public User save(User user) {
        if (user.isNew()) {
            user.setId(counter.incrementAndGet());
            repository.put(user.getId(), user);
            return user;
        }
        return repository.computeIfPresent(user.getId(), (id, oldUser) -> user);
    }

    @Override
    public User get(int id) {

        return repository.get(id);
    }

    @Override
    public List<User> getAll() {
        List<User> userList = new ArrayList<>(repository.values());
        userList.sort(Comparator.comparing(User::getName).thenComparing(User::getEmail));
        return userList;
    }

    @Override
    public User getByEmail(String email) {

        Optional<User> first = getAll().stream().filter(u -> u.getEmail().equals(email)).findFirst();
        return first.isPresent()? first.get() : null;
    }
}
