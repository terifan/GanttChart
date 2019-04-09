# GanttChart
This is a simple Gantt chart implementation used to measure performance of processes

Create an instance of the chart, visualise it in a window and then for each point of interest add an enter
before and exit after to measure and display the time. Processes can be nested and divided into subroutines
by calling the tick method.

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
  try (GanttChart unused = chart.enter("mySecondFunction"))
  {
    Thread.sleep(100); // perform work...
    chart.tick("doing something");
    Thread.sleep(100); // perform work...
  }
}
```
