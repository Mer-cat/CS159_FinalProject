package nlp.nbc;

import java.util.HashMap;

/**
 * Class which converts full state names and some large cities to
 * their state codes
 * 
 * @author Magali Ngouabou, Helen Paulini, Mercy Bickell
 * CS159 - Final Project
 *
 */
public class LocationFilterHelper {
	
	HashMap<String, String> states;
	HashMap<String, String> cities; 
	
	public LocationFilterHelper() {
		states = new HashMap<String, String>(); 
		cities = new HashMap<String, String>();
		populateStateHashMap();
		populateCitiesHashMap();
	}

	public void populateStateHashMap() {
		states.put("Alabama","AL"); 
		states.put("Alaska","AK"); 
		states.put("Arizona","AZ"); 
		states.put("Arkansas","AR"); 
		states.put("California","CA"); 
		states.put("Colorado","CO"); 
		states.put("Connecticut","CT"); 
		states.put("Delaware","DE"); 
		states.put("District Of Columbia","DC"); 
		states.put("Washington","DC"); 
		states.put("Florida","FL"); 
		states.put("Georgia","GA"); 
		states.put("Hawaii","HI"); 
		states.put("Idaho","ID"); 
		states.put("Illinois","IL"); 
		states.put("Indiana","IN"); 
		states.put("Iowa","IA"); 
		states.put("Kansas","KS"); 
		states.put("Kentucky","KY"); 
		states.put("Louisiana","LA"); 
		states.put("Maine","ME"); 
		states.put("Maryland","MD"); 
		states.put("Massachusetts","MA"); 
		states.put("Michigan","MI"); 
		states.put("Minnesota","MN"); 
		states.put("Mississippi","MS"); 
		states.put("Missouri","MO"); 
		states.put("Montana","MT"); 
		states.put("Nebraska","NE"); 
		states.put("Nevada","NV"); 
		states.put("New Hampshire","NH"); 
		states.put("New Jersey","NJ"); 
		states.put("New Mexico","NM"); 
		states.put("New York","NY"); 
		states.put("North Carolina","NC"); 
		states.put("North Dakota","ND"); 
		states.put("Ohio","OH"); 
		states.put("Oklahoma","OK"); 
		states.put("Oregon","OR"); 
		states.put("Pennsylvania","PA"); 
		states.put("Rhode Island","RI"); 
		states.put("South Carolina","SC"); 
		states.put("South Dakota","SD"); 
		states.put("Tennessee","TN"); 
		states.put("Texas","TX"); 
		states.put("Utah","UT"); 
		states.put("Vermont","VT"); 
		states.put("Virginia","VA"); 
		states.put("Washington","WA"); 
		states.put("West Virginia","WV"); 
		states.put("Wisconsin","WI"); 
		states.put("Wyoming","WY"); 
	}
	
	public void populateCitiesHashMap() {
		cities.put("San Francisco", "CA");
		cities.put("Brooklyn", "MA");
		cities.put("Los Angeles", "CA");
		cities.put("Chicago", "IL");
		cities.put("New York", "NY");
		cities.put("Lawrence", "KS");
		cities.put("Rotterdam", "NY");
		cities.put("Inglewood", "CA");
		cities.put("Miami", "FL");
		cities.put("Washington", "DC");
		cities.put("Houston", "TX");
		cities.put("San Diego", "CA");
		cities.put("Boston", "MA");
		cities.put("Ontario", "CA");
		cities.put("Kent", "WA");
		cities.put("New Orleans", "LA");
		cities.put("Las Vegas", "NV");
		cities.put("Atlanta", "GA");
		cities.put("Sausalito", "CA");
		cities.put("Seattle", "WA");
		cities.put("Dallas", "TX");
		cities.put("Dublin", "OH");
		cities.put("Birmingham", "AL");
		cities.put("Fresno", "CA");
		cities.put("Cincinnati", "OH");
		cities.put("Austin", "TX");
		cities.put("Boulder", "CO");
		cities.put("Mesa", "AZ");
		cities.put("St. Louis", "MO");
		cities.put("Orlando", "FL");
		cities.put("Omaha", "NE");
		cities.put("Denver", "CO");
		cities.put("Oakland", "CA");
		cities.put("Charlotte", "NC");
		cities.put("Bristol", "TN");
		cities.put("Santa Barbara", "CA");
		cities.put("Philadelphia", "PA");
		cities.put("Fresno", "CA");
		cities.put("Chesapeake", "VA");
		cities.put("Indianapolis", "IN");
		cities.put("Fort Worth", "TX");
		cities.put("Oxford", "MS");
		cities.put("El Paso", "TX");
		cities.put("Tulsa", "OK");
		cities.put("Minneapolis", "MN");
		cities.put("Virginia Beach", "VA");
		cities.put("Portland", "OR");
		cities.put("Detroit", "MI");
		cities.put("Phoenix", "AZ");
		cities.put("Milwaukee", "WI");
		cities.put("Jacksonville", "FL");
		cities.put("Pittsburgh", "PA");
		cities.put("Berkeley", "CA");
		cities.put("Manhattan", "NY");
		cities.put("Albuquerque", "NM");
		cities.put("Beverly Hills", "CA");
		cities.put("Palo Alto", "CA");
		cities.put("Sacramento", "CA");
		cities.put("Cleveland", "OH");
		cities.put("Columbus", "OH");
		cities.put("Tacoma", "WA");
		cities.put("Salt Lake City", "UT");
		cities.put("Baltimore", "MD");
		cities.put("Oklahoma City", "OK");	
		cities.put("Kansas City", "MO");
		cities.put("Little Rock", "AK");
		cities.put("York", "PA");
		cities.put("Long Beach", "CA");
		cities.put("Saratoga", "CA");
		cities.put("Tampa", "FL");
		cities.put("Hollywood", "CA");
		cities.put("West Covina", "CA");
		cities.put("Manchester", "NH");
		cities.put("Chattanooga", "TN");
		cities.put("Nederland", "TX");
		cities.put("Chapel Hill", "NC");
		cities.put("Glasgow", "KY");
		cities.put("Miami Beach", "FL");
		cities.put("Arlington", "VA");
		cities.put("Ann Arbor", "MI");
		cities.put("Trinidad", "CA");
		cities.put("Santa Monica", "CA");
		cities.put("Iowa City", "IA");
		cities.put("Belfast", "ME");
		cities.put("Ottawa", "IL");
		cities.put("Belgrade", "MT");
		cities.put("Leeds", "AL");
		cities.put("Wichita", "KS");
		cities.put("Scranton", "PA");
		cities.put("Cambridge", "MA");
		cities.put("Colorado Springs", "CO");
		cities.put("Sheffield", "AL");
		}
	
	public HashMap<String, String> getStateHashMap() {
		return states;
	}
	
	public HashMap<String, String> getCitiesHashMap() {
		return cities;
	}
}
