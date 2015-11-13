#!/bin/sh

# the input folders for the C++ sources
INPUT="-I./jni"

rm -rf src/nl/igorski/lib/audio/nativeaudio
mkdir -p src/nl/igorski/lib/audio/nativeaudio

/usr/local/bin/swig -java -package nl.igorski.lib.audio.nativeaudio -DNO_RTIO -c++ -lcarrays.i -verbose -outdir src/nl/igorski/lib/audio/nativeaudio -I/usr/local/include -I/System/Library/Frameworks/JavaVM.framework/Headers $INPUT -o jni/jni/java_interface_wrap.cpp mwengine.i

$ANDROID_NDK_HOME/ndk-build TARGET_PLATFORM=android-14 V=1





