package com.example.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.elevator.engine.ElevatorEngine;
import com.elevator.enums.State;
import com.elevator.model.Elevator;
import com.elevator.model.Mode;
import com.elevator.model.Person;
import com.elevator.model.UserInput;
import com.elevator.model.ElevInfo;

@Controller
@RequestMapping("elevator")
public class ElevatorController{

	ArrayList<Elevator> elevList = null; // 엘레베이터 리스트 (left, mid, right, Even, Odd) 엘레베이터.
	ElevatorEngine elevatorEngine = null; // 엘레베이터 엔진 초기화.

	/*
	 * Main page와 매핑해준다.
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String index() {		
		
		elevList = new ArrayList<Elevator>();
		elevatorEngine = new ElevatorEngine.Builder()
				.addElevator(new Elevator("Bubble1", 1, "normal")) // Normal	 
				.addElevator(new Elevator("Bubble2", 5, "normal")) // Normal
				
				.addElevator(new Elevator("Sky"  , 10, "sky"))	// Sky bubble		
				.addElevator(new Elevator("Even bubble", 2, "even")) // Even bubble
				.addElevator(new Elevator("Odd bubble", 7, "odd")) // Odd bubble
				.build();
		
		return "elevator/index";
	}

	/*
	 * 다수의 샘플 데이터들을 input값으로 지정해서 물방울들이 움직이게 한다. 
	 */
	@RequestMapping(value = "testing", method = RequestMethod.GET)
	public String testing() {
		
		// 샘플데이터 설정.
		setSample();

		return "elevator/index";
	}

	/*
	 * mode값의 상태를 파악
	 */
	@ResponseBody
	@RequestMapping(value = "mode",  method = RequestMethod.POST)
	public Mode confirmMode(@RequestBody Mode modeForm, Model model){

		String mode = modeForm.getElevMode();
				
		if(mode.equals("1")){
			//System.out.println("Normal mode.");
		}else if(mode.equals("2")){
			// Bubble1 조정
			if((elevList.get(0).getPersons().size() == 0) && // 승객이 없고
					(elevList.get(0).getState() == State.STOP) &&
			   (elevList.get(0).getFloor() != 10)){ // 10층이 아닐 경우.
				elevList.get(0).setFloor((float) (elevList.get(0).getFloor() - 10));// elevator 층수를 하나 내린다.
			}
			// Bubble2 조정
			if((elevList.get(1).getPersons().size() == 0) && // 승객이 없고
					(elevList.get(0).getState() == State.STOP) &&
			   (elevList.get(1).getFloor() != 10)){ // 10층이 아닐 경우.
				elevList.get(1).setFloor((float) (elevList.get(1).getFloor() - 10));// elevator 층수를 하나 내린다.
			}		
		}else if(mode.equals("3")){
			
			// Bubble1 조정 
			if((elevList.get(0).getPersons().size() == 0) && // 승객이 없고
				(elevList.get(0).getState() == State.STOP) &&	
			   (elevList.get(0).getFloor() != 50)){ // 5층이 아닐 경우.
				
				if(elevList.get(0).getFloor() < 50){
					elevList.get(0).setFloor((float) (elevList.get(0).getFloor() + 10));
				}else if(elevList.get(0).getFloor() > 50){
					elevList.get(0).setFloor((float) (elevList.get(0).getFloor() - 10));	
				}				
			}
			// Bubble2 조정 
			if((elevList.get(1).getPersons().size() == 0) && // 승객이 없고
				(elevList.get(0).getState() == State.STOP) &&
			   (elevList.get(1).getFloor() != 50)){ // 5층이 아닐 경우.
								
				if(elevList.get(1).getFloor() < 50){
					elevList.get(1).setFloor((float) (elevList.get(1).getFloor() + 10));
				}else if(elevList.get(1).getFloor() > 50){
					elevList.get(1).setFloor((float) (elevList.get(1).getFloor() - 10));	
				}
			}		
			
		}else{
			System.out.println("존재하지 않는 mode.");
		}		
		
		return modeForm;
	}
		
