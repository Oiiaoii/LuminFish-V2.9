# LuminFish 2.5 - PIXEL RECOGNITION (the method macro scripts use)

Built on 2.4: nothing was removed. Picture recognition (OpenCV) is still there as the backup.

## New: Pix.kt - script-style pixel search
- Looks for a colour on the screen (like PixelSearch in a script), then checks the shape of what it found:
  - **Fish button**: blue-violet pixels forming a wide, flat block on a dark panel -> its centre is tapped.
  - **Exit button**: salmon-red pixels forming a wide, flat block on a dark panel -> fishing is running, nothing is tapped there.
  - **X button**: a small white X in the upper right (both diagonals lit, corners lit, edge middles empty, nothing else white next to it) -> tapped.
- Sizes are relative, so it works at any screen size. Needs no pictures, takes a few milliseconds and even works before OpenCV is loaded.
- Scene.kt asks the pixel search first. Only when it sees nothing the picture matching of 2.3 runs. So every screen is found faster, and a screen that
  the pictures miss can still be found by colour.
- Setting: Settings > Detection > "Pixel recognition first" (panel: "Pixel recognition"), on by default. Off = pictures only (like 2.3/2.4).
- The Debug log shows "Fish button seen (NN%)" as before; Scene info says "Pixel Fish 9x%" when the pixel search found it.

## Tested here
tests/pix_test.py (python3 + opencv-python-headless; pix_ref.py has the same rules as Pix.kt): the 5 screenshots at 10 sizes (0.3x - 1.4x) with noise and
a little darkening: every screen is classified correctly (Fish / Exit / bar = nothing / Failed X / Caught X). The check runs in the GitHub "Code check" too.
NOT compiled and NOT run on a phone (no Android SDK here). The GitHub build is the first compile: send the error line if it fails.
The colours were measured on the screenshots you sent; if a skin/theme of the game uses other colours the picture matching still runs as the backup.
