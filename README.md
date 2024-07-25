# Advanced Planning and Scheduling(APS) Application

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

Thanks to [Roland's Gantt Plot code](https://stackoverflow.com/questions/27975898/gantt-chart-from-scratch), which helps me a lot on JavaFX. 

## Requirements

- Java 21 or above is recommended. Otherwise need to change some of the code to make it compatible with Java 8. See ``NOTE`` tags in the code.
- Maven 3. I test with mave

## Project Structure:
![Project Structure](structure.png)

## Output Demo:
