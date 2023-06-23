package com.mca.mindmelter.enums;

public enum TriviaCategory {
    ARTS_AND_LITERATURE("Arts & Literature"),
    SCIENCE_AND_NATURE("Science & Nature"),
    SPORTS_AND_LEISURE("Sports & Leisure"),
    HISTORY("History"),
    GEOGRAPHY("Geography"),
    ENTERTAINMENT("Entertainment");

    private final String displayName;

    TriviaCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}

