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

package com.alibaba.toolkit.util.resourcebundle;

import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import com.alibaba.toolkit.util.collection.SoftHashMap;
import com.alibaba.toolkit.util.resourcebundle.xml.XMLResourceBundleFactory;

/**
 * 创建<code>ResourceBundle</code>的实例的工厂.
 * 
 * @version $Id: ResourceBundleFactory.java,v 1.1 2003/07/03 07:26:35 baobao Exp
 *          $
 * @author Michael Zhou
 */
public abstract class ResourceBundleFactory {
    /**
     * 使用指定的bundle基本名, 默认的locale, 默认的factory中取得resource bundle.
     * 默认的factory是从线程的context class loader中取得资源文件, 并以XML的格式解释资源文件.
     * 
     * @param baseName bundle的基本名
     * @return resource bundle
     * @throws MissingResourceException 指定bundle未找到, 或创建bundle错误
     */
    public static final ResourceBundle getBundle(String baseName) {
        return getBundle(baseName, null, (ResourceBundleFactory) null);
    }

    /**
     * 使用指定的bundle基本名, 指定的locale, 默认的factory中取得resource bundle.
     * 默认的factory是从线程的context class loader中取得资源文件, 并以XML的格式解释资源文件.
     * 
     * @param baseName bundle的基本名
     * @param locale 区域设置
     * @return resource bundle
     * @throws MissingResourceException 指定bundle未找到, 或创建bundle错误
     */
    public static final ResourceBundle getBundle(String baseName, Locale locale) {
        return getBundle(baseName, locale, (ResourceBundleFactory) null);
    }

    /**
     * 使用指定的bundle基本名, 指定的locale, 默认的factory中取得resource bundle.
     * 默认的factory是从指定的class loader中取得资源文件, 并以XML的格式解释资源文件.
     * 
     * @param baseName bundle的基本名
     * @param locale 区域设置
     * @param classLoader class loader
     * @return resource bundle
     * @throws MissingResourceException 指定bundle未找到, 或创建bundle错误
     */
    public static final ResourceBundle getBundle(String baseName, Locale locale, ClassLoader classLoader) {
        return getBundle(baseName, locale, new XMLResourceBundleFactory(classLoader));
    }

    /**
     * 使用指定的bundle基本名, 指定的locale, 指定的loader, 默认的factory中取得resource bundle.
     * 默认的factory是从指定的loader中取得资源文件, 并以XML的格式解释资源文件.
     * 
     * @param baseName bundle的基本名
     * @param locale 区域设置
     * @param loader bundle的装入器
     * @return resource bundle
     * @throws MissingResourceException 指定bundle未找到, 或创建bundle错误
     */
    public static final ResourceBundle getBundle(String baseName, Locale locale, ResourceBundleLoader loader) {
        return getBundle(baseName, locale, new XMLResourceBundleFactory(loader));
    }

    /**
     * 使用指定的bundle基本名, 指定的locale, 指定的factory中取得resource bundle.
     * 
     * @param baseName bundle的基本名
     * @param locale 区域设置
     * @param factory bundle工厂
     * @return resource bundle
     * @throws MissingResourceException 指定bundle未找到, 或创建bundle错误
     */
    public static final ResourceBundle getBundle(String baseName, Locale locale, ResourceBundleFactory factory) {
        if (locale == null) {
            locale = Locale.getDefault();
        }

        if (factory == null) {
            factory = new XMLResourceBundleFactory();
        }

        return Helper.getBundleImpl(baseName, locale, factory);
    }

    /**
     * 创建<code>ResourceBundle</code>的实例.
     * 
     * @param bundleName 要创建的bundle名称
     * @return 新创建的<code>ResourceBundle</code>实例, 如果指定bundle不存在, 则返回
     *         <code>null</code>
     * @throws ResourceBundleCreateException 指定bundle文件存在, 但创建bundle实例失败,
     *             例如文件格式错误
     */
    public abstract ResourceBundle createBundle(String bundleName) throws ResourceBundleCreateException;

    /**
     * 判断两个<code>ResourceBundleFactory</code>是否等效. 这将作为
     * <code>ResourceBundle</code>的cache的依据.
     * 
     * @param obj 要比较的另一个对象
     * @return 如果等效, 则返回<code>true</code>
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * 取得hash值. 等效的<code>ResourceBundleFactory</code>应该具有相同的hash值.
     * 
     * @return hash值
     */
    @Override
    public abstract int hashCode();

