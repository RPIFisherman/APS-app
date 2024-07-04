package ygong.APS;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.util.List;
import java.util.ArrayList;

public class APSDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Interactive Gantt Chart Example");

        // Sample data
        List<ygong.APS.Order> orders = new ArrayList<>();
        orders.add(new ygong.APS.Order("Order 1", "Machine 1", 0, 10));
        orders.add(new ygong.APS.Order("Order 2", "Machine 2", 10, 20));
        orders.add(new ygong.APS.Order("Order 3", "Machine 1", 20, 30));
        orders.add(new ygong.APS.Order("Order 4", "Machine 3", 30, 40));

        CategoryAxis yAxis = new CategoryAxis();
        NumberAxis xAxis = new NumberAxis();

        ygong.APS.GanttChart ganttChart = new ygong.APS.GanttChart(xAxis, yAxis);
        ganttChart.setTitle("Interactive Gantt Chart");

        yAxis.setLabel("Machines");
        xAxis.setLabel("Time");

        XYChart.Series<Number, String> series1 = new XYChart.Series<>();
        for (ygong.APS.Order order : orders) {
            XYChart.Data<Number, String> data = new XYChart.Data(order.getStart(), order.getMachine(), new ygong.APS.GanttChart.ExtraData(order.getDuration(), "status-good"));
            series1.getData().add(data);
        }
        System.out.println(series1.getData());
        ganttChart.getData().add(series1);

        Scene scene = new Scene(ganttChart, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/ganttchart.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

}