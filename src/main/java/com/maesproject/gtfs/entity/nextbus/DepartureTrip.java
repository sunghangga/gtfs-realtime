package com.maesproject.gtfs.entity.nextbus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DepartureTrip {
    private String tripHeadSign;
    private String nextInfo;
}
