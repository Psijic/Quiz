package com.psvoid.quiz

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.io.IOException
import java.security.SecureRandom
import java.util.*

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {


	private var fileNameList: MutableList<String> = ArrayList() // flag file names
	private var questionsList: MutableList<String> = ArrayList() // countries in current quiz
//	private var regionsSet: Set<String>? = null // world regions in current quiz
	private var correctAnswer: String = "" // correct country for the current flag
	private var totalGuesses: Int = 0 // number of guesses made
	private var correctAnswers: Int = 0 // number of correct guesses
	private var guessRows: Int = 0 // number of rows displaying guess Buttons
	private var random: SecureRandom = SecureRandom() // used to randomize the quiz
	private var handler: Handler = Handler() // used to delay loading next flag

	private lateinit var shakeAnimation: Animation // animation for incorrect guess
	private lateinit var quizLinearLayout: LinearLayout// layout that contains the quiz
	private lateinit var questionNumberTextView: TextView // shows current question #
	private lateinit var questionImageView: ImageView // displays a flag
	private lateinit var guessLinearLayouts: Array<LinearLayout> // rows of answer Buttons
	private lateinit var answerTextView: TextView // displays correct answer

	private val CHAMPIONS_FOLDER = "Champions"
	private val ANIMATION_TIME: Long = 2000

	private val guessButtonListener = OnClickListener { v ->
		val guessButton = v as Button
		val guess = guessButton.text.toString()
		val answer = getChampionName(correctAnswer)

		totalGuesses++

		//right answer
		if (guess == answer) {
			correctAnswers++

			// display a correct answer
			answerTextView.text = "$answer!"
			answerTextView.setTextColor(resources.getColor(R.color.correct_answer, context.theme))
			disableAnswerButtons()

			// if the user has correctly identified QUESTIONS_COUNT flags
			if (correctAnswers == QUESTIONS_COUNT)
				finishGame()
			else
				handler.postDelayed({ animate(true) }, ANIMATION_TIME)
		}
		//wrong answer
		else {
			questionImageView.startAnimation(shakeAnimation)
			answerTextView.setText(R.string.incorrect_answer)
			answerTextView.setTextColor(resources.getColor(R.color.incorrect_answer, context.theme))
			guessButton.isEnabled = false
		}
	}

	/**
	 * End of the game
	 */
	private fun finishGame() {
		val builder = AlertDialog.Builder(activity)
		builder.setMessage(getString(R.string.results, totalGuesses, 1000 / totalGuesses.toDouble()))
		builder.setPositiveButton(R.string.reset_quiz) { _, _ -> resetQuiz() }
		builder.show()

//		return builder.create()
	}

	/**
	 * Configures the MainActivityFragment when its View is created
	 */
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		super.onCreateView(inflater, container, savedInstanceState)

		val view = inflater.inflate(R.layout.fragment_main, container, false)


		// load the shake animation that's used for incorrect answers
		shakeAnimation = AnimationUtils.loadAnimation(activity, R.anim.incorrect_shake)
		shakeAnimation.repeatCount = 3

		// get references to GUI components
		quizLinearLayout = view.findViewById(R.id.quizLinearLayout) as LinearLayout
		questionNumberTextView = view.findViewById(R.id.questionNumberTextView) as TextView
		questionImageView = view.findViewById(R.id.questionImageView) as ImageView
		answerTextView = view.findViewById(R.id.answerTextView) as TextView

		guessLinearLayouts = arrayOf(view.findViewById(R.id.row1LinearLayout) as LinearLayout,
				view.findViewById(R.id.row2LinearLayout) as LinearLayout,
				view.findViewById(R.id.row3LinearLayout) as LinearLayout,
				view.findViewById(R.id.row4LinearLayout) as LinearLayout)


		// configure listeners for the guess Buttons
		for (row in guessLinearLayouts) {
			(0 until row.childCount)
					.map { row.getChildAt(it) as Button }
					.forEach { it.setOnClickListener(guessButtonListener) }
		}

		// set question number
		questionNumberTextView.text = getString(R.string.question, 1, QUESTIONS_COUNT)
		// return the fragment's view for display
		return view
	}

	/**
	 * Update guessRows from SharedPreferences
	 */
	fun updateGuessRows(sharedPreferences: SharedPreferences) {
		// get the number of guess buttons that should be displayed
		val choices = sharedPreferences.getString(MainActivity.CHOICES, null)
		guessRows = Integer.parseInt(choices) / 2

		// hide all guess button LinearLayouts
		for (layout in guessLinearLayouts)
			layout.visibility = View.GONE

		// display appropriate guess button LinearLayouts
		for (row in 0 until guessRows)
			guessLinearLayouts[row].visibility = View.VISIBLE
	}

	/**
	 * Update world regions for quiz based on values in SharedPreferences
	 */
