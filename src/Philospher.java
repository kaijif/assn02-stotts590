import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

enum PhilosopherState {
    STOPPED,
    THINKING,
    EATING,
    HUNGRY
}

public class Philospher extends Thread {
    public PhilosopherState state;
    public int i;
    private final Lock criticalRegionLock;
    public final Semaphore bothForksAvailable;
    private Philospher[] others;
    
    public Philospher(int i, Philospher[] others, Lock critRegionLock) {
        this.state = PhilosopherState.STOPPED;
        this.i = i;
        this.criticalRegionLock = critRegionLock;
        this.bothForksAvailable = new Semaphore(0);
        this.others = others;
    }
    
    @Override
    public void run(){
        while (true) {
            try {        
                this.think();
                this.get_forks();
                this.eat();
                this.put_forks();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void think() throws InterruptedException {
        System.out.println("\t\t" + i + " is THINKING");
        this.state = PhilosopherState.THINKING;
        long duration = (long) (Math.random() * 1000);
        Thread.sleep(duration);
    }

    private void eat() throws InterruptedException {
        System.out.println("\t\t" + i + " is starting EATING");
        this.state = PhilosopherState.EATING;
        long duration = (long) (Math.random() * 1000);
        Thread.sleep(duration);
        System.out.println("\t\t" + i + " has finished EATING");

    }

    private Philospher getLeftNeighbor() {
        return this.others[(i - 1 + 5) % 5];
    }

    private Philospher getRightNeighbor() {
        return this.others[(i + 1) % 5];
    }

    private void test() {
        if (this.state == PhilosopherState.HUNGRY && 
            this.getLeftNeighbor().state != PhilosopherState.EATING &&
            this.getRightNeighbor().state != PhilosopherState.EATING
        ) {
            this.bothForksAvailable.release();
        }
    }

    private void get_forks() throws InterruptedException {
        this.criticalRegionLock.lock();
        
        try {
            this.state = PhilosopherState.HUNGRY;
            System.out.println("\t\t" + i + " is HUNGRY");
            this.test();
        } finally {
            criticalRegionLock.unlock();
        }

        this.bothForksAvailable.acquire();
    }

    private void put_forks() {
        criticalRegionLock.lock();
        try {
            this.state = PhilosopherState.THINKING;  // philosopher has finished eating
            this.getLeftNeighbor().test();   // see if left neighbor can now eat
            this.getRightNeighbor().test();  // see if right neighbor can now eat
        } finally {
            criticalRegionLock.unlock();
        }
    }


}
