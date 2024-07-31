package ygong.APS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * This GanttChart class is copied from
 * <a href="https://stackoverflow.com/questions/27975898/gantt-chart-from-scratch">Roland</a>
 *
 * @param <X> X-axis type
 * @param <Y> Y-axis type
 *
 * @see <a href="https://stackoverflow.com/questions/27975898/gantt-chart-from-scratch">StackOverflow</a>
 * @author <a href="https://stackoverflow.com/users/1844265/roland">Roland</a>, <a
 * href="mailto:yuyanggong.rpi@gmail.com">Yuyang
 * Gong</a>
 * @version 1.0
 * @since 1.0
 */
public class GanttChart<X, Y> extends XYChart<X, Y> {

  private double blockHeight = 10;

  /**
   * Construct a new GanttChart with the given axis.
   * @param xAxis X-axis of the chart
   * @param yAxis Y-axis of the chart
   */
  public GanttChart(@NamedArg("xAxis") Axis<X> xAxis,
      @NamedArg("yAxis") Axis<Y> yAxis) {
    this(xAxis, yAxis, FXCollections.observableArrayList());
  }

  /**
   * @param xAxis X-axis of the chart
   * @param yAxis Y-axis of the chart
   * @param data Data of the chart
   */
  public GanttChart(@NamedArg("xAxis") Axis<X> xAxis,
      @NamedArg("yAxis") Axis<Y> yAxis,
      @NamedArg("data") ObservableList<Series<X, Y>> data) {
    super(xAxis, yAxis);
    if (!(xAxis instanceof ValueAxis && yAxis instanceof CategoryAxis)) {
      throw new IllegalArgumentException(
          "Axis type incorrect, X and Y should both be NumberAxis");
    }
    setData(data);
  }

  private static String getStyleClass(Object obj) {
    return ((ExtraData) obj).getStyleClass();
  }

  private static double getLength(Object obj) {
    return ((ExtraData) obj).getLength();
  }

