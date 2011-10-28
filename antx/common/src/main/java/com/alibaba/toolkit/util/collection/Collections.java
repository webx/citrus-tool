/*
 * Copyright 2010 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.toolkit.util.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * 这个类是<code>java.util.Collections</code>类的扩展, 包括了对<code>ListMap</code>接口的支持.
 * 
 * @version $Id: Collections.java,v 1.1 2003/07/03 07:26:15 baobao Exp $
 * @author Michael Zhou
 */
public class Collections {
    /**
     * 取得一个线程安全的<code>Collection</code>对象.
     * 
     * @param collection 被包装的<code>Collection</code>对象
     * @return 线程安全的<code>Collection</code>对象
     */
    public static Collection synchronizedCollection(Collection collection) {
        return new SynchronizedCollection(collection);
    }

    /**
     * 取得一个线程安全的<code>Collection</code>对象.
     * 
     * @param collection 被包装的<code>Collection</code>对象
     * @param syncRoot 同步对象
     * @return 线程安全的<code>Collection</code>对象
     */
    public static Collection synchronizedCollection(Collection collection, Object syncRoot) {
        return new SynchronizedCollection(collection, syncRoot);
    }

    /**
     * 取得一个线程安全的<code>Set</code>对象.
     * 
     * @param set 被包装的<code>Set</code>对象
     * @return 线程安全的<code>Set</code>对象
     */
    public static Set synchronizedSet(Set set) {
        return new SynchronizedSet(set);
    }

    /**
     * 取得一个线程安全的<code>Set</code>对象.
     * 
     * @param set 被包装的<code>Set</code>对象
     * @param syncRoot 同步对象
     * @return 线程安全的<code>Set</code>对象
     */
    public static Set synchronizedSet(Set set, Object syncRoot) {
        return new SynchronizedSet(set, syncRoot);
    }

    /**
     * 取得一个线程安全的<code>SortedSet</code>对象.
     * 
     * @param set 被包装的<code>SortedSet</code>对象
     * @return 线程安全的<code>SortedSet</code>对象
     */
    public static SortedSet synchronizedSortedSet(SortedSet set) {
        return new SynchronizedSortedSet(set);
    }

    /**
     * 取得一个线程安全的<code>SortedSet</code>对象.
     * 
     * @param set 被包装的<code>SortedSet</code>对象
     * @param syncRoot 同步对象
     * @return 线程安全的<code>SortedSet</code>对象
     */
    public static SortedSet synchronizedSortedSet(SortedSet set, Object syncRoot) {
        return new SynchronizedSortedSet(set, syncRoot);
    }

    /**
     * 取得一个线程安全的<code>List</code>对象.
     * 
     * @param list 被包装的<code>List</code>对象
     * @return 线程安全的<code>List</code>对象
     */
    public static List synchronizedList(List list) {
        return new SynchronizedList(list);
    }

    /**
     * 取得一个线程安全的<code>List</code>对象.
     * 
     * @param list 被包装的<code>List</code>对象
     * @param syncRoot 同步对象
     * @return 线程安全的<code>List</code>对象
     */
    public static List synchronizedList(List list, Object syncRoot) {
        return new SynchronizedList(list, syncRoot);
    }

    /**
     * 取得一个线程安全的<code>Map</code>对象.
     * 
     * @param map 被包装的<code>Map</code>对象
     * @return 线程安全的<code>Map</code>对象
     */
    public static Map synchronizedMap(Map map) {
        return new SynchronizedMap(map);
    }

    /**
     * 取得一个线程安全的<code>Map</code>对象.
     * 
     * @param map 被包装的<code>Map</code>对象
     * @param syncRoot 同步对象
     * @return 线程安全的<code>Map</code>对象
     */
    public static Map synchronizedMap(Map map, Object syncRoot) {
        return new SynchronizedMap(map, syncRoot);
    }

    /**
     * 取得一个线程安全的<code>SortedMap</code>对象.
     * 
     * @param map 被包装的<code>SortedMap</code>对象
     * @return 线程安全的<code>SortedMap</code>对象
     */
    public static SortedMap synchronizedSortedMap(SortedMap map) {
        return new SynchronizedSortedMap(map);
    }

