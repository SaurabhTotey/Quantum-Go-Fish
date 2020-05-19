package com.saurabhtotey.quantumgofish

import kotlinx.cinterop.CPointer
import ncurses.*

/**
 * A terminal manager that handles actually displaying information to the screen and getting user input
 * Uses ncurses
 */
object TerminalManager {

    //Whether we can print with colors or not
    private val printWithColors: Boolean

    //The section of the terminal where information will be displayed
    private val displayWindow: CPointer<WINDOW>

    //The section of the terminal where user input is taken
    private val inputWindow: CPointer<WINDOW>

    /**
     * Initialize ncurses
     * TODO: install sigwinch handler
     */
    init {
        initscr()
        start_color()
        raw()
        noecho()
        keypad(stdscr, true)
        this.printWithColors = has_colors()
        val maxY = getmaxy(stdscr)
        val maxX = getmaxx(stdscr)
        this.displayWindow = newwin(maxY - 1, maxX, 0, 0)!!
        this.inputWindow = newwin(1, maxX, maxY - 1, 0)!!
        box(displayWindow, 0u, 0u)
        wrefresh(this.displayWindow)
        wrefresh(this.inputWindow)
    }

    /**
     * TODO: clears the screen
     */
    fun clear() {
        //TODO:
    }

    /**
     * TODO:
     * TODO: take in an optional color pair argument that defaults to a default color pair
     */
    fun print(info: String) {
        //TODO:
    }

    /**
     * Returns the inputted string
     * TODO:
     */
    fun getInput(): String {
        return "TODO:"
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
