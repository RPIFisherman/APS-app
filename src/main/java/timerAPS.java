import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import ygong.APS.Schedule;
import ygong.APS.Scheduler;


/**
 * helper timer class for the APS project
 */
class timerAPS {


  public static void main(String[] args) {

    Scheduler scheduler = getScheduler();
    List<Schedule> schedules = scheduler.getSchedules();
    List<Schedule> best_schedules = scheduler.getBestSchedule(10);
    System.out.println(schedules.size());
    System.out.println(best_schedules.size());

    // total memory
    Runtime runtime = Runtime.getRuntime();
    long memory = runtime.totalMemory() - runtime.freeMemory();
    System.out.println("Memory Usage: " + memory / 1024 / 1024 + "MB");
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
    System.out.println(
        "Heap Memory Usage: " + heapMemoryUsage.getUsed() / 1024 / 1024
            + "MB");

  }

  private static Scheduler getScheduler() {
    Scheduler scheduler = new Scheduler();
    int[] weights = {40, 30, 10, 10};

    scheduler.initRandom(3, 2, 3, 40, 5, 0, 1337);
    scheduler.initRandom(3, 3, 20, 30, 1.05, 1.0, 1337);
    scheduler.initRandom(3, 2, 20, 40, 1.5, 1.2, 1337);
    scheduler.initRandom(3, 2, 20, 40, 1.5, 1.4, 1337);
    scheduler.initRandom(3, 2, 10, 30, 1.5, 0.1, 1337);

    scheduler.generateAllPossible();

    // parallel branch
    scheduler.calcAllSchedulesGrade(weights[0], weights[1], weights[2],
        weights[3]);
    return scheduler;
  }

}