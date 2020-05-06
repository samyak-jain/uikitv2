# Agora UI Kit V2

2 Ways to Use it

## 1st Method: Templates

List of Templates:
1. Group Template
... More Coming Soon

### Usage

In the onCreate method of your activity, do the following: 

1. Call the bootstrap method

```
AgoraRTC.instance().bootstrap(this, "appid", "channel");
```

2. Set the Layout to the desired template

For Example:
```
setContentView(R.layout.group_template)
```

## 2nd Method: UI Composition

You can quickly generate a custom UI using the pre built components packaged with the UI Kit

List of Components:
- AgoraView: Uses SurfaceView internally to display the video feed
- AgoraTextureView: Uses TextureView internally to display video feed
- AudioButton: Button to automatically mute/unmute audio
- VideoButton: Button to automatically mute/unmute video
- SwitchCameraButton: Button to automatically switch the camera between front and rear
- EndCallButton: Button to end the call
- AgoraButton: Generic Button with pre made styles (optionally can be overriden) that can be used for misc functionality
- AgoraChannelTextView: A TextView that contains the name of the channel 
- AgoraRecyclerView: A RecyclerView that contains a list of AgoraView objects. 
- RemoteCountHolder: A styled component containing number of remote users in the call

### Usage

Example:

Let's see how to render a local view into the layout with rounded corners
```
<io.agora.uikit.ui.AgoraView 
	android:layout_width="match_parent"
	android_layout_height="100dp"
	app:local="true"
	app:corner_radius="5dp"
/>
```

Styling for all the components is done the same way you would style their parents
