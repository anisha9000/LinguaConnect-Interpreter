package interpreter.linguaconnect.com.linguaconnectinterpreter.ui.adapter;

/**
 * Created by anisha on 19/1/16.
 */
public class HistoryItem {
    int rating;
    String pictureUrl, firstName, lastName, language, startTime, endTime, status;

    public HistoryItem(String firstName, String lastName, String language, String startTime,
                       String endTime, String pictureUrl, int rating, String status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.language = language;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pictureUrl = pictureUrl;
        this.rating = rating;
        this.status = status;
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

    public String getStartTime() {
        return startTime;
    }

    public String getStatus() {
        return status;
    }
}
