package io.winebox.passaporto.services.routing.ferrovia.path;

import lombok.Getter;

import java.util.List;

/**
 * Created by AJ on 7/24/16.
 */
public final class PathException extends java.lang.Exception {
    @Getter
    private List<Throwable> errors;

    public PathException(List<Throwable> errors ) {
        this.errors = errors;
    }
}