package searchengine.dto.statistics;

import lombok.Data;

@Data
public class StartStopResponse {
    private boolean result;
    private String error;

    public StartStopResponse(boolean result) {
        this.result = result;
    }

    public StartStopResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
