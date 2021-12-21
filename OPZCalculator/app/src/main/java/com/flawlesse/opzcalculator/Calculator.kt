package com.flawlesse.opzcalculator

import java.lang.IllegalStateException
import java.math.BigDecimal
import java.util.*
import kotlin.math.*

class Calculator {
    private val rpn = mutableListOf<TokenState>()
    private val opStack = Stack<TokenState>()

    companion object {
        val BINARY_OPERATORS = listOf("-", "+", "/", "*", "^(")
        val PREFIX_OPERATORS = listOf("√(", "sin(", "cos(", "tan(", "ctg(", "(")
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
        while (balance-- != 0){
            tokens.add(TokenState(TOKEN_TYPE.OPERATOR, ")"))
        }

        // Conversion to RPN list of tokens
        for (t in tokens) {
            if (t.type == TOKEN_TYPE.NUMBER)
                rpn.add(t)
            else if (t.str in (PREFIX_OPERATORS + "^(") || t.isMinusPrefix)
                // prefix minus can only appear after '('
                opStack.push(t)
            else if (t.str == ")"){
                while (opStack.peek().str !in (PREFIX_OPERATORS + "^(")) {
                    rpn.add(opStack.pop())
                }
                rpn.add(opStack.pop())
            }
            else // it is binary operation without ^
            {
                var top = opStack.peek()
                while (
                    //top.str in PREFIX_OPERATORS ||
                    getPriority(top) > getPriority(t)
                    || top.str != "^(" && getPriority(top) == getPriority(t)
                ) {
                    if (top.str in PREFIX_OPERATORS)
                        break
                    opStack.pop()
                    rpn.add(top)
                    top = opStack.peek()
                }
                opStack.push(t)
            }
        }
        while (!opStack.empty()){
            rpn.add(opStack.pop())
        }
    }

    private fun fromRPN(): String {
        val stack = Stack<Double>()
        for (t in rpn) {
            if (t.type == TOKEN_TYPE.NUMBER) {
                if (t.isE) {
                    stack.push(Math.E)
                }
                else if (t.isPi) {
                    stack.push(Math.PI)
                }
                else {
                    stack.push(t.str.toDouble())
                }
            }
            else {
                when (t.str){
                    "-" -> {
                        if (t.isMinusPrefix) {
                            stack.push(realignDouble(-stack.pop()))
                        } else {
                            val rhs = stack.pop()
                            val lhs = stack.pop()
                            stack.push(realignDouble(lhs - rhs))
                        }
                    }
                    "+" -> {
                        val rhs = stack.pop()
                        val lhs = stack.pop()
                        stack.push(realignDouble(lhs + rhs))
                    }
                    "*" -> {
                        val rhs = stack.pop()
                        val lhs = stack.pop()
                        stack.push(realignDouble(lhs * rhs))
                    }
                    "/" -> {
                        val rhs = stack.pop()
                        val lhs = stack.pop()
                        stack.push(realignDouble(lhs / rhs))
                    }
                    "^(" -> {
                        val rhs = stack.pop()
                        val lhs = stack.pop()
                        stack.push(realignDouble(lhs.pow(rhs)))
                    }
                    "√(" -> {
                        stack.push(realignDouble(sqrt(stack.pop())))
                    }
                    "sin(" -> {
                        stack.push(realignDouble(sin(stack.pop())))
                    }
                    "cos(" -> {
                        stack.push(realignDouble(cos(stack.pop())))
                    }
                    "tan(" -> {
                        stack.push(realignDouble(tan(stack.pop())))
                    }
                    "ctg(" -> {
                        val top = stack.pop()
                        val res = 1.0 / tan(top)
                        stack.push(realignDouble(realignDouble(res)))
                    }
                }
            }
        }

        var res = stack.peek().toString()
        if (res.length > 15)
            res = res.substring(0,15)
        return res
    }

    private fun realignDouble(expr: Double): Double {
        return expr.toBigDecimal().toDouble()
    }

    private fun getPriority(token: TokenState): Int {
        if (token.type == TOKEN_TYPE.NUMBER)
            throw IllegalStateException("Numbers don't have priority!")
        return when(token.str) {
            ")" -> 5
            in PREFIX_OPERATORS -> 4
            "^(" -> 3
            "*", "/" -> 2
            else -> 1 // +-
        }
    }
}