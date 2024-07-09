package ygong.APS;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ygong.APS.GanttChart.ExtraData;

public class APSDemo extends Application {
  public static void main(String[] args) { launch(args); }

  @Override
  public void start(Stage stage) {

    stage.setTitle("Gantt Chart Sample");

    String[] machines = new String[] {"Machine 1", "Machine 2", "Machine 3"};

    final NumberAxis xAxis = new NumberAxis();
    final CategoryAxis yAxis = new CategoryAxis();

    final GanttChart<Number, String> chart = new GanttChart<>(xAxis, yAxis);
    xAxis.setLabel("");
    xAxis.setTickLabelFill(Color.CHOCOLATE);
    xAxis.setMinorTickCount(4);

    yAxis.setLabel("");
    yAxis.setTickLabelFill(Color.CHOCOLATE);
    yAxis.setTickLabelGap(10);
    yAxis.setCategories(
        FXCollections.observableArrayList(Arrays.asList(machines)));

    chart.setTitle("Machine Monitoring");
    chart.setLegendVisible(false);
    chart.setBlockHeight(50);
    String machine;

    machine = machines[0];
    XYChart.Series series1 = new XYChart.Series();
    series1.getData().add(
        new XYChart.Data(0, machine, new ExtraData(1, "status-red")));
    series1.getData().add(
        new XYChart.Data(1, machine, new ExtraData(1, "status-green")));
    series1.getData().add(
        new XYChart.Data(2, machine, new ExtraData(1, "status-red")));
    series1.getData().add(
        new XYChart.Data(3, machine, new ExtraData(1, "status-green")));

    machine = machines[1];
    XYChart.Series series2 = new XYChart.Series();
    series2.getData().add(
        new XYChart.Data(0, machine, new ExtraData(1, "status-green")));
    series2.getData().add(
        new XYChart.Data(1, machine, new ExtraData(1, "status-green")));
    series2.getData().add(
        new XYChart.Data(2, machine, new ExtraData(2, "status-red")));



    Scheduler scheduler = new Scheduler();
    scheduler.initRandom(3, 2, 10, 40, 1.3, 0.3, 1337);
    scheduler.generateAllPossible();
    ArrayList<ArrayList<Machine>> schedules = scheduler.getSchedules();
    scheduler.updateAllPossibleSchedule();
    //    scheduler.printSchedules();
    //    schedules.get(0).get(0).getOrders().iterator().next().start_time =
    //    999;
    System.out.println(schedules.size());
    System.out.println(schedules.get(0).get(0).getOrders().size());

    scheduler.plotSchedule(0);
    machine = machines[2];
    XYChart.Series series3 = new XYChart.Series();
    series3.getData().add(
        new XYChart.Data( schedules.get(0).get(0).getOrders().iterator().next().start_time
                , machine, new ExtraData(2, "status-blue")));
    series3.getData().add(
        new XYChart.Data(2, machine, new ExtraData(2, "status-red")));
    series3.getData().add(
        new XYChart.Data(3, machine, new ExtraData(1, "status-green")));

    chart.getData().addAll(series1, series2, series3);

    chart.getStylesheets().add(
        getClass().getResource("/ganttchart.css").toExternalForm());

    Scene scene = new Scene(chart, 620, 350);
    stage.setScene(scene);
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