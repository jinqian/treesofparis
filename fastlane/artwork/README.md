# Store artwork

Sources of the Google Play images in `fastlane/metadata/android/fr-FR/images/`, drawn from the launcher icon
(`app/src/main/res/drawable/ic_launcher_foreground.xml`) and the website fonts. Render them with headless Chrome:

```bash
CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
"$CHROME" --headless=new --hide-scrollbars --force-device-scale-factor=1 --window-size=512,512 \
  --screenshot="$PWD/fastlane/metadata/android/fr-FR/images/icon.png" "file://$PWD/fastlane/artwork/icon.html"
"$CHROME" --headless=new --hide-scrollbars --force-device-scale-factor=1 --virtual-time-budget=3000 \
  --window-size=1024,500 \
  --screenshot="$PWD/fastlane/metadata/android/fr-FR/images/featureGraphic.png" \
  "file://$PWD/fastlane/artwork/feature-graphic.html"
```

| Image | Size | Play Console |
|-------|------|--------------|
| `icon.png` | 512 × 512, full square (Play rounds the corners) | App icon |
| `featureGraphic.png` | 1024 × 500 | Feature graphic |
