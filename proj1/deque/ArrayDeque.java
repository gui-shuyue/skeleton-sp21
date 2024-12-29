package deque;

public class ArrayDeque<T> implements Deque<T> {
//    private T[] items;
//    private int size;
//    private int headIndex;
//    private int tailIndex;
//
//    public ArrayDeque() {
//        items = (T[]) new Object[8];
//        headIndex = 0;
//        tailIndex = 7;
//        size = 0;
//    }
//
//    private void resize(int capacity) {
//        T[] a = (T[]) new Object[capacity];
//
//        System.arraycopy(items, 0, a, 0, headIndex + 1);
//        System.arraycopy(items, tailIndex, a, tailIndex + size, size - tailIndex);
//        headIndex = headIndex;
//        tailIndex = tailIndex + size;
//    }
//
//    private void shrink(){
//        if (this.size > items.length / 4 || items.length == 8) {
//            return;
//        }
//        T[] a = (T[]) new Object[items.length / 2];
//        System.arraycopy(items, 0, a, 0, headIndex);
//        System.arraycopy(items, tailIndex + 1, a, tailIndex - items.length / 2 + 1, items.length - tailIndex - 1);
//
//        headIndex = headIndex;
//        tailIndex = tailIndex + items.length / 2;
//    }
//
//    @Override
//    public void addFirst(T i) {
//        items[headIndex] = i;
//        size++;
//
//        if (size == items.length) {
//            resize(items.length * 2);
//        }
//        headIndex++;
//    }
//
//    @Override
//    public void addLast(T i) {
//        items[tailIndex] = i;
//        size++;
//
//        if (size == items.length) {
//            resize(items.length * 2);
//        }
//        tailIndex--;
//    }
//
//
//    @Override
//    public int size() {
//        return size;
//    }
//
//    @Override
//    public T removeFirst() {
//        if (size == 0) {
//            return null;
//        }
//        T i = items[headIndex - 1] ;
//        items[headIndex - 1] = null;
//        headIndex--;
//        size--;
//        this.shrink();
//        return i;
//    }
//
//    @Override
//    public T removeLast() {
//        if (size == 0) {
//            return null;
//        }
//        if (tailIndex == items.length - 1) {
//
//            return items[0]
//        }
//
//        T i = items[tailIndex + 1];
//        items[tailIndex + 1] = null;
//        tailIndex++;
//        size--;
//        this.shrink();
//        return i;
//    }
//
//
//
//    @Override
//    public T get(int index) {
//        if (index < 0 || index >= size) {
//            return null;
//        }
//        if (index < headIndex) {
//            return items[index];
//        } else {
//            return items[headIndex - 1 + items.length - index];
//
//        }
//
//    }TAT

    private T[] items = (T[]) new Object[8];
    private int size;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque() {
        size = 0;
        nextFirst = 3;
        nextLast = 4;
    }

    public ArrayDeque(T item) {
        items[3] = item;
        size = 1;
        nextFirst = 2;
        nextLast = 4;
    }

    public void addFirst(T item) {
        items[nextFirst] = item;
        size += 1;
        nextFirst -= 1;
        if (nextFirst == -1) {
            resize(size * 2);
        }
    }

    public void addLast(T item) {
        items[nextLast] = item;
        size += 1;
        nextLast += 1;
        if (nextLast == items.length) {
            resize(size * 2);
        }
    }

    public int size() {
        return size;
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        nextFirst += 1;
        T item = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;
        shrinkSize();
        return item;
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        nextLast -= 1;
        T item = items[nextLast];
        items[nextLast] = null;
        size -= 1;
        shrinkSize();
        return item;
    }

    private void shrinkSize() {
        if (isEmpty()) {
            resize(8);
        } else if (items.length / 4 > size && size >= 4) {
            resize(size * 2);
        }
    }

    @SuppressWarnings("unchecked")
    private void resize(int s) {
        T[] newItems = (T[]) new Object[s];
        int firstPos = Math.abs(s - size) / 2;
        System.arraycopy(items, nextFirst + 1, newItems, firstPos, size);
        items = newItems;
        nextFirst = firstPos - 1;
        nextLast = firstPos + size;
    }

    public T get(int index) {
        if (index < 0 || index > size - 1) {
            return null;
        }
        int itemIndex = nextFirst + 1 + index;
        return items[itemIndex];
    }

}
