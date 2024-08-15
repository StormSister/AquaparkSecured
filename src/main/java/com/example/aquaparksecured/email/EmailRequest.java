package com.example.aquaparksecured.email;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class EmailRequest {
    private String to;
    private String subject;
    private String body;

}

