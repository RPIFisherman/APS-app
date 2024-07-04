package ygong.APS;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;

import ygong.APS.GanttChart.ExtraData;
public class APSDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        stage.setTitle("Gantt Chart Sample");

        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();

        final ygong.APS.GanttChart<Number, String> chart = new ygong.APS.GanttChart<Number, String>(xAxis, yAxis);
        xAxis.setLabel("Time");
        xAxis.setTickLabelFill(Color.CHOCOLATE);
        xAxis.setMinorTickCount(4);

        yAxis.setLabel("Machines");
        yAxis.setTickLabelFill(Color.CHOCOLATE);
        yAxis.setTickLabelGap(10);
        yAxis.setCategories(FXCollections.observableArrayList(Arrays.asList(machines)));

        addTestSeries(chart);

        chart.setTitle("Machine Monitoring");
        chart.setLegendVisible(false);
        chart.setBlockHeight(50);
        String machine;

        chart.getStylesheets().add(getClass().getResource("/ganttchart.css").toExternalForm());

        Scene scene = new Scene(chart, 620, 350);
        stage.setScene(scene);
        stage.show();
    }


    private static final String[] machines = {"Machine 1", "Machine 2", "Machine 3"};
    private void addTestSeries(ygong.APS.GanttChart<Number, String> chart) {
        for (String machine : machines) {
            XYChart.Series series = new XYChart.Series();
            series.setName(machine);

            series.getData().add(createData(0, machine, 1, "status-red"));
            series.getData().add(createData(1, machine, 1, "status-green"));
            series.getData().add(createData(2, machine, 1, "status-red"));
            series.getData().add(createData(3, machine, 1, "status-green"));

            chart.getData().add(series);
        }
    }
    private XYChart.Data<Number, String> createData(int start, String machine, int duration, String styleClass) {
        XYChart.Data<Number, String> data = new XYChart.Data<>(start, machine, new ygong.APS.GanttChart.ExtraData(duration, styleClass));
        StackPane node = new StackPane();
        node.getStyleClass().add(styleClass);
        data.setNode(node);

        enableDragAndDrop(data);

        return data;
    }

    private void enableDragAndDrop(XYChart.Data<Number, String> data) {
        StackPane node = (StackPane) data.getNode();

        node.setOnDragDetected(event -> {
            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(data.getYValue());
            db.setContent(content);
            event.consume();
        });

        node.setOnDragOver(event -> {
            if (event.getGestureSource() != node && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        node.setOnDragEntered(event -> {
            if (event.getGestureSource() != node && event.getDragboard().hasString()) {
                node.setOpacity(0.7);
            }
        });

        node.setOnDragExited(event -> {
            node.setOpacity(1);
        });

        node.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String newMachine = db.getString();
                data.setYValue(newMachine);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        node.setOnDragDone(DragEvent::consume);
    }
}