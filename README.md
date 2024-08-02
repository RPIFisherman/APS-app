# Advanced Planning and Scheduling(APS) Application

[![JavaDocs](https://img.shields.io/badge/javadoc-1.0.0-brightgreen.svg)](https://rpifisherman.github.io/APS-app/javadocs/index.html)
[![APS-JavaDocs](https://img.shields.io/badge/APS_Package-JavaDocs-blue)](https://rpifisherman.github.io/APS-app/javadocs/ygong/APS/package-summary.html)
[![README-Chinese](https://img.shields.io/badge/README-Chinese-red)](README_CN.md)

```text
         _                   _          _                 _                   _          _
        / /\                /\ \       / /\              / /\                /\ \       /\ \
       / /  \              /  \ \     / /  \            / /  \              /  \ \     /  \ \
      / / /\ \            / /\ \ \   / / /\ \__        / / /\ \            / /\ \ \   / /\ \ \
     / / /\ \ \          / / /\ \_\ / / /\ \___\      / / /\ \ \          / / /\ \_\ / / /\ \_\
    / / /  \ \ \        / / /_/ / / \ \ \ \/___/     / / /  \ \ \        / / /_/ / // / /_/ / /
   / / /___/ /\ \      / / /__\/ /   \ \ \          / / /___/ /\ \      / / /__\/ // / /__\/ /
  / / /_____/ /\ \    / / /_____/_    \ \ \        / / /_____/ /\ \    / / /_____// / /_____/
 / /_________/\ \ \  / / /      /_/\__/ / /       / /_________/\ \ \  / / /      / / /
/ / /_       __\ \_\/ / /       \ \/___/ /       / / /_       __\ \_\/ / /      / / /
\_\___\     /____/_/\/_/         \_____\/        \_\___\     /____/_/\/_/       \/_/
                                                                 
```

## Introduction

Extend from the [APS-MES](https://github.com/RPIFisherman/APS-MES). This project
add a graphical user interface (GUI) to the APS application. The GUI is
implemented using the [javafx](https://openjfx.io/) library.

[![Qodana](https://github.com/RPIFisherman/APS-app/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/RPIFisherman/APS-app/actions/workflows/qodana_code_quality.yml)
[![Mirror GitHub Auto Queried Repos to Gitee](https://github.com/RPIFisherman/APS-app/actions/workflows/auto_sync.yml/badge.svg?branch=master)](https://github.com/RPIFisherman/APS-app/actions/workflows/auto_sync.yml)
[![codecov](https://codecov.io/gh/RPIFisherman/APS-app/graph/badge.svg?token=AZXVMKN3W2)](https://codecov.io/gh/RPIFisherman/APS-app)

Thanks
to [Roland's Gantt Plot code](https://stackoverflow.com/questions/27975898/gantt-chart-from-scratch),
which helps me a lot on JavaFX.

## Requirements

- Java 21 is recommended, other version may not work.
    - Java 8 can compile and run [APSDemo.java](src/main/java/APSDemo.java) but
      may not pass the test.
- Maven 3.9.6 is recommended, other version may not work.

## APSDemo:

![APSDemo Structure](docs/APSDemo_structure.png)

## APSDemo Output:

![APSDemo Output](docs/demo.png)

## Project Structure:

![Scheduler Structure](docs/Scheduler_structure.png)

```mermaid
classDiagram
    class Schedule {
        -ArrayList~MachineWithOrders~ _machines
        -Grade _grade
        +Schedule(ArrayList~Machine~ machines)
        +Schedule(Schedule s)
        +double getMaxMakespan()
        +void calcStat(double min_makespan, int num_orders)
        +Grade calcGradeByWeights(int on_time_weight, int makespan_weight, int est_weight, int ldt_weight)
        +int compareTo(Schedule o)
        +double getGrade()
        +int hashCode()
        +String toString()
        +Iterator~MachineWithOrders~ iterator()
        +Spliterator~MachineWithOrders~ spliterator()
        +MachineWithOrders getMachine(int i)
        +void scheduleAllOrders(Scheduler scheduler)
    }

    class Schedule_MachineWithOrders {
        -Machine machine
        -ArrayList~OrderWithTime~ orders
        -double _approx_run_time
        +MachineWithOrders(Machine machine)
        +MachineWithOrders(MachineWithOrders machine)
        +boolean addOrder(Order o)
        +boolean removeOrder(Order o)
        +void scheduleAllOrders(Scheduler scheduler)
        +void scheduleAllOrders()
        +Iterator~OrderWithTime~ iterator()
        +String getName()
        +int getMachineID()
        +ArrayList~OrderWithTime~ getOrders()
    }

    class Schedule_OrderWithTime {
        -Order order
        -int _start_time
        -int _end_time
        -int status
        +OrderWithTime(Order order)
        +OrderWithTime(OrderWithTime o)
        +int getStartTime()
        +int getEndTime()
        +int getProductionTypeID()
        +int getOrderID()
        +String getColorCode()
        +void setStartEndTime(double start_time, double end_time)
        +void setStartEndTime(int start_time, int end_time)
        +int statusCheck()
        +int hashCode()
        +boolean equals(Object o)
    }

    class Schedule_Grade {
        -double on_time_percentage
        -double makespan_percentage
        -double est_percentage
        -double ldt_percentage
        -double grade
        +Grade(double grade, double on_time, double makespan, double est_percentage, double ldt_percentage)
        +void calcGradeByWeights(int on_time_weight, int makespan_weight, int est_weight, int ldt_weight)
        +String toString()
        +double getGrade()
        +int compareTo(Grade o)
    }

    class Machine {
        +String name
        +int machine_ID
        -HashMap~Integer, Integer~ products_pace_per_hour
        +Machine(String name, int machine_ID, HashMap~Integer, Integer~ products_pace_per_hour)
        +Machine(Machine machine)
        +boolean checkViableOrder(int production_type_ID)
        +int getProductionPace(int production_type_id)
        +int hashCode()
        +String toString()
    }

    class Order {
        +String name
        +int order_ID
        +int earliest_start_time
        +int delivery_time
        +int latest_due_time
        +int quantity
        +int production_type_ID
        +Order(String name, int order_ID, int quantity, int production_type_ID, int earliest_start_time, int delivery_time, int latest_due_time)
        +Order(Order o)
        +String getName()
        +int getOrderID()
        +int getEarliestStartTime()
        +int getDeliveryTime()
        +int getLatestDueTime()
        +int getQuantity()
        +int getProductionTypeID()
        +int hashCode()
        +boolean equals(Object o)
        +String toString()
    }

    class Rules {
        +static double capacityLowerBound
        +static double capacityUpperBound
        +static boolean belowCapacity(MachineWithOrders machine, double threshold)
        +static boolean belowCapacity(MachineWithOrders machine)
        +static boolean aboveCapacity(MachineWithOrders machine, double threshold)
        +static boolean aboveCapacity(MachineWithOrders machine)
        +static boolean orderFitsMachine(MachineWithOrders machine, OrderWithTime order)
        +static boolean orderFitsMachine(MachineWithOrders machine, Order order)
    }

    class Scheduler {
        -List~Schedule~ _schedules
        -ArrayList~Future~ _futures
        -int _num_threads
        -ExecutorService _executor
        -int _num_production_types
        -int _num_machines
        -int _num_orders
        -int _max_units_allowed
        -double _max_capacity_per_machine
        -double _min_capacity_per_machine
        -double _min_makespan
        -ArrayList~ArrayList~Double~~ _order_type_switch_times
        -ArrayList~Order~ _orders
        -ArrayList~Machine~ _machines
        +Scheduler()
        +Scheduler(int num_threads)
        +void generateAllPossible()
        +void calcAllSchedulesGrade(Integer... weights)
        +ArrayList~Schedule~ getSchedules()
        +GanttChart~Number, String~ createChart(int index)
        +GanttChart~Number, String~ createChart(Schedule schedule)
        +double getSwitchTime(int i, int j)
        +int getMachineNum()
        +int getOrderNum()
        +double getMaxCapacityPerMachine()
        +double getMinCapacityPerMachine()
        +double getMinMakespan()
        +List~Schedule~ getBestSchedule()
        +List~Schedule~ getBestSchedule(int num)
        +String toString()
    }

    Schedule "1" *-- "many" Schedule_MachineWithOrders
    Schedule_MachineWithOrders "1" *-- "many" Schedule_OrderWithTime
    Schedule_OrderWithTime "1" *-- "1" Order
    Schedule_MachineWithOrders "1" *-- "1" Machine
    Schedule "1" *-- "1" Schedule_Grade
    Scheduler "1" *-- "1" Rules
    Scheduler "1" *-- "1" Schedule
```

## Project Workflow:

```mermaid
graph TD
    A[Start] --> init[Input]
    init --> generateAllSchedules["Call generateAllSchedules()"]
    subgraph "generateAllSchedules()"
        generateAllSchedules --> depthFirstSearch{"Recursion with Constrains<br>depthFirstSearch()"}
        depthFirstSearch -->|Satisfied| C[Add to _schedules]
        depthFirstSearch -->|Unsatisfied| depthFirstSearch
        C --> D["Call scheduleAllOrders()"]
    end
    D --> E[Output Schedules]
    F[weights for grade] --> calcAllSchedulesGrade
    E --> calcAllSchedulesGrade["Call calcAllSchedulesGrade()"]
    subgraph "calcAllSchedulesGrade()"
        calcAllSchedulesGrade --> calcGradeByWeights["Call calcGradeByWeights()"]
        calcGradeByWeights --> Grade[Grade]
    end
    Grade --> G[Output Schedule with Grade]
    G --> H[Generate Plot etc.]
```