	/*
	 * Wait persons list를 index에 전송한다. 
	 */
	@ResponseBody
	@RequestMapping(value = "wait", method = RequestMethod.POST)
	public Object waitPersons(@RequestParam Map<String, Object> map) {				
		
		List<Person> persons = elevatorEngine.getWaitingPersons();
		
		Map<String, Object> retVal = new HashMap<String, Object>(); 
		
		retVal.put("persons", persons);
		retVal.put("code", "OK");
		
		return retVal;
	}
	/*
	 * 엘레베이터 사용자 한명을 출발층과 도착층을 랜덤하게 생성하여 만들어 준다. 
	 */
	@RequestMapping(value = "random", method = RequestMethod.POST)
	public void randomInput(){		
		
		int randomFrom; // random 출발층  
		int randomTo; // random 도착층 
		
		Random random = new Random();		
		
		do{
			randomFrom = random.nextInt(10);
			randomTo = random.nextInt(10);			
			
			if( (randomFrom != 0) && 
				(randomTo != 0) && 
				(randomFrom != randomTo)){
				break; 
			}
		}while(true);
					
		
		// random Person을 생성한다. 
		elevatorEngine.newPerson(new Person(randomFrom, randomTo));			
	}
	
	/*
	 * 사용자로부터 출발층, 도착층을 입력받는다. 
	 */	
	@ResponseBody
	@RequestMapping(value = "userFromTo", method = RequestMethod.POST)
	public UserInput userInput(@RequestBody UserInput form, Model model){
		
		int from = Integer.parseInt(form.getFrom());
		int to = Integer.parseInt(form.getTo());		
		
		System.out.println("from : " + from + " " + "to : " + to);
		
		elevatorEngine.newPerson(new Person(from, to));
		
		return form;
	}
	
	/*
	 * 클라이언트가 보내는 요청에 대한 응답 메세지를 준다.
	 * 
	 * @RequestBody : 서버에서 동작하는 엘레베이터 정보.
	 */
 	@ResponseBody
	@RequestMapping(value = "check", method = RequestMethod.POST)
	public ElevInfo getJsonByMap(@RequestBody ElevInfo form, Model model) {				
		
		elevInfoToClient(form);				
		
		return form;
	}

	/*
	 * ElevaInfo form 안에 정보를 저장한다.
	 */
	public void elevInfoToClient(ElevInfo form) {
		elevList = (ArrayList<Elevator>) elevatorEngine.getElevators();	
		
		// ElevLeft info.
		form.setElev1("-Floor:-" + ((int) elevList.get(0).getFloor() + "-") + "Persons:-"
				+ (elevList.get(0).getPersons().size() + "-") + "State:-"
				+ (elevList.get(0).getState().toString() + "-"));

		// ElevMid info.
		form.setElev2("-Floor:-" + ((int) elevList.get(1).getFloor() + "-") + "Persons:-"
				+ (elevList.get(1).getPersons().size() + "-") + "State:-"
				+ (elevList.get(1).getState().toString() + "-"));

		// ElevRight info.
		form.setElev3("-Floor:-" + ((int) elevList.get(2).getFloor() + "-") + "Persons:-"
				+ (elevList.get(2).getPersons().size() + "-") + "State:-"
				+ (elevList.get(2).getState().toString() + "-"));
		// ElevEven info. 
		form.setElev4("-Floor:-" + ((int) elevList.get(3).getFloor() + "-") + "Persons:-"
				+ (elevList.get(3).getPersons().size() + "-") + "State:-"
				+ (elevList.get(3).getState().toString() + "-"));
		
		// ElevOdd info.
		form.setElev5("-Floor:-" + ((int) elevList.get(4).getFloor() + "-") + "Persons:-"
				+ (elevList.get(4).getPersons().size() + "-") + "State:-"
				+ (elevList.get(4).getState().toString() + "-"));		
	}

	/*
	 * 승객을 탑승시키는 샘플데이터.
	 */
	public void setSample() {
		
		// Sky
		elevatorEngine.newPerson(new Person(1, 10));
		elevatorEngine.newPerson(new Person(10, 1));
		elevatorEngine.newPerson(new Person(10, 1));
		
		// Even
		elevatorEngine.newPerson(new Person(2, 10));
		elevatorEngine.newPerson(new Person(6, 2));
		elevatorEngine.newPerson(new Person(8, 2));
		
		// Odd
		elevatorEngine.newPerson(new Person(1, 7));
		elevatorEngine.newPerson(new Person(5, 1));
		elevatorEngine.newPerson(new Person(9, 3));
		
		// Normal
		elevatorEngine.newPerson(new Person(3, 8));
		elevatorEngine.newPerson(new Person(9, 2));
		elevatorEngine.newPerson(new Person(9, 4));
		elevatorEngine.newPerson(new Person(5, 2));
		elevatorEngine.newPerson(new Person(1, 8));
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
