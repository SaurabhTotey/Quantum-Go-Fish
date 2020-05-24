package com.saurabhtotey.quantumgofish

import kotlinx.cinterop.*
import ncurses.*

/**
 * A terminal manager that handles actually displaying information to the screen and getting user input
 * Uses ncurses
 * Is a class instead of an object because of https://github.com/JetBrains/kotlin-native/blob/master/IMMUTABILITY.md
 */
class TerminalManager {

	/**
	 * An enum of colors that wraps the allowable ncurses colors
	 */
	enum class Color(val ncursesColor: Int) {
		BLACK(COLOR_BLACK),
		BLUE(COLOR_BLUE),
		CYAN(COLOR_CYAN),
		GREEN(COLOR_GREEN),
		MAGENTA(COLOR_MAGENTA),
		RED(COLOR_RED),
		WHITE(COLOR_WHITE),
		YELLOW(COLOR_YELLOW)
	}

	/**
	 * A small data class that stores the information of a printed message
	 */
	private data class TerminalMessage(val message: String, val textColor: Color, val backgroundColor: Color)

	//All the data that has been printed to the screen: is stored for scrolling purposes
	private val printedMessages = mutableListOf<TerminalMessage>()

	//The index of the last displayed message: is used and edited for scrolling purposes
	private var lastDisplayedMessageIndex = -1
		set(value) {
			wclear(this.displayWindow)
			scrollok(this.displayWindow, true)
			(0 .. value).forEach {
				this.displayTerminalMessage(this.printedMessages[it])
			}
			scrollok(this.displayWindow, false)
			wrefresh(this.displayWindow)
			wrefresh(this.inputWindow)
			field = value
		}

	//A map of color pairs to their id
	private val colorPairToId: Map<Pair<Color, Color>, Short>

	//All the messages that have been inputted by the user
	private val inputQueue = mutableListOf<String>()

	//The first message to be handled: will return an empty string if none
	val input
		get() = if (inputQueue.size > 0) inputQueue.removeAt(0) else ""

	//The input that the user is currently inputting
	private var currentInput = ""

	//Whether we can print with colors or not
	private val printWithColors: Boolean

	//The section of the terminal enclosing the displayWindow: draws a box around it
	private lateinit var displayWindowBox: CPointer<WINDOW>

	//The section of the terminal where information will be displayed
	private lateinit var displayWindow: CPointer<WINDOW>

	//The section of the terminal where user input is taken
	private lateinit var inputWindow: CPointer<WINDOW>

	//The max height of the terminal
	private var maxY: Int = 0

	//The max width of the terminal
	private var maxX: Int = 0

	/**
	 * Initialize ncurses
	 */
	init {
		//Initialize ncurses
		initscr()
		//Check about color capabilities
		this.printWithColors = has_colors()
		if (this.printWithColors) {
			start_color()
			//Initialize all possible color pairs
			this.colorPairToId = Color.values().flatMap { color1 -> Color.values().map { color2 -> Pair(color1, color2) } }
					.mapIndexed { i: Int, pair: Pair<Color, Color> -> pair to (i + 1).toShort() }.toMap()
			this.colorPairToId.keys.forEach { pair -> init_pair(this.colorPairToId[pair]!!, pair.first.ncursesColor.toShort(), pair.second.ncursesColor.toShort()) }
		} else {
			this.colorPairToId = mapOf()
		}
		//Handle input as it comes; don't echo because we echo it ourselves anyways
		cbreak()
		noecho()
		//Actually initializes all relevant windows
		this.setupWindows()
	}

	/**
	 * Initializes all windows: is called on start and resize
	 */
	private fun setupWindows() {
		//Get screen size
		this.maxY = getmaxy(stdscr)
		this.maxX = getmaxx(stdscr)
		if (this.maxY < 7) {
			throw Error("Terminal is too small to be usable!")
		}
		//Create windows
		this.displayWindowBox = newwin(this.maxY - 3, this.maxX, 0, 0)!!
		this.displayWindow = newwin(this.maxY - 5, this.maxX - 2, 1, 1)!!
		this.inputWindow = newwin(1, this.maxX - 2, this.maxY - 2, 1)!!
		//Do not block on input but catch all keys (unless they generate signals)
		nodelay(this.inputWindow, true)
		keypad(this.inputWindow, true)
		//Actually updates/displays the windows after giving a border to the relevant window
		box(this.displayWindowBox, 0u, 0u)
		wrefresh(this.displayWindowBox)
		wrefresh(this.displayWindow)
		wrefresh(this.inputWindow)
	}

