import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;


public class App {
    public static void main(String[] args) throws Exception {
        Philospher[] philosphers = new Philospher[5];
        Lock criticalRegionLock = new ReentrantLock();
        for (int i = 0; i < 5; i++) {
            philosphers[i] = new Philospher(i, philosphers, criticalRegionLock);
        }

        for (int i = 0; i < 5; i++) {
            philosphers[i].start();
        }
    }
}
