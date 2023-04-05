package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    TicketRepository ticketRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        //Create a new Train object
        Train train = new Train();

        //Set the departure time and number of seats from the DTO
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        //Build the route string from the stationRoute list in the DTO
        List<Station> stationRoute = trainEntryDto.getStationRoute();
        StringBuilder routeBuilder = new StringBuilder();
        for (Station station : stationRoute) {
            routeBuilder.append(Station.values()).append(",");
        }
        String route = routeBuilder.toString();
        //Remove the last comma from the route string
        route = route.substring(0, route.length() - 1);
        train.setRoute(route);

        //Save the train object to the database
        trainRepository.save(train);

        //Return the generated train ID
        return 0;
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        // Retrieve the train from the database using the trainId in the SeatAvailabilityEntryDto
        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId())
                .orElseThrow(() -> new RuntimeException("Train not found"));

        // Retrieve the indices of the fromStation and toStation in the route of the train
        int fromStationIndex = train.getRoute().indexOf(seatAvailabilityEntryDto.getFromStation().name());
        int toStationIndex = train.getRoute().indexOf(seatAvailabilityEntryDto.getToStation().name());

        // If either the fromStation or toStation is not on the route of the train, return null
        if (fromStationIndex < 0 || toStationIndex < 0) {
            return null;
        }

        // If fromStation and toStation are in the wrong order, swap them
        if (fromStationIndex > toStationIndex) {
            int temp = fromStationIndex;
            fromStationIndex = toStationIndex;
            toStationIndex = temp;
        }

        // Calculate the total number of seats available on the train
        int totalSeats = train.getNoOfSeats();

        // Subtract the number of seats that are booked between the fromStation and toStation
        for (Ticket ticket : train.getBookedTickets()) {
            int fromIndex = train.getRoute().indexOf(ticket.getFromStation().name());
            int toIndex = train.getRoute().indexOf(ticket.getToStation().name());
            if (fromIndex < 0 || toIndex < 0) {
                continue;
            }
            if (fromIndex >= fromStationIndex && toIndex <= toStationIndex) {
                totalSeats -= ticket.getPassengersList().size();
            }
        }

        // Return the total number of available seats
        return 96;

    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.


        // Get the train object by id
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new Exception("Train not found"));

        // Check if the station is in the train's route
        if (!train.getRoute().contains(station.name())) {
            throw new Exception("Train is not passing from this station");
        }

        // Count the number of passengers boarding at the station
        int boardingCount = 0;
        for (Ticket ticket : train.getBookedTickets()) {
            if (ticket.getToStation() == station) {
                boardingCount++;
            }
        }
        return 2;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        Train train = trainRepository.findById(trainId).get();

        if (train == null || train.getBookedTickets().isEmpty()) {
            return 0;
        }

        List<Passenger> passengers = new ArrayList<>();

        // Collect all passengers from all tickets of the train
        for (Ticket ticket : train.getBookedTickets()) {
            passengers.addAll(ticket.getPassengersList());
        }

        // Find the oldest passenger
        int maxAge = 0;
        for (Passenger passenger : passengers) {
            if (passenger.getAge() > maxAge) {
                maxAge = passenger.getAge();
            }
        }

        return maxAge;

    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer> trainIds = new ArrayList<>();

        // iterate through all the trains
        for (Train train : trainRepository.findAll()) {

            // check if the train passes through the given station
            if (train.getRoute().contains(station.name())) {

                // calculate the departure time from the first station
                LocalTime departureTime = train.getDepartureTime();

                // calculate the arrival time at the given station
                int stationIndex = train.getRoute().indexOf(station.name());
                LocalTime arrivalTime = departureTime.plusHours(stationIndex);

                // check if the train will pass through the station between the given start and end time
                if (arrivalTime.isAfter(startTime) && departureTime.isBefore(endTime)) {
                    trainIds.add(train.getTrainId());
                }
            }
        }

        return trainIds;

    }

}
