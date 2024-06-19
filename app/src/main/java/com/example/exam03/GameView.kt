import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.exam03.GameTask
import com.example.exam03.R
import kotlin.math.abs

class GameView(var c: Context, var gameTask: GameTask) : View(c) {
    //Paint object for drawing on canvas
    private var myPaint: Paint = Paint()

    //Game variables
    private var speed = 1
    private var time = 0
    private var score = 0
    private var myCarPosition = 0
    private val otherCars = ArrayList<HashMap<String, Any>>()
    private var lives = 3 // Initialize with 3 lives for the red car
    private val heartBitmap = BitmapFactory.decodeResource(resources, R.drawable.heart)

    var viewWidth = 0
    var viewHeight = 0
    private var countdownTimer: CountDownTimer? = null
    private var timeRemaining = 0 // Initial countdown time
    private var gameRunning = false
    private var paused = false // Flag to track game pause state

    init {
        myPaint.style = Paint.Style.FILL

        startCountdown() //Start countdown when GameView is initialized
    }

    private fun startCountdown() {
        timeRemaining = 3 // Initial countdown time
        countdownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining--
                invalidate() // Redraw on tick
            }

            override fun onFinish() {
                timeRemaining = 0 // Ensure timeRemaining is zero at finish
                gameRunning = true // Set gameRunning flag to true after countdown
                invalidate() // Redraw after countdown finishes
            }
        }.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!gameRunning) {
            drawCountdown(canvas) // Display countdown if the game is not running
        } else {
            drawGame(canvas) // Display game elements if the game is running
        }

        drawButtons(canvas) // Draw pause button

        if (paused) {
            drawPauseScreen(canvas) // Draw resume, restart and exit buttons
        }
    }

    // Draw the countdown text on the canvas
    private fun drawCountdown(canvas: Canvas) {
        myPaint.color = Color.WHITE
        myPaint.textSize = 200f
        val countdownText = if (timeRemaining > 0) {
            timeRemaining.toString()// Display remaining seconds
        } else {
            "Go!"
        }
        val textWidth = myPaint.measureText(countdownText)
        canvas.drawText(
            countdownText,
            (width - textWidth) / 2f,
            height / 2f,
            myPaint
        )
    }

    // Draw game elements (cars, score, lives) on the canvas
    private fun drawGame(canvas: Canvas) {
        viewWidth = this.measuredWidth
        viewHeight = this.measuredHeight
        //Add new yellow cars periodically based on time and speed
        if (!paused) { // Only update game elements if not paused
            if (time % 700 < 10 + speed) {
                val map = HashMap<String, Any>()
                map["lane"] = (0..2).random()
                map["startTime"] = time
                otherCars.add(map)
            }
            time += 10 + speed
        }
        val carWidth = viewWidth / 5
        val carHeight = carWidth + 10
        myPaint.style = Paint.Style.FILL

        // Draw the red car
        val redCarDrawable = resources.getDrawable(R.drawable.red, null)
        redCarDrawable.setBounds(
            myCarPosition * viewWidth / 3 + viewWidth / 15 + 25,
            viewHeight - 2 - carHeight,
            myCarPosition * viewWidth / 3 + viewWidth / 15 + carWidth - 25,
            viewHeight - 2
        )
        redCarDrawable.draw(canvas)

        // Draw other cars and handle collisions
        for (i in otherCars.indices.reversed()) {
            try {
                val carX = otherCars[i]["lane"] as Int * viewWidth / 3 + viewWidth / 15
                var carY = time - otherCars[i]["startTime"] as Int
                val yellowCarDrawable = resources.getDrawable(R.drawable.yellow, null)

                yellowCarDrawable.setBounds(
                    carX + 25, carY - carHeight, carX + carWidth - 25, carY
                )
                yellowCarDrawable.draw(canvas)

                // Check collision with red car
                if (otherCars[i]["lane"] as Int == myCarPosition &&
                    carY > viewHeight - 2 - carHeight && carY < viewHeight - 2
                ) {
                    // Reduce life when collided
                    lives--
                    if (lives == 0) {
                        gameTask.closeGame(score)//Game over when all lives are gone
                    }
                    otherCars.removeAt(i)
                }

                // Remove cars that have passed beyond the screen
                if (carY > viewHeight + carHeight) {
                    otherCars.removeAt(i)
                    score++
                    speed = 1 + abs(score / 8)//Increase speed as score increases
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Display score, speed, and remaining lives
        myPaint.color = Color.WHITE
        myPaint.textSize = 40f
        canvas.drawText("Score : $score", 120f, 85f, myPaint)
        canvas.drawText("Speed : $speed", 380f, 85f, myPaint)

        // Draw hearts representing remaining lives
        val heartWidth = heartBitmap.width
        val heartHeight = heartBitmap.height
        val heartsSpacing = 20 // Spacing between hearts
        for (i in 0 until lives) {
            val heartLeft = viewWidth - (i + 1) * (heartWidth + heartsSpacing)
            val heartTop = 20 // Adjust vertical positioning for hearts
            canvas.drawBitmap(heartBitmap, heartLeft.toFloat(), heartTop.toFloat(), null)
        }

        // Invalidate the view to trigger a redraw
        invalidate()
    }

    // Draw pause button
    private fun drawButtons(canvas: Canvas) {
        if (!paused && gameRunning) {
            // Load pause button icon
            val pauseIcon = BitmapFactory.decodeResource(resources, R.drawable.pause)
            // Calculate button position and size
            val pauseButtonLeft = 20f
            val pauseButtonTop = 40f
            val pauseButtonRight = pauseButtonLeft + pauseIcon.width
            val pauseButtonBottom = pauseButtonTop + pauseIcon.height
            // Draw pause button icon
            canvas.drawBitmap(pauseIcon, null, RectF(pauseButtonLeft, pauseButtonTop, pauseButtonRight, pauseButtonBottom), myPaint)
        }
    }

    //Draw screen with resume, restart and exit buttons
    private fun drawPauseScreen(canvas: Canvas) {
        // Draw semi-transparent overlay
        myPaint.color = Color.parseColor("#C0000000")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), myPaint)

        // Draw resume button
        val buttonWidth = 400f
        val buttonHeight = 120f
        val verticalSpacing = 60f

        val resumeButtonRect = RectF(
            (width - buttonWidth) / 2f,
            height / 2f - buttonHeight / 2f,
            (width + buttonWidth) / 2f,
            height / 2f + buttonHeight / 2f
        )
        myPaint.color = Color.WHITE
        canvas.drawRect(resumeButtonRect, myPaint)
        myPaint.textSize = 60f
        myPaint.color = Color.BLACK
        canvas.drawText("Resume", resumeButtonRect.centerX() - 100f, resumeButtonRect.centerY() + 20f, myPaint)

        // Draw restart button
        val restartButtonRect = RectF(
            (width - buttonWidth) / 2f,
            resumeButtonRect.bottom + verticalSpacing,
            (width + buttonWidth) / 2f,
            resumeButtonRect.bottom + verticalSpacing + buttonHeight
        )
        myPaint.color = Color.WHITE
        canvas.drawRect(restartButtonRect, myPaint)
        myPaint.textSize = 60f
        myPaint.color = Color.BLACK
        canvas.drawText("Restart", restartButtonRect.centerX() - 100f, restartButtonRect.centerY() + 20f, myPaint)

        // Draw exit button
        val exitButtonRect = RectF(
            (width - buttonWidth) / 2f,
            restartButtonRect.bottom + verticalSpacing,
            (width + buttonWidth) / 2f,
            restartButtonRect.bottom + verticalSpacing + buttonHeight
        )
        myPaint.color = Color.WHITE
        canvas.drawRect(exitButtonRect, myPaint)
        myPaint.textSize = 60f
        myPaint.color = Color.BLACK
        canvas.drawText("Exit", exitButtonRect.centerX() - 75f, exitButtonRect.centerY() + 20f, myPaint)
    }

    //Handle touch events in game
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                if (!paused && gameRunning) { // Toggle game pause state on touch up
                    if (x >= 20f && x <= 140f && y >= 20f && y <= 100f) {
                        pauseGame()
                        return true
                    }
                    //Handle touch events for moving red car left or right
                    val laneWidth = viewWidth / 3
                    if (x < laneWidth) {
                        if (myCarPosition > 0) { //Move left
                            myCarPosition--
                        }
                    } else if (x > 2 * laneWidth) { //Move right
                        if (myCarPosition < 2) {
                            myCarPosition++
                        }
                    }
                    invalidate() // Request redraw on touch

                } else if (paused) { // Toggle game resume state on touch up
                    if (x >= (width - 400f) / 2f && x <= (width + 400f) / 2f &&
                        y >= height / 2f - 120f && y <= height / 2f + 120f
                    ) {
                        resumeGame()
                        return true
                    } else if (x >= (width - 400f) / 2f && x <= (width + 400f) / 2f && // Toggle game restart state on touch up
                        y >= height / 2f + 160f && y <= height / 2f + 300f
                    ) {
                        showRestartConfirmationDialog()
                        return true
                    } else if (x >= (width - 400f) / 2f && x <= (width + 400f) / 2f && // Toggle game exit state on touch up
                        y >= height / 2f + 340f && y <= height / 2f + 480f
                    ) {
                        showExitConfirmationDialog()
                        return true
                    }
                }
            }
        }
        return true
    }

    private fun showRestartConfirmationDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Restart Game")
        builder.setMessage("Are you sure you want to restart the game?")
        builder.setPositiveButton("Yes") { _, _ ->
            restartGame()
        }
        builder.setNegativeButton("No") { _, _ ->
            // Do nothing if the user chooses not to restart
        }
        builder.show()
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Exit Game")
        builder.setMessage("Are you sure you want to exit the game?")
        builder.setPositiveButton("Yes") { _, _ ->
            exitGame()
        }
        builder.setNegativeButton("No") { _, _ ->
            // Do nothing if the user chooses not to exit
        }
        builder.show()
    }

    //Pause the game state
    private fun pauseGame() {
        paused = true
        // Stop the countdown timer
        countdownTimer?.cancel()
        invalidate() // Redraw to display the resume button
    }

    //Resume the game state
    private fun resumeGame() {
        paused = false
        // Restart the countdown timer
        startCountdown()
        invalidate() // Redraw to remove the resume button
    }

    //Restart the game state
    private fun restartGame() {
        paused = false
        gameRunning = false
        timeRemaining = 3
        score = 0
        speed = 1
        time = 0
        otherCars.clear()
        myCarPosition = 0
        lives = 3
        startCountdown()
        invalidate()
    }

    // Handle exiting the game
    private fun exitGame() {
        gameTask.closeGame(score)
    }

    //Reset the game state
    fun resetGame() {
        gameRunning = false // Reset gameRunning flag
        paused = false // Reset gamePaused flag
        timeRemaining = 3 // Restart countdown
        startCountdown() // Start the countdown again
        score = 0
        speed = 1
        time = 0
        otherCars.clear()
        myCarPosition = 0
        lives = 3 // Reset lives to 3 when the game is reset
        invalidate() // Redraw after resetting the game state
    }
}
