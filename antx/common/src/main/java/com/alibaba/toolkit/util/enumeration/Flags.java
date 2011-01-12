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
 *
 */
package com.alibaba.toolkit.util.enumeration;

/**
 * 标记<code>Enum</code>为位操作的模式, 也就是说<code>Enum</code>值不是递增, 而是倍增(左移).
 *
 * @version $Id: Flags.java,v 1.1 2003/07/03 07:26:20 baobao Exp $
 * @author Michael Zhou
 */
public interface Flags extends IntegralNumber {
    /**
     * 设置成不可变的位集.
     *
     * @return 位集本身
     */
    Flags setImmutable();

    /**
     * 对当前位集执行逻辑与操作.
     *
     * @param flags  标志位
     *
     * @return 当前位集
     */
    Flags and(Flags flags);

    /**
     * 对当前位集执行逻辑非操作.
     *
     * @param flags  标志位
     *
     * @return 当前位集
     */
    Flags andNot(Flags flags);

    /**
     * 对当前位集执行逻辑或操作.
     *
     * @param flags  标志位
     *
     * @return 当前位集
     */
    Flags or(Flags flags);

    /**
     * 对当前位集执行逻辑异或操作.
     *
     * @param flags  标志位
     *
     * @return 当前位集
     */
    Flags xor(Flags flags);

    /**
     * 清除当前位集的全部位.
     *
     * @return 当前位集
     */
    Flags clear();

    /**
     * 清除当前位集的指定位, 等效于<code>andNot</code>操作.
     *
     * @param flags  标志位
     *
     * @return 当前位集
     */
    Flags clear(Flags flags);

    /**
     * 设置当前位集的指定位, 等效于<code>or</code>操作.
     *
     * @param flags  标志位
     *
     * @return 当前位集
     */
    Flags set(Flags flags);

    /**
     * 测试当前位集的指定位, 等效于<code>and(flags) != 0</code>.
     *
     * @param flags  标志位
     *
     * @return 如果指定位被置位, 则返回<code>true</code>
     */
    boolean test(Flags flags);

    /**
     * 测试当前位集的指定位, 等效于<code>and(flags) == flags</code>.
     *
     * @param flags  标志位
     *
     * @return 如果指定位被置位, 则返回<code>true</code>
     */
    boolean testAll(Flags flags);

    /**
     * 取得标志的值.
     *
     * @return 标志的值
     */
    Object getValue();
}
