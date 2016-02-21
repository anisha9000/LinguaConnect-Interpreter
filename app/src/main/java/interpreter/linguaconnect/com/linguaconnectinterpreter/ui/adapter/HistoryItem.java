package interpreter.linguaconnect.com.linguaconnectinterpreter.ui.adapter;

/**
 * Created by anisha on 19/1/16.
 */
public class HistoryItem {
    int rating;
    String pictureUrl, firstName, lastName, language, duration, endTime, status, bookingTime;

    public HistoryItem(String firstName, String lastName, String language, String duration,
                       String endTime, String pictureUrl, int rating, String status, String bookingTime) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.language = language;
        this.duration = this.duration;
        this.endTime = endTime;
        this.pictureUrl = pictureUrl;
        this.rating = rating;
        this.status = status;
        this.bookingTime = bookingTime;
    }

    public String getName() {
        return firstName + " "+ lastName;
    }

    public int getRating() {
        return rating;
    }

    public String getLanguage() {
        return language;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDuration() {
        return duration;
    }

    public String getStatus() {
        return status;
    }

    public String getBookingTime() {
        return bookingTime;
    }
}
