# APS 排程

[![JavaDocs](https://img.shields.io/badge/javadoc-1.0.0-brightgreen.svg)](https://rpifisherman.github.io/APS-app/javadocs/index.html)
[![APS-JavaDocs](https://img.shields.io/badge/APS_Package-JavaDocs-blue)](https://rpifisherman.github.io/APS-app/javadocs/ygong/APS/package-summary.html)
[![README-English](https://img.shields.io/badge/README-English-blue)](README.md)

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

[![Qodana](https://github.com/RPIFisherman/APS-app/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/RPIFisherman/APS-app/actions/workflows/qodana_code_quality.yml)
[![Mirror GitHub Auto Queried Repos to Gitee](https://github.com/RPIFisherman/APS-app/actions/workflows/auto_sync.yml/badge.svg?branch=master)](https://github.com/RPIFisherman/APS-app/actions/workflows/auto_sync.yml)
[![codecov](https://codecov.io/gh/RPIFisherman/APS-app/graph/badge.svg?token=AZXVMKN3W2)](https://codecov.io/gh/RPIFisherman/APS-app)

## 版本需求

- 建议使用 Java 21，其他版本可能无法工作。
    - Java 8 可以编译并运行 [APSDemo.java](src/main/java/APSDemo.java)，但
      可能无法通过测试。
- 建议使用 Maven 3.9.6，其他版本可能无法工作。

## APSDemo 输出:

![APSDemo Output](docs/demo.png)

## 项目结构：

### 简明图表：

[![classDiagram-concise](https://img.shields.io/badge/mermaid-mermaid?style=for-the-badge&logo=mermaid&logoColor=%23FFFFFF&labelColor=%23FF3670&color=%23FF3670)](https://www.mermaidchart.com/raw/eac1bfb2-a39f-4aeb-a1f3-b894f8abc53f?theme=light&version=v0.1&format=svg)

```mermaid
classDiagram
    class Schedule {
        -ArrayList~MachineWithOrders~ _machines 所有产线
        -Grade _grade 评分
        +void calcStat(double min_makespan, int num_orders) 计算统计，统计结果存入Grade
        +Grade calcGradeByWeights(int on_time_weight, int makespan_weight, int est_weight, int ldt_weight) 根据权重计算评分
        +void scheduleAllOrders(Scheduler scheduler) 排程所有订单
    }

    class Schedule_MachineWithOrders {
        一条有订单的产线
        -Machine machine 对应的单条产线
        -ArrayList~OrderWithTime~ orders 产线所对应的订单
        -double _approx_run_time 产线的大致运行时间
        +boolean addOrder(Order o) 添加订单
        +boolean removeOrder(Order o) 移除订单
        +void scheduleAllOrders(Scheduler scheduler) 排程所有订单，计算换行时间
        +void scheduleAllOrders() 排程所有订单，无换行时间
    }

    class Schedule_OrderWithTime {
        一个有开始结束时间的订单
        -Order order 对应的单个订单
        -int _start_time 开始时间
        -int _end_time 结束时间
        -int status 状态
        +void setStartEndTime(int start_time, int end_time) 设置开始结束时间
    }

    class Schedule_Grade {
        每个排程的评分
        -double on_time_percentage 准时百分比（交付时间达成率）
        -double makespan_percentage 最大时间（生产时间）百分比
        -double est_percentage 最早开始时间达成率
        -double ldt_percentage 最晚交付时间达成率
        -double grade 评分
        +void calcGradeByWeights(int on_time_weight, int makespan_weight, int est_weight, int ldt_weight) 根据权重计算评分
    }

    class Machine {
        产线
        +String name 产线名称
        +int machine_ID 产线ID
        -HashMap~Integer, Integer~ products_pace_per_hour 对应的产品类型ID在这条产线上的（每小时）生产节拍
        +boolean checkViableOrder(int production_type_ID) 检查是否可以接受某种产品类型的订单
        +int getProductionPace(int production_type_id) 获取某种产品类型的生产节拍
    }

    class Order {
        订单
        +String name 订单名称
        +int order_ID 订单ID
        +int earliest_start_time 最早开始时间
        +int delivery_time 交付时间
        +int latest_due_time 最晚交付时间
        +int quantity 订单数量
        +int production_type_ID 产品类型ID
    }

    class Rules {
        必要约束条件
        +static boolean belowCapacity(MachineWithOrders machine, double threshold) 产线是否低于最小负荷
        +static boolean aboveCapacity(MachineWithOrders machine, double threshold) 产线是否高于最大负荷
        +static boolean orderFitsMachine(MachineWithOrders machine, OrderWithTime order) 订单是否适合这条产线
    }

    class Scheduler {
        排程器APS算法
        -List~Schedule~ _schedules 所有排程
        -int _num_production_types 产品类型数量
        -int _num_machines 产线数量
        -int _num_orders 订单数量
        -int _max_hours_allowed 资源能力
        -double _max_capacity_per_machine 资源最大负荷
        -double _min_capacity_per_machine 资源最小负荷
        -double _min_makespan 方案最短总时间
        -ArrayList~ArrayList~Double~~ _order_type_switch_times
        -ArrayList~Order~ _orders 所有订单
        -ArrayList~Machine~ _machines 所有产线
        +void generateAllPossible() 生成所有可能的排程
        +void calcAllSchedulesGrade(Integer... weights) 根据权重计算所有排程的评分
    }

    Schedule "1" *-- "许多个" Schedule_MachineWithOrders: 包含多个产线
    Schedule_MachineWithOrders "1" *-- "许多个" Schedule_OrderWithTime: 包含多个订单
    Schedule_OrderWithTime "1" *-- "1" Order: 对应一个订单
    Schedule_MachineWithOrders "1" *-- "1" Machine: 对应一个产线
    Schedule "1" *-- "1" Schedule_Grade: 包含一个评分
    Scheduler "1" *-- "1" Rules: 调用必要约束条件
    Scheduler "1" *-- "许多个" Schedule: 包含多个可能的排程
```

### 详细图表：

![Scheduler Structure](docs/Scheduler_structure.png)


## APS 排程算法流程图：

```mermaid
graph TD
    A[开始] --> init["<strong>输入</strong>：<br> 产品类型数量，产线数量，订单数量，<br> 资源能力，资源最大负荷，资源最小负荷，<br> 方案最短总时间，订单类型换行时间"]
init --> generateAllSchedules["调用 generateAllSchedules()必要约束条件"]
subgraph "generateAllSchedules()"
generateAllSchedules --> depthFirstSearch{"带约束的递归<br>depthFirstSearch()"}
depthFirstSearch -->|满足必要约束条件|C[添加到 _schedules]
%%depthFirstSearch -->|不满足必要约束条件|depthFirstSearch
C --> D["调用 scheduleAllOrders()计算开始时间、结束时间"]
end
D --> E[输出可能的排程方案]
F[等级权重] --> calcAllSchedulesGrade
E --> calcAllSchedulesGrade["调用 calcAllSchedulesGrade()"]
subgraph "calcAllSchedulesGrade()"
calcAllSchedulesGrade --> calcStat["调用 calcStat()计算占比"]
calcStat --> calcGradeByWeights["调用 calcGradeByWeights()计算权重"]
calcGradeByWeights --> Grade[Grade]
end
Grade --> G[输出带评价分数的排程方案]
G --> H[生成图表等]
```
