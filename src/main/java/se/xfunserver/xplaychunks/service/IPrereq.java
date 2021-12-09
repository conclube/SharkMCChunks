package se.xfunserver.xplaychunks.service;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface IPrereq<T> {

    int getWeight();

    boolean getPassed(@NotNull T data);

    default Optional<String> getErrorMessage(@NotNull T data) {
        return Optional.empty();
    }

    default Optional<String> getSuccessMessage(@NotNull T data) {
        return Optional.empty();
    }

    default void onSuccess(@NotNull T data) {}
}
