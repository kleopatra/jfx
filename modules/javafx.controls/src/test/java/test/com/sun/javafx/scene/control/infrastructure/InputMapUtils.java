/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package test.com.sun.javafx.scene.control.infrastructure;

import java.util.List;
import java.util.stream.Collectors;

import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.Mapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;

import javafx.collections.ObservableList;
import javafx.scene.Node;

public class InputMapUtils {

 
//--------------- helpers for mappings/inputmap state     
    /**
     * Returns true if there the binding is active in any of the given 
     * InputMaps.
     * 
     * @param maps
     * @param binding
     * @return
     */
    protected boolean isActive(ObservableList<InputMap> maps, KeyBinding binding) {
        for (InputMap map : maps) {
            if (isActive(map, binding)) return true;
        }
        return false;
    }
    
    /**
     * 
     * @param map
     * @param binding
     * @return
     */
    protected boolean isActive(InputMap map, KeyBinding binding) {
        if (lookupMappingKey(map, binding).isEmpty()) return false;
        return isActiveMap(map);
    }
    
    /**
     * Returns true if the given map is active, that is either has no
     * interceptor or the interceptor doesn't block.
     * 
     * @param map
     * @return
     */
    protected boolean isActiveMap(InputMap map) {
        return map.getInterceptor() == null || !map.getInterceptor().test(null);
    }
    
    /**
     * Returns true if the given InputMap has at least one mapping for the
     * given key that is not disabled.
     * 
     * @param map
     * @param mappingKey
     * @return
     */
    protected boolean hasMapping(InputMap<Node> map, Object mappingKey) {
        return !lookupMappingKey(map, mappingKey).isEmpty();
    }
    
    private List<Mapping<?>> lookupMappingKey(InputMap<Node> map, Object mappingKey) {
        return map.getMappings().stream()
                .filter(mapping -> !mapping.isDisabled())
                .filter(mapping -> mappingKey.equals(mapping.getMappingKey()))
                .collect(Collectors.toList());
    }

//-------- end helpers
    
    
//    @Test
//    public void testListViewKeyBindings() {
//        ListView<String> listView = new ListView<>();
//        installDefaultSkin(listView);
//        ListViewBehavior<String> behavior = (ListViewBehavior<String>) getBehavior(listView.getSkin());
//        List<KeyBinding> bindings = getIgnoredListKeyBindings(behavior);
//        assertFalse("ignored bindings must not be empty", bindings.isEmpty());
//        InputMap<?> inputMap = behavior.getInputMap();
//        bindings.forEach(binding -> {
//            Optional<Mapping<?>> mapping = inputMap.lookupMapping(binding);
////            System.out.println("mapping: " + mapping + " for binding " + binding);
//            assertTrue("mapping must be available for " + binding, mapping.isPresent());
//        });
//    }
}
