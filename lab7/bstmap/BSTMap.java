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
            size = 1;
        }
        else{
            insert(root, key, value);
        }
    }

    @Override
    public Set keySet() throws UnsupportedOperationException{
        return Set.of();
    }

    @Override
    public V remove(K key) {
        if (containsKey(key)) {
            V val = get(key);
            root = delete(root, key);
            size--;
            return val;
        }
        return null;
    }

    private BSTNode delete(BSTNode node, K key) {
        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = delete(node.left, key);
        }
        else if (cmp > 0) {
            node.right = delete(node.right, key);
        }
        else {
            /** no child node */
            if (node.left == null && node.right == null) {
                return null;
            }
            /** one child */
            else if (node.left == null) {
                return node.right;
            }
            else if (node.right == null) {
                return node.left;
            }
            /** two children */
            else {
                BSTNode tmp = node;
                node = finMin(node.right);
                node.right = removeMin(tmp.right);
                node.left = tmp.left;
            }
        }
        return node;
    }

    // 找右侧最小节点
    private BSTNode finMin(BSTNode node) {
        if (node.left == null) {
            return node;
        }
        return finMin(node.left);
    }

    private BSTNode removeMin(BSTNode node) {
        if (node.left == null) {
            return node.right; // 注意不是return null, min可能有右节点
        }
        node.left = removeMin(node.left);
        return node;
    }

    @Override
    public V remove(K key, V value) {
        if (get(key).equals(value)) {
            remove(key);
        }
        return null;
    }

    @Override
    public Iterator iterator() throws UnsupportedOperationException {
        return null;
    }

    public void printInOrder(){
        printInOrder(root);
    }

    private void printInOrder(BSTNode node){
        if (node == null) {
            return;
        }
        printInOrder(node.left);
        System.out.print(node.key + " ");
        printInOrder(node.right);
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

