package manager;

import manager.dialogs.AddRoomDialog;
import manager.dialogs.AvailabilityDialog;
import manager.dialogs.TermDatesDialog;
import shared.data.*;
import shared.ui.TableSearchPanel;
import utils.DateUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public class RoomController implements Observer {

    private SharedRooms sharedRooms;
    private SharedTermDates sharedTermDates;
    private RoomManagerUI GUI;

    public RoomController(RoomManagerUI GUI) {
        sharedRooms = SharedRooms.getInstance();
        sharedRooms.addObserver(this);

        sharedTermDates = SharedTermDates.getInstance();

        this.GUI = GUI;
    }
    //TODO Also implement method for BookingController
    public void findRoom(){
        //Check if a room type has been selected
        int selected = GUI.getTableSearchPanel().roomTypeSelected();
        //If selected equals -1, a room type has not been selected
        if(selected == -1){
            JOptionPane.showMessageDialog(
                    new JFrame(),
                    "Please select a room type from the list");
            return;
        }
        //Get the selected value from the room type list
        String roomType = GUI.getTableSearchPanel().getRoomType();

        //Get the date from the date text field
        String searchDate = GUI.getTableSearchPanel().getDate();
        //Check if the date is in a valid format e.g. dd-mm-yyyy
        boolean dateValid = DateUtils.areValidDates(searchDate);
        if(!dateValid){
            JOptionPane.showMessageDialog(
                    new JFrame(),
                    "Date entered must be in the format of dd-mm-yyyy or dd-mm-yy");
            return;
        }

        //Get a list of keys from the shared room structure

        //Check if the date is before or during the term in shared term dates
        TermDate termDate = sharedTermDates.peekAtTermDate();
        Date sDate = DateUtils.toDate(searchDate);
        //If search date is before term starts or after term ends (holidays)
        if(sDate.before(termDate.getStartDate()) || sDate.after(termDate.getEndDate())){

        }
        //Else if search date is after term starts and before term ends (term-time)
        else if(sDate.after(termDate.getStartDate()) && sDate.before(termDate.getEndDate())){
            //Get the room availabilities that are from evening to evening

        }

        //Get the AM/PM options that were selected

    }

    public void handleButtonEvent(RoomActionPanel actionPanel, ActionEvent event) {
        Object eventSource = event.getSource();
        if (eventSource == actionPanel.getAddButton()) {
            //Method call to add a new room to the system
            addRoom();
        } else if (eventSource == actionPanel.getRemoveButton()) {
            //Method call to remove existing room from system
            removeRoom();
        } else if (eventSource == actionPanel.getAvailabilityButton()) {
            //Method call to change the availability status of a room
            manageRoomAvailability();
        } else {
            //Method call to set the term dates in the system
            setTermDates();
        }
    }

    private void addRoom() {
        AddRoomDialog addRoomWindow = new AddRoomDialog();
        //If the action is equal to one, the submit button was pressed
        if (addRoomWindow.getAction() == 1) {
            //Get the room availability array
            ArrayList<Availability> availabilities = addRoomWindow.getAvailability();
            //Get the new room information
            String name = addRoomWindow.getRoomName();
            String type = addRoomWindow.getRoomType();
            String capacity = addRoomWindow.getRoomCapacity();

            if (sharedRooms.keyExists(name)) {
                JOptionPane.showMessageDialog(
                        new JFrame(),
                        "Unable to create new room; " + name + " already exists");
            } else {
                //Create a new room and add it to the shared data structure
                sharedRooms.addRoom(name, new Room(name, type, capacity, availabilities));
            }
        }
    }

    private void removeRoom() {
        //Get the selected row from the table
        int selectedRow = GUI.getTableDisplayPanel().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(new JFrame(), "Please select a room to remove from the table");
            return;
        }
        //Get the room name from the table
        String roomName = (String) GUI.getTableDisplayPanel().getRoomName(selectedRow);
        //Remove the room from the shared rooms data structure
        sharedRooms.removeRoom(roomName);
    }

    private void manageRoomAvailability() {
        //Get the title of the tab selected
        String tabTitle = GUI.getTableDisplayPanel().getTabTitle();
        String roomName;
        int selectedRow;
        if (tabTitle.equals("Available")) {
            //Get the selected row from the available table
            selectedRow = GUI.getTableDisplayPanel().getSelectedRow();
            //If a row is not selected
            if (selectedRow == -1) {
                //Show error message to user
                JOptionPane.showMessageDialog(new JFrame(), "Please select a row from the table");
                return;
            }
            //Get the room name from the selected row
            roomName = (String) GUI.getTableDisplayPanel().getRoomName(selectedRow);
        } else {
            //Get the selected row from the unavailable rooms table
            selectedRow = GUI.getTableDisplayPanel().getSelectedUnavailRow();
            //If a row is not selected
            if (selectedRow == -1) {
                //Show error message to user
                JOptionPane.showMessageDialog(new JFrame(), "Please select a row from the table");
                return;
            }
            //Get the room name from the selected row
            roomName = (String) GUI.getTableDisplayPanel().getUnavailRoomName(selectedRow);
        }

        //Open the availability dialog window
        AvailabilityDialog availWindow = new AvailabilityDialog();

        switch (availWindow.getAction()) {
            ////Request to make the room available was selected
            //Room state transition: Unavailable -> Available
            case 0:
                //If the tab title equals available, then return
                if (tabTitle.equals("Available")) return;

                //Get the room object
                Room r = sharedRooms.getRoom(roomName);
                //Set the room as available
                r.setAvailable();
                //Replace the room in the data structure
                sharedRooms.updateRoom(roomName, r, tabTitle);

                break;
            //Request to make room unavailable was selected
            //Room state transition: Available -> Unavailable
            case 1:
                //if the tab title is unavailable, then return
                if (tabTitle.equals("Unavailable")) return;

                //Get the room unavailability information
                String reason = availWindow.getReason();
                String time = availWindow.getTimeframe();
                String timeFrame = availWindow.getTimeframeSelection();

                //Get the room from the shared data structure
                Room room = sharedRooms.getRoom(roomName);
                //Set the room as unavailable
                room.setUnavailable(reason, time, timeFrame);

                //Replace the room in the data structure
                sharedRooms.updateRoom(roomName, room, tabTitle);
                break;
        }
    }

    private void setTermDates() {
        //Open the term dates dialog
        TermDatesDialog termDatesWindow = new TermDatesDialog();

        //If the action is equal to 1
        if(termDatesWindow.getAction() == 1){
            String firstStart, firstEnd, secondStart, secondEnd;

            //Get the term dates from the window
            firstStart = termDatesWindow.getFirstStartDate();
            firstEnd = termDatesWindow.getFirstEndDate();
            secondStart = termDatesWindow.getScndStartDate();
            secondEnd = termDatesWindow.getScndEndDate();

            Date firStart = DateUtils.toDate(firstStart);
            Date firEnd = DateUtils.toDate(firstEnd);
            Date secStart = DateUtils.toDate(secondStart);
            Date secEnd = DateUtils.toDate(secondEnd);
            //Add a first term
            sharedTermDates.addTermDate(new TermDate(firStart, firEnd));
            //Add a second term
            sharedTermDates.addTermDate(new TermDate(secStart, secEnd));
        }


    }

    //TODO rename method signatures to shorter version
    private void removeFromAvailabilityTable(Object[] message) {
        //For each row in the table
        for (int i = 0; i < GUI.getTableDisplayPanel().getRowCount(); i++) {
            //Get the room name attached to the row
            String name = (String) GUI.getTableDisplayPanel().getRoomName(i);
            //if the room name in the row is equal to the name notified
            if (name.equals(message[1])) {
                //Remove it from the table
                GUI.getTableDisplayPanel().removeRow(i);
                i--;
            }
        }
    }
    private void addToAvailabilityTable(Room room){
        for (Availability av : room.getAvailabilities()) {
            if(av.isAvailable()) {
                String avTimings = av.getFromTime() + av.getFromTimeScale()
                        + " - " + av.getToTime() + av.getToTimeScale();
                Object[] rowData = {
                        room.getRoomName(),
                        room.getRoomType(),
                        room.getRoomCapacity(), avTimings};

                GUI.getTableDisplayPanel().addRow(rowData);
            }
        }
    }
    private void removeFromUnavailabilityTable(Object[] message){
        for(int i = 0; i < GUI.getTableDisplayPanel().getUnavailRowCount(); i++){
            if(GUI.getTableDisplayPanel().getUnavailRoomName(i).equals(message[1]))
            {
                GUI.getTableDisplayPanel().removeUnavailRow(i);
                break;
            }
        }
    }
    private void addToUnavailabilityTable(Object[] message, Unavailability unav){
        GUI.getTableDisplayPanel().addUnavailRow(new Object[]{
                message[1], unav.getReason(),
                unav.getTime() + " " + unav.getTimescale()
        });
    }

    //TODO Implement for booking clerks
    @Override
    public void update(Observable observable, Object o) {
        Object[] message = (Object[]) o;
        if (message[0].equals("Add")) {
            //Get the room by key
            Room room = sharedRooms.getRoom((String) message[1]);
            //Add room entry to room availability table
            addToAvailabilityTable(room);
        }
        else if (message[0].equals("Remove")) {
            /* Update the TableDisplayPanel to remove
             * any entries containing the same room name*/
            removeFromAvailabilityTable(message);
        }

        else if (message[0].equals("Update")) {
            /* Update the TableDisplayPanel to remove
             * any entries containing the same room name*/
            removeFromAvailabilityTable(message);

            //Get the room object
            Room room = sharedRooms.getRoom((String) message[1]);

            //Message contained requested to change room state: Available -> Unavailable
            if (message[2].equals("Available")) {
                //If the room is not available
                if (!room.isAvailable()) {
                    //Get the unavailability object
                    Unavailability unav = room.getUnavailability();
                    //Add the room information to the unavailable rooms table
                    addToUnavailabilityTable(message, unav);
                }
            }
            //Message contained request to change room state: Unavilable -> Available
            else if(message[2].equals("Unavailable")){
                //If the room is available
                if (room.isAvailable()) {
                    //Write the availability information to the available rooms table
                    addToAvailabilityTable(room);
                    //Remove the room entry from the unavailable rooms table
                    removeFromUnavailabilityTable(message);
                }
            }
        }
    }
}
