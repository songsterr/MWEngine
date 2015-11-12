#!/bin/sh

# custom to your machine : Android NDK library location
#export ANDROID_NDK_ROOT=/Library/AndroidNDK

# the input folders for the C++ sources
INPUT="-I./jni"

rm -rf src/nl/igorski/lib/audio/nativeaudio
mkdir -p src/nl/igorski/lib/audio/nativeaudio

./swig -java -package nl.igorski.lib.audio.nativeaudio -DNO_RTIO -c++ -lcarrays.i -verbose -outdir src/nl/igorski/lib/audio/nativeaudio -I/usr/local/include -I/System/Library/Frameworks/JavaVM.framework/Headers $INPUT -o jni/jni/java_interface_wrap.cpp mwengine.i

#$ANDROID_NDK_ROOT/ndk-build TARGET_PLATFORM=android-14 V=1




