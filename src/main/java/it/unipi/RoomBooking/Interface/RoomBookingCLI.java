package it.unipi.RoomBooking.Interface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Scanner;

import it.unipi.RoomBooking.Data.Interface.Person;
import it.unipi.RoomBooking.Data.Interface.Room;
import it.unipi.RoomBooking.Data.ORM.Classroom;
import it.unipi.RoomBooking.Data.ORM.ClassroomBooking;
import it.unipi.RoomBooking.Data.ORM.Laboratory;
import it.unipi.RoomBooking.Database.HibernateManager;
import it.unipi.RoomBooking.Database.ManagerDB;

public final class RoomBookingCLI {
	private static String version = " _____                         ____              _    _                       __   ___  \n"
			+ "|  __ \\                       |  _ \\            | |  (_)                     /_ | / _ \\ \n"
			+ "| |__) |___   ___  _ __ ___   | |_) | ___   ___ | | ___ _ __   __ _  __   __  | || | | |\n"
			+ "|  _  // _ \\ / _ \\| '_ ` _ \\  |  _ < / _ \\ / _ \\| |/ / | '_ \\ / _` | \\ \\ / /  | || | | |\n"
			+ "| | \\ \\ (_) | (_) | | | | | | | |_) | (_) | (_) |   <| | | | | (_| |  \\ V /   | || |_| |\n"
			+ "|_|  \\_\\___/ \\___/|_| |_| |_| |____/ \\___/ \\___/|_|\\_\\_|_| |_|\\__, |   \\_(_)  |_(_)___/ \n"
			+ "                                                               __/ |                    \n"
			+ "                                                              |___/                     \n";
		
	private static ManagerDB database;
	private static Scanner input;
	private static Person user;
	private static boolean isTeacher=true;

	private static void ident() {
		String email = null;
		boolean isValid2 = false;
		String value;
		System.out.println(version);

		try{
			database.start();
		

				while(!isValid2){
					System.out.print("Are you a teacher or a student?\n"+
									"T - Teacher \n"+
									"S - Student \n"+
									">"			
									);				
					value = input.next();
					if(value.toLowerCase().equals("t")){
						isTeacher = true;
						isValid2 = true;
					} else if(value.toLowerCase().equals("s")) {
						isTeacher = false;
						isValid2 = true;
					}
					else{
						System.out.print("Insert a correct value\n");
						isValid2 = false;
					}
				}		 
				System.out.print("\nInsert your Email > ");
				email = input.next().toString();
				user = database.authenticate(email, isTeacher);
				
			
		}catch(Exception e){ //gestire eccezione 
			System.out.println("exception occurred");
		}finally{
			database.exit();
		}
	}


	private static String getCommand() {
		String command;
		boolean isValid = false;

		System.out.println("\n1 - Book a Room." + "\n2 - Delete a booking." + "\n3 - Update a booking." + "\n4 - Close.");

		while (!isValid) {
			System.out.print("\nChoose an action > ");
			command = input.next();

			if (!command.equals("4") && !command.equals("3") && !command.equals("2") && !command.equals("1")) {
				System.out.println("\nPlease insert a valid command.");
			} else {
				isValid = true;
				return command;
			}
		}

		return null;
	}

	private static String setSchedule() {
		String requestedSchedule = null;
		boolean isValid = false;

		System.out.println("\n[M] - Morning." + "\n[A] - Afternoon.");

		while (!isValid) {
			System.out.print("\nChoose a schedule > ");
			requestedSchedule = input.next();
			requestedSchedule = requestedSchedule.toLowerCase(Locale.ENGLISH);

			if (!requestedSchedule.equals("a") && !requestedSchedule.equals("m")) {
				System.out.println("\nPlease insert a valid command.");
			} else {
				isValid = true;
			}
		}

		return requestedSchedule;
	}
	
	
	
	private static void showRooms(Collection<? extends Room> table, boolean booked) {
		if (booked) {
			if(isTeacher){
				System.out.println("\nList of your booked rooms:\n");
				System.out.printf("%-5s %-15s %-15s", "ID", "Room", "Schedule");
				System.out.println("\n=========================================");
						
				for(Room iteration : table){
				Classroom c = (Classroom) iteration;
				Collection<ClassroomBooking> collection=c.getBookedByTeacherId(user.getId());
				for(ClassroomBooking iterator: collection){
					System.out.println(iterator.toString());
				}
				}
			} else{
				System.out.println("\nList of your booked rooms:\n");
				System.out.printf("%-5s %-15s", "ID", "Room");
				System.out.println("\n=========================================");

				for (Room i : table ) 
					System.out.println(i.toStringBooked()); 
			}
			
		} else {
			System.out.println("\nList of the avaiable rooms:\n");
			System.out.printf("%-5s %-15s %-25s %-10s", "ID", "Room", "Building", "Capacity"); 
			System.out.println("\n===================================================================");
			
			for (Room i : table ) 
				   System.out.println(i.toString()); 
		}
	}