    /**
     * 查找和创建bundle的类.
     */
    private static final class Helper {
        /**
         * 将(factory, bundleName, defaultLocale)映射到bundle对象的cache. 当内存不足时,
         * cache的内容会自动释放.
         */
        private static final Cache cache = new Cache();

        /**
         * 使用指定的bundle基本名, 指定的locale, 指定的factory中取得resource bundle.
         * 
         * @param baseName bundle的基本名
         * @param locale 区域设置
         * @param factory bundle工厂
         * @return resource bundle
         * @throws MissingResourceException 指定bundle未找到, 或创建bundle错误
         */
        private static ResourceBundle getBundleImpl(String baseName, Locale locale, ResourceBundleFactory factory) {
            if (baseName == null) {
                throw new NullPointerException(ResourceBundleConstant.RB_BASE_NAME_IS_NULL);
            }

            // 使用factory作为bundle未找到的标记, 这样当factory被GC回收的时候, cache里对应的项也可以被回收.
            final Object NOT_FOUND = factory;

            // 从cache中取得bundle.
            String bundleName = baseName;
            String localeSuffix = locale.toString();

            if (localeSuffix.length() > 0) {
                bundleName += "_" + localeSuffix;
            } else if (locale.getVariant().length() > 0) {
                // 修正: new Locale("", "", "VARIANT").toString == ""
                bundleName += "___" + locale.getVariant();
            }

            // 取得系统locale, 注意, 这个值可能被改变, 所以每次执行时都重新取.
            Locale defaultLocale = Locale.getDefault();

            Object lookup = cache.get(factory, bundleName, defaultLocale);

            if (NOT_FOUND.equals(lookup)) {
                throwResourceBundleException(true, baseName, locale, null);
            } else if (lookup != null) {
                return (ResourceBundle) lookup;
            }

            // 开始查找并创建bundle.
            Object parent = NOT_FOUND;

            try {
                // 查找base bundle.
                Object root = findBundle(factory, baseName, defaultLocale, baseName, null, NOT_FOUND);

                if (root == null) {
                    root = NOT_FOUND;
                    cache.put(factory, baseName, defaultLocale, root);
                }

                // 查找主要路径, 例如getBundle("baseName", new Locale("zh", "CN", "Variant")),
                // 主要路径为baseName_zh, baseName_zh_CN, baseName_zh_CN_Varient.
                final List names = calculateBundleNames(baseName, locale);
                List bundlesFound = new ArrayList(ResourceBundleConstant.MAX_BUNDLES_SEARCHED);

                // 如果base bundle已经找到, 并且主路径为空.
                boolean foundInMainBranch = !NOT_FOUND.equals(root) && names.size() == 0;

                if (!foundInMainBranch) {
                    parent = root;

                    for (int i = 0; i < names.size(); i++) {
                        bundleName = (String) names.get(i);
                        lookup = findBundle(factory, bundleName, defaultLocale, baseName, parent, NOT_FOUND);
                        bundlesFound.add(lookup);

                        if (lookup != null) {
                            parent = lookup;
                            foundInMainBranch = true;
                        }
                    }
                }

                // 如果主路径未找到bundle, 则查找系统默认路径, 例如当前系统默认locale为en_US,
                // 则搜索路径为: baseName_en, baseName_US.
                parent = root;

                if (!foundInMainBranch) {
                    final List fallbackNames = calculateBundleNames(baseName, defaultLocale);

                    for (int i = 0; i < fallbackNames.size(); i++) {
                        bundleName = (String) fallbackNames.get(i);

                        // 如果系统默认路径和主路径一致, 则不需要再找下去了
                        if (names.contains(bundleName)) {
                            break;
                        }

                        lookup = findBundle(factory, bundleName, defaultLocale, baseName, parent, NOT_FOUND);

                        if (lookup != null) {
                            parent = lookup;
                        } else {
                            // 将父bundle传递给子bundle, 例如:
                            // 父bundle: baseName_en.xml已经找到, 子bundle: baseName_en_US未找到,
                            // 则cache中:
                            // baseName       => bundle对象: baseName.xml
                            // baseName_en    => bundle对象: baseName_en.xml
                            // baseName_en_US => bundle对象: baseName_en.xml
                            cache.put(factory, bundleName, defaultLocale, parent);
                        }
                    }
                }

                // 在主路径中, 将父bundle传递给子bundle, 这里有三种情况:
                // 1. bundle在主路径中, 例如getBundle("baseName", new Locale("zh", "CN")),
                //    baseName_zh被找到, 则cache中:
                //    baseName       => bundle对象: baseName.xml
                //    baseName_zh    => bundle对象: baseName_zh.xml
                //    baseName_zh_CN => bundle对象: baseName_zh.xml
                //
                // 2. bundle在系统路径中, 主路径未找到, 例如getBundle("baseName", new Locale("zh", "CN")),
                //    baseName_zh和baseName_zh_CN均未找到, 但系统路径中baseName_en被找到, 则cache中:
                //    baseName       => bundle对象: baseName.xml
                //    baseName_zh    => bundle对象: baseName_en.xml
                //    baseName_zh_CN => bundle对象: baseName_en.xml
                //    baseName_en    => bundle对象: baseName_en.xml
                //    baseName_en_US => bundle对象: baseName_en.xml
                //
                // 3. bundle的基本名未找到:
                //    baseName       => NOT_FOUND
                //    baseName_zh    => NOT_FOUND
                //    baseName_zh_CN => NOT_FOUND
                //    baseName_en    => NOT_FOUND
                //    baseName_en_US => NOT_FOUND
                for (int i = 0; i < names.size(); i++) {
                    final String name = (String) names.get(i);
                    final Object bundleFound = bundlesFound.get(i);

                    if (bundleFound == null) {
                        cache.put(factory, name, defaultLocale, parent);
                    } else {
                        parent = bundleFound;
                    }
                }
            } catch (Exception e) {
                // 可能是ResourceBundleCreateException和其它RuntimeException.
                cache.cleanUpConstructionList();
                throwResourceBundleException(false, baseName, locale, e);
            } catch (Error e) {
                cache.cleanUpConstructionList();
                throw e;
            }

            if (NOT_FOUND.equals(parent)) {
                throwResourceBundleException(true, baseName, locale, null);
            }

            return (ResourceBundle) parent;
        }

