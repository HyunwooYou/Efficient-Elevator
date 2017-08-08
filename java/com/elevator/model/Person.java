package com.elevator.model;

import com.elevator.enums.State;
import lombok.Data;

/**
 * @author gusdn
 * @since 2016-11-03
 */

@Data
public class Person {

	private int fromFloor;
	private int toFloor;
	private State purpose;
	private Elevator elevator;

	public Person(int from, int to) {
		this.fromFloor = from * 10;
		this.toFloor = to * 10;
		this.elevator = null;

		if (from - to < 0) {
			// 윗층으로 가려고 함
			this.purpose = State.UP;
		} else if (from - to > 0) {
			// 아래층으로 가려고 함
			this.purpose = State.DOWN;
		} else {
			throw new IllegalArgumentException("같은 층을 가려고 하는 승객이 있음");
		}

	}

	/*
	 * 출발층, 도착층을 String형식으로 보여준다. 
	 */
	public String toString() {
		return "Person [" + fromFloor + " -> " + toFloor + "]";
	}

}
