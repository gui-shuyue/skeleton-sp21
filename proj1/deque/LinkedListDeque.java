package deque;

public class LinkedListDeque<T> implements Deque<T> {
    private class Node {
        public T item;
        public Node next;
        public Node prev;
        public Node(Node prev, T item, Node next) {
            this.item = item;
            this.next = next;
            this.prev = prev;
        }
    }
    private Node head;
    private Node tail;
    private int size;
    private Node current;

    public LinkedListDeque() {
        tail = new Node(null, null, null);
        head = new Node(tail, null, tail);
        tail.next = head;
        tail.prev = head;
        size = 0;
    }

    public LinkedListDeque(T i){
        tail = new Node(null, null, null);
        head = new Node(tail, null, tail);
        tail.next = head;
        tail.prev = head;
        head.next = new Node(head, i, tail);
        tail.prev = head.next;
        size = 1;
    }

    @Override
    public void addFirst(T i){
        Node tmp = head.next;
        head.next = new Node(head, i, tmp);
        tmp.prev = head.next;
        size++;
    }

    @Override
    public void addLast(T i){
        Node tmp = tail.prev;
        tail.prev = new Node(tmp, i, tail);
        tmp.next = tail.prev;
        size++;
    }

    @Override
    public int size(){
        return size;
    }

    @Override
    public T removeFirst(){
        if (size == 0){
            return null;
        }
        T x = head.next.item;
        head.next = head.next.next;
        head.next.prev = head;
        size--;
        return x;
    }

    @Override
    public T removeLast(){
        if (size == 0){
            return null;
        }
        T x = tail.prev.item;
        tail.prev = tail.prev.prev;
        tail.prev.next = tail;
        size--;
        return x;
    }

    @Override
    public T get(int i){
        Node p = head.next;
        while (i != 0){
            p = p.next;
        }
        return p.item;
    }

    public T getRecursive(int index){
        Node p = head.next;
        if (index < 0 || index > size - 1) {
            return null;
        }
        return getRecursiveHelper(index, p);
    }

    private T getRecursiveHelper(int index, Node p){
        if (index == 0) {
            return p.item;
        } else {
            return getRecursiveHelper(index - 1, p.next);
        }
    }

    @Override
    public void printDeque(){
        Node p = head.next;
        if (p == tail) {
            System.out.println(" ");
            return;
        }
        for (int i = 0; i < size; i++) {
            System.out.print(p.item + " ");
        }
    }
}