        /**
         * 在cache中查找bundle, 或从factory中装入bundle. 如果此方法返回<code>null</code>,
         * 则调用者必须自己定义bundle, 并调用<code>cache.put</code>方法.
         * 
         * @param factory bundle工厂
         * @param bundleName bundle名称
         * @param defaultLocale 系统默认的locale
         * @param baseName bundle基本名
         * @param parent 父bundle, 对于根bundle, 为<code>null</code>
         * @param NOT_FOUND 标记"未找到"状态的对象
         * @return resource bundle, 或者<code>null</code>表示bundle未找到
         * @throws ResourceBundleCreateException bundle被找到, 但构造不成功
         */
        private static Object findBundle(ResourceBundleFactory factory, String bundleName, Locale defaultLocale,
                                         String baseName, Object parent, final Object NOT_FOUND)
                throws ResourceBundleCreateException {
            Object result = cache.getWait(factory, bundleName, defaultLocale);

            if (result != null) {
                return result;
            }

            // 尝试从factory中装入bundle.
            result = factory.createBundle(bundleName);

            if (result != null) {
                // 在调用factory时, 有可能递归地调用了getBundle方法, 并且这个bundle已经被创建了.
                // 这种情况下, bundle一定在cache中.  为了一致性, 应返回cache中的bundle.
                Object otherBundle = cache.get(factory, bundleName, defaultLocale);

                if (otherBundle != null) {
                    result = otherBundle;
                } else {
                    // 设置bundle的父bundle, 并把它放到cache中.
                    final ResourceBundle bundle = (ResourceBundle) result;

                    if (!NOT_FOUND.equals(parent) && bundle.getParent() == null) {
                        bundle.setParent((ResourceBundle) parent);
                    }

                    bundle.setBaseName(baseName);
                    bundle.setLocale(baseName, bundleName);
                    cache.put(factory, bundleName, defaultLocale, result);
                }
            }

            return result;
        }

        /**
         * 取得备选的bundle名.
         * 
         * @param baseName bundle的基本名
         * @param locale 区域设置
         * @return 所有备选的bundle名
         */
        private static List calculateBundleNames(String baseName, Locale locale) {
            final List result = new ArrayList(ResourceBundleConstant.MAX_BUNDLES_SEARCHED);
            final String language = locale.getLanguage();
            final int languageLength = language.length();
            final String country = locale.getCountry();
            final int countryLength = country.length();
            final String variant = locale.getVariant();
            final int variantLength = variant.length();

            // 如果locale是("", "", "").
            if (languageLength + countryLength + variantLength == 0) {
                return result;
            }

            final StringBuffer buffer = new StringBuffer(baseName);

            // 加入baseName_language
            buffer.append('_');
            buffer.append(language);

            if (languageLength > 0) {
                result.add(buffer.toString());
            }

            if (countryLength + variantLength == 0) {
                return result;
            }

            // 加入baseName_language_country
            buffer.append('_');
            buffer.append(country);

            if (countryLength > 0) {
                result.add(buffer.toString());
            }

            if (variantLength == 0) {
                return result;
            }

            // 加入baseName_language_country_variant
            buffer.append('_');
            buffer.append(variant);
            result.add(buffer.toString());

            return result;
        }

