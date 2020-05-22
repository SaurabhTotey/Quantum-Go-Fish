package com.saurabhtotey.quantumgofish

import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ncurses.*
import kotlin.time.ExperimentalTime

/**
 * A terminal manager that handles actually displaying information to the screen and getting user input
 * Uses ncurses
 * Is a class instead of an object because of https://github.com/JetBrains/kotlin-native/blob/master/IMMUTABILITY.md
 */
@ExperimentalTime class TerminalManager {

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
	data class TerminalMessage(val message: String, val textColor: Color, val backgroundColor: Color)

	//A mutex that must be used whenever modifying the terminal
	private val terminalModificationMutex = Mutex()

	//All the data that has been printed to the screen: is stored for scrolling purposes
	private val printedLines = mutableListOf<TerminalMessage>()

	//The input that the user is currently inputting
	var currentInput = ""

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

	//The y position of the cursor
	private val cursorY: Int

	//The x position of the cursor
	private var cursorX: Int

	//A job that repeatedly calls getch and echos it appropriately
//	private val acceptInputThread: Job

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
		}
		//Program will manage all input
		cbreak()
		noecho()
		keypad(stdscr, true)
		//Get screen size
		this.maxY = getmaxy(stdscr)
		this.maxX = getmaxx(stdscr)
		if (this.maxX < 50 || this.maxY < 8) {
			throw Error("Terminal is too small to be usable!")
		}
		//Create windows
		this.displayWindowBox = newwin(this.maxY - 3, this.maxX, 0, 0)!!
		this.displayWindow = newwin(this.maxY - 5, this.maxX - 2, 1, 1)!!
		this.inputWindow = newwin(1, this.maxX - 2, this.maxY - 2, 1)!!
		//Moves the cursor to the input window
		this.cursorY = this.maxY - 2
		this.cursorX = 1
		//Actually updates/displays the windows after giving a border to the relevant window
		box(this.displayWindowBox, 0u, 0u)
		wrefresh(this.displayWindowBox)
		wrefresh(this.displayWindow)
		wrefresh(this.inputWindow)
		//Starts a thread that repeatedly polls input
//		this.acceptInputThread = GlobalScope.launch {
//			while (this.isActive) {
//				val inputtedCharacter = getch()
//				this@TerminalManager.terminalModificationMutex.withLock {
//					when (inputtedCharacter) {
//						KEY_ENTER, '\n'.toInt() -> {
//							//TODO: input has been submitted
//							this@TerminalManager.currentInput = ""
//							this@TerminalManager.cursorX = 1
//						}
//						inputtedCharacter.shl(24).ushr(24) -> {
//							if (this@TerminalManager.currentInput.length < this@TerminalManager.maxX - 2) {
//								this@TerminalManager.currentInput += inputtedCharacter.toChar()
//								this@TerminalManager.cursorX += 1
//							}
//						}
//						KEY_BACKSPACE -> {
//							if (this@TerminalManager.currentInput.isNotEmpty()) {
//								this@TerminalManager.currentInput = this@TerminalManager.currentInput.dropLast(1)
//								this@TerminalManager.cursorX -= 1
//							}
//						}
//						//TODO: arrow keys and other stuff to make this cleaner
//					}
//					wclear(this@TerminalManager.inputWindow)
//					wprintw(this@TerminalManager.inputWindow, this@TerminalManager.currentInput)
//					wrefresh(this@TerminalManager.inputWindow)
//					move(this@TerminalManager.cursorY, this@TerminalManager.cursorX)
//				}
//			}
//		}
	}

	/**
	 * Clears the display section of the terminal
	 */
	fun clear() {
		runBlocking {
			this@TerminalManager.terminalModificationMutex.withLock {
				wclear(this@TerminalManager.displayWindow)
				this@TerminalManager.printedLines.clear()
			}
		}
	}

	/**
	 * Prints the given information out on the display section of the screen with the given colors
	 */
	fun print(info: String, textColor: Color = Color.WHITE, backgroundColor: Color = Color.BLACK) {
		runBlocking {
			this@TerminalManager.terminalModificationMutex.withLock {
				if (this@TerminalManager.printWithColors) {
					init_pair(1, textColor.ncursesColor.convert(), backgroundColor.ncursesColor.convert())
					attron(COLOR_PAIR(1))
				}
				wprintw(this@TerminalManager.displayWindow, info)
				wrefresh(this@TerminalManager.displayWindow)
				if (this@TerminalManager.printWithColors) {
					attroff(COLOR_PAIR(1))
				}
				this@TerminalManager.printedLines.add(TerminalMessage(info, textColor, backgroundColor))
			}
		}
	}

	/**
	 * Closes ncurses properly
	 */
	fun end() {
//		this.acceptInputThread.cancel()
		delwin(this.displayWindowBox)
		delwin(this.displayWindow)
		delwin(this.inputWindow)
		endwin()
	}

}
