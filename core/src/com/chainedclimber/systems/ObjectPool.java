package com.chainedclimber.systems;

import com.badlogic.gdx.utils.Pool;

/**
 * Generic object pool for zero-allocation gameplay
 * Reuses objects instead of creating/destroying them
 */
public class ObjectPool<T> extends Pool<T> {
    private final PoolableFactory<T> factory;
    
    public interface PoolableFactory<T> {
        T create();
        void reset(T object);
    }
    
    public ObjectPool(PoolableFactory<T> factory, int initialCapacity, int max) {
        super(initialCapacity, max);
        this.factory = factory;
    }
    
    @Override
    protected T newObject() {
        return factory.create();
    }
    
    @Override
    public T obtain() {
        T object = super.obtain();
        return object;
    }
    
    @Override
    public void free(T object) {
        factory.reset(object);
        super.free(object);
    }
}
