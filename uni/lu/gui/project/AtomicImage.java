package uni.lu.gui.project;

import java.awt.Image;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// This class is used to store an image in a thread-safe way.
public class AtomicImage {
    private Image image;
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public AtomicImage(Image image) {
        this.image = image;
    }

    public Image getImage() {
        lock.readLock().lock();
        try {
            return image;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setImage(Image image) {
        lock.writeLock().lock();
        try {
            this.image = image;
        } finally {
            lock.writeLock().unlock();
        }
    }
}