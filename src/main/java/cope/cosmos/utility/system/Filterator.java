package cope.cosmos.utility.system;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

// pasted from scala :yawn:
public class Filterator<E> implements Iterator<E> {
    
    private Iterator<? extends E> iterator;
    private Predicate<? super E> predicate;
    private E nextObject;
    private boolean nextObjectSet = false;
    
    public Filterator() {
        super();
    }
    
    public Filterator(Iterator<? extends E> iterator) {
        super();
        this.iterator = iterator;
    }
    
    public Filterator(Iterator<? extends E> iterator, Predicate<? super E> predicate) {
        super();
        this.iterator = iterator;
        this.predicate = predicate;
    }
    
    @Override
    public boolean hasNext() {
        return nextObjectSet || setNextObject();
    }
    
    @Override
    public E next() {
        if (!nextObjectSet && !setNextObject()) 
            throw new NoSuchElementException();
        
        nextObjectSet = false;
        return nextObject;
    }

    @Override
    public void remove() {
        if (nextObjectSet) 
            throw new IllegalStateException("remove() cannot be called");
        
        iterator.remove();
    }

    public Iterator<? extends E> getIterator() {
        return iterator;
    }

    public void setIterator(Iterator<? extends E> iterator) {
        this.iterator = iterator;
        nextObject = null;
        nextObjectSet = false;
    }

    public Predicate<? super E> getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate<? super E> predicate) {
        this.predicate = predicate;
        nextObject = null;
        nextObjectSet = false;
    }
    
    private boolean setNextObject() {
        while (iterator.hasNext()) {
            E object = iterator.next();
            if (predicate.test(object)) {
                nextObject = object;
                nextObjectSet = true;
                return true;
            }
        }
        
        return false;
    }
}