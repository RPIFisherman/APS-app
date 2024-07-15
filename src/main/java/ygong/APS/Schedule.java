package ygong.APS;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

// a helper class that works the same as List<Machine> with grades
public class Schedule
        implements Comparable<Schedule>, Iterable<Machine> {
  public static Comparator<Schedule> scheduleComparator
          = Comparator.comparingDouble(Schedule::getGrade);
  private final ArrayList<Machine> _machines;
  private Grade _grade;

  public Schedule(ArrayList<Machine> machines, Grade grade) {
    _machines = machines;
    this._grade = grade;
  }

  public Schedule(ArrayList<Machine> machines) {
    _machines = new ArrayList<>(machines.size());
    for (Machine m : machines) {
      _machines.add(new Machine(m));
    }
    _grade = new Grade(0, 0, 0, 0, 0);
  }

  public ArrayList<Machine> getMachines() {
    return _machines;
  }

  public double getGrade() {
    return _grade.getGrade();
  }

  public void calcGrade(int on_time_weight, int makespan_weight,
                        int est_weight, int ldt_weight) {
    double on_time = 0;
    double makespan = 0;
    double est = 0;
    double ldt = 0;
    for (Machine m : _machines) {
      // on_time += m.getStat().on_time_percentage;
      // makespan += m.getStat().makespan_percentage;
      // est += m.getStat().est_percentage;
      // ldt += m.getStat().ldt_percentage;
    }
    on_time /= _machines.size();
    makespan /= _machines.size();
    est /= _machines.size();
    ldt /= _machines.size();
    // _grade.on_time_percentage = on_time;
    // _grade.makespan_percentage = makespan;
    // _grade.est_percentage = est;
    // _grade.ldt_percentage = ldt;
    // _grade.calcGradeByWeights(on_time_weight, makespan_weight, est_weight, ldt_weight);
  }

  public ArrayList<String> getMachineNames() {
    ArrayList<String> names = new ArrayList<>();
    for (Machine m : _machines) {
      names.add(m.name);
    }
    return names;
  }

  @Override
  public int compareTo(Schedule o) {
    return Double.compare(this.getGrade(), o.getGrade());
  }

  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("0.000");
    return "Schedule{" +
           "grade=" + df.format(_grade.getGrade()) +
           ", machines=" + _machines +
           '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Schedule schedule = (Schedule) o;
    return _machines.equals(schedule._machines);
  }

  @Override
  public int hashCode() {
    return _machines.hashCode();
  }

  @Override
  public Iterator<Machine> iterator() {
    return _machines.iterator();
  }

  @Override
  public void forEach(Consumer<? super Machine> action) {
    _machines.forEach(action);
  }

  @Override
  public Spliterator<Machine> spliterator() {
    return _machines.spliterator();
  }

  // Helper class for Grades
  public final static class Grade {
    // compare parameter for sorted map
    public static Comparator<Grade> gradeComparator =
            Comparator.comparingDouble(o -> o.grade_);
    public final double on_time_percentage;
    public final double makespan_percentage;
    public final double est_percentage;
    public final double ldt_percentage;
    private double grade_;

    Grade(double grade, double on_time, double makespan, double est_percentage,
          double ldt_percentage) {
      grade_ = grade;
      on_time_percentage = on_time;
      makespan_percentage = makespan;
      this.est_percentage = est_percentage;
      this.ldt_percentage = ldt_percentage;
    }

    void calcGradeByWeights(int on_time_weight, int makespan_weight,
                            int est_weight, int ldt_weight) {
      grade_ = on_time_percentage * on_time_weight +
               makespan_percentage * makespan_weight +
               est_percentage * est_weight + ldt_percentage * ldt_weight;
    }

    @Override
    public String toString() {
      DecimalFormat df = new DecimalFormat("0.000");
      return "Grade{"
             + "grade: " + df.format(grade_) +
             ", on_time=" + df.format(on_time_percentage * 100) +
             "%, makespan(2-best%)=" + df.format(makespan_percentage * 100) +
             "%, earliest=" + df.format(est_percentage * 100) +
             "%, latest=" + df.format(ldt_percentage * 100) + "%}";
    }

    public double getGrade() {return grade_;}
  }
}
