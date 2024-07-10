package ygong.APS;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ygong.APS.Machine.Stat;

public class APSDemo extends Application {
  public static void main(String[] args) { launch(args); }

  @Override
  public void start(Stage stage) {

    Scheduler scheduler = new Scheduler();
    scheduler.initRandom(3, 2, 20, 40, 1.3, 0.5, 1337);
    long startTime = System.nanoTime();
    scheduler.generateAllPossible();
    long endTime = System.nanoTime();
    System.out.println("Time elapsed for generate all possible schedules: " +
                       (endTime - startTime) / 1000000 + "ms");
    ArrayList<ArrayList<Machine>> schedules = scheduler.getSchedules();

    startTime = System.nanoTime();
    ArrayList<ArrayList<Stat>> stats = scheduler.calcAllPossibleSchedule();
    endTime = System.nanoTime();
    System.out.println("Time elapsed for update all possible schedules: " +
                       (endTime - startTime) / 1000000 + "ms");

    final int PRINT_NUM = 10;
    System.out.println(schedules.size());
    if (schedules.size() < PRINT_NUM) {
      return;
    }

    // plot in different tabs
    TabPane tabPane = new TabPane();
    // print out the best 3 schedules by grade
    Map<Scheduler.Grade, ArrayList<Machine>> grade_map =
        scheduler.getBestSchedule(PRINT_NUM);
    DecimalFormat df = new DecimalFormat("0.000");
    for (Map.Entry<Scheduler.Grade, ArrayList<Machine>> entry :
         grade_map.entrySet()) {
      Tab tab = new Tab(df.format(entry.getKey().getGrade()));
      // add a label
      Label label = new Label(entry.getKey().toString());
      label.setFont(new javafx.scene.text.Font("Arial", 20));
      label.setWrapText(true);
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
        "Heap Memory Usage: " + heapMemoryUsage.getUsed() / 1024 / 1024 + "MB");
  }
}