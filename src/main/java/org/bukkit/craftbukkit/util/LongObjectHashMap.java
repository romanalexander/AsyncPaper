package org.bukkit.craftbukkit.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.craftbukkit.util.Java15Compat.Arrays_copyOf;

@SuppressWarnings("unchecked")
public class LongObjectHashMap<V> extends ConcurrentHashMap<Long, V> implements Cloneable, Serializable {
}
