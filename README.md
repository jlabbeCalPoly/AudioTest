1. We created an audio scope for our final project, which is a graphical representation of the varying amplitudes (based on loudness) taken from a group of signals. Currently, a user of our app is able to click the "Start" and "Stop" button, which begins/terminates real-time audio recording accordingly. When the user pressed the "Stop" button while the graph is being displayed/audio data is being recorded, they're able to see a snapshot of the last recorded audio data and their respective amplitudes.
2. Figma Design: https://docs.google.com/document/d/1YV3YtTGTIyGIcUngwJ0iycdPodyM0bzve1IOACq9J_0/edit?usp=sharing
3. AudioRecorder: Allows our app to process real-time audio data from the device's built-in microphone. Canvas()/Path(): Allows our app to graphically display the audio data by drawing many lines consecutively, creating a graph.
4. The user must grant recording permissions in order to get use out of our app. The oldest SDK our project will use is 24, but the target is 35.
5. Lots of effort was made to ensure that our app and its contents displayed nicely for the user, regardless of whether or not the phone was in landscape or portrait mode. Rotating the phone to landscape would make the graph larger, making it easier to analyze the snapshot of audio data. In addition, we needed to devise a clever way to resize the graph if it was paused in order to avoid an infinite loop (Recomputing the path after a configuration change would cause a recomposition, but each recomposition computes the path in the event the data aquisition is paused, leading to an infinite loop). The way we got around this was using activity lifecycle events to determine when a reconfiguration occured. 
