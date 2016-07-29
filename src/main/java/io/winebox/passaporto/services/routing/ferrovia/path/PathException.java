package io.winebox.passaporto.services.routing.ferrovia.path;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Created by AJ on 7/24/16.
 */
@AllArgsConstructor @ToString
public final class PathException extends java.lang.Exception {
    @Getter private List<Throwable> errors;
}