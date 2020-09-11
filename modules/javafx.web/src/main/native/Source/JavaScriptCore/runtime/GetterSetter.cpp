/*
 *  Copyright (C) 1999-2002 Harri Porten (porten@kde.org)
 *  Copyright (C) 2001 Peter Kelly (pmk@post.com)
 *  Copyright (C) 2004-2017 Apple Inc. All rights reserved.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public License
 *  along with this library; see the file COPYING.LIB.  If not, write to
 *  the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 *  Boston, MA 02110-1301, USA.
 *
 */

#include "config.h"
#include "GetterSetter.h"

#include "Error.h"
#include "Exception.h"
#include "JSObject.h"
#include "JSCInlines.h"
#include <wtf/Assertions.h>

namespace JSC {

STATIC_ASSERT_IS_TRIVIALLY_DESTRUCTIBLE(GetterSetter);

const ClassInfo GetterSetter::s_info = { "GetterSetter", nullptr, nullptr, nullptr, CREATE_METHOD_TABLE(GetterSetter) };

void GetterSetter::visitChildren(JSCell* cell, SlotVisitor& visitor)
{
    GetterSetter* thisObject = jsCast<GetterSetter*>(cell);
    ASSERT_GC_OBJECT_INHERITS(thisObject, info());
    Base::visitChildren(thisObject, visitor);

    visitor.append(thisObject->m_getter);
    visitor.append(thisObject->m_setter);
}

JSValue callGetter(JSGlobalObject* globalObject, JSValue base, JSValue getterSetter)
{
    VM& vm = globalObject->vm();
    auto scope = DECLARE_THROW_SCOPE(vm);
    // FIXME: Some callers may invoke get() without checking for an exception first.
    // We work around that by checking here.
    RETURN_IF_EXCEPTION(scope, scope.exception()->value());

    JSObject* getter = jsCast<GetterSetter*>(getterSetter)->getter();

    CallData callData;
    CallType callType = getter->methodTable(vm)->getCallData(getter, callData);
    RELEASE_AND_RETURN(scope, call(globalObject, getter, callType, callData, base, ArgList()));
}

bool callSetter(JSGlobalObject* globalObject, JSValue base, JSValue getterSetter, JSValue value, ECMAMode ecmaMode)
{
    VM& vm = globalObject->vm();
    auto scope = DECLARE_THROW_SCOPE(vm);

    GetterSetter* getterSetterObj = jsCast<GetterSetter*>(getterSetter);

    if (getterSetterObj->isSetterNull())
        return typeError(globalObject, scope, ecmaMode == StrictMode, ReadonlyPropertyWriteError);

    JSObject* setter = getterSetterObj->setter();

    MarkedArgumentBuffer args;
    args.append(value);
    ASSERT(!args.hasOverflowed());

    CallData callData;
    CallType callType = setter->methodTable(vm)->getCallData(setter, callData);
    scope.release();
    call(globalObject, setter, callType, callData, base, args);
    return true;
}

} // namespace JSC
