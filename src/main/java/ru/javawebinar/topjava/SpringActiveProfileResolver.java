package ru.javawebinar.topjava;

import org.springframework.test.context.ActiveProfilesResolver;

public class SpringActiveProfileResolver implements ActiveProfilesResolver {
    @Override
    public String[] resolve(final Class<?> aClass) {

        return new String[] { Profiles.REPOSITORY_IMPLEMENTATION, Profiles.getActiveDbProfile() };
    }
}
