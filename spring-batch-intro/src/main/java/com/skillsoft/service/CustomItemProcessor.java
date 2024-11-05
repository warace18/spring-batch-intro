package com.skillsoft.service;

import com.skillsoft.model.FlightTicket;
import org.springframework.batch.item.ItemProcessor;

public class CustomItemProcessor implements ItemProcessor<FlightTicket, FlightTicket> {

    public FlightTicket process(FlightTicket item){

        System.out.printf("Processing %s...%n", item);
        item.setRoute(item.getRoute().toUpperCase());

        return item;

    }
}
