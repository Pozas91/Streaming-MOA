package example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

public class ConvertToArff {
    private String fileName;
    private Calendar calendar = GregorianCalendar.getInstance();

    private String[] DAYS_OF_WEEK = new String[]{"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};

    ConvertToArff(String fileName) {
        this.fileName = fileName;
    }

    public String convert(Set<Parking> parkingSet) throws IOException {
        String path = this.fileName + ".arff";

        FileWriter fileWriter = new FileWriter(path);

        // Header
        fileWriter.write("@relation " + this.fileName + "\n\n");

        // Attribute definitions
        fileWriter.write("@attribute id numeric\n");
        fileWriter.write("@attribute day_of_week {monday, tuesday, wednesday, thursday, friday, saturday, sunday}\n");
        fileWriter.write("@attribute minutes numeric\n");
        fileWriter.write("@attribute free numeric \n\n");

        // Data
        fileWriter.write("@data\n");

        for (Parking parking : parkingSet) {

            Integer id = parking.getId();
            Date date = parking.getDate();
            Integer free = parking.getFree();

            calendar.setTime(date);
            String dayOfWeek = DAYS_OF_WEEK[calendar.get(Calendar.DAY_OF_WEEK) - 1];
            int minutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

            fileWriter.write(id.toString() + "," + dayOfWeek + "," + minutes + "," + free + "\n");
        }

        fileWriter.close();

        return path;
    }
}
