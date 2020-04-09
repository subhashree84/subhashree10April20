package com.ims.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SMSModel implements Serializable{
  private String message;
  private String phoneNumber[];
}
