package com.techelevator.excelsior;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale.Category;
import java.util.Scanner;

import javax.print.attribute.standard.PrinterLocation;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.mockito.internal.stubbing.answers.Returns;

import com.techelevator.excelsior.model.CategoryDAO;
import com.techelevator.excelsior.model.Reservation;
import com.techelevator.excelsior.model.ReservationDAO;
import com.techelevator.excelsior.model.Space;
import com.techelevator.excelsior.model.SpaceDAO;
import com.techelevator.excelsior.model.Venue;
import com.techelevator.excelsior.model.VenueDAO;
import com.techelevator.excelsior.model.jdbc.JDBCCategoryDAO;
import com.techelevator.excelsior.model.jdbc.JDBCRservationDAO;
import com.techelevator.excelsior.model.jdbc.JDBCSpaceDAO;
import com.techelevator.excelsior.model.jdbc.JDBCVenueDAO;
import com.techelevator.excelsior.view.Menu;
import com.techelevator.excelsior.view.SpaceReservation;

public class ExcelsiorCLI {
	
	private final static String MAIN_MENU_DISPLAY_ITEMS = "1";
	private final static String MAIN_MENU_QUIT = "Q";
	private final static String[] MAIN_MENU = { "List Venues", "Quit" };
	
	private final static String ASK_NEXT_STEP = "What would you like to do next?";
	
	private final static String VIEW_SPACES = "1";
	private final static String SEARCH_FOR_RESERVATION = "2";
	private final static String VENUE_MENU_QUIT = "R";
	private final static String[] VENUE_MENU = { "View Spaces","Search for Reservation", "Return to Previous Screen" };
	
	private final static String[] SPACES_MENU = { "Reserve a Space","Return to Previous Screen" };
	
	private final static String RETURN_TO_PREVIOUS_MENU = "R";

	private VenueDAO venueDAO;
	private SpaceDAO spaceDAO;
	private ReservationDAO reservationDAO;
	private CategoryDAO categoryDAO;
	private Menu menu;
	

	public static void main(String[] args) {
			
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl("jdbc:postgresql://localhost:5432/excelsior_venues");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres1");

		ExcelsiorCLI application = new ExcelsiorCLI(dataSource);
		application.run();
	}

	public ExcelsiorCLI(DataSource datasource) {
		// create your DAOs here
		venueDAO = new JDBCVenueDAO(datasource);
		spaceDAO = new JDBCSpaceDAO(datasource);
		reservationDAO = new JDBCRservationDAO(datasource);
		categoryDAO=new JDBCCategoryDAO(datasource);
	
	}
	
	public void run() {
		
		menu=new Menu();
		boolean running = true;
		while (running) {	
			printMainMenu();
			
			String choice = menu.getChoiceFromMenu(MAIN_MENU);	
			
			switch (choice) {
				case MAIN_MENU_DISPLAY_ITEMS://1
					viewVenues();	
					break;
				case MAIN_MENU_QUIT://2
					break;		   
			}		
		}		
	}

	private void viewVenues() {
		
		List<Venue> allVenues = venueDAO.getAllVenues();
		
		listVenues(allVenues);
		
		while (true) {
			
			String userChoice = menu.getUserInput("Please select the venue for details.");

			if(userChoice.equalsIgnoreCase(RETURN_TO_PREVIOUS_MENU))
				break;
			
			viewVenueDetails(allVenues, userChoice);
					
		}
	}
	

	private void listVenues(List<Venue> venues) {	

		System.out.println();

		if(venues.size() > 0) {
			
			for (int i = 0; i < venues.size(); i++) {
				
				System.out.printf("%1s) %-20s%n", (i + 1), venues.get(i).getName());
			}
			System.out.printf("%1s) %-20s%n", ("R"), "Returns to Previous Screen");
			} else {
			System.out.println("\n*** No results ***");
		}
	}