    /**
     * 取得一个线程安全的<code>SortedMap</code>对象.
     * 
     * @param map 被包装的<code>SortedMap</code>对象
     * @param syncRoot 同步对象
     * @return 线程安全的<code>SortedMap</code>对象
     */
    public static SortedMap synchronizedSortedMap(SortedMap map, Object syncRoot) {
        return new SynchronizedSortedMap(map, syncRoot);
    }

    /**
     * 取得一个线程安全的<code>ListMap</code>对象.
     * 
     * @param map 被包装的<code>ListMap</code>对象
     * @return 线程安全的<code>ListMap</code>对象
     */
    public static ListMap synchronizedListMap(ListMap map) {
        return new SynchronizedListMap(map);
    }

    /**
     * 取得一个线程安全的<code>ListMap</code>对象.
     * 
     * @param map 被包装的<code>ListMap</code>对象
     * @param syncRoot 同步对象
     * @return 线程安全的<code>ListMap</code>对象
     */
    public static ListMap synchronizedListMap(ListMap map, Object syncRoot) {
        return new SynchronizedListMap(map, syncRoot);
    }
}

/**
 * 线程安全的<code>Collection</code>的包装器.
 */
class SynchronizedCollection implements Collection, Serializable {
    private static final long serialVersionUID = 3053995032091335093L;
    protected Collection collection;
    protected Object syncRoot;

    SynchronizedCollection(Collection collection) {
        this(collection, null);
    }

    SynchronizedCollection(Collection collection, Object syncRoot) {
        if (collection == null) {
            throw new NullPointerException();
        } else {
            this.collection = collection;
        }

        this.syncRoot = syncRoot == null ? this : syncRoot;
    }

    public int size() {
        synchronized (syncRoot) {
            return collection.size();
        }
    }

    public boolean isEmpty() {
        synchronized (syncRoot) {
            return collection.isEmpty();
        }
    }

    public boolean contains(Object o) {
        synchronized (syncRoot) {
            return collection.contains(o);
        }
    }

    public Object[] toArray() {
        synchronized (syncRoot) {
            return collection.toArray();
        }
    }

    public Object[] toArray(Object[] a) {
        synchronized (syncRoot) {
            return collection.toArray(a);
        }
    }

    public Iterator iterator() {
        return collection.iterator(); // Must be manually synched by user!
    }

    public boolean add(Object o) {
        synchronized (syncRoot) {
            return collection.add(o);
        }
    }

    public boolean remove(Object o) {
        synchronized (syncRoot) {
            return collection.remove(o);
        }
    }

    public boolean containsAll(Collection coll) {
        synchronized (syncRoot) {
            return collection.containsAll(coll);
        }
    }

    public boolean addAll(Collection coll) {
        synchronized (syncRoot) {
            return collection.addAll(coll);
        }
    }

    public boolean removeAll(Collection coll) {
        synchronized (syncRoot) {
            return collection.removeAll(coll);
        }
    }

    public boolean retainAll(Collection coll) {
        synchronized (syncRoot) {
            return collection.retainAll(coll);
        }
    }

    public void clear() {
        synchronized (syncRoot) {
            collection.clear();
        }
    }

    @Override
    public String toString() {
        synchronized (syncRoot) {
            return collection.toString();
        }
    }
}

/**
 * 线程安全的<code>Set</code>的包装器.
 */
class SynchronizedSet extends SynchronizedCollection implements Set {
    private static final long serialVersionUID = -1688591608929270671L;

    SynchronizedSet(Set set) {
        super(set);
    }

    SynchronizedSet(Set set, Object syncRoot) {
        super(set, syncRoot);
    }

    @Override
    public boolean equals(Object o) {
        synchronized (syncRoot) {
            return collection.equals(o);
        }
    }

    @Override
    public int hashCode() {
        synchronized (syncRoot) {
            return collection.hashCode();
        }
    }
}

/**
 * 线程安全的<code>SortedSet</code>的包装器.
 */
class SynchronizedSortedSet extends SynchronizedSet implements SortedSet {
    private static final long serialVersionUID = -4254250562122002613L;

