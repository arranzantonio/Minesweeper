package minesweeper
import kotlin.random.Random

// Symbol used to represent mined. Change it to whatever columns you want
//const val MINED = "X"
// Symbol used to represent not mined. Change it to whatever columns you want
const val NOT_EXPLORED = "."
// Symbol used to represent not mined. Change it to whatever columns you want
const val MARKED = "*"
// Symbol used to represent empty explored mines.
const val WITHOUT_MINES_AROUND = "/"

// Number of rows and columns for the mined field.
const val ROWS = 9
const val COLUMNS = 9

class MinesOverPlacesException(message:String):Throwable(message)

// This is a class representing field Points.
class Point(
    private val x: Int,
    private val y: Int,
    var marked: Boolean = false,
    var explored: Boolean = false,
    var surroundingMines: Int = 0
    ) {


    fun isMined() = surroundingMines == -1
    fun isMarked() = marked
    fun setSurroundingMines(field: MutableList<MutableList<Point>>) {
        for (shiftx in -1..1) {
            for (shifty in -1 .. 1)
                if (inBounds(x, y, shiftx, shifty) && !(shiftx == 0 && shifty == 0)) {
                        if (field[x + shiftx][y + shifty].isMined())  ++surroundingMines
                        if (field[x + shiftx][y + shifty].isMined())  ++surroundingMines
                    }
        }
    }
}

// This function returns true if one point plus shiftx or shifty is into field's bounds.
fun inBounds(x: Int, y: Int, shiftx: Int, shifty: Int) = y + shifty >= 0 &&
                                                         y + shifty <= ROWS - 1 &&
                                                         x + shiftx >= 0 &&
                                                         x + shiftx <= COLUMNS - 1

fun printHeadFoot(printColumNumbers: Boolean = false) {
    if (printColumNumbers) print(" │") else print("—│")
    for (i in 0 until COLUMNS) {
        if (printColumNumbers) print("${i+1}") else print("—")
    }
    println("│")
}

// Thia ia a function that shows the field.
fun showField(field: MutableList<MutableList<Point>>) {
    println()
    printHeadFoot(printColumNumbers = true)
    printHeadFoot(printColumNumbers = false)
    for (y in 0 until COLUMNS) {
        print("${y + 1}" + "│")
        for (x in 0 until ROWS) {
            when {
                field[x][y].isMarked() -> print(MARKED)
                field[x][y].explored && field[x][y].surroundingMines == 0 -> print(WITHOUT_MINES_AROUND)
                field[x][y].explored && field[x][y].surroundingMines > 0 -> print(field[x][y].surroundingMines)
                else -> print(NOT_EXPLORED)
            }
        }
        println("│")
    }
    printHeadFoot(printColumNumbers = false)
}

fun getNumberOfMines(): Int {
    try {
        print("How many mines do you want on the field? > ")
        val numberOfMines = readln().toInt()
        if (numberOfMines > ROWS * COLUMNS)
            throw MinesOverPlacesException("Please the mines should be under ${ROWS * COLUMNS}")
        return numberOfMines
    } catch (e: NumberFormatException) {
        println("Please you must give a number.")
        return -1
    } catch (e: MinesOverPlacesException) {
        println(e.message)
        return -1
    } catch (e: Exception) {
        println(e.message)
        return -1
    }
}

// This fun populates de n column
fun fillColumn(n: Int):MutableList<Point> {
    val column = mutableListOf<Point>()
    for (y in 0 until ROWS) {
        column.add(Point(n,y))
    }
    return column
}

fun explore(field: MutableList<MutableList<Point>>, x: Int, y: Int) {
    field[x][y].explored = true
    if (field[x][y].marked) field[x][y].marked = false
    if (field[x][y].surroundingMines == 0) {
        for (shiftx in -1 .. 1) {
            for (shifty in -1 .. 1) {
                if (
                    inBounds(x, y, shiftx, shifty) &&
                    !field[x + shiftx][y + shifty].explored
                ) {
                    if (field[x + shiftx][y + shifty].surroundingMines > 0)
                        field[x + shiftx][y + shifty].explored = true
                    if (field[x][y].marked) field[x][y].marked = false
                    else
                        explore(field, x + shiftx, y + shifty)
                }
            }
        }
    }
}

fun main() {
    val field = mutableListOf<MutableList<Point>>()

    // Populating the field's rows with not mined Points
    for (x in 0 until COLUMNS) {
       field.add(fillColumn(x))
    }

    val numberOfMines = getNumberOfMines()
    var placedMines = 0
    while (placedMines < numberOfMines) {
        val x = Random.nextInt(0, COLUMNS)
        val y = Random.nextInt(0, ROWS)
        if (!field[x][y].isMined()) {
//            println("${x+1}, ${y+1}")
            field[x][y].surroundingMines = -1
            ++placedMines
        }
    }

    for (x in 0 until COLUMNS) {
        for (y in 0 until ROWS) {
            if (!field[x][y].isMined()) field[x][y].setSurroundingMines(field)
        }
    }

    showField(field)

    var foundMines = 0

    while (foundMines < placedMines) {
        print("Set/unset mine marks or claim a cell as free: > ")
        val input = readln().split(" ")
        var x: Int
        var y: Int
        var action: String
        try {
            x = input[0].toInt() - 1
            y = input[1].toInt() - 1
            action = input[2]
        } catch (e: Exception) {
            println("You must read the rules")
            x = 0
            y = 0
            action = "shiftx"
        }

        when (action) {
            "mine" ->   if (field[x][y].explored) println("This mine has been already explored.")
                        else {
                            if (field[x][y].isMarked()) {
                                field[x][y].marked = false
                                if (field[x][y].isMined()) --foundMines
                            } else {
                                field[x][y].marked = true
                                if (field[x][y].isMined()) ++foundMines
                            }
                        }

            "free" ->   if (field[x][y].isMined()) {
                             println("You stepped on a mine and failed!")
                             showField(field)
                             break
                        } else {
                            if (field[x][y].explored) println("This mine has been already explored.")
                            else explore(field, x, y)
                        }
            else ->     println("Wrong option. Try it again!")

        }
        showField(field)
    }
    if (foundMines == placedMines) print("Congratulations! You found all the mines!")
}


