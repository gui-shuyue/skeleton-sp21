package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    BSTNode root;
    int size = 0;


    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        if (getNode(root, key) != null) {
            return true;
        }
        return false;
    }

    @Override
    public V get(K key) {
        BSTNode node = getNode(root, key);
        if (node == null) {
            return null;
        } else{
            return node.value;
        }
    }


    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void put(K key, V value) {
        if (root == null) {
            root = new BSTNode(key, value);
        }
        else{
            insert(root, key, value);
        }
    }

    @Override
    public Set keySet() {
        return Set.of();
    }

    @Override
    public V remove(K key) {
        return null;
    }

    @Override
    public V remove(K key, V value) {
        return null;
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    private BSTNode getNode(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return getNode(node.left, key);
        }else if (cmp > 0) {
            return getNode(node.right, key);
        }else {
            return node;
        }
    }

    private BSTNode insert(BSTNode N, K key, V value) {
        if (N == null) {
            size++;
            return new BSTNode(key, value);
        }
        if (key.compareTo(N.key) < 0)
            N.left = insert(N.left, key, value);
        else if (key.compareTo(N.key) > 0)
            N.right = insert(N.right, key, value);
        else
            N.value = value;
        return N;
    }

    private class BSTNode {
        K key;
        V value;
        BSTNode left = null, right = null;

        private BSTNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
