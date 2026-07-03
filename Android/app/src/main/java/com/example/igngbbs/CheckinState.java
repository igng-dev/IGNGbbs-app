package com.example.igngbbs;

import org.json.JSONObject;

public final class CheckinState {
    public final boolean isCheckedIn;
    public final int exp;
    public final int level;
    public final int rewardExp;
    public final String lastMobileCheckin;

    public CheckinState(boolean isCheckedIn, int exp, int level, int rewardExp, String lastMobileCheckin) {
        this.isCheckedIn = isCheckedIn;
        this.exp = exp;
        this.level = level;
        this.rewardExp = rewardExp;
        this.lastMobileCheckin = lastMobileCheckin == null ? "" : lastMobileCheckin;
    }

    static CheckinState fromJson(JSONObject json) {
        if (json == null) {
            return new CheckinState(false, 0, 1, 5, "");
        }
        return new CheckinState(
                json.optBoolean("isCheckedIn", false),
                json.optInt("exp", 0),
                json.optInt("level", 1),
                json.optInt("rewardExp", 5),
                json.optString("lastMobileCheckin", "")
        );
    }
}
