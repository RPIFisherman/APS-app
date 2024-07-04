package ygong.APS;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class GanttChart extends XYChart<Number, String> {

    public static class ExtraData {
        public long length;
        public String styleClass;

        public ExtraData(long lengthMs, String styleClass) {
            super();
            this.length = lengthMs;
            this.styleClass = styleClass;
        }

        public long getLength() {
            return length;
        }

        public String getStyleClass() {
            return styleClass;
        }
    }

    public GanttChart(Axis<Number> xAxis, Axis<String> yAxis) {
        super(xAxis, yAxis);
        setData(FXCollections.observableArrayList());
    }

    @Override
    protected void layoutPlotChildren() {
        for (Series<Number, String> series : getData()) {
            for (Data<Number, String> item : series.getData()) {
                double x = getXAxis().getDisplayPosition(item.getXValue());
                double y = getYAxis().getDisplayPosition(item.getYValue());
                double width = getXAxis().getDisplayPosition(item.getXValue().doubleValue() + ((ExtraData) item.getExtraValue()).getLength()) - x;
                double height = 10;

                StackPane rectangle = (StackPane) item.getNode();
                rectangle.setLayoutX(x);
                rectangle.setLayoutY(y - height / 2.0);
                rectangle.setPrefWidth(width);
                rectangle.setPrefHeight(height);
            }
        }
    }

    @Override
    protected void dataItemAdded(Series<Number, String> series, int itemIndex, Data<Number, String> item) {
        Node block = createContainer(series, getData().indexOf(series), item, itemIndex);
        getPlotChildren().add(block);
    }

    @Override
    protected void dataItemRemoved(Data<Number, String> item, Series<Number, String> series) {
        getPlotChildren().remove(item.getNode());
    }

    @Override
    protected void dataItemChanged(Data<Number, String> item) {
    }

    @Override
    protected void seriesAdded(Series<Number, String> series, int seriesIndex) {
        for (int j = 0; j < series.getData().size(); j++) {
            Data<Number, String> item = series.getData().get(j);
            Node container = createContainer(series, seriesIndex, item, j);
            getPlotChildren().add(container);
        }
    }

    @Override
    protected void seriesRemoved(Series<Number, String> series) {
        for (XYChart.Data<Number, String> d : series.getData()) {
            getPlotChildren().remove(d.getNode());
        }
    }

    private Node createContainer(Series<Number, String> series, int seriesIndex, final Data<Number, String> item, int itemIndex) {
        StackPane container = new StackPane();
        container.getStyleClass().add(((ExtraData) item.getExtraValue()).getStyleClass());
        item.setNode(container);
        return container;
    }
}
