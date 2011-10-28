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

package com.alibaba.toolkit.util.resourcebundle.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import com.alibaba.toolkit.util.ContextClassLoader;
import com.alibaba.toolkit.util.collection.ArrayHashMap;
import com.alibaba.toolkit.util.collection.ListMap;
import com.alibaba.toolkit.util.enumeration.Enum;
import com.alibaba.toolkit.util.resourcebundle.ResourceBundle;
import com.alibaba.toolkit.util.resourcebundle.ResourceBundleConstant;
import com.alibaba.toolkit.util.resourcebundle.ResourceBundleCreateException;
import com.alibaba.toolkit.util.resourcebundle.ResourceBundleEnumeration;

/**
 * XML格式的<code>ResourceBundle</code>.
 * 
 * @version $Id: XMLResourceBundle.java,v 1.1 2003/07/03 07:26:35 baobao Exp $
 * @author Michael Zhou
 */
public class XMLResourceBundle extends ResourceBundle {
    protected ListMap values = new ArrayHashMap();

    /**
     * 从XML文档中创建<code>ResourceBundle</code>.
     * 
     * @param doc XML文档
     * @throws ResourceBundleCreateException 解析错误
     */
    public XMLResourceBundle(Document doc) throws ResourceBundleCreateException {
        // 解析group.
        for (Iterator i = doc.selectNodes(ResourceBundleConstant.XPATH_GROUPS).iterator(); i.hasNext();) {
            Node groupNode = (Node) i.next();

            initGroup(groupNode);
        }

        // 解析没有group的resource.
        for (Iterator i = doc.selectNodes(ResourceBundleConstant.XPATH_UNGROUPED_RESOURCES).iterator(); i.hasNext();) {
            Node resourceNode = (Node) i.next();

            initResource(resourceNode, null);
        }
    }

    /**
     * 根据XML Node初始化一个resource项.
     * 
     * @param groupNode 代表resource信息的XML Node
     * @throws ResourceBundleCreateException 解析错误
     */
    protected void initGroup(Node groupNode) throws ResourceBundleCreateException {
        String enumTypeName = (String) groupNode.selectObject(ResourceBundleConstant.XPATH_GROUP_ENUM);
        Class enumType = null;

        if (enumTypeName.length() > 0) {
            try {
                enumType = ContextClassLoader.loadClass(enumTypeName);
            } catch (ClassNotFoundException e) {
                throw new ResourceBundleCreateException(ResourceBundleConstant.RB_ENUM_CLASS_NOT_FOUND, new Object[] {
                        enumTypeName, ContextClassLoader.getClassLoader() }, e);
            }
        }

        for (Iterator i = groupNode.selectNodes(ResourceBundleConstant.XPATH_RESOURCES).iterator(); i.hasNext();) {
            Node resourceNode = (Node) i.next();

            initResource(resourceNode, enumType);
        }
    }

    /**
     * 根据XML Node初始化一个resource项.
     * 
     * @param resourceNode 代表resource信息的XML Node
     * @param enumType <code>Enum</code>类
     * @throws ResourceBundleCreateException 解析错误
     */
    protected void initResource(Node resourceNode, Class enumType) throws ResourceBundleCreateException {
        String id = (String) resourceNode.selectObject(ResourceBundleConstant.XPATH_RESOURCE_ID);

        // 如果指定了enum属性, 则以此enum值作为resource key.
        if (enumType != null) {
            Enum enumObj = Enum.getEnumByName(enumType, id);

            if (enumObj == null) {
                throw new ResourceBundleCreateException(ResourceBundleConstant.RB_ENUM_ID_NOT_FOUND, new Object[] { id,
                        enumType.getName() }, null);
            }

            id = enumObj.toString();
        }

        Object value = null;
        String type = resourceNode.getName();

        if (ResourceBundleConstant.RB_RESOURCE_TYPE_MESSAGE.equals(type)) {
            value = getMessageResource(id, resourceNode);
        } else if (ResourceBundleConstant.RB_RESOURCE_TYPE_MAP.equals(type)) {
            value = getMapResource(id, resourceNode);
        } else if (ResourceBundleConstant.RB_RESOURCE_TYPE_LIST.equals(type)) {
            value = getListResource(id, resourceNode);
        }

        if (values.containsKey(id)) {
            throw new ResourceBundleCreateException(ResourceBundleConstant.RB_DUPLICATED_RESOURCE_KEY,
                    new Object[] { id }, null);
        }

        values.put(id, value);
    }

