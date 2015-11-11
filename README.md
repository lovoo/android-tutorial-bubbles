# AndroidTutorialBubbles [![Build Status](https://travis-ci.org/Lovoo/android-tutorial-bubbles.svg)](https://travis-ci.org/Lovoo/android-tutorial-bubbles)
A little ui framework that displays a styled tutorial bubble, which positions and scales itself based on a given anchor view.

###Usage

If you have some element within your layout, that should be explained with a small tutorial, this small library may be your way to go! 

__Features:__
* displays a styleable bubble with custom XML layout
* popup bubble scales itself according to its content and relative to the given anchor view and the available screen space
* bubble displays a dynamically drawn funnel that points toward the anchor view
* anchor view or any other view can be highlighted while the background is dimmed
* simple builder-pattern with chaining config calls

The library uses two approaches to display the tutorial bubble. If you supply a parent view within the builder, the parent will be used to draw the tutorial. 
If no parent view is set and you set `android.permission.SYSTEM_ALERT_WINDOW` permission in your manifest instead, the system window will be used to draw the bubble. In the later case, you'll need to relay the `onResume()` and `onPause()` events from either an activity or fragment to your tutorial. 

Check out the code example in the demo project.

###Screenshots

|<img src="https://github.com/Lovoo/android-tutorial-bubbles/blob/master/screen1.png" width="270" height="480" />|
|<img src="https://github.com/Lovoo/android-tutorial-bubbles/blob/master/screen2.png" width="270" height="480" />|


Licence

Copyright (c) 2015, LOVOO GmbH
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of LOVOO GmbH nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
