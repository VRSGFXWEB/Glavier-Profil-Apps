package com.glacier.profilapps;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.lifecycle.ViewTreeViewModelStoreOwner;
import androidx.savedstate.SavedStateRegistry;
import androidx.savedstate.SavedStateRegistryController;
import androidx.savedstate.SavedStateRegistryOwner;
import androidx.savedstate.ViewTreeSavedStateRegistryOwner;

/** Owner agar ComposeView bisa hidup di dalam Service (overlay). */
public class OverlayOwner implements LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private final LifecycleRegistry registry = new LifecycleRegistry(this);
    private final SavedStateRegistryController controller = SavedStateRegistryController.create(this);
    private final ViewModelStore store = new ViewModelStore();

    public OverlayOwner() {
        controller.performAttach();
        controller.performRestore(null);
        registry.setCurrentState(Lifecycle.State.CREATED);
    }

    public void start() { registry.setCurrentState(Lifecycle.State.RESUMED); }

    public void stop() {
        registry.setCurrentState(Lifecycle.State.DESTROYED);
        store.clear();
    }

    public void attachTo(View v) {
        ViewTreeLifecycleOwner.set(v, this);
        ViewTreeViewModelStoreOwner.set(v, this);
        ViewTreeSavedStateRegistryOwner.set(v, this);
    }

    @NonNull @Override public Lifecycle getLifecycle() { return registry; }
    @NonNull @Override public SavedStateRegistry getSavedStateRegistry() { return controller.getSavedStateRegistry(); }
    @NonNull @Override public ViewModelStore getViewModelStore() { return store; }
}
