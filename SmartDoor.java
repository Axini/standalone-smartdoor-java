// Copyright 2023 Axini B.V. https://www.axini.com, see: LICENSE.txt.

// This version of the SmartDoor is the Java version of the original
// Ruby version of the Standalone Smartdoor SUT. The structure and
// implementation is completely the same.

import java.util.*;

// Implementation of the SmartDoor SUT.
public class SmartDoor {
    enum State { CLOSED, OPENED, LOCKED, OFF }

    // The implemented manufacturers
    public static final String M_AXINI                  = "Axini";
    public static final String M_BESTO                  = "Besto";
    public static final String M_LOGICA                 = "Logica";
    public static final String M_ON_TARGET              = "OnTarget";
    public static final String M_QUICKERR               = "quickerr";
    public static final String M_SMART_SOFT             = "SmartSoft";
    public static final String M_TRUSTED_TECHNOLOGIES   = "TrustedTechnologies";
    public static final String M_UNIVER_SOLUTIONS       = "univerSolutions";
    public static final String M_XTRA_SAFE              = "XtraSafe";

    public static final List<String> MANUFACTURERS = Collections.unmodifiableList(
        new ArrayList<String>() {{
            add(M_AXINI); add(M_BESTO); add(M_LOGICA); add(M_ON_TARGET);
            add(M_QUICKERR); add(M_SMART_SOFT); add(M_TRUSTED_TECHNOLOGIES);
            add(M_UNIVER_SOLUTIONS); add(M_XTRA_SAFE);
        }});

    private SmartDoorServer server;
    private String          manufacturer;
    private State           state;
    private int             passcode;
    private int             attempts;

    private boolean bug1, bug2, bug3a, bug3b, bug4, bug5, bug6a, bug6b,
                    bug7a, bug7b, bug8, bug9, bug10, bug11;

    public SmartDoor(SmartDoorServer server, String manufacturer) {
        this.server = server;
        this.manufacturer = manufacturer;
        this.state = State.CLOSED;
        this.passcode = -1;
        this.attempts = 0;

        // Phase 0: open/close good weather
        bug1 = false;   // easy: 20% chance for door to respond too late
        bug2 = false;   // easy: door cannot be closed in opened state

        // Phase 1: open/close bad weather
        bug3a = false;  // easy: door accepts  open command when opened
        bug3b = false;  // easy: door accepts close command when closed

        // Phase 2: lock/unlock good and bad weather
        bug4 = false;   // easy: door can be locked twice, overrides passcode

        // Phase 3: passcode good weather
        bug5 = false;   // easy: door can be unlocked with incorrect passcode
        bug6a = false;  // hard: door rejects   lock command with passcodes < 1000
        bug6b = false;  // hard: door rejects unlock command with passcodes < 1000

        // Phase 4: passcode bad weather
        bug7a = false;  // easy: door accepts   lock command with invalid passcode
        bug7b = false;  // easy: door accepts unlock command with invalid passcode

        // Phase 5: unlock attempts and shut_off
        bug8 = false;   // hard: door never shuts off
        bug9 = false;   // hard: door counts invalid passcodes as unlock attempt
        bug10 = false;  // hard: door accepts open command after being shut off

        // Phase 6: ugly weather
        bug11 = false;  // very hard: door shuts off when receiving response message

        switch(manufacturer) {
        case M_AXINI:
            // no bugs, it is possible ;-)
            break;
        case M_BESTO:
            bug1 = true;
            break;
        case M_LOGICA:
            bug6a = true;
            bug6b = true;
            break;
        case M_ON_TARGET:
            bug4 = true;
            break;
        case M_QUICKERR:
            bug2 = true;
            bug7a = true;
            bug7b = true;
            break;
        case M_SMART_SOFT:
            bug9 = true;
            break;
        case M_TRUSTED_TECHNOLOGIES:
            bug5 = true;
            break;
        case M_UNIVER_SOLUTIONS:
            bug3a = true;
            bug3b = true;
            bug8 = true;
            break;
        case M_XTRA_SAFE:
            bug10 = true;
            bug11 = true;
            break;
        default:
            System.out.println("ERROR: unknown manufacturer: " + manufacturer);
        }
    }

    // Handle the input (i.e., the stimulus).
    public void handle_input(String message) {
        String[] arr = message.split(":");

        String action = arr[0];
        String param = (arr.length == 2) ? arr[1] : null;

        switch (action) {
        case "OPEN"     : open(); break;
        case "CLOSE"    : close(); break;
        case "LOCK"     : lock(param); break;
        case "UNLOCK"   : unlock(param); break;
        case "OPENED", "CLOSED", "LOCKED", "UNLOCKED",
            "INVALID_COMMAND", "INVALID_PASSCODE", "INCORRECT_PASSCODE":
            response(); break;
        default:
            server.send("INVALID_COMMAND");
        }
    }

