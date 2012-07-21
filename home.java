// Advanced Console Calculator.
// Programmed and Developed by Zana Aziz.
// Open Source.
// Use for any purpose.

import java.util.*; // Enabling the Scanner.

public class home {

	public static void main(String[] args) {
		Scanner user = new Scanner(System.in); // Making a user input.
		int firstNumber, secondNumber, answer; // Creating our variables
		int option;
		
		// Option menu.
		
		System.out.println("Choose one of the options below by typing a number.");
		System.out.println(""); // This just skips another line.
		System.out.println("1 - for addition");
		System.out.println("2 - for subtraction");
		System.out.println("3 - for multiplication");
		System.out.println("4 - for division");
		option = user.nextInt(); // Allowing the user to input.
		
		// Getting the first number.
		
		System.out.println("Enter your first number: ");
		firstNumber = user.nextInt();
		
		// Getting the second number.
		
		System.out.println("Enter your second number: ");
		secondNumber = user.nextInt();
		
		// Telling the program if it will add, subtract, multiply or divide based on what the user inputs.
		
		if(option == 1){ // If the user typed in the number 1 in the Option menu. Which stands for addition.
			answer = firstNumber + secondNumber; // Adding the two number that were stored in the variables and store that value in the answer.
			System.out.println("The answer is: " +answer); // Finally we display the answer.
		}
		
		if(option == 2){ // If the user typed in the number 2 in the Option menu. Which stands for subtraction.
			answer = firstNumber - secondNumber; // Subtracting the two number that were stored in the variables and store that value in the answer.
			System.out.println("The answer is: " +answer); // Finally we display the answer.
		}
		
		if(option == 3){ // If the user typed in the number 3 in the Option menu. Which stands for multiplication.
			answer = firstNumber * secondNumber; // Multiplying the two number that were stored in the variables and store that value in the answer.
			System.out.println("The answer is: " +answer); // Finally we display the answer.
		}
		
		if(option == 4){ // If the user typed in the number 4 in the Option menu. Which stands for division.
			answer = firstNumber / secondNumber; // Dividing the two number that were stored in the variables and store that value in the answer.
			System.out.println("The answer is: " +answer); // Finally we display the answer.
		}
	}
}
