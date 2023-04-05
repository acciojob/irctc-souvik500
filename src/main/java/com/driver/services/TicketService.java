package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
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


        // Check for validity
        if (bookTicketEntryDto.getPassengerIds() == null || bookTicketEntryDto.getPassengerIds().isEmpty()) {
            throw new IllegalArgumentException("At least one passenger ID must be provided");
        }
        if (bookTicketEntryDto.getNoOfSeats() <= 0) {
            throw new IllegalArgumentException("The number of seats must be greater than 0");
        }

        // Retrieve the train and stations from the database
        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid train ID"));
        Station fromStation = bookTicketEntryDto.getFromStation();
        Station toStation = bookTicketEntryDto.getToStation();
        if (fromStation == null || toStation == null) {
            throw new IllegalArgumentException("From station and to station must be specified");
        }

        // Check if the train stops at the requested stations
        if (!train.getBookedTickets().contains(fromStation) || !train.getBookedTickets().contains(toStation)) {
            throw new IllegalArgumentException("The train doesn't stop at the requested stations");
        }

        // Check seat availability
        SeatAvailabilityEntryDto seatAvailabilityEntryDto = new SeatAvailabilityEntryDto(
                bookTicketEntryDto.getTrainId(), bookTicketEntryDto.getFromStation(), bookTicketEntryDto.getToStation());
        int availableSeats = train.getNoOfSeats() - bookTicketEntryDto.getNoOfSeats();
        if (availableSeats < bookTicketEntryDto.getNoOfSeats()) {
            throw new Exception("Less tickets are available");
        }

        // Retrieve the passengers from the database
        List<Passenger> passengers = passengerRepository.findAllById(bookTicketEntryDto.getPassengerIds());
        if (passengers.size() != bookTicketEntryDto.getPassengerIds().size()) {
            throw new IllegalArgumentException("One or more passenger IDs are invalid");
        }

        // Book the ticket
        Ticket ticket = new Ticket();
        ticket.setTrain(train);
        ticket.setFromStation(fromStation);
        ticket.setToStation(toStation);
        ticket.setTotalFare(calculateTicketPrice(train, fromStation, toStation, bookTicketEntryDto.getNoOfSeats()));
        ticket.setPassengersList(passengers);
        ticketRepository.save(ticket);

        // Update the train and passengers
        train.getBookedTickets().add(ticket);
        trainRepository.save(train);
        for (Passenger passenger : passengers) {
            passenger.getBookedTickets().add(ticket);
            passengerRepository.save(passenger);
        }

        // Return the ticket ID
        return ticket.getTicketId();

    }

    private int calculateTicketPrice(Train train, Station fromStation, Station toStation, int noOfSeats) {
        int basePrice = 10;
        int distance = train.getBookedTickets().indexOf(toStation) - train.getBookedTickets().indexOf(fromStation);
        int distanceFactor = (int) (1.0 + (distance / 10.0));
        int occupancy = 120;
        int occupancyFactor = (int) (1.0 + (occupancy / 100.0));
        return basePrice * distanceFactor * occupancyFactor * noOfSeats;
    }

}
