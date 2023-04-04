package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        // Check if the train exists
        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId())
                .orElseThrow(() -> new Exception("Invalid train"));

        // Check if the requested stations are valid
        String[] stations = Station.values().toString().split(",");
        int fromIndex = -1, toIndex = -1;
        for (int i = 0; i < stations.length; i++) {
            if (stations[i].equalsIgnoreCase(bookTicketEntryDto.getFromStation().toString())) {
                fromIndex = i;
            }
            if (stations[i].equalsIgnoreCase(bookTicketEntryDto.getToStation().toString())) {
                toIndex = i;
            }
        }
        if (fromIndex == -1 || toIndex == -1 || fromIndex >= toIndex) {
            throw new Exception("Invalid stations");
        }

        // Check if there are enough available tickets
        List<Ticket> bookedTickets = ticketRepository.findByTrainIdAndStation(train.getTrainId(), bookTicketEntryDto.getFromStation().toString());
        int availableTickets = train.getNoOfSeats() - bookedTickets.size();
        if (availableTickets < bookTicketEntryDto.getNoOfSeats()) {
            throw new Exception("Less tickets are available");
        }

        // Create new passenger entities and save them to the database
        List<Passenger> passengers = new ArrayList<>();
        for (int i = 0; i < bookTicketEntryDto.getNoOfSeats(); i++) {
            Passenger passenger = new Passenger();
            passenger.setName(bookTicketEntryDto.getPassengerIds().get(i).toString());
            passenger.setPassengerId(bookTicketEntryDto.getBookingPersonId());
            passengers.add(passenger);
            passengerRepository.save(passenger);
        }

        // Calculate the price of the ticket based on the fare system
        int distance = toIndex - fromIndex;
        int baseFare = 100;
        int price = (int) (baseFare * (1 + 0.1 * distance));

        // Create a new ticket entity and save it to the database
        Ticket ticket = new Ticket();
        ticket.setTrain(train);
        ticket.setPassengersList(passengers);
        ticket.setTotalFare(price);
        ticketRepository.save(ticket);

        // Update the train entity with the new booked tickets
//        for (int i = fromIndex; i < toIndex; i++) {
//            train.getBookedTickets().put(stations[i], train.getBookedTickets().getOrDefault(stations[i], 0) + bookTicketEntryDto.getNumPassengers());
//        }
        trainRepository.save(train);

        // Update the passenger entities with the booked ticket IDs
        for (Passenger passenger : passengers) {
            passenger.setBookedTickets(ticket.getTrain().getBookedTickets());
            passengerRepository.save(passenger);
        }

        // Return the ID of the newly created ticket
        return ticket.getTicketId();

    }
}
