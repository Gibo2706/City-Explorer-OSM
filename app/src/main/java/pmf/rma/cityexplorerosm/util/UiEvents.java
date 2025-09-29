package pmf.rma.cityexplorerosm.util;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/** Global event bus for lightweight UI messages (snackbars / toasts). */
public final class UiEvents {
    private static final UiEvents INSTANCE = new UiEvents();
    private final MutableLiveData<String> messages = new MutableLiveData<>();
    private UiEvents() {}
    public static UiEvents get() { return INSTANCE; }
    public LiveData<String> messages() { return messages; }
    public void emit(String msg) { messages.postValue(msg); }
}

