import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to store a thread-safe list of login details.
 *
 * @author Bradley Davis
 */
public class LoginDetailsList {
    private static LoginDetailsList instance;
    private final List<LoginDetails> loginDetails;

    private LoginDetailsList() {
        loginDetails = new ArrayList<LoginDetails>();
    }

    public static LoginDetailsList getInstance() {
        if (instance == null) {
            instance = new LoginDetailsList();
        }
        return instance;
    }

    /**
     * Check to see if a given username is already in use within the system.
     *
     * @param username the username to validate
     * @return whether or not the given username is already in use in the system.
     */
    public synchronized boolean userExists(String username) {
        for (LoginDetails user : loginDetails) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines whether the user exists and if they do, whether the password that has been provided is correct or not.
     *
     * @param userToVerify the username and password which have been entered.
     * @return whether or not the user was successfully validated.
     */
    public synchronized boolean verifyUser(LoginDetails userToVerify) {
        for (LoginDetails user : loginDetails) {
            if (user.getUsername().equals(userToVerify.getUsername())) {
                return user.getPassword().equals(userToVerify.getPassword());
            }
        }

        return false;
    }

    /**
     * Allows for users to be added to the system so that new accounts can be created.
     *
     * @param username the username which the user wishes to use.
     * @param password the password which the user wishes to use.
     * @return whether the user was added or not. Will return false if a given username already exists.
     */
    public boolean addUser(String username, String password) {
        if (userExists(username)) {
            return false;
        }

        LoginDetails userToAdd = new LoginDetails(username, password);
        synchronized (loginDetails) {
            loginDetails.add(userToAdd);
        }

        return true;
    }

    /**
     * Loads the list of usernames and passwords from the file.
     *
     * @param filePath the path at which the file can be found.
     * @return whether the file could be loaded or not.
     */
    public boolean loadFromFile(String filePath) {
        File userDetailsFile = new File(filePath);
        try {
            if (userDetailsFile.exists())
            {
                FileReader fileReader = new FileReader(userDetailsFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String currentLine = bufferedReader.readLine();

                //file formatted as username, password.
                while (currentLine != null) {
                    String[] details = currentLine.split(", ");
                    addUser(details[0], details[1]);
                    currentLine = bufferedReader.readLine();
                }

                fileReader.close();
            }
            else {
                return false;
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("Unable to find user details file.");
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Write the list of user details to the file.
     *
     * @param filePath the path of the user file.
     * @return whether or not the file was written.
     */
    public boolean writeToFile(String filePath) {
        File userDetailsFile = new File(filePath);
        try {
            if (!userDetailsFile.exists()) {
                //if the login file does not exist, create it.
                try {
                    byte[] data = new byte[] {};
                    FileOutputStream outputStream = new FileOutputStream(filePath);
                    outputStream.write(data);
                    outputStream.close();
                }
                catch (IOException e) {
                    return false;
                }
            }

            FileWriter fileWriter = new FileWriter(userDetailsFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(""); //first, empty the file.

            synchronized (loginDetails) {
                for (LoginDetails detail : loginDetails) {
                    bufferedWriter.append(detail.getUsername());
                    bufferedWriter.append(", ");
                    bufferedWriter.append(detail.getPassword());
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }

            bufferedWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
