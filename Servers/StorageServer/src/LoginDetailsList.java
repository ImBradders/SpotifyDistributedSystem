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
    private List<LoginDetails> loginDetails;

    private LoginDetailsList() {
        loginDetails = new ArrayList<LoginDetails>();
    }

    public static LoginDetailsList getInstance() {
        if (instance == null) {
            instance = new LoginDetailsList();
        }
        return instance;
    }

    public synchronized boolean userExists(String username) {
        for (LoginDetails user : loginDetails) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }

        return false;
    }

    public synchronized boolean verifyUser(LoginDetails userToVerify) {
        for (LoginDetails user : loginDetails) {
            if (user.getUsername().equals(userToVerify.getUsername())) {
                return user.getPassword().equals(userToVerify.getPassword());
            }
        }

        return false;
    }

    public void addUser(String username, String password) {
        LoginDetails userToAdd = new LoginDetails(username, password);
        synchronized (loginDetails) {
            loginDetails.add(userToAdd);
        }
    }

    public boolean loadFromFile(String filePath) {
        File userDetailsFile = new File(filePath);
        try {
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

            return true;
        }
        catch (FileNotFoundException e) {
            System.out.println("Unable to find user details file.");
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
