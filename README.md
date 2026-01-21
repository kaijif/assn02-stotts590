# Team Members
Kaiji Fu
Aayush Prakash

# Design Rationale

This implementation solves the Dining Philosophers problem with semaphores and a critical region lock. The design prioritizes deadlock prevention while allowing philosophers to operate concurrently.

## Representation Choices

Each philosopher is represented as a separate thread extending Java's Thread class, with their identity captured by an integer index (0-4). The philosopher's lifecycle is modeled through an enumerated state that transitions between THINKING, HUNGRY, and EATING phases. Rather than explicitly modeling physical forks as separate objects, this solution takes an abstract approach where fork availability is implicitly represented through the states of neighboring philosophers. A philosopher can only acquire both forks (and thus eat) when both neighbors are not in the EATING state. This abstraction eliminates the need for explicit fork objects and simplifies the synchronization logic.

The table structure is implicit in the circular arrangement of the philosopher array, where each philosopher maintains a reference to the shared array and can compute their left and right neighbors using modulo arithmetic. The "spaghetti" itself isn't explicitly represented but is conceptually present during the eating phase, which is simulated through a randomized sleep duration between 0 and 1000 milliseconds. Similarly, the thinking phase uses a longer random duration (0-10,000 milliseconds) to simulate contemplation between meals.

## Synchronization Mechanism

The core synchronization strategy employs a global ReentrantLock (the critical region lock) that protects all state changes and neighbor checks. When a philosopher becomes hungry, they acquire this lock, set their state to HUNGRY, and execute a test to see if both forks are available. If the test succeeds, the philosopher's state transitions to EATING and they release a permit on their personal semaphore. The philosopher then releases the critical region lock and blocks on acquiring their semaphore, which will succeed immediately if both forks were available, or will cause them to wait if a neighbor was eating.

Each philosopher has their own binary semaphore (bothForksAvailable) that starts with zero permits. This semaphore acts as a conditional variable that blocks the philosopher until both forks become available. When a philosopher finishes eating and puts down their forks, they acquire the critical region lock, transition back to THINKING, and then test both neighbors to see if they can now eat. This testing of neighbors is crucial—it ensures that when a philosopher releases their forks, any hungry neighbors are immediately given the opportunity to acquire forks if the conditions are now favorable.

## Deadlock and Starvation Prevention

This algorithm prevents deadlock through its fundamental design: philosophers never hold resources while waiting for additional resources. The critical region lock is always released before a philosopher blocks on their semaphore, and the semaphore is only released by the test method when conditions are actually satisfied. There is no circular wait condition because philosophers don't directly hold forks—they only hold a state that indicates they're eating. All state checks happen atomically within the critical region, preventing race conditions that could lead to inconsistent states.

Deadlock is therefore impossible in this implementation. The system cannot reach a state where all philosophers are waiting indefinitely because at least one philosopher will always be able to transition from THINKING to HUNGRY to EATING when their neighbors are not eating. Starvation, however, remains theoretically possible but highly improbable. If a philosopher is particularly unlucky with timing, their neighbors could keep alternating eating states such that this philosopher never finds a window where both neighbors are simultaneously not eating. In practice, the randomized sleep durations make this scenario extremely unlikely, as the asynchronous timing virtually guarantees that every philosopher will eventually have a window of opportunity. A truly starvation-free solution would require additional fairness mechanisms, such as priority queues based on waiting time, but the current probabilistic fairness is generally sufficient.