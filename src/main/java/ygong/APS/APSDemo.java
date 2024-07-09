package ygong.APS;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;

public class APSDemo extends Application {
  public static void main(String[] args) {launch(args);}

  @Override
  public void start(Stage stage) {

    Scheduler scheduler = new Scheduler();
    scheduler.initRandom(3, 2, 20, 40, 1.5, 0.5, 1337);
    long startTime = System.nanoTime();
    scheduler.generateAllPossible();
    long endTime = System.nanoTime();
    System.out.println("Time elapsed for generate all possible schedules: " + (endTime - startTime) / 1000000
            + "ms");
    ArrayList<ArrayList<Machine>> schedules = scheduler.getSchedules();

    startTime = System.nanoTime();
    ArrayList<ArrayList<Scheduler.Stat>> stats
            = scheduler.updateAllPossibleSchedule();
    endTime = System.nanoTime();
    System.out.println("Time elapsed for update all possible schedules: " + (endTime - startTime) / 1000000
            + "ms");

    //    scheduler.printSchedules();
    //    schedules.get(0).get(0).getOrders().iterator().next().start_time
    System.out.println(schedules.size());
    if(schedules.size() < 3) {
      return;
    }

//    /*
    // plot in different tabs
    TabPane tabPane = new TabPane();
    for (int i = 1337; i < 1340; i++) {
      Tab tab = new Tab("Schedule " + (i + 1));
      tab.setContent(scheduler.createChart(i));
      tabPane.getTabs().add(tab);
    }
    Scene scene = new Scene(tabPane, 620, 350);
    // Set the scene to the stage and show it
    stage.setScene(scene);
    stage.setTitle("Scheduler Application");
    stage.show();
//     */

    // check memory usage
    Runtime runtime = Runtime.getRuntime();
    long memory = runtime.totalMemory() - runtime.freeMemory();
    System.out.println("Memory Usage: " + memory / 1024 / 1024 + "MB");
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
    System.out.println(
            "Heap Memory Usage: " + heapMemoryUsage.getUsed() / 1024 / 1024
            + "MB");

  }
}