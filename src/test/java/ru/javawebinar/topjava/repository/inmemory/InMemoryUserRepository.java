package ru.javawebinar.topjava.repository.inmemory;

import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.UserTestData;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.javawebinar.topjava.UserTestData.admin;
import static ru.javawebinar.topjava.UserTestData.user;


@Repository
public class InMemoryUserRepository extends InMemoryBaseRepository<User> implements UserRepository {

        private final Map<Integer, User> usersMap = new ConcurrentHashMap<>();
        private final AtomicInteger counter = new AtomicInteger(0);

    public void init() {
                usersMap.clear();
               usersMap.put(UserTestData.USER_ID, user);
                usersMap.put(UserTestData.ADMIN_ID, admin);
            }
        @Override
        public User save(User user) {
            if (user.isNew()) {
                user.setId(counter.incrementAndGet());
                usersMap.put(user.getId(), user);
                return user;
            }
            return usersMap.computeIfPresent(user.getId(), (id, oldUser) -> user);
        }

        @Override
        public boolean delete(int id) {
            return usersMap.remove(id) != null;
        }

        @Override
        public User get(int id) {
            return usersMap.get(id);
        }

        @Override
        public List<User> getAll() {
            return getCollection().stream()
                    .sorted(Comparator.comparing(User::getName).thenComparing(User::getEmail))
                    .collect(Collectors.toList());
        }

        @Override
        public User getByEmail(String email) {
            return getCollection().stream()
                    .filter(u -> email.equals(u.getEmail()))
                    .findFirst()
                    .orElse(null);
        }
}