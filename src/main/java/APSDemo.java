import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import ygong.APS.Schedule;
import ygong.APS.Schedule.MachineWithOrders;
import ygong.APS.Scheduler;

public class APSDemo extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) {
    showWeightInputWindow(stage, weights -> {
      Scheduler scheduler = new Scheduler(2);
      scheduler.initRandom(3, 2, 26, 40, 1.25, 0.77, 1337);
      // scheduler.initRandom(3, 2, 4, 40, 2, 0.0, 1337);
      long startTime = System.nanoTime();
      scheduler.generateAllPossible();
      long endTime = System.nanoTime();
      System.out.println("Time elapsed for generate all possible schedules: "
          + (endTime - startTime) / 1000000 + "ms");

      startTime = System.nanoTime();
      /*
       NOTE:we can directly use default weights
        ArrayList<ArrayList<Stat>> stats =
               scheduler.calcAllPossibleSchedule();
        Or customize Weights:
             on_time(% of product), the products doesn't violate due and
             earliest
             makespan(2-% to the best), the time elapsed to finish all products
             est_violate(% of product), violate the earliest start time
             ldt_violate(% of product), violate the latest due time
      */
      scheduler.calcAllSchedulesGrade(weights[0], weights[1], weights[2],
          weights[3]);
      endTime = System.nanoTime();
      List<Schedule> schedules = scheduler.getSchedules();
      System.out.println("Time elapsed for update all possible schedules: "
          + (endTime - startTime) / 1000000 + "ms");

      int PRINT_NUM = 10;
      System.out.println(schedules.size());
      if (schedules.size() < PRINT_NUM) {
        PRINT_NUM = schedules.size();
      }

      // plot in different tabs
      TabPane tabPane = new TabPane();
      // print out the best 103 schedules by grade
      schedules = scheduler.getBestSchedule(PRINT_NUM);
      DecimalFormat df = new DecimalFormat("0.000");
      for (int i = 0; i < PRINT_NUM; i++) {
        double grade = schedules.get(i).getGrade();
        Schedule schedule = schedules.get(i);

        Tab tab = new Tab(df.format(grade));

        // add a label
        Label label = new Label(schedule.toString());

        System.out.println("Grade: " + grade);
        for(MachineWithOrders m : schedule) {
          for (ygong.APS.Schedule.OrderWithSchedule o : m) {
            System.out.print(o.getOrderID() + " ");
          }
          System.out.println();
        }
        System.out.println();

        label.setFont(new javafx.scene.text.Font("Arial", 20));
        label.setWrapText(true);
        label.setMaxWidth(1000);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(label, scheduler.createChart(schedule));
        vbox.setSpacing(50);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);

        tab.setClosable(false);
        tab.setContent(vbox);
        tabPane.getTabs().add(tab);
      }
      Scene scene = new Scene(tabPane, 1280, 720);

      // Set the scene to the stage and show it
      stage.setScene(scene);
      stage.setTitle("Scheduler Application");
      stage.show();

      // center the stage
      Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
      stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
      stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);

      // check memory usage
      Runtime runtime = Runtime.getRuntime();
      long memory = runtime.totalMemory() - runtime.freeMemory();
      System.out.println("Memory Usage: " + memory / 1024 / 1024 + "MB");
      MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
      MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
      System.out.println(
          "Heap Memory Usage: " + heapMemoryUsage.getUsed() / 1024 / 1024
              + "MB");
    });
  }

  private void showWeightInputWindow(Stage stage,
      Consumer<int[]> onWeightsSubmitted) {
    // Create the form for weight inputs
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(25, 25, 25, 25));

    Label on_time_label = new Label("On Time (%):");
    grid.add(on_time_label, 0, 1);
    TextField on_time_field = new TextField("100");
    grid.add(on_time_field, 1, 1);

    Label makespan_label = new Label("Makespan (%):");
    grid.add(makespan_label, 0, 2);
    TextField makespan_field = new TextField("100");
    grid.add(makespan_field, 1, 2);

    Label est_violate_label = new Label("EST Violate (%):");
    grid.add(est_violate_label, 0, 3);
    TextField est_violate_field = new TextField("100");
    grid.add(est_violate_field, 1, 3);

    Label ldt_violate_label = new Label("LDT Violate (%):");
    grid.add(ldt_violate_label, 0, 4);
    TextField ldt_violate_field = new TextField("100");
    grid.add(ldt_violate_field, 1, 4);

    Button submit_button = new Button("Submit");
    grid.add(submit_button, 1, 5);

    Scene input_scene = new Scene(grid, 300, 275);
    stage.setScene(input_scene);
    stage.setTitle("Set Weights");
    stage.show();

    submit_button.setOnAction(e -> {
      submit_button.setDisable(true);
      int on_time = Integer.parseInt(on_time_field.getText());
      int makespan = Integer.parseInt(makespan_field.getText());
      int est_violate = Integer.parseInt(est_violate_field.getText());
      int ldt_violate = Integer.parseInt(ldt_violate_field.getText());

      int[] weights = {on_time, makespan, est_violate, ldt_violate};
      onWeightsSubmitted.accept(weights);
    });
  }
}