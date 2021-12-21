package com.flawlesse.opzcalculator

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.*

class Calculator {
    private val rpn = mutableListOf<TokenState>()
    private val opStack = Stack<TokenState>()

    companion object {
        // well, they all act like a scope
        val OPEN_SCOPE = listOf("^(", "√(", "sin(", "cos(", "tan(", "ctg(", "(")
    }

    fun calculate(tokens: List<TokenState>, scopeBalance: Int): String {
        rpn.clear()
        opStack.clear()
        toRPN(tokens.toMutableList(), scopeBalance)
        return fromRPN()
    }

    private fun toRPN(tokens: MutableList<TokenState>, scopeBalance: Int) {
        var balance = scopeBalance
        tokens.add(TokenState(TOKEN_TYPE.OPERATOR, ")"))
        while (balance-- != 0) {
            tokens.add(TokenState(TOKEN_TYPE.OPERATOR, ")"))
        }

        // Conversion to RPN list of tokens
        for (t in tokens) {
            if (t.type == TOKEN_TYPE.NUMBER) {
                rpn.add(t)
                continue
            }

            if (t.str in OPEN_SCOPE) {
                opStack.push(t)
            } else if (t.str == ")") {
                while (opStack.peek().str !in OPEN_SCOPE) {
                    val top = opStack.pop()
                    rpn.add(top)
                }
                if (opStack.peek().str != "(")
                    rpn.add(opStack.peek())
                opStack.pop()
            } else { // -, +, /, *
                while (getPriority(opStack.peek()) >= getPriority(t)
                    && opStack.peek().str !in OPEN_SCOPE
                ) {
                    val top = opStack.pop()
                    rpn.add(top)
                }
                opStack.push(t)
            }
        }
    }

    private fun fromRPN(): String {
        val stack = Stack<Double>()
        for (t in rpn) {
            if (t.type == TOKEN_TYPE.NUMBER) {
                if (t.isE) {
                    stack.push(Math.E)
                } else if (t.isPi) {
                    stack.push(Math.PI)
                } else {
                    stack.push(t.str.toDouble())
                }
            } else {
                when (t.str) {
                    "-" -> {
                        if (t.isMinusPrefix) {
                            stack.push(realignDouble(-stack.pop()))
                        } else {
                            val rhs = realignDouble(stack.pop())
                            val lhs = realignDouble(stack.pop())
                            stack.push(realignDouble(lhs - rhs))
                        }
                    }
                    "+" -> {
                        val rhs = realignDouble(stack.pop())
                        val lhs = realignDouble(stack.pop())
                        stack.push(realignDouble(lhs + rhs))
                    }
                    "*" -> {
                        val rhs = realignDouble(stack.pop())
                        val lhs = realignDouble(stack.pop())
                        stack.push(realignDouble(lhs * rhs))
                    }
                    "/" -> {
                        val rhs = realignDouble(stack.pop())
                        val lhs = realignDouble(stack.pop())
                        if (rhs == 0.0)
                            throw ArithmeticException("Деление на ноль!")
                        stack.push(realignDouble(lhs / rhs))
                    }
                    "^(" -> {
                        val rhs = realignDouble(stack.pop())
                        val lhs = realignDouble(stack.pop())
                        if (lhs < 0 && rhs.rem(1) != 0.0) {
                            throw ArithmeticException("Дробная степень с отрицательным числом!")
                        }
                        stack.push(realignDouble(lhs.pow(rhs)))
                    }
                    "√(" -> {
                        stack.push(realignDouble(sqrt(realignDouble(stack.pop()))))
                    }
                    "sin(" -> {
                        stack.push(realignDouble(sin(realignDouble(stack.pop()))))
                    }
                    "cos(" -> {
                        stack.push(realignDouble(cos(realignDouble(stack.pop()))))
                    }
                    "tan(" -> {
                        val top = realignDouble(stack.pop())
                        var res = realignDouble(cos(top))
                        if (res == 0.0)
                            throw ArithmeticException("Деление на ноль!")
                        res = realignDouble(realignDouble(sin(top)) / res)
                        stack.push(res)
                    }
                    "ctg(" -> {
                        val top = realignDouble(stack.pop())
                        var res = realignDouble(sin(top))
                        if (res == 0.0)
                            throw ArithmeticException("Деление на ноль!")
                        res = realignDouble(realignDouble(cos(top)) / res)
                        stack.push(res)
                    }
                }
            }
        }

        var res = realignDouble(stack.peek()).toString()
        if (res.length > 15)
            res = res.substring(0, 15)
        return res
    }

    private fun realignDouble(expr: Double): Double {
        val decimal = BigDecimal(expr).setScale(7, RoundingMode.HALF_EVEN)
        return decimal.toDouble()
    }

    private fun getPriority(token: TokenState): Int {
        return when (token.str) {
            in OPEN_SCOPE -> 3
            "*", "/" -> 2
            else -> 1 // +-
        }
    }
}