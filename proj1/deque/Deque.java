package deque;

public interface Deque<T> {
    public void addFirst(T item);
    public void addLast(T item);
    default boolean isEmpty(){
        return size() == 0;
    }
    public int size();
    public T removeFirst();
    public T removeLast();
    public T get(int index);

    public boolean equals(Object o);
    public void printDeque();

    //Iterator<T> iterator();
}
