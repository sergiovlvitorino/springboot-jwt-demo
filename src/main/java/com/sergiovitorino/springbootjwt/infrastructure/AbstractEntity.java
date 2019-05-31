package com.sergiovitorino.springbootjwt.infrastructure;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Calendar;
import java.util.UUID;

@MappedSuperclass
@Data
public abstract class AbstractEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@Type(type = "uuid-binary")
	private UUID userIdCreatedAt;
	@Type(type = "uuid-binary")
	private UUID userIdUpdatedAt;
	@Type(type = "uuid-binary")
	private UUID userIdDisabledAt;
	@NotNull
	private Calendar dateCreatedAt;
	private Calendar dateUpdatedAt;
	private Calendar dateDisabledAt;

}
