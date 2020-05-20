package com.saurabhtotey.quantumgofish

import kotlinx.cinterop.*
import ncurses.*

/**
 * A terminal manager that handles actually displaying information to the screen and getting user input
 * Uses ncurses
 */
object TerminalManager {

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

	//Whether we can print with colors or not
	private val printWithColors: Boolean

	//The section of the terminal enclosing the displayWindow: draws a box around it
	private val displayWindowBox: CPointer<WINDOW>

	//The section of the terminal where information will be displayed
	private val displayWindow: CPointer<WINDOW>

	//The section of the terminal where user input is taken
	private val inputWindow: CPointer<WINDOW>

	//The max width of the terminal
	private val maxX: Int

	//The max height of the terminal
	private val maxY: Int

	/**
	 * Initialize ncurses
	 * TODO: install sigwinch handler, and exit program if terminal is too small (not wide enough to run commands or tall enough to display info and take input)
	 */
	init {
		//Initialize ncurses
		initscr()
		//Check about color capabilities
		this.printWithColors = has_colors()
		if (this.printWithColors) {
			start_color()
		}
		//Program only needs to handle input when the user submits it: allow the user to see their input
		nocbreak()
		echo()
		//TODO: allow keypad entry and make arrow keys functional
		//Get screen size
		this.maxY = getmaxy(stdscr)
		this.maxX = getmaxx(stdscr)
		//Create windows
		this.displayWindowBox = newwin(maxY - 3, maxX, 0, 0)!!
		this.displayWindow = newwin(maxY - 5, maxX - 2, 1, 1)!! //TODO: make this a pad https://linux.die.net/man/3/newpad
		this.inputWindow = newwin(1, maxX - 2, maxY - 2, 1)!!
		//Actually updates/displays the windows after giving a border to the relevant window
		box(this.displayWindowBox, 0u, 0u)
		wrefresh(this.displayWindowBox)
		wrefresh(this.displayWindow)
		wrefresh(this.inputWindow)
		//TODO: start thread to repeatedly call getInput and store the results in some queue with mutex
	}

	/**
	 * Clears the display section of the terminal
	 */
	fun clear() {
		wclear(this.displayWindow)
	}

	/**
	 * Prints the given information out on the display section of the screen with the given colors
	 * TODO: ensure that this correctly prints at the same time that the user is typing
	 */
	fun print(info: String, textColor: Color = Color.WHITE, backgroundColor: Color = Color.BLACK) {
		if (this.printWithColors) {
			init_pair(1, textColor.ncursesColor.convert(), backgroundColor.ncursesColor.convert())
			attron(COLOR_PAIR(1))
		}
		wprintw(this.displayWindow, info)
		wrefresh(this.displayWindow)
		wrefresh(this.inputWindow)
		if (this.printWithColors) {
			attroff(COLOR_PAIR(1))
		}
	}

	/**
	 * Returns any inputted string
	 * Is blocking
	 * TODO: caps the allowed input length based on the size of the terminal (this is bad)
	 */
	fun getInput(): String {
		memScoped {
			val maxInputLength = this@TerminalManager.maxX - 2
			val inputCString = this.allocArray<ByteVar>(maxInputLength + 1)
			wscanw(this@TerminalManager.inputWindow, "%${maxInputLength + 1}[^\\n]", inputCString)
			wclear(this@TerminalManager.inputWindow)
			wrefresh(this@TerminalManager.inputWindow)
			val inputKString = inputCString.toKString()
			if (inputKString.length > maxInputLength) {
				this@TerminalManager.print("TODO: SOME SORT OF ERROR ABOUT THE INPUT STRING BEING TOO LONG AND THAT IT IS NOT BEING HANDLED")
				return ""
			}
			return inputKString
		}
	}

	/**
	 * Closes ncurses properly
	 */
	fun end() {
		delwin(this.displayWindow)
		delwin(this.inputWindow)
		endwin()
	}

}
