# LuminFish 2.4 - the screens are handled IN ORDER and each step WAITS for its own screen

Built on 2.3: nothing was removed. Only the order checks below were added (MacroService.kt).

Order: Fish button -> Exit button -> bar -> Failed / Caught -> X

1. After the Fish button is tapped the macro now WAITS until the Exit button is really seen (the cast started) before it looks for the bar.
   If Fish is still showing (the tap missed) it taps Fish again (max 2 times). If Exit is not seen within 5 s it looks for the bar anyway (same as 2.3).
2. While the Exit button shows, nothing is tapped. The bar starts the reeling (unchanged: the tap ledger keeps the light-blue fill in the zone, any zone colour).
3. After the reel the macro WAITS for the X of the Failed / Caught box to really show, looking again up to 3 times (250 ms apart), and only taps it when it is seen.
   Only if it is never seen the saved Close point is used (same fallback as 2.3).
4. Debug log lines to look for: "Exit button seen: the cast started", "Fish button still showing, tapping it again", "Result: ... closing it".
5. Status text while waiting for the Exit button: "Waiting for Exit button".

NOT compiled and NOT run on a phone (no Android SDK here). The GitHub build is the first compile: send the error line if it fails.