//	fun updateRegions(sharedPreferences: SharedPreferences) {
//		regionsSet = sharedPreferences.getStringSet(MainActivity.REGIONS, null)
//	}

	/**
	 * start a new game
	 */
	fun resetQuiz() {
		// use AssetManager to get image file names for enabled regions
		val assets = activity.assets
		fileNameList.clear() // empty list of image file names
		try {
			// loop through each folder
//			for (region in regionsSet!!) {
				// get a list of all images in this folder
//				val paths = assets.list(region)

			val paths = assets.list(CHAMPIONS_FOLDER)
				for (path in paths)
					fileNameList.add(path.replace(".png", ""))
//			}
		} catch (exception: IOException) {
			Log.e(TAG, "Error loading image file names", exception)
		}

		correctAnswers = 0
		totalGuesses = 0
		// clear prior list of quiz countries
		questionsList.clear()

		var questionCounter = 1
		val championsNumber = fileNameList.size

		// add QUESTIONS_COUNT random file names to the questionsList
		while (questionCounter <= QUESTIONS_COUNT) {
			val randomIndex = random.nextInt(championsNumber)
			// get the random file name
			val filename = fileNameList[randomIndex]
			// if the region is enabled and it hasn't already been chosen
			if (!questionsList.contains(filename)) {
				// add the file to the list
				questionsList.add(filename)
				questionCounter++
			}
		}

		// start the game by loading the first question
		loadNextQuestion()
	}

	/**
	 * Load next question
	 */
	private fun loadNextQuestion() {
		// get file name of the next question and remove it from the list
		val nextImage = questionsList.removeAt(0)

		// update the correct answer
		correctAnswer = nextImage
		answerTextView.text = ""
		// display current question number
		questionNumberTextView.text = getString(R.string.question, correctAnswers + 1, QUESTIONS_COUNT)

		// use AssetManager to load next image from assets folder
		val assets = activity.assets
		// get an InputStream to the asset representing the next image and try to use the InputStream
		try {
			assets.open("$CHAMPIONS_FOLDER/$nextImage.png").use { stream ->
				// load the asset as a Drawable and display on the questionImageView
				val champion = Drawable.createFromStream(stream, nextImage)
				questionImageView.setImageDrawable(champion)
				// animate the image
				animate(false)
			}
		} catch (exception: IOException) {
			Log.e(TAG, "Error loading " + nextImage, exception)
		}

		// shuffle filenames
		Collections.shuffle(fileNameList)
		// put the correct answer at the end of fileNameList
		val correct = fileNameList.indexOf(correctAnswer)
		fileNameList.add(fileNameList.removeAt(correct))

		// add buttons based on the value of guessRows
		for (row in 0 until guessRows) {
			// place Buttons in currentTableRow
			for (column in 0 until guessLinearLayouts[row].childCount) {
				// get reference to Button to configure
				val newGuessButton = guessLinearLayouts[row].getChildAt(column) as Button
				newGuessButton.isEnabled = true
				// get country name and set it as newGuessButton's text
				val filename = fileNameList[row * 2 + column]
				newGuessButton.text = getChampionName(filename)
			}
		}
		// randomly replace one Button with the correct answer
		val row = random.nextInt(guessRows) // pick random row
		val column = random.nextInt(2) // pick random column
		val randomRow = guessLinearLayouts[row] // get the row

		(randomRow.getChildAt(column) as Button).text = getChampionName(correctAnswer)
	}

	/**
	 * Parse filename
	 */
//	private fun getChampionName(name: String?): String = name!!.substring(name.indexOf('-') + 1).replace('_', ' ')
	private fun getChampionName(name: String): String = name

	/**
	 * Animates the entire quizLinearLayout on or off screen
	 */
	private fun animate(animateOut: Boolean) {
		// prevent animation into the the UI for the first question
		if (correctAnswers == 0)
			return

		val centerX = (quizLinearLayout.left + quizLinearLayout.right) / 2
		val centerY = (quizLinearLayout.top + quizLinearLayout.bottom) / 2
		val radius = Math.max(quizLinearLayout.width, quizLinearLayout.height)

		val animator: Animator

		// if the quizLinearLayout should animate out rather than in
		if (animateOut) {
			// create circular reveal animation
			animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout, centerX, centerY, radius.toFloat(), 0f)
			animator.addListener(object : AnimatorListenerAdapter() {
				override fun onAnimationEnd(animation: Animator) {
					loadNextQuestion()
				}
			}
			)
		} else {
			// if the quizLinearLayout should animate in
			animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout, centerX, centerY, 0f, radius.toFloat())
		}

		animator.duration = 500
		animator.start()
	}

	/**
	 * Disable answer buttons
	 */
	private fun disableAnswerButtons() {
		for (row in 0 until guessRows) {
			val guessRow = guessLinearLayouts[row]
			for (i in 0 until guessRow.childCount)
				guessRow.getChildAt(i).isEnabled = false
		}
	}

	companion object {
		private val TAG = "Quiz Activity"
		private val QUESTIONS_COUNT = 10
	}

}
