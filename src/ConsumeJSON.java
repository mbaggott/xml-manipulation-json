import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.apache.commons.io.IOUtils;

public class ConsumeJSON {
	public static void main(String args[]) {
		
		/* Declare variables and default values */
		String apiKey = "michaelbaggottrmit";
		
		Double north = -1000.0;
		Double south = -1000.0;
		Double east = -1000.0;
		Double west = -1000.0;
		
		/* Declare and set date and decimal formats */
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setLenient(false);
		Date date = new Date();
		DecimalFormat df = new DecimalFormat("#.#");
		
		Double minMagnitude = 0.0;
		int maxResults = 10;
		
		System.out.println("**********************************************");
		System.out.println("*                                            *");
		System.out.println("*   Welcome to the Earthquake search tool!   *");
		System.out.println("*                                            *");
		System.out.println("*   Written by Michael Baggott 2016          *");
		System.out.println("**********************************************\n\n");
		
		/* Instantiate input scanner and xml parser */
		Scanner reader = new Scanner(System.in);
		String response = "";
		JSONParser parser = new JSONParser();
		
		/* Request user input for longitude/latitude bounding box */
		response = getAnswer(reader, "Would you like to enter a Longitude/Latitude bounding box? (Y/N): ");
		
		if (response.toUpperCase().equals("Y")) {
			north = Double.valueOf(df.format(getRange("Latitude", "North", reader)));
			south = Double.valueOf(df.format(getRange("Latitude", "South", reader)));
			System.out.println("You have selected a Latitude range of " + north + "N to " + south + "S\n");
			east = Double.valueOf(df.format(getRange("Longitude", "East", reader)));
			west = Double.valueOf(df.format(getRange("Longitude", "West", reader)));
			System.out.println("You have selected a Latitude range of " + north + "N to " + south + "S\n");
		}
		/* Use default values (maximum range) if user does not want to enter their own values */
		else {
			north = 179.9;
			south = -179.9;
			east = 179.9;
			west = -179.9;
		}
		
		/* Get user input for a date or use default current date if no date is input */
		date = getDate(reader, dateFormat);
		
		/* Get user input for minimum magnitude of earthquake from user */
		response = getAnswer(reader, "Would you like to enter a minimum magnitude for the earthquake?");
		if (response.toUpperCase().equals("Y")) {
			minMagnitude = Double.valueOf(df.format(getMagnitude(reader)));
			System.out.println("You have entered a magnitude of " + minMagnitude + " or above.\n");
		}
	
		/* Get user input for number of results to return to the console, or use the default of 10 */
		maxResults = getNumResults(reader);
		System.out.println("You have requested a maximum of " + maxResults + " results");

		try {
						
			/* Connect to the REST service */
			URL url = new URL(String.format("http://api.geonames.org/earthquakesJSON?username=%s&north=%s&south=%s&east=%s&west=%s&date=%s&minMagnitude=%s&maxRows=%s", apiKey, north, south, east, west, dateFormat.format(date), minMagnitude, maxResults)); 
			HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.connect();
			System.out.println(request.getResponseMessage());
			/* Execute the connection to get the output from the REST service */
			try (InputStream stream = (InputStream) request.getContent()){
				File file = new File("resources/jsonoutput.txt");
				OutputStream outputStream = new FileOutputStream(file);
				/* Copy the content input stream to the file output stream */
				IOUtils.copy(stream,  outputStream);
				outputStream.close();
				/* Parse the output stream */
				Object obj = parser.parse(new FileReader("resources/jsonoutput.txt"));
				JSONObject jsonObject = (JSONObject)obj;
				
				/* Create a new JSON object from the parsed output stream */ 
				JSONArray earthquakes = (JSONArray)jsonObject.get("earthquakes");
				
				/* Create the results if they were found */
				if (earthquakes.size() == 0) {
					System.out.println("No earthquakes found, please try adjusting the search requirements!");
				}
				else {
					/* Loop through all of the returned earhtquakes and output the details of each earthquake */
					for (int x = 0; x < earthquakes.size(); x++) {
						System.out.println("\n********** EARTHQUAKE **********");
						JSONObject earthquake = (JSONObject)earthquakes.get(x);
						System.out.println("Earthquake ID: " + earthquake.get("eqid"));
						System.out.println("Date/Time: " + earthquake.get("datetime"));
						System.out.println("Depth: " + earthquake.get("depth") + "km");
						System.out.println("Magnitude: " + earthquake.get("magnitude"));
						System.out.println("Latitude: " + earthquake.get("lat"));
						System.out.println("Longitude: " + earthquake.get("lng"));
					}
				}
			}
			catch(Exception e) {
				System.out.println("Error getting data: " + e.getMessage());
			}
		
		}
		catch (Exception e) {
			System.out.println("Error building html string: " + e.getMessage());
		}
		
			
	}
	