	private static void bookARoom() {
		String requestedSchedule = null;
		String requestedRoom = null;
		Collection<? extends Room> availableRooms;
		boolean isValid = false;

		try{
			database.start();
			if (isTeacher){
				requestedSchedule = setSchedule();
			}

			availableRooms = database.getAvailable(user, requestedSchedule);
			if (availableRooms.size()==0){
				System.out.println("No available rooms\n");
				return;
			}
			showRooms(availableRooms, false);

			
			while (!isValid) {
				System.out.print("\nChoose a room by ID > ");
				requestedRoom = input.next();
			
				long id;
				for (Room i : availableRooms ) {
					if(i.getId()==Long.parseLong(requestedRoom)){
						id=i.getId(); 
						isValid = true;
						System.out.println("stanza richiesta: "+ id);
						break;
					}
				}
				if (!isValid) {
					System.out.println("\nPlease insert a valid room.");
				} else {
			    	database.setBooking(user,Long.parseLong(requestedRoom), requestedSchedule);
					System.out.println("\nRoom succesfully booked.");
				}
			}
		}catch(Exception e){
			System.out.println("exception occurred");
		}finally{
			database.exit();
		}
	}

	private static void deleteBooking() {
		String requestedRoom = null;
		Collection<? extends Room> bookedRooms;
		boolean isValid = false;
		database.start();
		bookedRooms = database.getBooked(user);
		showRooms(bookedRooms, true);

		while (!isValid) {
			System.out.print("\nChoose the room you booked by ID > ");
			requestedRoom = input.next();
			System.out.println("stanza richiesta: "+ requestedRoom);

			long id;
			for (Room i : bookedRooms ) {

				//deve cerca da una aprte l id della stanza a da una l ide del classsroom per ogni classroom piu di una volta 
				System.out.println(i.getId());
				if(i.getId()==Long.parseLong(requestedRoom)){
					id=i.getId(); 
					isValid = true;
					System.out.println("stanza richiesta: "+ id);
					break;
				}
			}

			if (!isValid) {
				System.out.println("\nPlease insert a valid room.");
			} else {
				database.deleteBooking(user, Long.parseLong(requestedRoom));
				System.out.println("\nBooking succesfully delete.");
			}
		}

	}
/*
	private static void updateBooking() {
		String oldSchedule = null;
		String oldRoom = null;
		String requestedSchedule = null;
		String requestedRoom = null;
		ArrayList<ArrayList<String>> availableRooms;
		ArrayList<ArrayList<String>> bookedRooms;
		boolean isValid = false;

		bookedRooms = roomBookingDatabase.getBookedRooms(userId);
		showRooms(bookedRooms, true);

		while (!isValid) {
			System.out.print("\nChoose the room you want to change by ID > ");
			oldRoom = input.nextLine();
			System.out.print("\nChoose the schedule you want to change: ");
			oldSchedule = setSchedule();
			int numRows = bookedRooms.get(0).size() - 1;

			while (numRows >= 0) {
				if (
					bookedRooms.get(0).get(numRows).equals(oldRoom) && 
					bookedRooms.get(2).get(numRows).equals(oldSchedule)
				) {
					isValid = true;
					break;
				}
				numRows--;
			}

			if (!isValid) {
				System.out.println("\nPlease insert a valid room.");
			} else {
				isValid = false;
				System.out.print("\nChoose the new schedule: ");
				requestedSchedule = setSchedule();
				availableRooms = roomBookingDatabase.getAvailableRooms(requestedSchedule);
				showRooms(availableRooms, false);

				while (!isValid) {
					System.out.print("\nChoose a new room by ID > ");
					requestedRoom = input.nextLine();
					numRows = availableRooms.get(0).size() - 1;

					while (numRows >= 0) {
						if (availableRooms.get(0).get(numRows).equals(requestedRoom)) {
							isValid = true;
							break;
						}
						numRows--;
					}

					if (!isValid) {
						System.out.println("\nPlease insert a valid room.");
					} else {
						roomBookingDatabase.updateBooking(userId, Integer.parseInt(requestedRoom), requestedSchedule, Integer.parseInt(oldRoom), oldSchedule);
						System.out.println("\nSchedule succesfully updated.");
					}
				}

			}
		}

	}
*/
	public static void main(String[] args) {
		input = new Scanner(System.in);
		boolean terminate = false;
		String command = null;

		database = new HibernateManager();
	
		ident();

		while (!terminate) {
			command = getCommand();

			if (command == null) {
				input.close();
				terminate = true;
			}

			switch (Integer.parseInt(command)) {
			case 1:
				bookARoom();
				break;
			case 2:
				deleteBooking();
				break;
			case 3:
				//updateBooking();
				break;
			case 4:
				terminate = true;
				System.out.println("\nSee you soon!");
				break;
			}
		}

		input.close();
	}
}
