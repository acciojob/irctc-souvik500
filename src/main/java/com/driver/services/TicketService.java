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

        // Check for validity
        if (bookTicketEntryDto.getFromStation() == null || bookTicketEntryDto.getToStation() == null) {
            throw new Exception("Invalid stations");
        }
        if (bookTicketEntryDto.getNoOfSeats() <= 0) {
            throw new Exception("Number of seats should be greater than 0");
        }

        // Get the train from the repository
        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();

        // Use bookedTickets List from the TrainRepository to get bookings done against that train
        List<Ticket> bookedTickets = trainRepository.findByTrain(train.getTrainId());

        // Check if there are sufficient tickets
        int totalSeats = train.getNoOfSeats();
        int availableSeats = totalSeats - bookedTickets.size();
        if (bookTicketEntryDto.getNoOfSeats() > availableSeats) {
            throw new Exception("Less tickets are available");
        }

        // Calculate the price and other details
        int basePrice = 100;
        int totalPrice = basePrice * bookTicketEntryDto.getNoOfSeats();

        // Save the information in corresponding DB Tables
        // Create the ticket
        Ticket ticket = new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(totalPrice);

        // Create the passengers
        List<Passenger> passengers = new ArrayList<>();
        for (int passengerId : bookTicketEntryDto.getPassengerIds()) {
            Passenger passenger = passengerRepository.findByPassenger(passengerId);
//            if (passenger == null) {
//                throw new Exception("Passenger with id " + passengerId + " does not exist");
//            }
            passengers.add(passenger);
            passenger.getBookedTickets().add(ticket);
        }

        // Set the ticket's passengers
        ticket.setPassengersList(passengers);

        // Set the ticket's train
        ticket.setTrain(train);

        // Save the ticket
        ticketRepository.save(ticket);

        // Save the bookedTickets in the train Object
        List<Ticket> trainTickets = train.getBookedTickets();
        trainTickets.add(ticket);
        train.setBookedTickets(trainTickets);

        // Return the ticket id
        return ticket.getTicketId();

    }


}
