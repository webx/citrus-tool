/*
 * Copyright 2010 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.antx.config.generator.expr;

import java.util.ArrayList;
import java.util.List;

/**
 * 代表一个组合的表达式。
 * 
 * @author Michael Zhou
 */
public class CompositeExpression implements Expression {
    private String       expr;
    private Expression[] expressions;

    /**
     * 创建一个组合的表达式。
     * 
     * @param expr 表达式字符串
     * @param expressions 表达式列表
     */
    public CompositeExpression(String expr, List expressions) {
        this.expr = expr;
        this.expressions = (Expression[]) expressions.toArray(new Expression[expressions.size()]);
    }

    /**
     * 取得表达式字符串表示。
     * 
     * @return 表达式字符串表示
     */
    public String getExpressionText() {
        return expr;
    }

    /**
     * 在指定的上下文中计算表达式。
     * 
     * @param context 上下文
     * @return 表达式的计算结果
     */
    public Object evaluate(ExpressionContext context) {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < expressions.length; i++) {
            Expression expression = expressions[i];
            Object value = expression.evaluate(context);

            if (value != null) {
                buffer.append(value);
            }
        }

        return buffer.toString();
    }

    /**
     * 创建表达式。
     * <ul>
     * <li>如果表达式中不包含“<code>${...}</code>”，则创建<code>ConstantExpression</code>。</li>
     * <li>如果表达式以“<code>${</code>”开始，并以“<code>}</code>”结尾，则创建引用表达式。</li>
     * <li>如果表达式包含“<code>${...}</code>”，但在此之外还有别的字符，则创建
     * <code>CompositeExpression</code>。</li>
     * </ul>
     * 
     * @param expr 表达式字符串
     * @return 表达式
     */
    public static Expression parse(String expr) {
        int length = expr.length();
        int startIndex = expr.indexOf("${");

        // 如果表达式不包含${}，则创建constant expression。
        if (startIndex < 0) {
            return new ConstantExpression(expr);
        }

        int endIndex = expr.indexOf("}", startIndex + 2);

        if (endIndex < 0) {
            throw new ExpressionException("Missing '}' character at the end of expression: " + expr);
        }

        // 如果表达式以${开头，以}结尾，则直接调用factory来创建表达式。
        if ((startIndex == 0) && (endIndex == (length - 1))) {
            return new ReferenceExpression(expr.substring(2, endIndex));
        }

        // 创建复合的表达式。
        List expressions = new ArrayList();
        char ch = 0;
        int i = 0;

        StringBuffer chars = new StringBuffer();
        StringBuffer exprBuff = new StringBuffer();

        MAIN: while (i < length) {
            ch = expr.charAt(i);

            switch (ch) {
                case ('$'): {
                    if ((i + 1) < length) {
                        ++i;
                        ch = expr.charAt(i);

                        switch (ch) {
                            case ('$'): {
                                chars.append(ch);
                                break;
                            }

                            case ('{'): {
                                if (chars.length() > 0) {
                                    expressions.add(new ConstantExpression(chars.toString()));
                                    chars.delete(0, chars.length());
                                }

                                if ((i + 1) < length) {
                                    ++i;

                                    while (i < length) {
                                        ch = expr.charAt(i);

                                        {
                                            switch (ch) {
                                                case ('"'): {
                                                    exprBuff.append(ch);
                                                    ++i;

                                                    DOUBLE_QUOTE: while (i < length) {
                                                        ch = expr.charAt(i);

                                                        boolean escape = false;

                                                        switch (ch) {
                                                            case ('\\'): {
                                                                escape = true;
                                                                ++i;
                                                                exprBuff.append(ch);
                                                                break;
                                                            }

                                                            case ('"'): {
                                                                ++i;
                                                                exprBuff.append(ch);
                                                                break DOUBLE_QUOTE;
                                                            }

                                                            default: {
                                                                escape = false;
                                                                ++i;
                                                                exprBuff.append(ch);
                                                            }
                                                        }
                                                    }

                                                    break;
                                                }

                                                case ('\''): {
                                                    exprBuff.append(ch);
                                                    ++i;

                                                    SINGLE_QUOTE: while (i < length) {
                                                        ch = expr.charAt(i);

                                                        boolean escape = false;

                                                        switch (ch) {
                                                            case ('\\'): {
                                                                escape = true;
                                                                ++i;
                                                                exprBuff.append(ch);
                                                                break;
                                                            }

                                                            case ('\''): {
                                                                ++i;
                                                                exprBuff.append(ch);
                                                                break SINGLE_QUOTE;
                                                            }

                                                            default: {
                                                                escape = false;
                                                                ++i;
                                                                exprBuff.append(ch);
                                                            }
                                                        }
                                                    }

                                                    break;
                                                }

                                                case ('}'): {
                                                    expressions.add(new ReferenceExpression(exprBuff.toString()));

                                                    exprBuff.delete(0, exprBuff.length());
                                                    ++i;
                                                    continue MAIN;
                                                }

                                                default: {
                                                    exprBuff.append(ch);
                                                    ++i;
                                                }
                                            }
                                        }
                                    }
                                }

                                break;
                            }

                            default:
                                chars.append(ch);
                        }
                    } else {
                        chars.append(ch);
                    }

                    break;
                }

                default:
                    chars.append(ch);
            }

            ++i;
        }

        if (chars.length() > 0) {
            expressions.add(new ConstantExpression(chars.toString()));
        }

        return new CompositeExpression(expr, expressions);
    }
}