  @Override
  protected void layoutPlotChildren() {

    for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++) {

      Series<X, Y> series = getData().get(seriesIndex);

      Iterator<Data<X, Y>> iter = getDisplayedDataIterator(series);
      while (iter.hasNext()) {
        Data<X, Y> item = iter.next();
        double x = getXAxis().getDisplayPosition(item.getXValue());
        double y = getYAxis().getDisplayPosition(item.getYValue());
        if (Double.isNaN(x) || Double.isNaN(y)) {
          continue;
        }
        Node block = item.getNode();
        Rectangle ellipse;
        if (block != null) {
          if (block instanceof StackPane) {
            StackPane region = (StackPane) item.getNode();
            if (region.getShape() == null) {
              ellipse = new Rectangle(getLength(item.getExtraValue()),
                  getBlockHeight());
            } else if (region.getShape() instanceof Rectangle) {
              ellipse = (Rectangle) region.getShape();
            } else {
              return;
            }
            ellipse.setWidth(
                getLength(item.getExtraValue()) *
                    ((getXAxis() instanceof NumberAxis)
                        ? Math.abs(((NumberAxis) getXAxis()).getScale())
                        : 1));
            ellipse.setHeight(
                getBlockHeight() *
                    ((getYAxis() instanceof NumberAxis)
                        ? Math.abs(((NumberAxis) getYAxis()).getScale())
                        : 1));
            y -= getBlockHeight() / 2.0;

            // Note: workaround for RT-7689 - saw this in ProgressControlSkin
            // The region doesn't update itself when the shape is mutated in
            // place, so we
            // null out and then restore the shape in order to force
            // invalidation.
            region.setShape(null);
            region.setShape(ellipse);
            region.setScaleShape(false);
            region.setCenterShape(false);
            region.setCacheShape(false);

            block.setLayoutX(x);
            block.setLayoutY(y);
          }
        }
      }
    }
  }

  /**
   * @return the blockHeight
   */
  public double getBlockHeight() {
    return blockHeight;
  }

  /**
   * @param blockHeight the blockHeight to set
   */
  public void setBlockHeight(double blockHeight) {
    this.blockHeight = blockHeight;
  }

  /**
   * @param series the series to add
   * @param itemIndex the index of the item
   * @param item the item to add
   *
   * @see GanttChart#createContainer(Series, int, Data, int)
   */
  @Override
  protected void dataItemAdded(Series<X, Y> series, int itemIndex,
      Data<X, Y> item) {
    Node block =
        createContainer(series, getData().indexOf(series), item, itemIndex);
    getPlotChildren().add(block);
  }

  /**
   * @param item the item to remove
   * @param series the series to remove
   */
  @Override
  protected void dataItemRemoved(final Data<X, Y> item,
      final Series<X, Y> series) {
    final Node block = item.getNode();
    getPlotChildren().remove(block);
    removeDataItemFromDisplay(series, item);
  }

  /**
   * @param item the item to change
   */
  @Override
  protected void dataItemChanged(Data<X, Y> item) {
  }

  /**
   * @param series the series to change
   * @param seriesIndex the index of the series
   *
   * @see GanttChart#createContainer(Series, int, Data, int)
   */
  @Override
  protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
    for (int j = 0; j < series.getData().size(); j++) {
      Data<X, Y> item = series.getData().get(j);
      Node container = createContainer(series, seriesIndex, item, j);
      getPlotChildren().add(container);
    }
  }

  /**
   * @param series the series to remove
   */
  @Override
  protected void seriesRemoved(final Series<X, Y> series) {
    for (XYChart.Data<X, Y> d : series.getData()) {
      final Node container = d.getNode();
      getPlotChildren().remove(container);
    }
    removeSeriesFromDisplay(series);
  }

  private Node createContainer(Series<X, Y> series, int seriesIndex,
      final Data<X, Y> item, int itemIndex) {

    Node container = item.getNode();

    if (container == null) {
      container = new StackPane();
      item.setNode(container);
    }

    container.getStyleClass().add(getStyleClass(item.getExtraValue()));

    return container;
  }

  @Override
  protected void updateAxisRange() {
    final Axis<X> xa = getXAxis();
    final Axis<Y> ya = getYAxis();
    List<X> xData = null;
    List<Y> yData = null;
    if (xa.isAutoRanging()) {
      xData = new ArrayList<>();
    }
    if (ya.isAutoRanging()) {
      yData = new ArrayList<>();
    }
    if (xData != null || yData != null) {
      for (Series<X, Y> series : getData()) {
        for (Data<X, Y> data : series.getData()) {
          if (xData != null) {
            xData.add(data.getXValue());
            xData.add(xa.toRealValue(xa.toNumericValue(data.getXValue()) +
                getLength(data.getExtraValue())));
          }
          if (yData != null) {
            yData.add(data.getYValue());
          }
        }
      }
      if (xData != null) {
        xa.invalidateRange(xData);
      }
      if (yData != null) {
        ya.invalidateRange(yData);
      }
    }
  }

  /**
   * The colored block for {@link GanttChart}
   */
  public static class ExtraData {

    /**
     * Length of the data
     */
    public double length;

    /**
     * the style string of the data, see the src/main/resources/ganttchart.css
     */
    public String styleClass;

    /**
     * @param length the length of the data
     * @param styleClass the style string
     */
    public ExtraData(double length, String styleClass) {
      super();
      this.length = length;
      this.styleClass = styleClass;
    }

    /**
     * @return the length of the block
     */
    public double getLength() {
      return length;
    }

    /**
     * @param length the length to be set
     */
    public void setLength(double length) {
      this.length = length;
    }

    /**
     * @return the styleClass
     */
    public String getStyleClass() {
      return styleClass;
    }

    /**
     * @param styleClass the styleClass to set
     */
    public void setStyleClass(String styleClass) {
      this.styleClass = styleClass;
    }
  }
}
