package nl.igorski.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import nl.igorski.lib.audio.MWEngine;
import nl.igorski.lib.audio.definitions.Pitch;
import nl.igorski.lib.audio.helpers.DevicePropertyCalculator;
import nl.igorski.lib.audio.nativeaudio.*;

import java.util.Vector;

public class MWEngineActivity extends Activity
{
    public final String LOG_ID = "MWENGINE";

    /**
     * IMPORTANT : when creating native layer objects through JNI it
     * is important to remember that when the Java references go out of scope
     * (and thus are finalized by the garbage collector), the SWIG interface
     * will invoke the native layer destructors. As such we hold strong
     * references to JNI Objects during the application lifetime
     */
    private Finalizer _finalizer;
    private LPFHPFilter _lpfhpf;
    private SynthInstrument _synth1;
    private SynthInstrument     _synth2;
    private Filter              _filter;
    private Phaser              _phaser;
    private Delay               _delay;
    private MWEngine _engine;
    private SequencerController _sequencerController;
    private Vector<SynthEvent>  _synth1Events;
    private Vector<SynthEvent>  _synth2Events;

    private boolean _sequencerPlaying = false;
    private boolean _inited           = false;

    private float minFilterCutoff = 50.0f;
    private float maxFilterCutoff;

    private int SAMPLE_RATE;
    private int BUFFER_SIZE;

    private static String LOG_TAG = "MWENGINE"; // logcat identifier

