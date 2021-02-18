package com.vovamisjul.dserver.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController("/tasks")
public class TaskQueueController {

    @RequestMapping(path = "/add", method = RequestMethod.PUT)
    public ResponseEntity addTask() {
        return new ResponseEntity(HttpStatus.OK);
    }
}
