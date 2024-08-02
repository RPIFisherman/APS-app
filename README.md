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

### Concise Diagram:
```mermaid
classDiagram
    class Schedule {
        -ArrayList~MachineWithOrders~ _machines
        -Grade _grade
        +void calcStat(double min_makespan, int num_orders)
        +Grade calcGradeByWeights(int on_time_weight, int makespan_weight, int est_weight, int ldt_weight)
        +void scheduleAllOrders(Scheduler scheduler)
    }

    class Schedule_MachineWithOrders {
        -Machine machine
        -ArrayList~OrderWithTime~ orders
        -double _approx_run_time
        +boolean addOrder(Order o)
        +boolean removeOrder(Order o)
        +void scheduleAllOrders(Scheduler scheduler)
    }

    class Schedule_OrderWithTime {
        -Order order
        -int _start_time
        -int _end_time
        -int status
        +void setStartEndTime(int start_time, int end_time)
    }

    class Schedule_Grade {
        -double on_time_percentage
        -double makespan_percentage
        -double est_percentage
        -double ldt_percentage
        -double grade
        +void calcGradeByWeights(int on_time_weight, int makespan_weight, int est_weight, int ldt_weight)
    }

    class Machine {
        +String name
        +int machine_ID
        -HashMap~Integer, Integer~ products_pace_per_hour
        +boolean checkViableOrder(int production_type_ID)
        +int getProductionPace(int production_type_id)
    }

    class Order {
        +String name
        +int order_ID
        +int earliest_start_time
        +int delivery_time
        +int latest_due_time
        +int quantity
        +int production_type_ID
    }

    class Rules {
        +static boolean belowCapacity(MachineWithOrders machine, double threshold)
        +static boolean aboveCapacity(MachineWithOrders machine, double threshold)
        +static boolean orderFitsMachine(MachineWithOrders machine, OrderWithTime order)
    }

    class Scheduler {
        -List~Schedule~ _schedules
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
        +void generateAllPossible()
        +void calcAllSchedulesGrade(Integer... weights)
    }

    Schedule "1" *-- "many" Schedule_MachineWithOrders : Contains many machines
    Schedule_MachineWithOrders "1" *-- "many" Schedule_OrderWithTime : Contains many orders
    Schedule_OrderWithTime "1" *-- "1" Order : refers to one order
    Schedule_MachineWithOrders "1" *-- "1" Machine : refers to one machine
    Schedule "1" *-- "1" Schedule_Grade : has one grade
    Scheduler "1" *-- "1" Rules : can apply many rules
    Scheduler "1" *-- "many" Schedule : has many schedules
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
