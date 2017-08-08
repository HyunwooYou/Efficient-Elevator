package com.elevator.model;


import groovy.transform.ToString;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ToString
public class ElevInfo {
	
	private String elev1; // Left elevator info. 
	private String elev2; // Mid elevator info.
	private String elev3; // Right elevator info.
	private String elev4; // Even elevator info.
	private String elev5; // Odd elevator info.	
}