	/* Function to get and validate user input for various questions to the user */
	private static String getAnswer(Scanner reader, String question) {
		
		String response = "";
		
		System.out.println(question);
		
		while (!response.toUpperCase().equals("Y") || !response.toUpperCase().equals("N")) {
			response = reader.nextLine();
			if (response.toUpperCase().equals("Y") || response.toUpperCase().equals("N")) {
				break;
			}
			System.out.println("Error, please enter Y or N!");
			System.out.println(question);
		}
		
		return response;
		
	}
	
	/* Function to get and validate input from the user for the longitude/latitude bounding box */
	private static double getRange(String latLon, String direction, Scanner reader) {
		double directionValue = -1000.0;
		String response = "";
		System.out.println("Requesting details for " + latLon + "\n");
		System.out.println("What is the " + latLon + " of the " + direction + " boundary you would like to search? (Enter a value between -179.9 and 179.9): ");
		while (directionValue < 0.0 || directionValue > 179.9 ) {
			response = reader.nextLine();
			try {
				directionValue = Double.parseDouble(response);
			}
			catch (Exception e) {
				
			}
			if (directionValue >= -179.9 && directionValue <= 179.9) {
				break;
			}
			System.out.println("Error, please enter a value between -179.9 and 179.9 for " + latLon + direction + "!");
			System.out.println("What is the  " + latLon + " of the " + direction + " boundary you would like to search? (Enter a value between -179.9 and 179.9): ");
		}
		
		return directionValue;
	}
	
	/* Function to get and validate the date from the user */
	private static Date getDate(Scanner reader, DateFormat dateFormat) {
		
		boolean validDate = false;
		int year = 0;
		Date date = new Date();
		String response = "";
		
		while (validDate == false) {
			
			System.out.println("Please enter a date, the program will search earthquakes that occurred\nup to and including this date. Leave blank to search up to todays date. (yyyy-MM-dd): ");
			response = reader.nextLine();
			
			try {
				if (response.length() == 0) {
					break;
				}
				date = dateFormat.parse(response);
				DateFormat yearFormat = new SimpleDateFormat("yyyy");
				year = Integer.parseInt(yearFormat.format(date));
				if (year > 2016) {
					System.out.println("Error, the maximum year allowable is 2016!");
				}
				else {
					validDate = true;
				}
			}
			
			catch (Exception e) {
				System.out.println("Incorrect date format!");
			}
		}
		
		return date;
	}
	
	/* function to get and validate the minimum magnitude of the earthquake from the user */
	private static double getMagnitude(Scanner reader) {
	
		String response = "";
		double magnitude = -1.0;
		
		while (magnitude < 0.0 || magnitude > 20.0) {
			System.out.println("Please enter the minimum magnitude: (0.0 - 20.0): ");
			response = reader.nextLine();
			try {
				magnitude = Double.parseDouble(response);
			}
			catch (Exception e) {
				
			}
			if (magnitude >= 0.0 && magnitude <= 20.0 ) {
				break;
			}
			else {
				System.out.println("Error, incorrect format!");
			}
			
		}
		
		return magnitude;
	}
	
	/* Function to get and validate the number of results the user would like returned from the REST service */
	public static int getNumResults(Scanner reader) {
		
		String response = "";
		int results = 10;
		boolean validResponse = false;
		
		while (validResponse == false) {
			System.out.println("Please enter the maximum number of results (Leave blank for the default of the 10 most recent up to the requested date): ");
			response = reader.nextLine();
			try {
				if (response.length() == 0) {
					break;
				}
				results = Integer.parseInt(response);
				validResponse = true;
			}
			catch (Exception e) {
				System.out.println("Error, please enter a valid integer");
			}
		}
		
		return results;
	}
	
}
