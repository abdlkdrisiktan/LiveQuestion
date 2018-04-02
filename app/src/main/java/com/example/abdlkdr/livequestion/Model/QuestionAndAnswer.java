package com.example.abdlkdr.livequestion.Model;

/**
 * Created by abdlkdr on 30.03.2018.
 */

public class QuestionAndAnswer {
    private String question;
    private String answer;
    private String answer2;
    private String answer3;
    private int questionNumber;


    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswer2() {
        return answer2;
    }

    public void setAnswer2(String answer2) {
        this.answer2 = answer2;
    }

    public String getAnswer3() {
        return answer3;
    }

    public void setAnswer3(String answer3) {
        this.answer3 = answer3;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public QuestionAndAnswer() {
    }

    public QuestionAndAnswer(String question, String answer, String answer2, String answer3, int questionNumber) {
        this.question = question;
        this.answer = answer;
        this.answer2 = answer2;
        this.answer3 = answer3;
        this.questionNumber = questionNumber;
    }
}
