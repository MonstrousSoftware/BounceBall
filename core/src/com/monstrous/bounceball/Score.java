package com.monstrous.bounceball;


// simple little class to keep track of the score
//
// putting this in a separate class allows for better separation between World, Main and GUI.
// Main creates a Score object and passes it to World and GUI.
// World updates the Score and GUI renders it.
// There is no need for World to know about Main or GUI

public class Score {
    private int score;

    public Score() {
        score = 0;
    }

    public void setScore(int score ) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
