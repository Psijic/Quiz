package com.psvoid.quiz

import android.preference.PreferenceManager
import android.widget.LinearLayout
import org.junit.Test

class MainActivityFragmentTest {

	private val activity:MainActivityFragment = MainActivityFragment()
	private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.context)

	@Test
	fun onCreateView() {
		val guessLinearLayouts: Array<LinearLayout>  = arrayOf<LinearLayout>()
		assert(guessLinearLayouts.isNotEmpty())
	}


}