    /**
     * 根据XML Node创建message resource项.
     * 
     * @param id resource ID
     * @param resourceNode 代表resource信息的XML Node
     * @return resource的值
     * @throws ResourceBundleCreateException 解析错误
     */
    protected Object getMessageResource(String id, Node resourceNode) throws ResourceBundleCreateException {
        return resourceNode.selectObject(ResourceBundleConstant.XPATH_RESOURCE_MESSAGE_DATA);
    }

    /**
     * 根据XML Node创建map resource项.
     * 
     * @param id resource ID
     * @param resourceNode 代表resource信息的XML Node
     * @return resource的值
     * @throws ResourceBundleCreateException 解析错误
     */
    protected Object getMapResource(String id, Node resourceNode) throws ResourceBundleCreateException {
        ListMap map = new ArrayHashMap();

        for (Iterator i = resourceNode.selectNodes(ResourceBundleConstant.XPATH_RESOURCES).iterator(); i.hasNext();) {
            Node mapItemNode = (Node) i.next();
            Object mapKey = mapItemNode.selectObject(ResourceBundleConstant.XPATH_RESOURCE_ID);

            if (map.containsKey(id)) {
                throw new ResourceBundleCreateException(ResourceBundleConstant.RB_DUPLICATED_MAP_RESOURCE_KEY,
                        new Object[] { mapKey, id }, null);
            }

            String mapItemType = mapItemNode.getName();
            Object value = null;

            if (ResourceBundleConstant.RB_RESOURCE_TYPE_MESSAGE.equals(mapItemType)) {
                value = getMessageResource(id, mapItemNode);
            } else if (ResourceBundleConstant.RB_RESOURCE_TYPE_MAP.equals(mapItemType)) {
                value = getMapResource(id, mapItemNode);
            } else if (ResourceBundleConstant.RB_RESOURCE_TYPE_LIST.equals(mapItemType)) {
                value = getListResource(id, mapItemNode);
            }

            map.put(mapKey, value);
        }

        return Collections.unmodifiableMap(map);
    }

    /**
     * 根据XML Node创建list resource项.
     * 
     * @param id resource ID
     * @param resourceNode 代表resource信息的XML Node
     * @return resource的值
     * @throws ResourceBundleCreateException 解析错误
     */
    protected Object getListResource(String id, Node resourceNode) throws ResourceBundleCreateException {
        List list = new ArrayList();

        for (Iterator i = resourceNode.selectNodes(ResourceBundleConstant.XPATH_RESOURCES).iterator(); i.hasNext();) {
            Node listItemNode = (Node) i.next();
            String listItemType = listItemNode.getName();
            Object value = null;

            if (ResourceBundleConstant.RB_RESOURCE_TYPE_MESSAGE.equals(listItemType)) {
                value = getMessageResource(id, listItemNode);
            } else if (ResourceBundleConstant.RB_RESOURCE_TYPE_MAP.equals(listItemType)) {
                value = getMapResource(id, listItemNode);
            } else if (ResourceBundleConstant.RB_RESOURCE_TYPE_LIST.equals(listItemType)) {
                value = getListResource(id, listItemNode);
            }

            list.add(value);
        }

        return Collections.unmodifiableList(list);
    }

    /**
     * 根据指定的键, 从resource bundle中取得相应的对象. 如果返回<code>null</code>表示对应的对象不存在.
     * 
     * @param key 要查找的键
     * @return key对应的对象, 或<code>null</code>表示不存在该对象
     */
    @Override
    protected Object handleGetObject(String key) {
        return values.get(key);
    }

    /**
     * 取得所有keys.
     * 
     * @return 所有keys
     */
    @Override
    public Enumeration getKeys() {
        java.util.ResourceBundle parent = getParent();

        return new ResourceBundleEnumeration(values.keySet(), parent != null ? parent.getKeys() : null);
    }
}