    // Handles the 'open' command.
    private void open() {
        if (bug1) { random_sleep(0.6); }

        switch(state) {
        case CLOSED:
            server.send("OPENED");
            state = State.OPENED;
            break;

        case OPENED:
            if (bug3a)
                server.send("OPENED");
            else
                server.send("INVALID_COMMAND");
            break;

        case LOCKED:
            server.send("INVALID_COMMAND");
            break;

        case OFF:
            if (bug10) {
                server.send("OPENED");
                state = State.OPENED;
            }
            break;

        default:
            System.out.println("ERROR: unknown state");
        }
    }

    // Handles the 'close' command.
    private void close() {
        if (bug1) { random_sleep(0.6); }

        switch(state) {
        case CLOSED:
            if (bug3b)
                server.send("CLOSED");
            else
                server.send("INVALID_COMMAND");
            break;

        case OPENED:
            if (bug2)
                server.send("OPENED");
            else {
                server.send("CLOSED");
                state = State.CLOSED;
            }
            break;

        case LOCKED:
            server.send("INVALID_COMMAND");
            break;

        case OFF:
            break;

        default:
            System.out.println("ERROR: unknown state");
        }
    }

    // Handles the 'lock' command.
    private void lock(String passcode_param) {
        if (passcode_param == null) {
            server.send("INVALID_COMMAND");
            return;
        }

        int passcode = Integer.parseInt(passcode_param);

        if (bug1) { random_sleep(0.6); }

        switch(state) {
        case CLOSED:
            if (invalid_lock_passcode(passcode)) {
                server.send("INVALID_PASSCODE");
                return;
            }

            server.send("LOCKED");
            this.state = State.LOCKED;
            this.passcode = passcode;
            this.attempts = 0;
            break;

        case OPENED:
            server.send("INVALID_COMMAND");
            break;

        case LOCKED:
            if (bug4) { // allow to be locked twice
                if (invalid_lock_passcode(passcode)) {
                    server.send("INVALID_PASSCODE");
                    return;
                }

                server.send("LOCKED");
                this.state = State.LOCKED;
                this.passcode = passcode;
                this.attempts = 0;
            } else {
                server.send("INVALID_COMMAND");
            }
            break;

        case OFF:
            break;

        default:
            System.out.println("ERROR: unknown state");
        }

    }

    // Handles the 'unlock' command.
    private void unlock(String passcode_param) {
        if (passcode_param == null) {
            server.send("INVALID_COMMAND");
            return;
        }

        int passcode = Integer.parseInt(passcode_param);

        if (bug1) { random_sleep(0.6); }

        switch(state) {
        case CLOSED, OPENED:
            server.send("INVALID_COMMAND");
            break;

        case LOCKED:
            if (invalid_unlock_passcode(passcode)) {
                server.send("INVALID_PASSCODE");
                if (bug9) { attempts += 1; }

                if (attempts == 3 && !bug8) {
                    server.send("SHUT_OFF");
                    state = State.OFF;
                }
                return;
            }

            if (this.passcode == passcode || bug5) {
                server.send("UNLOCKED");
                this.state = State.CLOSED;
                this.passcode = -1;
                this.attempts = 0;
            } else {
                server.send("INCORRECT_PASSCODE");
                attempts += 1;
                if (attempts == 3 && !bug8) {
                    server.send("SHUT_OFF");
                    state = State.OFF;
                }
            }
            break;

        case OFF:
            break;

        default:
            System.out.println("ERROR: unknown state");
        }
    }

    // Handle the case where a response is received (as stimulus).
    private void response() {
        if (bug11) {
            state = State.OFF;
            server.send("SHUT_OFF");
        } else {
            server.send("INVALID_COMMAND");
        }
    }

    private boolean invalid_lock_passcode(int passcode) {
        if (!bug7a && ((passcode < 0) || (passcode > 9999)))
            return true;
        if (bug6a && ((passcode < 1000) || (passcode > 9999)))
            return true;
        return false;
    }

    private boolean invalid_unlock_passcode(int passcode) {
        if (!bug7b && ((passcode < 0) || (passcode > 9999)))
            return true;
        if (bug6b && ((passcode < 1000) || (passcode > 9999)))
            return true;
        return false;
    }

    // Sleep for a maximum of max seconds.
    private void random_sleep(double max) {
        try { Thread.sleep(Math.round(1000 * max * Math.random())); }
        catch (InterruptedException ex) {}
    }
}
