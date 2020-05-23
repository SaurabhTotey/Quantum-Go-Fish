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
	private val printedLines = mutableListOf<TerminalMessage>()

	//A map of color pairs to their id
	private val colorPairToId: Map<Pair<Color, Color>, Short>

	//All the messages that have been inputted by the user
	val inputQueue = mutableListOf<String>()

	//The input that the user is currently inputting
	private var currentInput = ""

	//Whether we can print with colors or not
	private val printWithColors: Boolean

	//The section of the terminal enclosing the displayWindow: draws a box around it
	private val displayWindowBox: CPointer<WINDOW>

	//The section of the terminal where information will be displayed
	private val displayWindow: CPointer<WINDOW>

	//The section of the terminal where user input is taken
	private val inputWindow: CPointer<WINDOW>

	//The max height of the terminal
	private val maxY: Int

	//The max width of the terminal
	private val maxX: Int

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
		//Handle input as it comes and allow it to be echoed back on screen
		cbreak()
		noecho()
		//Get screen size
		this.maxY = getmaxy(stdscr)
		this.maxX = getmaxx(stdscr)
		//Create windows
		this.displayWindowBox = newwin(this.maxY - 3, this.maxX, 0, 0)!!
		this.displayWindow = newwin(this.maxY - 5, this.maxX - 2, 1, 1)!!
		this.inputWindow = newwin(1, this.maxX - 2, this.maxY - 2, 1)!!
		//Do not block on input
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
		wclear(this.displayWindow)
		this.printedLines.clear()
	}

	/**
	 * Prints the given information out on the display section of the screen with the given colors
	 */
	fun print(info: String, textColor: Color = Color.WHITE, backgroundColor: Color = Color.BLACK) {
		var colorId = 0.toShort()
		if (this.printWithColors) {
			colorId = this.colorPairToId[Pair(textColor, backgroundColor)]!!
			wattron(this.displayWindow, COLOR_PAIR(colorId.toInt()))
		}
		wprintw(this.displayWindow, info)
		if (this.printWithColors) {
			wattroff(this.displayWindow, COLOR_PAIR(colorId.toInt()))
		}
		wrefresh(this.displayWindow)
		wrefresh(this.inputWindow)
		this.printedLines.add(TerminalMessage(info, textColor, backgroundColor))
	}

	/**
	 * Checks to see if anything has been inputted
	 * Also checks for a window resize
	 * Must be called within an event loop
	 * TODO: maybe eventually handle arrows and control arrows and maybe even shift arrows to highlight for copying, and pasting
	 */
	fun run() {
		var inputtedChar = wgetch(this.inputWindow)
		val oldInput = this.currentInput
		while (inputtedChar != ERR) {
			when (inputtedChar) {
				KEY_RESIZE -> {
					//TODO:
				}
				KEY_BACKSPACE, '\b'.toInt() -> {
					if (this.currentInput.isNotEmpty()) {
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
