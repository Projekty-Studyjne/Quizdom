package com.project.quizdom.game;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Quiz {
    public List<String> getQuestions(String category) throws FileNotFoundException {
        List<String> questions = new ArrayList<>();
        File file = new File(category);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            questions.add(scanner.nextLine());
        }
        scanner.close();
        return questions;
    }
}