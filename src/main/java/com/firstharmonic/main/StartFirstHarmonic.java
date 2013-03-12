package com.firstharmonic.main;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class StartFirstHarmonic {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("firstharmonic.xml");
    }

}