	/**
	 * Clears the display section of the terminal
	 */
	fun clear() {
		this.lastDisplayedMessageIndex = -1
		wclear(this.displayWindow)
		this.printedMessages.clear()
	}

	/**
	 * Actually handles printing a terminalMessage to the display window; is called from the setter of lastDisplayedMessageIndex
	 */
	private fun displayTerminalMessage(terminalMessage: TerminalMessage) {
		var colorId = 0.toShort()
		if (this.printWithColors) {
			colorId = this.colorPairToId[Pair(terminalMessage.textColor, terminalMessage.backgroundColor)]!!
			wattron(this.displayWindow, COLOR_PAIR(colorId.toInt()))
		}
		wprintw(this.displayWindow, terminalMessage.message)
		if (this.printWithColors) {
			wattroff(this.displayWindow, COLOR_PAIR(colorId.toInt()))
		}
	}

	/**
	 * Prints the given information out on the display section of the screen with the given colors
	 * Automatically scrolls entire display down to bottom
	 */
	fun print(info: String, textColor: Color = Color.WHITE, backgroundColor: Color = Color.BLACK) {
		this.printedMessages.add(TerminalMessage(info, textColor, backgroundColor))
		this.lastDisplayedMessageIndex = this.printedMessages.lastIndex
	}

	/**
	 * Checks to see if anything has been inputted
	 * Also checks for a window resize
	 * Must be called within an event loop
	 * TODO: maybe eventually handle arrows and control arrows and maybe even shift arrows to highlight for copying, and pasting (basically better cursor controls)
	 */
	fun run() {
		var inputtedChar = wgetch(this.inputWindow)
		val oldInput = this.currentInput
		while (inputtedChar != ERR) {
			when (inputtedChar) {

				//Handle resizes
				KEY_RESIZE -> {
					ncurses.clear()
					refresh()
					delwin(this.displayWindowBox)
					delwin(this.displayWindow)
					delwin(this.inputWindow)
					this.setupWindows()
					this.lastDisplayedMessageIndex = this.lastDisplayedMessageIndex //so that messages are updated
				}

				//Handle actual typing
				KEY_BACKSPACE, '\b'.toInt() -> {
					if (keyname(inputtedChar)?.toKString()?.startsWith("^") == true) {
						this.currentInput = this.currentInput.split(" ").dropLast(1).joinToString(" ")
					} else if (this.currentInput.isNotEmpty()) {
						this.currentInput = this.currentInput.dropLast(1)
					}
				}
				KEY_ENTER, '\n'.toInt() -> {
					this.inputQueue.add(this.currentInput)
					this.currentInput = ""
				}
				inputtedChar.shl(24).ushr(24) -> {
					this.currentInput += inputtedChar.toChar()
				}

				//Handle scrolling
				KEY_UP -> {
					if (this.lastDisplayedMessageIndex > 0) { //TODO: this allows for weird scrolling early on that should be disabled; only enable this once the index is greater than the first index in this.printedMessages that fills the screen
						this.lastDisplayedMessageIndex -= 1
					}
				}
				KEY_DOWN -> {
					if (this.lastDisplayedMessageIndex < this.printedMessages.lastIndex) {
						this.lastDisplayedMessageIndex += 1
					}
				}

			}
			inputtedChar = wgetch(this.inputWindow)
		}
		if (this.currentInput != oldInput) {
			wclear(this.inputWindow)
			wprintw(this.inputWindow, this.currentInput.takeLast(this.maxX - 3))
			wrefresh(this.inputWindow)
		}
	}

	/**
	 * Closes ncurses properly
	 */
	fun end() {
		delwin(this.displayWindowBox)
		delwin(this.displayWindow)
		delwin(this.inputWindow)
		endwin()
	}

}
