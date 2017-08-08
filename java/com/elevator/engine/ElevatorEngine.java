package com.elevator.engine;

import com.elevator.enums.State;
import com.elevator.model.Elevator;
import com.elevator.model.Person;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author gusdn
 * @since 2016-11-03
 */
@Data
public class ElevatorEngine {

	public static final int MAX_FLOOR = 100;	 // 최대 층수-10층 
	public static final int MIN_FLOOR = 10;		 // 최저 층수-1층

	private List<Elevator> elevators;		// 엘레베이터 집합 
	private List<Person> waitingPersons;	// 기다리고 있는 사람들  
	private ExecutorService executorService;

	// Constructor. 
	private ElevatorEngine(List<Elevator> elevators) {
		this.elevators = elevators;
		this.waitingPersons = new ArrayList<>();
		this.executorService = Executors.newSingleThreadExecutor();

		// 엔진 가동
		/*******/
		run();
		/*******/
	}

	/*
	 * 승객 대기
	 * 새로운 사람 추가. 
	 */
	public void newPerson(Person person) {
		this.waitingPersons.add(person);
	}

	/*
	 * 실제 기동 처리 메소드
	 * 서버상에서 엘레베이터의 속도를 조정할 수있다.  
	 */
	private void run() {
		executorService.submit(() -> {
			// 0.1초마다 갱신
			while (true) {
				EmptyElevatorStop();
				elevatorMovingAndAddPerson();
				callStateStopElevator();
				
				// 상태 출력
				/*
				 * RandomInput 버튼 테스트를 위해 잠시 막아둠. 
				 */
				//showState();
			
				TimeUnit.MILLISECONDS.sleep(1000);
								
			}
		});
	}

	/*	 
	 * 엘리베이터가 움직일 필요가 없으면 멈추도록 함	 
	 */
	private void EmptyElevatorStop() {
		if (waitingPersons.size() == 0) {
			// 엘레베이터 안에 탄 사람이 없고 
			elevators.stream().filter(e -> e.getPersons().size() == 0)
			// 엘리베이터가 멈춰있지 않으며
			.filter(e -> e.getState() != State.STOP)
			// 제대로된 층에 있다면
			.filter(e -> e.getFloor() % 10 == 0) 
			// 엘리베이터를 멈춤
			.forEach(e -> e.setState(State.STOP)); 
		}
	}

