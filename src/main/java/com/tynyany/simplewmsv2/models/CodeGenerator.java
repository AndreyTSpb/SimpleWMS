package com.tynyany.simplewmsv2.models;

import lombok.Getter;

import java.util.Random;

/**
 * енератор бар-кода
 */
public class CodeGenerator {
    @Getter
    String generatedCode;

    public CodeGenerator(String prefix) {
        StringBuilder code = new StringBuilder(prefix);
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            code.append(rand.nextInt(10));
        }
        this.generatedCode =  code.toString();
    }
}