    SynchronizedSortedSet(SortedSet set) {
        super(set);
    }

    SynchronizedSortedSet(SortedSet set, Object syncRoot) {
        super(set, syncRoot);
    }

    public Comparator comparator() {
        synchronized (syncRoot) {
            return ((SortedSet) collection).comparator();
        }
    }

    public SortedSet subSet(Object fromElement, Object toElement) {
        synchronized (syncRoot) {
            return Collections.synchronizedSortedSet(((SortedSet) collection).subSet(fromElement, toElement), syncRoot);
        }
    }

    public SortedSet headSet(Object toElement) {
        synchronized (syncRoot) {
            return Collections.synchronizedSortedSet(((SortedSet) collection).headSet(toElement), syncRoot);
        }
    }

    public SortedSet tailSet(Object fromElement) {
        synchronized (syncRoot) {
            return Collections.synchronizedSortedSet(((SortedSet) collection).tailSet(fromElement), syncRoot);
        }
    }

    public Object first() {
        synchronized (syncRoot) {
            return ((SortedSet) collection).first();
        }
    }

    public Object last() {
        synchronized (syncRoot) {
            return ((SortedSet) collection).last();
        }
    }
}

/**
 * 线程安全的<code>List</code>的包装器.
 */
class SynchronizedList extends SynchronizedCollection implements List {
    private static final long serialVersionUID = -7754090372962971524L;

    SynchronizedList(List list) {
        super(list);
    }

    SynchronizedList(List list, Object syncRoot) {
        super(list, syncRoot);
    }

    @Override
    public boolean equals(Object o) {
        synchronized (syncRoot) {
            return ((List) collection).equals(o);
        }
    }

    @Override
    public int hashCode() {
        synchronized (syncRoot) {
            return ((List) collection).hashCode();
        }
    }

    public Object get(int index) {
        synchronized (syncRoot) {
            return ((List) collection).get(index);
        }
    }

    public Object set(int index, Object element) {
        synchronized (syncRoot) {
            return ((List) collection).set(index, element);
        }
    }

    public void add(int index, Object element) {
        synchronized (syncRoot) {
            ((List) collection).add(index, element);
        }
    }

    public Object remove(int index) {
        synchronized (syncRoot) {
            return ((List) collection).remove(index);
        }
    }

    public int indexOf(Object o) {
        synchronized (syncRoot) {
            return ((List) collection).indexOf(o);
        }
    }

    public int lastIndexOf(Object o) {
        synchronized (syncRoot) {
            return ((List) collection).lastIndexOf(o);
        }
    }

    public boolean addAll(int index, Collection c) {
        synchronized (syncRoot) {
            return ((List) collection).addAll(index, c);
        }
    }

    public ListIterator listIterator() {
        return ((List) collection).listIterator(); // Must be manually synched by user
    }

    public ListIterator listIterator(int index) {
        return ((List) collection).listIterator(index); // Must be manually synched by usr
    }

    public List subList(int fromIndex, int toIndex) {
        synchronized (syncRoot) {
            return Collections.synchronizedList(((List) collection).subList(fromIndex, toIndex), syncRoot);
        }
    }
}

/**
 * 线程安全的<code>Map</code>的包装器.
 */
class SynchronizedMap implements Map, Serializable {
    private static final long serialVersionUID = 1978198479659022715L;
    protected Map map;
    protected Object syncRoot;

    SynchronizedMap(Map map) {
        this(map, null);
    }

    SynchronizedMap(Map map, Object syncRoot) {
        if (map == null) {
            throw new NullPointerException();
        } else {
            this.map = map;
        }

        this.syncRoot = syncRoot == null ? this : syncRoot;
    }

    public int size() {
        synchronized (syncRoot) {
            return map.size();
        }
    }

    public boolean isEmpty() {
        synchronized (syncRoot) {
            return map.isEmpty();
        }
    }

    public boolean containsKey(Object key) {
        synchronized (syncRoot) {
            return map.containsKey(key);
        }
    }

    public boolean containsValue(Object value) {
        synchronized (syncRoot) {
            return map.containsValue(value);
        }
    }

