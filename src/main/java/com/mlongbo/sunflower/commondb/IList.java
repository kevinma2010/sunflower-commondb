package com.mlongbo.sunflower.commondb;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author malongbo
 */
public class IList<T> extends ArrayList<T> {
    public IList(int initialCapacity) {
        super(initialCapacity);
    }

    public IList() {
    }

    public IList(Collection<? extends T> c) {
        super(c);
    }
}
