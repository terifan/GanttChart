# GanttChart
This is a simple Gantt chart implementation used to measure performance of processes modelled after the network performance graph found in the Mozilla FireFox web browser.

### Prerequisites
Java 8, Netbeans project but not required

### Overview

Create an instance of the chart, visualise it in a window and then for each point of interest add an enter
before and exit after to measure and display the time. Processes can be nested and divided into subparts
by calling the tick method.

### Sample output

<img src="https://github.com/terifan/GanttChart/blob/master/src/org/terifan/ganttchart/samples/sample_output.png"></img>

### Example
```
GanttChart chart = new GanttChart();
new SimpleGanttWindow(chart).show();

void myFunction()
{
  chart.enter("myFunction");
  Thread.sleep(100); // perform work...
  chart.tick("doing something");
  Thread.sleep(100); // perform work...
  mySecondFunction();
  chart.exit();
}

void mySecondFunction()
{
  try (GanttChart unused = chart.enter("mySecondFunction")) // Java 9: try (chart.enter("mySecondFunction"))
  {
    Thread.sleep(100); // perform work...
    chart.tick("doing something");
    Thread.sleep(100); // perform work...
  }
}
```
