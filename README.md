# GanttChart
This is a simple Gantt chart implementation used to measure performance of processes

Create an instance of the chart, visualise it in a window and then for each point of interest add an enter
before and exit after to measure and display the time. Processes can be nested and divided into subroutines
by calling the tick method.


GanttChart chart = new GanttChart();

new SimpleGanttWindow(chart).show();

chart.enter("step 1");

// perform work...

chart.tick("step 2");

// perform work...
 
chart.exit();
