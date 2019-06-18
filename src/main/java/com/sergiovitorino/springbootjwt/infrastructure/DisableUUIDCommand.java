package com.sergiovitorino.springbootjwt.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisableUUIDCommand {

    @NotNull(message = "Id not found")
    private UUID id;

}
