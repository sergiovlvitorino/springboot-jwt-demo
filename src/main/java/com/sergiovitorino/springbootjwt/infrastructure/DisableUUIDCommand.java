package com.sergiovitorino.springbootjwt.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisableUUIDCommand {

    private UUID id;

}
