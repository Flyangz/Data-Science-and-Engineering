


import java.util.Iterator;
import java.util.NoSuchElementException;


public class Deque<Item> implements Iterable<Item> {
		
		private int n;	//number of elements on deque
		private Node first;	//begining of deque
		private Node last;	//end of deque
		
		//helper linked list class
		private class Node{
			private Item item;
			private Node next;
			private Node prev;
		}
		
	   //Initialize an empty deque.
	   public Deque() {
		   first = null;
		   last = null;
		   n= 0;
	   }
	   
	   public boolean isEmpty() {
	   /**   
		 * is the deque empty?
		 */
		   return n == 0;
	   }
	   
	   public int size() {
	   /**
	    *  return the number of items on the deque
	    */
		   return n;
	   }
	   
	   public void addFirst(Item item) {
	   /**
	    *  add the item to the front
	    */
		  if (item == null) throw new java.lang.IllegalArgumentException();
		   
		  Node oldfirst = first;
		  first = new Node();
		  first.item = item;
		  first.prev = null;		  
		  first.next = oldfirst;
		  if (isEmpty()) {
			  last = first;
		  }
		  else { oldfirst.prev = first;}
		  n++;
	   }
	   
	   public void addLast(Item item) {
	   /**
	    *  add the item to the end
	    */
		   if (item == null) throw new java.lang.IllegalArgumentException();
		   
		   Node oldlast = last;
		   last = new Node();
		   last.item = item;
		   last.next = null;		   
		   last.prev = oldlast;
		   if (isEmpty()) {
			   first = last;
		   }
		   else {oldlast.next = last;}
		   n++;
	   }
	   
	   public Item removeFirst() {
	   /**
	    *  remove and return the item from the front
	    */
		   if (isEmpty()) throw new java.util.NoSuchElementException();
		   
		   Item item = first.item;
		   first = first.next;
		   n--;
		   if (isEmpty()) {
			   first = null;
			   last = null;
		   }else {
			   first.prev = null;
		   }		   
		   return item;
		   
	   }
	   
	   public Item removeLast() {
	   /**
	    *  remove and return the item from the end
	    */
		   if (isEmpty()) throw new java.util.NoSuchElementException();
		   
		   Item item = last.item;
		   last = last.prev;		   
		   n--;
		   if (isEmpty()) {
			   last = null;
			   first = null;
		   }else {
			   last.next = null;
		   }
		   return item;
	   }
	   
	   public Iterator<Item> iterator(){	   
	   /**
	    *  return an iterator over items in order from front to end
	    * @param args
	    */
		  return new ListIterator();
		}
	   
	   private class ListIterator implements Iterator<Item> {
		   private Node current = first;
		   
		   public boolean hasNext() { return current != null; }
		   public Item next() {
			   if(!hasNext()) throw new NoSuchElementException();
			   Item item = current.item;
			   current = current.next;
			   return item;
		   }
	   }
	   
	   public static void main(String[] args) {
		   // unit testing (optional)

	        }
}

