package com.psvoid.quiz

import android.os.Bundle
import android.preference.PreferenceFragment

/**
 * A placeholder fragment containing a simple view.
 */
class SettingsActivityFragment : PreferenceFragment() {

	/**
	 * Creates preferences GUI from preferences.xml file in res/xml
	 */
	override fun onCreate(bundle: Bundle?) {
		super.onCreate(bundle)
		// load from XML
		addPreferencesFromResource(R.xml.preferences)
	}
}
