import sailpoint.tools.GeneralException;
import sailpoint.object.*;
import java.util.ArrayList;
import sailpoint.api.*;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import java.text.Normalizer;

public String generateUsername(String firstName, String lastName, String employeetype, int iteration) throws GeneralException {
    // Trim and clean the first and last names (remove leading/trailing spaces, special characters)
    firstName = StringUtils.trimToNull(firstName);
    lastName = StringUtils.trimToNull(lastName);

    if (firstName != null) {
        // Normalize the first name to remove accents or diacritical marks and clean up any non-alphanumeric characters
        firstName = Normalizer.normalize(firstName, Normalizer.Form.NFD)
                      .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        firstName = firstName.replaceAll("[^a-zA-Z0-9]", "");
    }

    if (lastName != null) {
        // Normalize the last name similarly
        lastName = Normalizer.normalize(lastName, Normalizer.Form.NFD)
                     .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        lastName = lastName.replaceAll("[^a-zA-Z0-9]", "");
    }

    // Check if either first name or last name is missing, and log the exit if so
    if ((firstName == null) || (lastName == null)) {
        log.debug("AD Create User Name | Exit from generateUsername method. No last name and/or first name for user.");
        return null;
    }

    // Initialize the base SAMAccountName based on employee type
    String baseSAMAccountName = null;
    if (employeetype != null && (employeetype.equalsIgnoreCase("Contractor") ||
                                 employeetype.equalsIgnoreCase("Employee") ||
                                 employeetype.equalsIgnoreCase("Director/Advisor") || employeetype.equalsIgnoreCase("Internal Vendor"))) {
        // For these all employee types, base the username on the first letter of the first name and the last three letters of the last name
        baseSAMAccountName = "rad" + firstName.charAt(0) + lastName.substring(Math.max(lastName.length() - 3, 0));
    } else {
        log.error("AD Create User Name | Unsupported employee type: " + employeetype);
        throw new GeneralException("Unsupported employee type: " + employeetype);
    }

    // Convert to lowercase for consistency
    baseSAMAccountName = baseSAMAccountName.toLowerCase();

    // Log the base SAMAccountName for debugging
    log.debug("AD Create User Name | Base SAMAccountName: " + baseSAMAccountName);

    // Check if the generated username is unique
    if (isUnique(baseSAMAccountName)) {
        log.debug("AD Create User Name | Unique username generated: " + baseSAMAccountName);
        log.debug("AD Create User Name | Exit from the generateUsername method.");
        return baseSAMAccountName;
    } else {
        // If the username is not unique, try appending an iteration number to make it unique
        log.debug("AD Create User Name | Username is not unique, attempting iterations...");

        // Attempt to generate a unique username by appending iteration numbers (with leading zeros)
        for (int i = 1; i <= iteration; i++) {
            // Format the iteration number with a leading zero (e.g., 01, 02, 03, ...)
            String newUsername = baseSAMAccountName + (i < 10 ? "0" + i : Integer.toString(i));
            
            // Log each attempt
            log.debug("AD Create User Name | Attempting username: " + newUsername);
            
            // Check if the generated username with the iteration is unique
            if (isUnique(newUsername)) {
                log.debug("AD Create User Name | Unique username generated: " + newUsername);
                log.debug("AD Create User Name | Exit from the generateUsername method.");
                return newUsername;
            }
        }
    }

    // If no unique username is found after all iterations, log the failure and return null
    log.debug("AD Create User Name | Unable to generate a unique username after " + iteration + " attempts.");
    return null;
}

public boolean isUnique(String username) throws GeneralException {
    // Check if the account exists by display name and log the result
    boolean exists = idn.accountExistsByDisplayName(application.getName(), username);
    log.debug("AD Create User Name | Checking if username exists: " + username + " | Exists: " + exists);
    return !exists;
}

// Example call to the generateUsername method (with iteration count and employee type passed in)
log.debug("AD Create User Name | Calling generateUsername method.");
return generateUsername(identity.getFirstname(), identity.getLastname(), identity.getAttribute("employeeType"), 100);