    /* public methods */

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );

        init();
    }

    private void init()
    {
        if ( _inited )
            return;

        Log.d( LOG_TAG, "initing MWEngineActivity" );

        // STEP 1 : preparing the native audio engine

        _engine = new MWEngine( getApplicationContext(), new StateObserver() );

        // get the recommended buffer size for this device (NOTE : lower buffer sizes may
        // provide lower latency, but make sure all buffer sizes are powers of two of
        // the recommended buffer size (overcomes glitching in buffer callbacks )
        // getting the correct sample rate upfront will omit having audio going past the system
        // resampler reducing overall latency

        BUFFER_SIZE = DevicePropertyCalculator.getRecommendedBufferSize( getApplicationContext() );
        SAMPLE_RATE = DevicePropertyCalculator.getRecommendedSampleRate( getApplicationContext() );

        _engine.createOutput( SAMPLE_RATE, BUFFER_SIZE );
        _sequencerController = _engine.getSequencerController();

        // cache some of the engines properties

        final ProcessingChain masterBus     = _engine.getMasterBusProcessors();
        final SequencerController sequencer = _engine.getSequencerController();

        sequencer.updateMeasures( 1, 16 ); // we'll loop just a single measure with 16th note subdivisions
        _engine.start();                   // starts engines render thread (NOTE : sequencer is still paused!)

        final int outputChannels = 1;   // see global.h

        // create a lowpass filter to catch all low rumbling and a Finalizer (limiter) to prevent clipping of output :)
        _lpfhpf    = new LPFHPFilter(( float )  MWEngine.SAMPLE_RATE, 55, outputChannels );
        _finalizer = new Finalizer  ( 2f, 500f, MWEngine.SAMPLE_RATE,     outputChannels );

        masterBus.addProcessor( _finalizer );
        masterBus.addProcessor( _lpfhpf );

        // STEP 2 : let's create some instruments =D

        _synth1 = new SynthInstrument();
        _synth2 = new SynthInstrument();

        _synth1.getOscillatorProperties( 0 ).setWaveform( 2 ); // sawtooth (see global.h for enumerations)
        _synth2.getOscillatorProperties( 0 ).setWaveform( 5 ); // pulse width modulation

        // a high decay for synth 1 (bubblier effect)
        _synth1.getAdsr().setDecay( .9f );

        // add a filter to synth 1
        maxFilterCutoff = ( float ) SAMPLE_RATE / 8;

        _filter = new Filter( maxFilterCutoff / 2, ( float ) ( Math.sqrt( 1 ) / 2 ), minFilterCutoff, maxFilterCutoff, 0f, 1 );
        _synth1.getAudioChannel().getProcessingChain().addProcessor( _filter );

        // add a phaser to synth 1
        _phaser = new Phaser( .5f, .7f, .5f, 440.f, 1600.f );
        _synth1.getAudioChannel().getProcessingChain().addProcessor( _phaser );

        // add some funky delay to synth 2
        _delay = new Delay( 250, 2000, .35f, .5f, outputChannels );
        _synth2.getAudioChannel().getProcessingChain().addProcessor( _delay );

        // prepare synthesizer volumes
        _synth2.setVolume( .7f );

        // STEP 3 : let's create some music !

        _synth1Events = new Vector<SynthEvent>();   // remember : strong references!
        _synth2Events = new Vector<SynthEvent>();   // remember : strong references!
        sequencer.setTempoNow( 130.0f, 4, 4 );      // 130 BPM at 4/4 time

        // bubbly sixteenth note bass line for synth 1

        createSynthEvent( _synth1, Pitch.note( "C", 2 ),  0 );
        createSynthEvent( _synth1, Pitch.note( "C", 2 ),  1 );
        createSynthEvent( _synth1, Pitch.note( "C", 3 ),  2 );
        createSynthEvent( _synth1, Pitch.note( "C", 2 ),  3 );
        createSynthEvent( _synth1, Pitch.note( "A#", 1 ), 4 );
        createSynthEvent( _synth1, Pitch.note( "C", 2 ),  5 );
        createSynthEvent( _synth1, Pitch.note( "C", 3 ),  6 );
        createSynthEvent( _synth1, Pitch.note( "C", 2 ),  7 );
        createSynthEvent( _synth1, Pitch.note( "C", 2 ),  8 );
        createSynthEvent( _synth1, Pitch.note( "C", 2 ),  9 );
        createSynthEvent( _synth1, Pitch.note( "D#", 2 ), 10 );
        createSynthEvent( _synth1, Pitch.note( "C", 2 ),  11 );
        createSynthEvent( _synth1, Pitch.note( "A#", 1 ), 12 );
        createSynthEvent( _synth1, Pitch.note( "A#", 2 ), 13 );
        createSynthEvent( _synth1, Pitch.note( "C", 2 ),  14 );
        createSynthEvent( _synth1, Pitch.note( "C", 2 ),  15 );

        // off-beat minor seventh chord stabs for synth 2

        createSynthEvent( _synth2, Pitch.note( "C", 3 ),  4 );
        createSynthEvent( _synth2, Pitch.note( "G", 3 ),  4 );
        createSynthEvent( _synth2, Pitch.note( "A#", 3 ), 4 );
        createSynthEvent( _synth2, Pitch.note( "D#", 3 ), 4 );

        createSynthEvent( _synth2, Pitch.note( "D", 3 ), 8 );
        createSynthEvent( _synth2, Pitch.note( "A", 3 ), 8 );
        createSynthEvent( _synth2, Pitch.note( "C", 3 ), 8 );
        createSynthEvent( _synth2, Pitch.note( "F", 3 ), 8 );

        // STEP 4 : attach event handler to the UI elements (see main.xml layout)

        final Button playPauseButton = ( Button ) findViewById( R.id.PlayPauseButton );
        playPauseButton.setOnClickListener( new PlayClickHandler() );

        final SeekBar filterSlider = ( SeekBar ) findViewById( R.id.FilterCutoffSlider );
        filterSlider.setOnSeekBarChangeListener( new FilterCutOffChangeHandler() );

        final SeekBar decaySlider = ( SeekBar ) findViewById( R.id.SynthDecaySlider );
        decaySlider.setOnSeekBarChangeListener( new SynthDecayChangeHandler() );

        final SeekBar feedbackSlider = ( SeekBar ) findViewById( R.id.MixSlider );
        feedbackSlider.setOnSeekBarChangeListener( new DelayMixChangeHandler() );

        final SeekBar tempoSlider = ( SeekBar ) findViewById( R.id.TempoSlider );
        tempoSlider.setOnSeekBarChangeListener( new TempoChangeHandler() );

        _inited = true;
    }

    /* protected methods */

    @Override
    public void onWindowFocusChanged( boolean hasFocus )
    {
        Log.d( LOG_TAG, "window focus changed for MWEngineActivity, has focus > " + hasFocus );

        if ( !hasFocus )
        {
            // suspending the app - stop threads to save CPU cycles

            if ( _engine != null )
            {
                _engine.pause();
                _engine.dispose();
            }
        }
        else
        {
            // returning to the app
            if ( !_inited )
                init();
            else
                _engine.start(); // resumes audio render thread
        }
    }

    /* event handlers */

    /**
     *  invoked when user presses the play / pause button
     */
    private class PlayClickHandler implements View.OnClickListener
    {
        public void onClick( View v )
        {
            // start/stop the sequencer so we can toggle hearing actual output! ;)

            _sequencerPlaying = !_sequencerPlaying;
            _engine.getSequencerController().setPlaying( _sequencerPlaying );
        }
    }

    /**
     *  invoked when user interacts with the filter cutoff slider
     */
    private class FilterCutOffChangeHandler implements SeekBar.OnSeekBarChangeListener
    {
        public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
        {
            _filter.setCutoff(( progress / 100f ) * ( maxFilterCutoff - minFilterCutoff ) + minFilterCutoff );
        }

        public void onStartTrackingTouch( SeekBar seekBar ) {}
        public void onStopTrackingTouch ( SeekBar seekBar ) {}
    }

    private class SynthDecayChangeHandler implements SeekBar.OnSeekBarChangeListener
    {
        public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
        {
            _synth1.getAdsr().setDecay( progress / 100f );
            _synth1.updateEvents(); // update all synth events to match new ADSR properties
        }

        public void onStartTrackingTouch( SeekBar seekBar ) {}
        public void onStopTrackingTouch ( SeekBar seekBar ) {}
    }

    /**
     *  invoked when user interacts with the delay mix slider
     */
    private class DelayMixChangeHandler implements SeekBar.OnSeekBarChangeListener
    {
        public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
        {
            _delay.setFeedback( progress / 100f );
        }

        public void onStartTrackingTouch( SeekBar seekBar ) {}
        public void onStopTrackingTouch ( SeekBar seekBar ) {}
    }

    /**
     *  invoked when user interacts with the tempo slider
     */
    private class TempoChangeHandler implements SeekBar.OnSeekBarChangeListener
    {
        public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
        {
            final float minTempo = 40f;     // minimum allowed tempo is 40 BPM
            final float maxTempo = 260f;    // maximum allowed tempo is 260 BPM

            final float newTempo = ( progress / 100f ) * ( maxTempo - minTempo ) + minTempo;

            _engine.getSequencerController().setTempo( newTempo, 4, 4 ); // update to match new tempo in 4/4 time
        }

        public void onStartTrackingTouch( SeekBar seekBar ) {}
        public void onStopTrackingTouch ( SeekBar seekBar ) {}
    }

    /* state change message listener */

    private class StateObserver implements MWEngine.IObserver
    {
        // cache the enumerations (from native layer) as integer Array

        private final Notifications.ids[] _notificationEnums = Notifications.ids.values();

        public void handleNotification( int aNotificationId )
        {
            switch ( _notificationEnums[ aNotificationId ])
            {
                case ERROR_HARDWARE_UNAVAILABLE:

                    Log.d( LOG_TAG, "ERROR : received Open SL error callback from native layer" );

                    // re-initialize thread
                    if ( _engine.canRestartEngine() )
                    {
                        _engine.dispose();
                        _engine.createOutput( SAMPLE_RATE, BUFFER_SIZE );
                        _engine.start();
                    }
                    else {
                        Log.d( LOG_TAG, "exceeded maximum amount of retries. Cannot continue using audio engine" );
                    }
                    break;

                case MARKER_POSITION_REACHED:

                    Log.d( LOG_TAG, "Marker position has been reached" );
                    break;
            }
        }

        public void handleNotification( int aNotificationId, int aNotificationValue )
        {
            switch ( _notificationEnums[ aNotificationId ])
            {
                case SEQUENCER_POSITION_UPDATED:

                    // for this notification id, the notification value describes the precise buffer offset of the
                    // engine when the notification fired (as a value in the range of 0 - BUFFER_SIZE). using this value
                    // we can calculate the amount of samples pending until the next step position is reached
                    // which in turn allows us to calculate the engine latency

                    int sequencerPosition = _sequencerController.getStepPosition();
                    int elapsedSamples    = _sequencerController.getBufferPosition();

                    Log.d( LOG_TAG, "seq. position: " + sequencerPosition + ", buffer offset: " + aNotificationValue +
                            ", elapsed samples: " + elapsedSamples );
                    break;
            }
        }
    }

    /* private methods */

    /**
     * convenience method for creating a new SynthEvent (a "musical instruction") for a given
     * SynthInstrument, this defaults to a note of a 16th note duration in this context
     *
     * @param synth     {SynthInstrument} the instrument that is to play the note
     * @param frequency {double} frequency in Hz of the note to play
     * @param position  {int}    position the position of the note within the bar
     */
    private void createSynthEvent( SynthInstrument synth, double frequency, int position )
    {
        final int duration     = 1; // 16th note at a BAR_SUBDIVISION of 16 (see MWEngine)
        final SynthEvent event = new SynthEvent(( float ) frequency, position, duration, synth );

        event.calculateBuffers();

        if ( synth == _synth1 )
            _synth1Events.add( event );
        else
            _synth2Events.add( event );
    }
}
