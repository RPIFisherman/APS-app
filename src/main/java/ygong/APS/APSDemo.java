package ygong.APS;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ygong.APS.Machine.Stat;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Map;

public class APSDemo extends Application {
  public static void main(String[] args) {launch(args);}

  @Override
  public void start(Stage stage) {

    Scheduler scheduler = new Scheduler();
    scheduler.initRandom(3, 2, 20, 40, 1.3, 0.5, 1337);
    long startTime = System.nanoTime();
    scheduler.generateAllPossible();
    long endTime = System.nanoTime();
    System.out.println("Time elapsed for generate all possible schedules: "
                       + (endTime - startTime) / 1000000
                       + "ms");
    ArrayList<ArrayList<Machine>> schedules = scheduler.getSchedules();

    startTime = System.nanoTime();
    ArrayList<ArrayList<Stat>> stats
            = scheduler.calcAllPossibleSchedule();
    endTime = System.nanoTime();
    System.out.println("Time elapsed for update all possible schedules: "
                       + (endTime - startTime) / 1000000
                       + "ms");

    //    scheduler.printSchedules();
    //    schedules.get(0).get(0).getOrders().iterator().next().start_time
    System.out.println(schedules.size());
    if (schedules.size() < 3) {
      return;
    }

    // plot in different tabs
    TabPane tabPane = new TabPane();
    // print out the best 3 schedules by grade
    Map<Scheduler.Grade, ArrayList<Machine>>
            grade_map = scheduler.getBestSchedule(3);
    for (Map.Entry<Scheduler.Grade, ArrayList<Machine>> entry :
            grade_map.entrySet()) {
      Tab tab = new Tab(Double.toString(entry.getKey().grade_));
      // add a label
      Label label = new Label(entry.getKey().toString());
      VBox vbox = new VBox();
      vbox.getChildren().addAll(label, Scheduler.createChart(entry.getValue()));
      vbox.setSpacing(10);
      vbox.setAlignment(javafx.geometry.Pos.CENTER);
      tab.setContent(vbox);
      tabPane.getTabs().add(tab);
    }
    Scene scene = new Scene(tabPane, 1280, 720);
    // Set the scene to the stage and show it
    stage.setScene(scene);
    stage.setTitle("Scheduler Application");
    stage.show();

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