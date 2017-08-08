package com.elevator.model;

import com.elevator.engine.ElevatorEngine;
import com.elevator.enums.State;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * @author gusdn
 * @since 2016-11-03
 */
@Data
public class Elevator {

	private String name;
	private String type;
	private float floor;
	private State state;
	private List<Person> persons;	
	private int speed;
	Timer timer = new Timer(true); // 시간을 늦추는 메소드. 

	/*
	 * name : 엘레베이터 이름 floor : 엘레베이터 시작층
	 */
	public Elevator(String name, float floor, String type) {
		this.name = name;
		this.type = type;
		this.floor = floor * 10; // 1층을 10으로 표현 (시간적 세밀화를 위함)
		this.state = State.STOP;
		this.persons = new ArrayList<>();
		
		if(type == "normal"){
			speed = 10; 
		} //홀,짝수층의 경우에는 2층씩 이동하게 된다. 
		else if(type == "odd" || type == "even"){
			speed = 20;
		}else if(type == "sky"){ // Sky bubble에서는 9층씩 움직인다. 
			speed = 90;
		}else{// 일반, 홀,짝수층 엘레베이터가 아닌 경우. 
			System.out.println("올바른 type이 아닙니다.");
		}
	}

	/**
	 * 탄 사람의 목적 층에 맞게 이동 int로 하면 층수만 표현
	 * 엘레베이터 이동 속도를 조정하는 method 
	 * >> this.floor += speed. 
	 */
	public float moving() {

		if (persons.size() > 0 && this.floor % 10 == 0) { // 각 층에 도착하면
			
			// 내리는 사람이 있는지 판단. 
			Boolean leavingPerson = persons.stream()
					.filter(p->p.getToFloor() == this.floor)
					.findFirst().isPresent();
			
			if(leavingPerson){
				leavePersons(); // 승객 내림	
			}									
			
			
			if (!persons.stream().anyMatch(p -> p.getPurpose() == this.state)) {
				// 지금 진행 중인 방향에 가는 사람이 아무도 없으면 다음 방향 설정함
				this.state = getDirection();
			}
		}

		// 가던 방향으로 계속 감
		if (State.UP == this.state) {
			if (this.floor >= ElevatorEngine.MAX_FLOOR) // 최대층 처리
				this.setState(State.STOP);
			else
				this.floor += speed;
		} else if (State.DOWN == this.state){
			if (this.floor <= ElevatorEngine.MIN_FLOOR) // 최소층 처리
				this.setState(State.STOP);
			else
				this.floor -= speed;
		}

		return this.floor;
	}

	/*
	 * 먼저 탄 사람의 방향에 맞게 이동 시작
	 */
	private State getDirection() {
		double to = persons.stream().findFirst().map(Person::getToFloor).orElseGet(() -> -1);

		if (to == -1) { // 값이 없어서 -1.0이 되어버리면 멈춤
			return State.STOP;
		} else if (this.floor < to) {
			return State.UP;
		} else {
			return State.DOWN;
		}
	}

	/**
	 * 현재 층
	 *
	 * @return the double int 형식으로 하면 층수만 표시된다.
	 */
	public double getFloor() {
		return this.floor;
	}

	/**
	 * 엘리베이터에 사람 탑승
	 *
	 * @param person
	 *            the person
	 * @return the boolean
	 */
	public boolean addPerson(Person person) {		
		if (persons.size() < 10) {						
			// 승객이 탔을때 STOP상태로 설정한다. 
			this.setState(State.STOP);															
			this.persons.add(person);
			
			MatrixTime(2000);
			
			return true;
		} else
			return false;
	}		
	
	/*
	 * 내릴 층인 사람들 내림
	 */
	private void leavePersons() {
				
		MatrixTime(2000);
		persons.removeIf(p -> p.getToFloor() == this.floor);		
		this.setState(State.STOP);		
		
	}
	
	/*
	 * Delay를 준다.
	 */
	public void MatrixTime(int delayTime) {
		long saveTime = System.currentTimeMillis();
		long currTime = 0;

		while (currTime - saveTime < delayTime) {
			currTime = System.currentTimeMillis();
		}
	}
}
