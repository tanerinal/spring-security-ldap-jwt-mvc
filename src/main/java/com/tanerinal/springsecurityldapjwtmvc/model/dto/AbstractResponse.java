package com.tanerinal.springsecurityldapjwtmvc.model.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@SuperBuilder
public abstract class AbstractResponse implements Serializable {
    private static final long serialVersionUID = -6332381530908723437L;

    private int status;
    private String message;
}