        /**
         * 掷出"resource bundle未找到"的异常.
         * 
         * @param missing 指定bundle未找到, 还是创建bundle错误
         * @param baseName 未找到的bundle基本名
         * @param locale 未找到的bundle的区域设置
         * @param cause 异常起因
         */
        private static void throwResourceBundleException(boolean missing, String baseName, Locale locale,
                                                         Throwable cause) {
            String bundleName = baseName + "_" + locale;

            if (missing) {
                throw new ResourceBundleException(ResourceBundleConstant.RB_MISSING_RESOURCE_BUNDLE, new Object[] {
                        baseName, locale }, cause, bundleName, "");
            } else {
                throw new ResourceBundleException(ResourceBundleConstant.RB_FAILED_LOADING_RESOURCE_BUNDLE,
                        new Object[] { baseName, locale }, cause, bundleName, "");
            }
        }
    }

    /**
     * 将(factory, bundleName, defaultLocale)映射到bundle对象的cache类.
     */
    private static final class Cache extends SoftHashMap {
        /** 静态的key, 用来在cache中查找bundle. 使用静态量可以减少GC的负担. 使用cacheKey必须对整个cache进行同步. */
        private static final CacheKey cacheKey = new CacheKey();

        /**
         * 这个hash表用来同步多个线程, 以便同时装入同一个bundle. 这个hash表保存了cacheKey到thread的映射.
         * 使用此hash表必须对整个cache进行同步.
         */
        private final Map underConstruction = new HashMap(ResourceBundleConstant.MAX_BUNDLES_SEARCHED,
                ResourceBundleConstant.CACHE_LOAD_FACTOR);

        /**
         * 构造一个cache.
         */
        public Cache() {
            super(ResourceBundleConstant.INITIAL_CACHE_SIZE, ResourceBundleConstant.CACHE_LOAD_FACTOR);
        }

        /**
         * 在cache中查找bundle.
         * 
         * @param factory bundle工厂
         * @param bundleName bundle名称
         * @param defaultLocale 系统locale
         * @return 被cache的bundle. 如果未找到, 则返回<code>null</code>
         */
        public synchronized Object get(ResourceBundleFactory factory, String bundleName, Locale defaultLocale) {
            cacheKey.set(factory, bundleName, defaultLocale);

            Object result = get(cacheKey);

            cacheKey.clear();
            return result;
        }

        /**
         * 在cache中查找bundle, 如果bundle不存在, 并且有另一个线程正在构造此bundle, 则等待之. 如果此方法返回
         * <code>null</code>, 则调用者必须负责调用<code>put</code>或
         * <code>cleanUpConstructionList</code>方法, 否则别的线程可能等待它, 而造成死锁.
         * 
         * @param factory bundle工厂
         * @param bundleName bundle名称
         * @param defaultLocale 系统locale
         * @return 被cache的bundle. 如果未找到, 则返回<code>null</code>
         */
        public synchronized Object getWait(ResourceBundleFactory factory, String bundleName, Locale defaultLocale) {
            Object result;

            // 首先查找cache中是否已经有这个bundle了, 如果有, 直接返回.
            cacheKey.set(factory, bundleName, defaultLocale);
            result = get(cacheKey);

            if (result != null) {
                cacheKey.clear();
                return result;
            }

            // 检查是不已经有另一个thread正在创建这个bundle.
            // 注意, 有可能递归调用getBundle方法, 例如, 在factory中调用了getBundle.
            // 这种情况下, beingBuilt == false
            Thread builder = (Thread) underConstruction.get(cacheKey);
            boolean beingBuilt = builder != null && builder != Thread.currentThread();

            // 如果已经有另一个thread正在创建这个bundle.
            if (beingBuilt) {
                while (beingBuilt) {
                    cacheKey.clear();

                    try {
                        // 等待, 直到别的线程创建完成.
                        wait();
                    } catch (InterruptedException e) {
                    }

                    cacheKey.set(factory, bundleName, defaultLocale);
                    beingBuilt = underConstruction.containsKey(cacheKey);
                }

                // 如果另一个线程把这个bundle创建好了, 则直接返回即可
                result = get(cacheKey);

                if (result != null) {
                    cacheKey.clear();
                    return result;
                }
            }

            // 如果bundle不在cache中, 则准备构造此bundle.
            // 调用者必须在随后调用put或cleanUpConstructionList方法, 否则将会死锁.
            underConstruction.put(cacheKey.clone(), Thread.currentThread());

            cacheKey.clear();

            return null;
        }