	private void viewVenueDetails(List<Venue> venues,String userChoice) {	
		
			int userChoiceInt=Integer.parseInt(userChoice)-1;  
			String venueName = venues.get(userChoiceInt).getName();
			System.out.println("-----------------------------");
			System.out.println("Below is the venue details:");
			System.out.println();
			System.out.println("Veneu Name: " +venueName);
			
	
			System.out.println("Location: " + venues.get(userChoiceInt).getCityName()+" "+venues.get(userChoiceInt).getStateAbbrev()
					);
			System.out.printf(
					"Categories: "
					);
			viewCatgories(venueName);
			

			System.out.println();
			System.out.println("Below is the description:");
			System.out.println(venues.get(userChoiceInt).getDescription());
			System.out.println();
	
			boolean running = true;
			while (running) {
				
				System.out.println("--------------");
				System.out.println(ASK_NEXT_STEP);
				String choice = menu.getChoiceFromMenu(VENUE_MENU);
				if(userChoice.equalsIgnoreCase(RETURN_TO_PREVIOUS_MENU))
					break;
				switch (choice) {
					case VIEW_SPACES://1
						viewSpaces(venueName);	
						spaceMenu(venueName);
						break;
					case SEARCH_FOR_RESERVATION://1
						//viewSpaces(venueName);	
						break;
					case VENUE_MENU_QUIT://2
						break;
				}					
			}		
			
		
	}
	
	private void viewCatgories(String venueName) {
		
		List<com.techelevator.excelsior.model.Category> allCategories = categoryDAO.getAllCategories(venueName);
		for(int i=0;i<allCategories.size();i++){
			
			System.out.print("/ "+allCategories.get(i).getCategoryName());
			}
	}
	
	private void viewSpaces(String venueName) {
		
		List<Space> allSpaces = spaceDAO.getAllSpaces(venueName);
		
		
		System.out.println();
		System.out.printf("#%1s %-25s %-10s %-10s %-10s %-10s%n", 
				" ",
				"Name",
				"Open",
				"Close",
				"Daily Rate",
				"Max. Occupancy");
				
		if(allSpaces.size() > 0) {
	
			
			for (int i = 0; i < allSpaces.size(); i++) {
				if (allSpaces.get(i).getOpen_from()==0) {
					System.out.printf("#%1s %-25s %-10s %-10s $%-10s %-10s%n", 
							(i + 1),
							allSpaces.get(i).getName(),
							" ",
							" ",
							allSpaces.get(i).getDaily_rate(),
							Integer.toString(allSpaces.get(i).getMax_occupancy())
							);
				} else  {
					System.out.printf("#%1s %-25s %-10s %-10s $%-10s %-10s%n", 
							(i + 1), 
							allSpaces.get(i).getName(),
							Month.of(allSpaces.get(i).getOpen_from()).name(),
							Month.of(allSpaces.get(i).getOpen_to()).name(),
							allSpaces.get(i).getDaily_rate(),
							Integer.toString(allSpaces.get(i).getMax_occupancy())
							);		
				}
				
				}
			} else {
			System.out.println("\n*** No results ***");
		}
	
	}
	
	private void spaceMenu(String venueName) {
		
		SpaceReservation spaceReservation = new SpaceReservation();
		
		List<Reservation> reservationSpaces= reservationDAO.getAllReservationsForVenue(venueName);
		
		
		List<Space> allSpaces = spaceDAO.getAllSpaces(venueName);
	
		while (true) {
			System.out.println(ASK_NEXT_STEP);
			String choice = menu.getChoiceFromMenu(SPACES_MENU);

			if(choice.equalsIgnoreCase(RETURN_TO_PREVIOUS_MENU))
				break;
			System.out.println("The date format has to be as example: MM/DD/YYYY!");
			String userChoiceStartDate = menu.getUserInput("When do you need the space?>>>");
			String userChoiceDays = menu.getUserInput("How many days will you need the space?");
			String userChoiceAttendance = menu.getUserInput("How many people will be in attendance?>>>");
			spaceReservation.reserveSpace( reservationSpaces,
					allSpaces,
					venueName,
					userChoiceStartDate,
					userChoiceDays,
					userChoiceAttendance);

			break;

		}
	}
	
	public void printMainMenu(){
		System.out.println("Main Menu");
		System.out.println("--------------");
		System.out.println("What would you like to do?");
	}
	

	
}
