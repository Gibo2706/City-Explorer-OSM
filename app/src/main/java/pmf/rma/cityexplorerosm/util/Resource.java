package pmf.rma.cityexplorerosm.util;

import androidx.annotation.Nullable;

/** Generic wrapper for UI state (Loading/Success/Error). */
public class Resource<T> {
    public enum Status { LOADING, SUCCESS, ERROR }
    public final Status status;
    @Nullable public final T data;
    @Nullable public final String message;

    private Resource(Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }
    public static <T> Resource<T> loading() { return new Resource<>(Status.LOADING, null, null); }
    public static <T> Resource<T> success(T data) { return new Resource<>(Status.SUCCESS, data, null); }
    public static <T> Resource<T> error(String msg) { return new Resource<>(Status.ERROR, null, msg); }
}

