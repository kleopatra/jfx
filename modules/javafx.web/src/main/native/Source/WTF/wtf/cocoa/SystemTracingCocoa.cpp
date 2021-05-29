/*
* Copyright (C) 2019 Apple Inc. All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY APPLE INC. ``AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
* PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL APPLE INC. OR
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
* OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

#import "config.h"
#import <wtf/SystemTracing.h>

#import <cstdlib>
#import <dispatch/dispatch.h>
#import <wtf/spi/darwin/OSVariantSPI.h>

bool WTFSignpostsEnabled()
{
    static dispatch_once_t once;
    static bool enabled;

    dispatch_once(&once, ^{
        // Signposts may contain sensitive info that we don't want to emit to logd except when
        // profiling (such as URLs). To guard against accidental leakage, only enable them on Apple
        // internal builds when an environment variable is set.
        if (os_variant_allows_internal_security_policies("com.apple.WebKit"))
            enabled = !strcmp(getenv("WEBKIT_SIGNPOSTS_ENABLED") ?: "0", "1");
    });

    return enabled;
}

os_log_t WTFSignpostLogHandle()
{
    static dispatch_once_t once;
    static os_log_t handle;

    dispatch_once(&once, ^{
        handle = os_log_create("com.apple.WebKit", "Signposts");
    });

    return handle;
}
