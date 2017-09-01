package com.psvoid.quiz

import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

class MainActivity : AppCompatActivity() {

	private var isPhoneDevice = true
	private var isPreferencesChanged = true

	private val preferencesChangeListener = OnSharedPreferenceChangeListener { sharedPreferences, key ->

		// called when user changes the app's preferences
		isPreferencesChanged = true

		val quizFragment = supportFragmentManager.findFragmentById(R.id.quizFragment) as MainActivityFragment

		if (key == CHOICES) {
			// # of choices to display changed
			quizFragment.updateGuessRows(sharedPreferences)
			quizFragment.resetQuiz()
		} else if (key == REGIONS) {
			// regions to include changed
			val regions = sharedPreferences.getStringSet(REGIONS, null)
			if (regions.isNotEmpty()) {
				quizFragment.updateRegions(sharedPreferences)
				quizFragment.resetQuiz()
			} else {
				// must select one region--set North America as default
				val editor = sharedPreferences.edit()
				regions!!.add(getString(R.string.default_region))
				editor.putStringSet(REGIONS, regions)
				editor.apply()

				Toast.makeText(this@MainActivity, R.string.default_region_message, Toast.LENGTH_SHORT).show()
			}
		}

		Toast.makeText(this@MainActivity, R.string.restarting_quiz, Toast.LENGTH_SHORT).show()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		val toolbar = findViewById(R.id.toolbar) as Toolbar
		setSupportActionBar(toolbar)

		// set default values in the app's SharedPreferences
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

		// register listener for SharedPreferences changes
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferencesChangeListener)

		// determine screen size
		val screenSize = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK

		// if device is a tablet, set isPhoneDevice to false
		if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
			isPhoneDevice = false

		// if running on phone-sized device, allow only portrait orientation
		if (isPhoneDevice)
			requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
	}

	override fun onStart() {
		super.onStart()
		if (isPreferencesChanged) {
			// now that the default preferences have been set, initialize MainActivityFragment and start the game
			val quizFragment = supportFragmentManager.findFragmentById(R.id.quizFragment) as MainActivityFragment

			quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this))
			quizFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this))
			quizFragment.resetQuiz()

			isPreferencesChanged = false
		}
	}

	/**
	 * Show menu if app is running on a phone or a portrait-oriented tablet
	 */
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		// get the device's current orientation
		val orientation = resources.configuration.orientation

		// display the app's menu only in portrait orientation
		return if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			// Inflate the menu; this adds items to the action bar if it is present.
			menuInflater.inflate(R.menu.menu_main, menu)
			true
		} else
			false
	}

	/**
	 * Displays the SettingsActivity when running on a phone
	 */
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		val preferencesIntent = Intent(this, SettingsActivity::class.java)
		startActivity(preferencesIntent)
		return super.onOptionsItemSelected(item)
	}

	companion object {

		/**
		 * keys for reading data from SharedPreferences
		 */
		val CHOICES = "pref_numberOfChoices"
		val REGIONS = "pref_regionsToInclude"
	}

}
