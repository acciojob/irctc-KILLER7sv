package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        Optional<Train> optionalTrain = trainRepository.findById(bookTicketEntryDto.getTrainId());
//        if(!optionalTrain.isPresent()){
//            throw new RuntimeException("Train doesn't exists");
//        }
        Optional<Passenger> optionalBookingPerson = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
//        if(!optionalBookingPerson.isPresent()){
//            throw new RuntimeException("Passenger doesn't exists");
//        }
        Passenger bookingPerson = optionalBookingPerson.get();
        Train train = optionalTrain.get();

        List<Ticket> tickets = train.getBookedTickets();
        int consumeSeats = 0;
        for(Ticket t : tickets){
            consumeSeats+=t.getPassengersList().size();
        }
        int leftSeats = train.getNoOfSeats()-consumeSeats;
        if(leftSeats < bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }
        List<Passenger> passengers = new ArrayList<>();
        //get all passengers
        for(int passengerId : bookTicketEntryDto.getPassengerIds()){
            Optional<Passenger> p = passengerRepository.findById(passengerId);
            if(!p.isPresent()){
                throw new RuntimeException("Passenger doesn't exists");
            }
            passengers.add(p.get());
        }
        Ticket ticket = new Ticket();
        ticket.setPassengersList(passengers);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());

        int totalFare=0;
        String route = train.getRoute();
        String[] stations = route.split(",");
        int fromStation = -1;
        int toStation = -1;
        for(int i=0;i<stations.length;i++){
            if(bookTicketEntryDto.getFromStation().toString().equals(stations[i])){
                fromStation = i;
            }
            if(bookTicketEntryDto.getToStation().toString().equals(stations[i])){
                toStation = i;
            }
        }
        if(fromStation==-1 || toStation==-1 || toStation-fromStation<=0){
            throw new Exception("Invalid stations");
        }
        int travelCostPerPassenger = (toStation-fromStation)*300;
        totalFare = travelCostPerPassenger*bookTicketEntryDto.getNoOfSeats();
        ticket.setTotalFare(totalFare);
        ticket.setTrain(train);

        Ticket savedTicket = ticketRepository.save(ticket);
        bookingPerson.getBookedTickets().add(savedTicket);
        passengerRepository.save(bookingPerson);
        train.getBookedTickets().add(savedTicket);
        trainRepository.save(train);

        return savedTicket.getTicketId();

    }
}
