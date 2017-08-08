package com.elevator.model;

import com.elevator.enums.State;
import com.elevator.model.Elevator;
import com.elevator.model.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author gusdn
 * @since 2016-11-03
 */

public class TempTemp {

  public static final int MAX_FLOOR = 100;
  public static final int MIN_FLOOR = 10;

  private List<Elevator> elevators;
  private List<Person> waitingPersons;
  private ExecutorService executorService;

  private TempTemp(List<Elevator> elevators) {
    this.elevators = elevators;
    this.waitingPersons = new ArrayList<>();
    this.executorService = Executors.newSingleThreadExecutor();

    // 엔진 기동
    run();
  }
  
  /**
   * 승객 대기
   *
   * @param person the person
   */
  public void newPerson(Person person){
    this.waitingPersons.add(person);
  }
  /*
   * 실제 기동 처리 메소드
   */
  private void run() {
    executorService.submit(() -> {
      // 0.1초마다 갱신
      while (true) {
          EmptyElevatorStop();
          elevatorMovingAndAddPerson();
          callStateStopElevator();

          // 상태 출력
          showState();
          TimeUnit.MILLISECONDS.sleep(1000);
      }
    });
  }

  /*
   *
   * 엘리베이터가 움직일 필요가 없으면 멈추도록 함
   *
   */
  private void EmptyElevatorStop() {
    if(waitingPersons.size() == 0){
      elevators.stream()
              .filter(e -> e.getPersons().size() == 0) // 엘리베이터 안에 탄 사람이 없고
              .filter(e -> e.getState() != State.STOP) // 엘리베이터가 멈춰있지 않으며
              .filter(e -> e.getFloor() % 10 == 0) // 제대로된 층에 있다면
              .forEach(e -> e.setState(State.STOP)); // 엘리베이터를 멈춤
    }
  }
  /*
   * 멈춰있는 엘리베이터를 오게 하는 메소드
   */
  private void callStateStopElevator() {

    // 승객 진행방향으로 가고있거나 멈춰있는 엘리베이터를 오게 함
    waitingPersons.forEach(p -> {

      boolean notCallElevator = true;
      if (p.getPurpose() == State.DOWN) {
        // 승객이 내려가려고 하는데 승객의 층보다 위에서 내려오고 있는 엘리베이터가 없으면
        notCallElevator = elevators.stream()
                .filter(e -> e.getFloor() > p.getFromFloor())
                .filter(e -> e.getState() == State.DOWN)
                .findFirst()
                .isPresent();
      } else {
        // 승객이 올라가려고 하는데 승객의 층보다 아래에서 올라오고 있는 엘리베이터가 없으면
        notCallElevator = elevators.stream()
                .filter(e -> e.getFloor() < p.getFromFloor())
                .filter(e -> e.getState() == State.UP)
                .findFirst()
                .isPresent();
      }

      if (!notCallElevator) { //엘리베이터를 불러야 하는 상황

        elevators.stream()
                .filter(el -> el.getState() == State.STOP)
                // 승객과 가장 가까운 엘리베이터를 1순위로
                .sorted((e1, e2) -> Double.compare(Math.abs(e1.getFloor() - p.getFromFloor()),
                        Math.abs(e2.getFloor() - p.getFromFloor())))
                .findFirst()
                .ifPresent(el -> { // 멈춰있는 엘리베이터가 존재하고
                  if (el.getFloor() < p.getFromFloor()) { //엘리베이터의 층이 승객보다 아래면
                    el.setState(State.UP);
                  } else {
                    el.setState(State.DOWN);
                  }
                });
      }
    });
  }
  /*
   * 엘리베이터를 움직이고 승객을 태우는 메소드
   */
  private void elevatorMovingAndAddPerson() {

    elevators.forEach(elevator -> {

      // 엘리베이터 이동
      double floor = elevator.moving();

      // 엘리베이터가 오면 승객은 탑승
      waitingPersons.stream()
                    .filter(p -> p.getFromFloor() == floor)
                    .filter(p -> p.getPurpose() == elevator.getState()
                            || elevator.getState() == State.STOP)
                    .forEach(p -> {
                              if(p.getElevator() == null) {
                                elevator.addPerson(p);
                                p.setElevator(elevator);
                              }
                            });
    });

    // 엘리베이터에 탄 승객은 대기 목록에서 제거
    waitingPersons.removeIf(p -> p.getElevator() != null);

  }
  /*
   * 엘리베이터 상태 콘솔 출력
   */
  private void showState() {
    // 화면 지우기
    for (int i = 0; i < 50; ++i) System.out.println();

    // 각 정보들 출력
    System.out.println("==============================================================");
    elevators.forEach(e -> {
      System.out.println(e.getName());
      System.out.println("Floor : " + e.getFloor() / 10);
      System.out.println("Persons : " + e.getPersons().size());
      System.out.println("State : " + e.getState().toString());
      System.out.println("\t" + e.getPersons());
      System.out.println();
    });
    System.out.println("==============================================================");

    System.out.println("Waiting Persons : ");
    waitingPersons.forEach(System.out::println);
  }

  /**
   * 빌더패턴으로 엘리베이터들이 포함된 상태에서만 생성할 수 있도록 함
   */
  public static class Builder {
    private List<Elevator> elevators = new ArrayList<>();

    /**
     * 엘리베이터 추가 메소드
     *
     * @param e the e
     * @return the builder
     */
    public Builder addElevator(Elevator e) {
      elevators.add(e);
      return this;
    }

    /**
     * 추가된 엘리베이터들을 이용해 엔진을 만드는 메소드
     *
     * @return the elevator engine
     */
    public TempTemp build() {
      if (elevators.size() > 0) {
        return new TempTemp(this.elevators);
      } else {
        throw new IllegalStateException("엘리베이터의 수가 너무 적습니다");
      }
    }

  }
}
