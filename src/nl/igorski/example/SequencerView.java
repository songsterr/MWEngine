package nl.igorski.example;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;

public class SequencerView extends SurfaceView implements SurfaceHolder.Callback
{
    private Caffeine _caffeine;

    /**
     * The constructor called from the main class / Activity
     *
     * @param context  {Context}
     * @param attrs    {AttributeSet}
     */
    public SequencerView( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        Log.d("MWENGINE", "argh");

        _caffeine = new Caffeine();
        _caffeine.start();
    }

    /* callbacks invoked when the surface dimensions change. */

    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
    {

    }

    public void surfaceCreated( SurfaceHolder arg0 )
    {

    }

    public void surfaceDestroyed( SurfaceHolder arg0 )
    {

    }

    private class Caffeine extends Thread
    {
        private boolean _isRunning;

        protected final Object _pauseLock;
        protected boolean      _paused;

        public Caffeine()
        {
            _pauseLock = new Object();
            _paused    = false;
            _isRunning = false;
        }

        @Override
        public void start()
        {
            if ( !_isRunning )
                super.start();
            else
                unpause();

            _isRunning = true;
        }

        /**
         * invoke when the application suspends, this should
         * halt the execution of the run method and cause the
         * thread to clean up to free CPU resources
         */
        public void pause()
        {
            synchronized ( _pauseLock )
            {
                _paused = true;
            }
        }

        /**
         * invoke when the application regains focus
         */
        public void unpause()
        {
            synchronized ( _pauseLock )
            {
                _paused = false;
                _pauseLock.notifyAll();
            }
        }

        @Override
        public void run()
        {
            long _lastRender = System.currentTimeMillis();
            long now;
            double foo;

            while ( _isRunning )
            {
                now = System.currentTimeMillis();

                for ( int i = 0; i < 2000000; ++i )
                    foo = Math.tan( Math.atan( Math.tan( Math.atan( i ))));

                Log.d( "MWENGINE", "calculations took " + ( now - _lastRender ) + "ms" );

                _lastRender = now;

                synchronized ( _pauseLock )
                {
                    while ( _paused )
                    {
                        try
                        {
                            _pauseLock.wait();
                        }
                        catch ( InterruptedException ex ) {}
                    }
                }
            }
        }
    }
}