        /**
         * 将bundle放入cache, 并唤醒所有等待的线程.
         * 
         * @param factory bundle工厂
         * @param bundleName bundle名称
         * @param defaultLocale 系统locale
         * @param bundle 将被cache的bundle对象
         */
        public synchronized void put(ResourceBundleFactory factory, String bundleName, Locale defaultLocale,
                                     Object bundle) {
            cacheKey.set(factory, bundleName, defaultLocale);

            put(cacheKey.clone(), bundle);

            underConstruction.remove(cacheKey);

            cacheKey.clear();

            // 唤醒所有线程
            notifyAll();
        }

        /**
         * 从"正在构造bundle"的线程表中清除当前线程. 如果装入bundle失败, 则需要调用此方法.
         */
        public synchronized void cleanUpConstructionList() {
            final Collection entries = underConstruction.values();
            final Thread thisThread = Thread.currentThread();

            while (entries.remove(thisThread)) {
            }

            // 唤醒所有线程
            notifyAll();
        }
    }

    /**
     * 和bundle对应的cache key, 由bundle工厂, bundle名称, 系统locale几个字段组成.
     */
    private static final class CacheKey implements Cloneable {
        private SoftReference factoryRef;
        private String bundleName;
        private Locale defaultLocale;
        private int hashCode;

        /**
         * 设置cache key.
         * 
         * @param factory bundle工厂
         * @param bundleName bundle名称
         * @param defaultLocale 系统locale
         */
        public void set(ResourceBundleFactory factory, String bundleName, Locale defaultLocale) {
            this.bundleName = bundleName;
            this.hashCode = bundleName.hashCode();
            this.defaultLocale = defaultLocale;

            if (defaultLocale != null) {
                hashCode ^= defaultLocale.hashCode();
            }

            if (factory == null) {
                this.factoryRef = null;
            } else {
                factoryRef = new SoftReference(factory);
                hashCode ^= factory.hashCode();
            }
        }

        /**
         * 清除cache key.
         */
        public void clear() {
            set(null, "", null);
        }

        /**
         * 检查两个key是否匹配.
         * 
         * @param other 另一个cache key
         * @return 如果匹配, 则返回<code>true</code>
         */
        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            try {
                final CacheKey otherKey = (CacheKey) other;

                // hash值不同, 则立即返回
                if (hashCode != otherKey.hashCode) {
                    return false;
                }

                // bundle名称是否相同?
                if (!eq(bundleName, otherKey.bundleName)) {
                    return false;
                }

                // locale是否相同
                if (!eq(defaultLocale, otherKey.defaultLocale)) {
                    return false;
                }

                // factory是否相同?
                if (factoryRef == null) {
                    return otherKey.factoryRef == null;
                } else {
                    return otherKey.factoryRef != null && eq(factoryRef.get(), otherKey.factoryRef.get());
                }
            } catch (NullPointerException e) {
                return false;
            } catch (ClassCastException e) {
                return false;
            }
        }

        /**
         * 比较两个对象是否相等.
         * 
         * @param o1 对象1
         * @param o2 对象2
         * @return 如果相等, 则返回<code>true</code>
         */
        private boolean eq(Object o1, Object o2) {
            return o1 == null ? o2 == null : o1.equals(o2);
        }

        /**
         * 取得hash值, 如果两个对象等效, 则hash值也相等.
         * 
         * @return hash值
         */
        @Override
        public int hashCode() {
            return hashCode;
        }

        /**
         * 复制对象.
         * 
         * @return cache key的复本
         */
        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw new InternalError(MessageFormat.format(ResourceBundleConstant.RB_CLONE_NOT_SUPPORTED,
                        new Object[] { CacheKey.class.getName() }));
            }
        }

        /**
         * 取得字符串值表示.
         * 
         * @return 字符串表示
         */
        @Override
        public String toString() {
            return new StringBuffer("CacheKey[factory=").append(factoryRef == null ? "null" : factoryRef.get())
                    .append(", bundleName=").append(bundleName).append(", defaultLocale=").append(defaultLocale)
                    .append("]").toString();
        }
    }
}
