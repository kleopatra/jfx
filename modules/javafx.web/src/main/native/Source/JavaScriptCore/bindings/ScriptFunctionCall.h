/*
 * Copyright (C) 2013 Apple Inc. All rights reserved.
 * Copyright (C) 2009 Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#pragma once

#include "ArgList.h"
#include "Exception.h"
#include "ScriptObject.h"
#include <wtf/Expected.h>
#include <wtf/text/WTFString.h>

namespace JSC {
class JSValue;
}

namespace Deprecated {

class JS_EXPORT_PRIVATE ScriptCallArgumentHandler {
public:
    ScriptCallArgumentHandler(JSC::JSGlobalObject* globalObject) : m_globalObject(globalObject) { }

    void appendArgument(const char*);
    void appendArgument(const String&);
    void appendArgument(JSC::JSValue);
    void appendArgument(long);
    void appendArgument(long long);
    void appendArgument(unsigned int);
    void appendArgument(uint64_t);
    void appendArgument(int);
    void appendArgument(bool);

protected:
    JSC::MarkedArgumentBuffer m_arguments;
    JSC::JSGlobalObject* m_globalObject;

private:
    // MarkedArgumentBuffer must be stack allocated, so prevent heap
    // alloc of ScriptFunctionCall as well.
    void* operator new(size_t) { ASSERT_NOT_REACHED(); return reinterpret_cast<void*>(0xbadbeef); }
    void* operator new[](size_t) { ASSERT_NOT_REACHED(); return reinterpret_cast<void*>(0xbadbeef); }
};

class JS_EXPORT_PRIVATE ScriptFunctionCall : public ScriptCallArgumentHandler {
public:
    typedef JSC::JSValue (*ScriptFunctionCallHandler)(JSC::JSGlobalObject* globalObject, JSC::JSValue functionObject, const JSC::CallData& callData, JSC::JSValue thisValue, const JSC::ArgList& args, NakedPtr<JSC::Exception>&);
    ScriptFunctionCall(const ScriptObject& thisObject, const String& name, ScriptFunctionCallHandler handler = nullptr);
    Expected<JSC::JSValue, NakedPtr<JSC::Exception>> call();

protected:
    ScriptFunctionCallHandler m_callHandler;
    ScriptObject m_thisObject;
    String m_name;
};

} // namespace Deprecated
