package searchengine.dto.statistics;

import lombok.Data;

@Data
public class IndexingSinglePageResponse {
    private boolean result;
    private String error;

    public IndexingSinglePageResponse(boolean result) {
        this.result = result;
    }

    public IndexingSinglePageResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
