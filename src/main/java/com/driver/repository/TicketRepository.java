package com.driver.repository;

import com.driver.model.Station;
import com.driver.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket,Integer> {

    List<Ticket> findByTrainIdAndFromStation(int trainId, Station fromStation);

    List<Ticket> findByTrainId(int trainId);
}
