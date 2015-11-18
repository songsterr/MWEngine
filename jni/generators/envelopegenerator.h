/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Igor Zinken - http://www.igorski.nl
 *
 * wave table generation adapted from sources by Matt @ hackmeopen.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
#ifndef __ENVELOPEGENERATOR_H_INCLUDED__
#define __ENVELOPEGENERATOR_H_INCLUDED__

#include "../global.h"
#include "../wavetable.h"

namespace EnvelopeGenerator
{
    // waveTable is the output WaveTable the envelope shape is generated into
    // startAmplitude describes the amplitude level at the beginning of the envelope where
    // endAmplitude describes the amplitude level at the end of the envelope, e.g.
    // a startAmplitude of MAX_PHASE and an endAmplitude of 0.0 would create a fade out-envelope
    // and a startAmplitude of 0.0 and an endAmplitude of MAX_PHASE would create a fade in-envelope
    // releaseTime describes the release of the envelope in milliseconds

    extern void generate( WaveTable* waveTable, SAMPLE_TYPE startAmplitude, SAMPLE_TYPE endAmplitude, float releaseTime );
}

#endif