    public Object get(Object key) {
        synchronized (syncRoot) {
            return map.get(key);
        }
    }

    public Object put(Object key, Object value) {
        synchronized (syncRoot) {
            return map.put(key, value);
        }
    }

    public Object remove(Object key) {
        synchronized (syncRoot) {
            return map.remove(key);
        }
    }

    public void putAll(Map map) {
        synchronized (syncRoot) {
            this.map.putAll(map);
        }
    }

    public void clear() {
        synchronized (syncRoot) {
            map.clear();
        }
    }

    private transient Set keySet = null;
    private transient Set entrySet = null;
    private transient Collection values = null;

    public Set keySet() {
        synchronized (syncRoot) {
            if (keySet == null) {
                keySet = Collections.synchronizedSet(map.keySet(), syncRoot);
            }

            return keySet;
        }
    }

    public Set entrySet() {
        synchronized (syncRoot) {
            if (entrySet == null) {
                entrySet = Collections.synchronizedSet(map.entrySet(), syncRoot);
            }

            return entrySet;
        }
    }

    public Collection values() {
        synchronized (syncRoot) {
            if (values == null) {
                values = Collections.synchronizedCollection(map.values(), syncRoot);
            }

            return values;
        }
    }

    @Override
    public boolean equals(Object o) {
        synchronized (syncRoot) {
            return map.equals(o);
        }
    }

    @Override
    public int hashCode() {
        synchronized (syncRoot) {
            return map.hashCode();
        }
    }

    @Override
    public String toString() {
        synchronized (syncRoot) {
            return map.toString();
        }
    }
}

/**
 * 线程安全的<code>SortedMap</code>的包装器.
 */
class SynchronizedSortedMap extends SynchronizedMap implements SortedMap {
    private static final long serialVersionUID = -4739449073617952001L;

    SynchronizedSortedMap(SortedMap map) {
        super(map);
    }

    SynchronizedSortedMap(SortedMap map, Object syncRoot) {
        super(map, syncRoot);
    }

    public Comparator comparator() {
        synchronized (syncRoot) {
            return ((SortedMap) map).comparator();
        }
    }

    public SortedMap subMap(Object fromKey, Object toKey) {
        synchronized (syncRoot) {
            return Collections.synchronizedSortedMap(((SortedMap) map).subMap(fromKey, toKey), syncRoot);
        }
    }

    public SortedMap headMap(Object toKey) {
        synchronized (syncRoot) {
            return Collections.synchronizedSortedMap(((SortedMap) map).headMap(toKey), syncRoot);
        }
    }

    public SortedMap tailMap(Object fromKey) {
        synchronized (syncRoot) {
            return Collections.synchronizedSortedMap(((SortedMap) map).tailMap(fromKey), syncRoot);
        }
    }

    public Object firstKey() {
        synchronized (syncRoot) {
            return ((SortedMap) map).firstKey();
        }
    }

    public Object lastKey() {
        synchronized (syncRoot) {
            return ((SortedMap) map).lastKey();
        }
    }
}

/**
 * 线程安全的<code>Collection</code>的包装器.
 */
class SynchronizedListMap extends SynchronizedMap implements ListMap {
    private static final long serialVersionUID = -8069282343682823184L;

    SynchronizedListMap(ListMap map) {
        super(map);
    }

    SynchronizedListMap(ListMap map, Object syncRoot) {
        super(map, syncRoot);
    }

    public Object get(int index) {
        synchronized (syncRoot) {
            return ((ListMap) map).get(index);
        }
    }

    public Object getKey(int index) {
        synchronized (syncRoot) {
            return ((ListMap) map).getKey(index);
        }
    }

    public Entry remove(int index) {
        synchronized (syncRoot) {
            return ((ListMap) map).remove(index);
        }
    }

    public List keyList() {
        synchronized (syncRoot) {
            return Collections.synchronizedList(((ListMap) map).keyList());
        }
    }

    public List valueList() {
        synchronized (syncRoot) {
            return Collections.synchronizedList(((ListMap) map).valueList());
        }
    }

    public List entryList() {
        synchronized (syncRoot) {
            return Collections.synchronizedList(((ListMap) map).entryList());
        }
    }
}
