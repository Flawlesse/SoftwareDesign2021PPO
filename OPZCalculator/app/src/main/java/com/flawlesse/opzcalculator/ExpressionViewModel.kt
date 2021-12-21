package com.flawlesse.opzcalculator

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.lang.ArithmeticException

@Suppress("ClassName")
enum class TOKEN_TYPE { NUMBER, OPERATOR }

class TokenState(
    val type: TOKEN_TYPE,
    var str: String,
    val isMinusPrefix: Boolean = false,
    var wasDot: Boolean = false,
    var dotBefore: Boolean = false,
    var wasE: Boolean = false,
    var eBefore: Boolean = false,
    var isE: Boolean = false,
    var isPi: Boolean = false,
) {

    fun updateState() {
        if (str == "E") {
            isE = true
        } else if (str == "π") {
            isPi = true
        }
        // since the token str is never empty, it is safe
        wasDot = str.contains('.')
        dotBefore = str.last() == '.'
        wasE = str.contains('E')
        eBefore = str.last() == 'E'
    }
}



class ExpressionViewModel : ViewModel() {
    private val _expressionString = MutableLiveData<String>()
    val expressionString: LiveData<String> = _expressionString

    private val tokens = mutableListOf(TokenState(TOKEN_TYPE.OPERATOR, "("))
    private var scopeBalance = 0
    private val calculator = Calculator()
    private var blockInput: Boolean = false

    init {
        clearAll()
    }

    companion object {
        val BINARY_OPERATORS = listOf("-", "+", "/", "*", "^(")
        val PREFIX_OPERATORS = listOf("√(", "sin(", "cos(", "tan(", "ctg(", "(")
        const val MAX_LENGTH = 15
    }

    fun clearAll() {
        tokens.clear()
        tokens.add(TokenState(TOKEN_TYPE.OPERATOR, "("))
        scopeBalance = 0
        _expressionString.value = ""
        blockInput = false
    }

    fun clearToken() {
        if (tokens.size == 1) return
        if (tokens.last().type == TOKEN_TYPE.OPERATOR) {
            if (tokens.last().str in (PREFIX_OPERATORS + "^("))
                scopeBalance--
            else if (tokens.last().str == ")")
                scopeBalance++
            tokens.removeLast()
        }
        else {
            tokens.last().str = tokens.last().str.dropLast(1)
            if (tokens.last().str.isEmpty())
                tokens.removeLast()
            else
                tokens.last().updateState()
        }
        update()
    }

    private fun update() {
        var res = ""
        for (t in tokens.subList(1, tokens.size))
            res += t.str
        _expressionString.value = res
    }

    private fun isValidNumber(currentToken: TokenState): Boolean {
        if (currentToken.type == TOKEN_TYPE.NUMBER) {
            return (currentToken.str.matches(Regex("""^[0-9]+(\.[0-9]+)?(E-?[0-9]+)?$"""))
                    || currentToken.isE || currentToken.isPi)
        }
        return false
    }

    private fun tryInput(tokenPart: String): Boolean {
        val lastToken = tokens.last()
        if (lastToken.type == TOKEN_TYPE.NUMBER) {
            // can it be a following symbol in the last number?

            if (lastToken.str.length == MAX_LENGTH && tokenPart !in BINARY_OPERATORS)
                return false
            when (tokenPart) {
                "π" -> return false
                "E" -> {
                    if (lastToken.wasE || lastToken.dotBefore)
                        return false
                    lastToken.str += tokenPart
                    lastToken.updateState()
                    return true
                }
                "." -> {
                    if (lastToken.wasDot || lastToken.str.isEmpty())
                        return false
                    lastToken.str += tokenPart
                    lastToken.updateState()
                    return true
                }
                "-" -> { // this IS in binary operators so we must check it against operand size
                    if (lastToken.eBefore && !lastToken.isE && lastToken.str.length < MAX_LENGTH) {
                        lastToken.str += tokenPart
                        lastToken.updateState()
                    } else {
                        if (isValidNumber(lastToken)) {
                            tokens.add(TokenState(TOKEN_TYPE.OPERATOR, "-"))
                            return true
                        }
                        return false
                    }
                    return true
                }
                "+", "/", "*", "^(" -> { // these can only be operators
                    if (isValidNumber(lastToken)) {
                        tokens.add(TokenState(TOKEN_TYPE.OPERATOR, tokenPart))
                        if (tokenPart == "^(")
                            scopeBalance++
                        return true
                    }
                    return false
                }
                in PREFIX_OPERATORS -> { // no prefix operators can be set RIGHT AFTER operand
                    return false
                }
                ")" -> {
                    if (scopeBalance > 0 && isValidNumber(lastToken)) {
                        tokens.add(TokenState(TOKEN_TYPE.OPERATOR, tokenPart))
                        scopeBalance--
                        return true
                    }
                    return false
                }
                else -> { // checking numerals
                    if (lastToken.str == "0" && tokenPart == "0") // no multiple zeros from start
                        return false
                    if (lastToken.isE || lastToken.isPi) // no digits allowed after those as is
                        return false
                    lastToken.str += tokenPart
                    lastToken.updateState()
                    return true
                }
            }
        } else {
            // preceding token was an operator
            when (tokenPart) {
                "." -> return false
                ")" -> {
                    if (lastToken.str == ")" && scopeBalance > 0){
                        tokens.add(TokenState(TOKEN_TYPE.OPERATOR, ")"))
                        scopeBalance--
                        return true
                    }
                    return false
                }
                "π", "E" -> {
                    tokens.add(TokenState(TOKEN_TYPE.NUMBER, tokenPart))
                    tokens.last().updateState()
                    return true
                }
                in BINARY_OPERATORS -> {
                    if (lastToken.str in (PREFIX_OPERATORS + "^(")) { // opening scope before
                        if (tokenPart == "-") {
                            // it has to be the unary minus then
                            tokens.add(
                                TokenState(
                                    TOKEN_TYPE.OPERATOR,
                                    tokenPart,
                                    isMinusPrefix = true
                                )
                            )
                            return true
                        }
                    } else if (lastToken.str == ")") {
                        tokens.add(TokenState(TOKEN_TYPE.OPERATOR, tokenPart))
                        if (tokenPart == "^(")
                            scopeBalance++
                        return true
                    }
                    return false
                }
                else -> { // prefix operators + numerals
                    if (lastToken.str != ")") {
                        val tokenType = if (tokenPart in PREFIX_OPERATORS) TOKEN_TYPE.OPERATOR else TOKEN_TYPE.NUMBER
                        if (tokenType == TOKEN_TYPE.OPERATOR)
                            scopeBalance++
                        tokens.add(TokenState(tokenType, tokenPart))
                        return true
                    }
                    return false
                }
            }
        }
    }

    fun updateWithToken(token: String): Boolean {
        if (tryInput(token)) {
            update()
            return true
        }
        return false
    }

    fun calculate() {
        if (tokens.last().type == TOKEN_TYPE.NUMBER && !isValidNumber(tokens.last())){
            throw ArithmeticException("Неверное число на конце!")
        }
        val res = calculator.calculate(tokens, scopeBalance)
        var answer: Double = res.toDouble()

        clearAll()
        if (answer.isNaN() || answer.isInfinite()){
            _expressionString.value = res
            blockInput = true
        }
        else {
            if (answer < 0){
                tokens.add(TokenState(TOKEN_TYPE.OPERATOR, "-", isMinusPrefix = true))
                answer = -answer
            }
            tokens.add(TokenState(TOKEN_TYPE.NUMBER, answer.toString()))
            update()
        }
    }
}