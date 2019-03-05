package flashcards.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniel
 */
public class User {

    private String username, password;
    private Subject[] subjects;
    private int numSubjects;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        subjects = new Subject[10];
    }

    public Subject[] getSubjects() {
        return subjects;
    }

    public void addSubject(Subject subject) {
        subjects[numSubjects++] = subject;
    }

    public int getNumSubjects() {
        return numSubjects;
    }

    public void saveUserState() {
        new File("userStates").mkdirs();
        String filename = "userStates/" + username + ".txt";
        PrintWriter output;
        FlashCard[] flashcards;

        try {
            output = new PrintWriter(filename);

            for (int i = 0; i < numSubjects; i++) {
                output.println("Subject: " + subjects[i].getTitle());
                flashcards = subjects[i].getFlashCards();

                for (int j = 0; j < subjects[i].getNumFlashcards(); j++) {
                    output.print(flashcards[j].getQuestion() + " #ANSW "
                            + flashcards[j].getAnswer() + " #ANSW ");

                    int[] history = flashcards[j].getRightWrongHistory();
                    for (int k = 0; k < flashcards[j].getNumAttempts(); k++) {
                        output.print(history[k] + " ");
                    }
                    
                    output.println();
                }
            }

            output.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadUserState() {
        String filename = "userStates/" + username + ".txt";
        File userState = new File(filename);
        Scanner input;
        String inputLine;
        String[] questionAndAnswer;
        FlashCard flashcard;

        try {
            input = new Scanner(userState);
            while (input.hasNextLine()) {
                inputLine = input.nextLine();
                if (inputLine.startsWith("Subject: ")) {
                    inputLine = inputLine.substring("Subject: ".length()); // ignore "Subject: "
                    addSubject(new Subject(inputLine));
                } else {
                    questionAndAnswer = inputLine.split(" #ANSW ");
                    flashcard = new FlashCard(questionAndAnswer[0],
                            questionAndAnswer[1]);
                    flashcard.setRightWrongHistory(questionAndAnswer[2]);
                    subjects[numSubjects - 1].addFlashCard(flashcard);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean isDuplicateUser(String potentialUsername) {
        File userList = new File("users.txt");
        Scanner input;
        String usernameAndPassword, usernameOnly;

        if (!userList.exists()) {
            return false; // we have no users yet
        } else {
            try {
                input = new Scanner(userList);
                while (input.hasNextLine()) {
                    usernameAndPassword = input.nextLine();
                    usernameOnly = usernameAndPassword.split(" ")[0];
                    if (potentialUsername.equals(usernameOnly)) {
                        return true;
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return false; // this username was not found as duplicate
    }

    public boolean saveUsernameAndPassword() {
        File userList = new File("users.txt");
        PrintWriter output;
        String oldFileContents = "";

        if (userList.exists()) {
            oldFileContents = getOldFileContents(userList);
        }

        try {
            output = new PrintWriter(userList);
            output.print(oldFileContents);
            output.println(username + " " + password);
            output.close();
            return true; // username, password saved
        } catch (FileNotFoundException ex) {
            return false; // username, password not saved due to error
        }
    }

    public static User login(String typedUsername, String typedPassword) {
        File userList = new File("users.txt");
        String[] usernamePasswordEntry;
        User theUser;

        try {
            Scanner input = new Scanner(userList);

            while (input.hasNextLine()) {
                usernamePasswordEntry = input.nextLine().split(" ");
                if (typedUsername.equals(usernamePasswordEntry[0])) {
                    if (typedPassword.equals(usernamePasswordEntry[1])) {
                        theUser = new User(typedUsername, typedPassword);
                        theUser.loadUserState();
                        return theUser;
                    }
                }
            }

            return null; // we couldn't find username/password combo in file
        } catch (FileNotFoundException ex) {
            return null; // there are currently no registered users to login
        }
    }

    public static String getOldFileContents(File file) {
        Scanner input;
        String oldFileContents = "";

        try {
            input = new Scanner(file);

            while (input.hasNextLine()) {
                oldFileContents += input.nextLine() + "\n";
            }

            input.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }

        return oldFileContents;

    }
}
