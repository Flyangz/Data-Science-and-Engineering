
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.lang.IllegalArgumentException;
import edu.princeton.cs.algs4.StdRandom;

public class RandomizedQueue<Item> implements Iterable<Item> {
	
	private Item[] q;	// queue elements
	private int n;		// number of elements in RandomizedQueue

    public RandomizedQueue() {
    	// construct an empty randomized queue
        q = (Item[]) new Object[2];
        n = 0;
    }


    public boolean isEmpty() {
    	// is the queue empty?
    	return n == 0;
    }


    public int size() {
    	// return the number of items on the queue
    	return n;
    }

    private void resize(int capacity) {
    	assert capacity >=n;
    	Item[] temp =(Item[]) new Object[capacity];
    	for (int i=0; i<n; i++) {
    		temp[i] = q[i];
    	}
    	q = temp;
    }
    
    public void enqueue(Item item) {
    	// add the item
    	if (item ==null) throw new java.lang.IllegalArgumentException();  
    	if (n == q.length) resize(q.length*2);
    	q[n++] = item;
    }
   

    public Item dequeue() {
    	// delete and return a random item
    	if (isEmpty()) throw new java.util.NoSuchElementException(); 
    	int index = StdRandom.uniform(n);  
        Item item = q[index];
        q[index] = q[n-1];
        q[n-1] = null;
        n--;
        if (n > 0 && n == q.length/4) resize(q.length/2);
        return item;
    }


    public Item sample() {
    	// return (but do not delete) a random item
    	if (isEmpty()) throw new java.util.NoSuchElementException();
    	int index = StdRandom.uniform(n);  
    	return q[index]; 
    }


    public Iterator<Item> iterator() {
    	// return an independent iterator over items in random order
       return new RandomIterator();
    }
    
    private class RandomIterator implements Iterator<Item>{
    	
    	private int r;
    	private Item[] iterQueue;
    	
    	public RandomIterator() {
    		r = n;
    		iterQueue = (Item[]) new Object[r];
    		for (int i=0; i<r; i++) {
    			iterQueue[i] = q[i];
    		}
    	}
    	
    	public boolean hasNext()	{return r != 0;}
    	public Item next() {
    		if (!hasNext()) throw new java.util.NoSuchElementException();
    		
    		int i = StdRandom.uniform(r);
    		Item item = iterQueue[i];
    		iterQueue[i] = iterQueue[r-1];
    		iterQueue[r-1] = null;
    		r--;
    		return item;
    	}
    }
    	public static void main (String[] args) {
            // unit testing (optional)

        }
    }