	/*
	 * 멈춰있는 엘리베이터를 오게 하는 메소드
	 */
	private void callStateStopElevator() {

		// 승객 진행방향으로 가고있거나 멈춰있는 엘리베이터를 오게 함
		waitingPersons.forEach(p -> {
			
			int from, to; 
			boolean notCallElevator = true;
			
			// Even = 0, Odd = 10
			from = p.getFromFloor()%20; 
			to = p.getToFloor()%20;	
			
			if (p.getPurpose() == State.DOWN) {
				// 승객이 내려가려고 하는데 승객의 층보다 위에서 내려오고 있는 엘리베이터가 없으면
				notCallElevator = elevators.stream()
					.filter(e -> e.getFloor() > p.getFromFloor())
					.filter(e -> e.getState() == State.DOWN)
					.findFirst().isPresent();
			} else {
				// 승객이 올라가려고 하는데 승객의 층보다 아래에서 올라오고 있는 엘리베이터가 없으면
				notCallElevator = elevators.stream()
					.filter(e -> e.getFloor() < p.getFromFloor())
					.filter(e -> e.getState() == State.UP)
					.findFirst().isPresent();
			}						
			
			if (!notCallElevator) { // 엘리베이터를 불러야 하는 상황
								
				// odd, sky bubble 제한.
				if((Math.abs(p.getFromFloor()-p.getToFloor()) == 90)){
					elevators.stream()
					.filter(el -> el.getState() == State.STOP)
					.filter(el -> el.getType() != "odd" && el.getType() != "even" && el.getType() != "normal")
				// 승객과 가장 가까운 엘리베이터를 1순위로
				.sorted((e1, e2) -> Double.compare(Math.abs(e1.getFloor() - p.getFromFloor()),
						Math.abs(e2.getFloor() - p.getFromFloor())))
				// 멈춰있는 엘리베이터가 존재하고
				.findFirst().ifPresent(el -> {
					// 일반 엘레베이터
					if (el.getFloor() < p.getFromFloor()) {
						el.setState(State.UP);
						// elevator type이 normal이라면
					} else {
						el.setState(State.DOWN);
					}
				});
				}else if(from == 0 && to == 0){// 출발층, 도착층 둘다 짝수층
					elevators.stream()
						.filter(el -> el.getState() == State.STOP)
						.filter(el -> el.getType() != "odd" && el.getType() != "sky")
					// 승객과 가장 가까운 엘리베이터를 1순위로
					.sorted((e1, e2) -> Double.compare(Math.abs(e1.getFloor() - p.getFromFloor()),
							Math.abs(e2.getFloor() - p.getFromFloor())))
					// 멈춰있는 엘리베이터가 존재하고
					.findFirst().ifPresent(el -> {
						// 일반 엘레베이터
						if (el.getFloor() < p.getFromFloor()) {
							el.setState(State.UP);
							// elevator type이 normal이라면
						} else {
							el.setState(State.DOWN);
						}
					});
					
				}else if(from == 10 && to == 10){// 출발층, 도착층 둘다 홀수
					elevators.stream()
						.filter(el -> el.getState() == State.STOP)
						.filter(el -> el.getType() != "even" && el.getType() != "sky")
					// 승객과 가장 가까운 엘리베이터를 1순위로
					.sorted((e1, e2) -> Double.compare(Math.abs(e1.getFloor() - p.getFromFloor()),
							Math.abs(e2.getFloor() - p.getFromFloor())))
					// 멈춰있는 엘리베이터가 존재하고
					.findFirst().ifPresent(el -> {
						// 일반 엘레베이터
						if (el.getFloor() < p.getFromFloor()) {
							el.setState(State.UP);
							// elevator type이 normal이라면
						} else {
							el.setState(State.DOWN);
						}
					});				
				}else{ // 출발층 또는 도착층에 홀수가 포함되어 있을때
					elevators.stream()
						.filter(el -> el.getState() == State.STOP)
						// 출발층, 도착층에 [홀 , 짝], [짝 , 홀]로 이루어졌기 때문에 
						// even, odd type elevator를 움직임에서 제외시킨다. 
						.filter(el -> el.getType() != "even" && el.getType() != "odd" && el.getType() != "sky")						
					// 승객과 가장 가까운 엘리베이터를 1순위로
					.sorted((e1, e2) -> Double.compare(Math.abs(e1.getFloor() - p.getFromFloor()),
							Math.abs(e2.getFloor() - p.getFromFloor())))
					// 멈춰있는 엘리베이터가 존재하고
					.findFirst().ifPresent(el -> {
						// 일반 엘레베이터
						if (el.getFloor() < p.getFromFloor()) {
							el.setState(State.UP);							
						} else {
							el.setState(State.DOWN);
						}
					});	
				}
				
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
			
			
			waitingPersons.stream()
				.filter(p -> p.getFromFloor() == floor)				
				.forEach(p -> {
					if (p.getElevator() == null) {
																
						if((elevator.getType() == "even") &&
								(p.getFromFloor()%20 == 0 && p.getToFloor()%20 == 0)){
							// Elevator type이 even이고 from, to 둘다 짝수인 경우 사람을 추가한다. 
							elevator.addPerson(p);
							p.setElevator(elevator);
							
						}else if((elevator.getType() == "odd") &&
								(p.getFromFloor()%20 == 10 && p.getToFloor()%20 == 10)){
							// Elevator type이 even이고 from, to 둘다 홀수인 경우 사람을 추가한다. 
							elevator.addPerson(p);
							p.setElevator(elevator);	
							
						}else if((elevator.getType() == "sky") &&
								(Math.abs(p.getFromFloor()-p.getToFloor()) == 90)){
							// Elevator type이 even이고 from, to이 1,10층 또는 10,1층인 경우 사람을 추가.  
							elevator.addPerson(p);
							p.setElevator(elevator);
							
						}else if((elevator.getType() == "normal") &&
								!(Math.abs(p.getFromFloor()-p.getToFloor()) == 90)){
							elevator.addPerson(p);
							p.setElevator(elevator);							
						}						
					}
				});	
			// 엘리베이터에 탄 승객은 대기 목록에서 제거
			waitingPersons.removeIf(p -> p.getElevator() != null);		
		});
	}

	/*
	 * 엘리베이터 상태 콘솔 출력
	 */
	private void showState() {
		// 화면 지우기
		for (int i = 0; i < 50; ++i)
			System.out.println();

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
		 * @param e
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
		public ElevatorEngine build() {
			if (elevators.size() > 0) {
				return new ElevatorEngine(this.elevators);
			} else {
				throw new IllegalStateException("엘리베이터의 수가 너무 적습니다");
			}
		}

	}
	/*
	 * 메소드에 delay를 준다.
	 */
	public void MatrixTime(int delayTime) {
		long saveTime = System.currentTimeMillis();
		long currTime = 0;

		while (currTime - saveTime < delayTime) {
			currTime = System.currentTimeMillis();
		}
	}
}
