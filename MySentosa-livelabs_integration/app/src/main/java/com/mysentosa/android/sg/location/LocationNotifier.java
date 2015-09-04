package com.mysentosa.android.sg.location;

import android.location.Location;

public interface LocationNotifier {
	public void updatedLocation(Location location);
	public void listenersDisabled();
}
